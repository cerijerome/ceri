package ceri.log.rpc.client;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.protobuf.Empty;
import ceri.common.collect.Sets;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.Concurrent;
import ceri.common.event.Listenable;
import ceri.common.function.Functions;
import ceri.common.property.TypedProperties;
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
	private final Functions.Function<StreamObserver<V>, StreamObserver<Empty>> call; // rpc stub
																						// call
	private final Functions.Function<V, T> transform;
	private final StreamObserver<V> callback; // handles notifications from service
	private final Lock lock = new ReentrantLock();
	private final BoolCondition sync = BoolCondition.of(lock);
	private final Set<Functions.Consumer<? super T>> listeners = Sets.link();
	private final Config config;
	private boolean reset = false;
	private RpcStreamer<Empty> caller = null; //

	public record Config(int resetDelayMs) {
		public static final Config DEFAULT = new Config(3000);
	}

	public static class Properties extends TypedProperties.Ref {
		private static final String RESET_DELAY_MS_KEY = "reset.delay.ms";

		public Properties(TypedProperties properties, String... groups) {
			super(properties, groups);
		}

		public Config config() {
			return parse(RESET_DELAY_MS_KEY).asInt().as(Config::new).get(Config.DEFAULT);
		}
	}

	private static enum Action {
		none,
		receive,
		resetAndReceive,
		stop
	}

	public static <T, V> RpcClientNotifier<T, V> of(
		Functions.Function<StreamObserver<V>, StreamObserver<Empty>> call,
		Functions.Function<V, T> transform, Config config) {
		return new RpcClientNotifier<>(call, transform, config);
	}

	RpcClientNotifier(Functions.Function<StreamObserver<V>, StreamObserver<Empty>> call,
		Functions.Function<V, T> transform, Config config) {
		this.call = call;
		this.transform = transform;
		this.config = config;
		callback = RpcUtil.observer(this::onNotify, this::onCompleted, this::onError);
		start();
	}

	public void clear() {
		Concurrent.lockedRun(lock, () -> {
			if (listeners.isEmpty()) return;
			listeners.clear();
			sync.signal(); // signal to stop listening
		});
	}

	@Override
	public boolean listen(Functions.Consumer<? super T> listener) {
		return Concurrent.lockedGet(lock, () -> {
			boolean result = listeners.add(listener);
			if (result && listeners.size() == 1) sync.signal(); // signal to start listening
			return result;
		});
	}

	@Override
	public boolean unlisten(Functions.Consumer<? super T> listener) {
		return Concurrent.lockedGet(lock, () -> {
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
		if (action == Action.resetAndReceive) Concurrent.delay(config.resetDelayMs);
	}

	private Action waitForAction() throws InterruptedException {
		return Concurrent.lockedGet(lock, () -> {
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
		caller.next(RpcUtil.EMPTY); // start receiving; close() to stop
	}

	private void stopReceiving() {
		logger.debug("Stopping notifications");
		LogUtil.close(caller);
	}

	private void onNotify(V v) {
		logger.trace("Notification: {}", LogUtil.compact(v));
		T t = transform.apply(v);
		var listeners = Concurrent.lockedGet(lock, () -> Sets.link(this.listeners));
		notifyListeners(listeners, t);
	}

	private void notifyListeners(Set<Functions.Consumer<? super T>> listeners, T t) {
		listeners.forEach(listener -> listener.accept(t));
	}

	private void onCompleted() {
		logger.info("Streaming completed by server");
		signalReset();
	}

	private void onError(Throwable t) {
		if (!RpcClientUtil.ignorable(t))
			logger.warn("Streaming error: {}", RpcUtil.cause(t).getMessage());
		signalReset();
	}

	private void signalReset() {
		Concurrent.lockedRun(lock, () -> {
			reset = true;
			sync.signal();
		});
	}
}
