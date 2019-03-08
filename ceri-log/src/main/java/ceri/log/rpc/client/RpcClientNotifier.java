package ceri.log.rpc.client;

import static ceri.log.rpc.client.RpcClientUtil.isChannelShutdown;
import static ceri.log.rpc.util.RpcUtil.EMPTY;
import java.util.LinkedHashSet;
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
import ceri.common.util.BasicUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.rpc.util.RpcStreamer;
import ceri.log.rpc.util.RpcUtil;
import ceri.log.util.LogUtil;
import io.grpc.stub.StreamObserver;

/**
 * Handles notifications from remote serial service to local listeners. The rpc call is passed to
 * the constructor as well as a function to transform the rpc notify type to a non-rpc object. Local
 * listeners then receive the non-rpc object. The rpc call proto definition should be of the form:
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
	private final Function<StreamObserver<V>, StreamObserver<Empty>> call;
	private final Function<V, T> transform;
	private final StreamObserver<V> callback;
	private final Lock lock = new ReentrantLock();
	private final BooleanCondition sync = BooleanCondition.create(lock);
	private final Set<Consumer<? super T>> listeners = new LinkedHashSet<>();
	private final RpcClientNotifierConfig config;
	private boolean reset = false;
	private RpcStreamer<Empty> caller = null;

	public static <T, V> RpcClientNotifier<T, V> of(
		Function<StreamObserver<V>, StreamObserver<Empty>> call, Function<V, T> transform,
		RpcClientNotifierConfig config) {
		return new RpcClientNotifier<>(call, transform, config);
	}

	private RpcClientNotifier(Function<StreamObserver<V>, StreamObserver<Empty>> call,
		Function<V, T> transform, RpcClientNotifierConfig config) {
		this.call = call;
		this.transform = transform;
		this.config = config;
		callback = callback();
		start();
	}

	private static enum Action {
		none,
		receive,
		resetAndReceive,
		stop
	}

	@Override
	public boolean listen(Consumer<? super T> listener) {
		return ConcurrentUtil.executeGet(lock, () -> {
			boolean result = listeners.add(listener);
			if (result && listeners.size() == 1) sync.signal(); // signal to start listening
			return result;
		});
	}

	@Override
	public boolean unlisten(Consumer<? super T> listener) {
		return ConcurrentUtil.executeGet(lock, () -> {
			boolean result = listeners.remove(listener);
			if (result && listeners.isEmpty()) sync.signal(); // signal to stop listening
			return result;
		});
	}

	@Override
	public void close() {
		LogUtil.close(logger, caller);
		super.close();
	}

	@Override
	protected void loop() throws InterruptedException {
		Action action = waitForAction();
		if (action == Action.none) return;
		if (action != Action.receive) stopReceiving();
		if (action != Action.stop) startReceiving();
		if (action == Action.resetAndReceive) BasicUtil.delay(config.resetDelayMs);
	}

	private Action waitForAction() throws InterruptedException {
		return ConcurrentUtil.executeGet(lock, () -> {
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

	private StreamObserver<V> callback() {
		return RpcUtil.observer(this::onNotify, this::onCompleted, this::onError);
	}

	private void startReceiving() {
		if (caller != null && !caller.closed()) return; // already receiving
		logger.debug("Waiting for notifications");
		caller = RpcStreamer.of(call.apply(callback));
		caller.next(EMPTY);
	}

	private void stopReceiving() {
		if (caller == null || caller.closed()) return; // already stopped
		logger.debug("Stopping notifications");
		LogUtil.close(logger, caller);
	}

	private void onNotify(V v) {
		logger.trace("New notification received: {}", v);
		T t = transform.apply(v);
		Set<Consumer<? super T>> listeners = ConcurrentUtil.executeGet(lock, () -> this.listeners);
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
		if (!isChannelShutdown(t))
			logger.warn("Streaming error: {}", RpcUtil.cause(t).getMessage());
		signalReset();
	}

	private void signalReset() {
		ConcurrentUtil.execute(lock, () -> {
			reset = true;
			sync.signal();
		});
	}

}
