package ceri.log.rpc.service;

import static ceri.log.util.LogUtil.hashId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.protobuf.Empty;
import ceri.common.concurrent.SafeReadWrite;
import ceri.common.concurrent.ValueCondition;
import ceri.common.event.Listenable;
import ceri.common.function.Excepts.RuntimeCloseable;
import ceri.common.util.Enclosed;
import ceri.log.rpc.util.RpcUtil;
import ceri.log.util.LogUtil;
import io.grpc.stub.StreamObserver;

/**
 * Handles notifications from a local notifier to remote rpc listeners. Remote rpc clients invoke a
 * streaming rpc call to receive notifications; notifications only continue while the call is
 * active. Services can delegate the rpc call to this class. Its proto definition should be of the
 * form:
 *
 * <pre>
 * rpc &lt;notify-call-name&gt; (stream google.protobuf.Empty)
 *   returns (stream &lt;notify-type&gt;) {}
 * </pre>
 *
 * This class receives local notifications, transforms to the rpc notify-type and uses the rpc
 * stream observers to notify remote clients.
 */
public class RpcServiceNotifier<T, V> implements RuntimeCloseable {
	static final Logger logger = LogManager.getLogger();
	private final SafeReadWrite safe = SafeReadWrite.of();
	/** Used to notify whenever a listener is added or removed */
	private final ValueCondition<Integer> listenerSync = ValueCondition.of(safe.conditionLock());
	private final Set<StreamObserver<V>> observers = new LinkedHashSet<>(); // rpc clients
	private final Enclosed<RuntimeException, ?> listener; // listen on create, unlisten on close
	private final Function<T, V> transform; // transforms T to grpc value type V

	public static <T, V> RpcServiceNotifier<T, V> of(Listenable<T> listenable,
		Function<T, V> transform) {
		return new RpcServiceNotifier<>(listenable, transform);
	}

	private RpcServiceNotifier(Listenable<T> listenable, Function<T, V> transform) {
		this.transform = transform;
		listener = listenable.enclose(this::notification);
		logger.debug("Started");
	}

	/**
	 * Wait for a listeners to be added/removed. The predicate acts on the current number of active
	 * listeners.
	 */
	public void waitForListener(Predicate<Integer> test) throws InterruptedException {
		listenerSync.await(test);
	}

	/**
	 * Called by service when a client has requested to listen. Returned stream observer adds to
	 * list of observers on next, and removes on completion/error. Notifications start after client
	 * has called onNext(EMPTY).
	 */
	public StreamObserver<Empty> listen(StreamObserver<V> response) {
		logger.trace("Listen: {}", hashId(response));
		return RpcUtil.observer(_ -> add(response), () -> remove(response),
			t -> error(response, t));
	}

	@Override
	public void close() {
		LogUtil.close(listener);
		logger.debug("Stopped");
	}

	private void notification(T t) {
		logger.debug("Notification: {}", t);
		V v = transform.apply(t);
		List<StreamObserver<V>> observers = safe.read(() -> new ArrayList<>(this.observers));
		observers.forEach(observer -> observer.onNext(v));
	}

	private void add(StreamObserver<V> response) {
		logger.debug("Listener added: {}", hashId(response));
		safe.write(() -> {
			observers.add(response);
			listenerSync.signal(observers.size());
		});
	}

	private void remove(StreamObserver<V> response) {
		logger.debug("Listener removed: {}", hashId(response));
		safe.write(() -> {
			observers.remove(response);
			listenerSync.signal(observers.size());
		});
	}

	private void error(StreamObserver<V> response, Throwable t) {
		if (!RpcServiceUtil.ignorable(t)) logger.catching(Level.WARN, t);
		remove(response);
	}

}
