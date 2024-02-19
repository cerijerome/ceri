package ceri.log.rpc.client;

import static ceri.log.rpc.client.RpcClientUtil.ignorable;
import static ceri.log.rpc.util.RpcUtil.EMPTY;
import static ceri.log.util.LogUtil.compact;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.protobuf.Empty;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.event.Listenable;
import ceri.common.text.ToString;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.rpc.util.RpcStreamer;
import ceri.log.rpc.util.RpcUtil;
import ceri.log.util.LogUtil;
import io.grpc.stub.StreamObserver;

/**
 * Handles notifications from remote serial service to local listeners. The rpc call is passed to
 * the constructor as well as a function to transform the rpc notify type (V) to a non-rpc object
 * (T). Local listeners then receive the non-rpc object. The rpc call proto definition should be of
 * the form:
 *
 * <pre>
 * rpc &lt;notify-call-name&gt; (stream google.protobuf.Empty)
 *   returns (stream &lt;notify-type&gt;) {}
 * </pre>
 *
 * When the first listener starts listening, a streaming rpc call is started. The service returns
 * notification events. When no listeners are listening the call is stopped. When the service causes
 * the call to end, attempts are continually made to restart the call, with a delay between failed
 * calls. This class typically works with the service-side RpcServiceNotifier.
 */
public class RpcClientNotifier<T, V> extends LoopingExecutor implements Listenable<T> {
	private static final Logger logger = LogManager.getLogger();
	private final Function<StreamObserver<V>, StreamObserver<Empty>> call; // rpc stub call
	private final Function<V, T> transform;
	private final StreamObserver<V> callback; // handles notifications from service
	private final Lock lock = new ReentrantLock();
	private final BooleanCondition sync = BooleanCondition.of(lock);
	private final Set<Consumer<? super T>> listeners = new LinkedHashSet<>();
	private final Config config;
	private boolean reset = false;
	private RpcStreamer<Empty> caller = null; //

	public static class Config {
		public static final Config DEFAULT = builder().build();
		public final int resetDelayMs;

		public static Config of() {
			return builder().build();
		}

		public static Config of(int resetDelayMs) {
			return builder().resetDelayMs(resetDelayMs).build();
		}

		public static class Builder {
			int resetDelayMs = 3000;

			Builder() {}

			public Builder resetDelayMs(int resetDelayMs) {
				this.resetDelayMs = resetDelayMs;
				return this;
			}

			public Config build() {
				return new Config(this);
			}
		}

		public static Builder builder() {
			return new Builder();
		}

		Config(Builder builder) {
			resetDelayMs = builder.resetDelayMs;
		}

		@Override
		public int hashCode() {
			return Objects.hash(resetDelayMs);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Config)) return false;
			Config other = (Config) obj;
			if (resetDelayMs != other.resetDelayMs) return false;
			return true;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, resetDelayMs);
		}
	}

	private static enum Action {
		none,
		receive,
		resetAndReceive,
		stop
	}

	public static <T, V> RpcClientNotifier<T, V> of(
		Function<StreamObserver<V>, StreamObserver<Empty>> call, Function<V, T> transform,
		Config config) {
		return new RpcClientNotifier<>(call, transform, config);
	}

	RpcClientNotifier(Function<StreamObserver<V>, StreamObserver<Empty>> call,
		Function<V, T> transform, Config config) {
		this.call = call;
		this.transform = transform;
		this.config = config;
		callback = RpcUtil.observer(this::onNotify, this::onCompleted, this::onError);
		start();
	}

	public void clear() {
		ConcurrentUtil.lockedRun(lock, () -> {
			if (listeners.isEmpty()) return;
			listeners.clear();
			sync.signal(); // signal to stop listening
		});
	}

	@Override
	public boolean listen(Consumer<? super T> listener) {
		return ConcurrentUtil.lockedGet(lock, () -> {
			boolean result = listeners.add(listener);
			if (result && listeners.size() == 1) sync.signal(); // signal to start listening
			return result;
		});
	}

	@Override
	public boolean unlisten(Consumer<? super T> listener) {
		return ConcurrentUtil.lockedGet(lock, () -> {
			boolean result = listeners.remove(listener);
			if (result && listeners.isEmpty()) sync.signal(); // signal to stop listening
			return result;
		});
	}

	@Override
	public void close() {
		LogUtil.close(caller);
		super.close();
	}

	@Override
	protected void loop() throws InterruptedException {
		Action action = waitForAction();
		logger.trace("Action: {}", action);
		if (action != Action.receive) stopReceiving();
		if (action != Action.stop) startReceiving();
		if (action == Action.resetAndReceive) ConcurrentUtil.delay(config.resetDelayMs);
	}

	private Action waitForAction() throws InterruptedException {
		return ConcurrentUtil.lockedGet(lock, () -> {
			sync.await();
			Action action = action(this.reset);
			this.reset = false;
			return action;
		});
	}

	private Action action(boolean reset) {
		if (listeners.isEmpty()) return Action.stop;
		if (reset) return Action.resetAndReceive;
		return Action.receive;
	}

	private void startReceiving() {
		// if (caller != null && !caller.closed()) return; // already receiving (not possible?)
		logger.debug("Waiting for notifications");
		caller = RpcStreamer.of(call.apply(callback)); // wrap observer as new closable instance
		caller.next(EMPTY); // start receiving; close() to stop
	}

	private void stopReceiving() {
		logger.debug("Stopping notifications");
		LogUtil.close(caller);
	}

	private void onNotify(V v) {
		logger.trace("Notification: {}", compact(v));
		T t = transform.apply(v);
		Set<Consumer<? super T>> listeners =
			ConcurrentUtil.lockedGet(lock, () -> new LinkedHashSet<>(this.listeners));
		notifyListeners(listeners, t);
	}

	private void notifyListeners(Set<Consumer<? super T>> listeners, T t) {
		listeners.forEach(listener -> listener.accept(t));
	}

	private void onCompleted() {
		logger.info("Streaming completed by server");
		signalReset();
	}

	private void onError(Throwable t) {
		if (!ignorable(t)) logger.warn("Streaming error: {}", RpcUtil.cause(t).getMessage());
		signalReset();
	}

	private void signalReset() {
		ConcurrentUtil.lockedRun(lock, () -> {
			reset = true;
			sync.signal();
		});
	}

}
