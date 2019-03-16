package ceri.log.rpc.service;

import static ceri.log.util.LogUtil.hashId;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.protobuf.Empty;
import ceri.common.concurrent.SafeReadWrite;
import ceri.common.event.CloseableListener;
import ceri.common.event.Listenable;
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
public class RpcServiceNotifier<T, V> implements Closeable {
	static final Logger logger = LogManager.getLogger();
	private final SafeReadWrite safe = SafeReadWrite.create();
	private final Set<StreamObserver<V>> observers = new LinkedHashSet<>();
	private final CloseableListener<T> listener;
	private final Function<T, V> transform;

	public static <T, V> RpcServiceNotifier<T, V> of(Listenable<T> listenable,
		Function<T, V> transform) {
		return new RpcServiceNotifier<>(listenable, transform);
	}

	private RpcServiceNotifier(Listenable<T> listenable, Function<T, V> transform) {
		this.transform = transform;
		listener = CloseableListener.of(listenable, this::notification);
		logger.info("Started");
	}

	public StreamObserver<Empty> listen(StreamObserver<V> response) {
		return RpcUtil.observer(empty -> add(response), () -> remove(response),
			t -> error(response, t));
	}

	@Override
	public void close() {
		LogUtil.close(logger, listener);
		logger.info("Stopped");
	}

	private void notification(T t) {
		logger.debug("Notification: {}", t);
		V v = transform.apply(t);
		List<StreamObserver<V>> observers = safe.read(() -> new ArrayList<>(this.observers));
		observers.forEach(observer -> observer.onNext(v));
	}

	private void add(StreamObserver<V> response) {
		logger.debug("Listener added: {}", hashId(response));
		safe.write(() -> observers.add(response));
	}

	private void remove(StreamObserver<V> response) {
		logger.debug("Listener removed: {}", hashId(response));
		safe.write(() -> observers.remove(response));
	}

	private void error(StreamObserver<V> response, Throwable t) {
		logger.catching(Level.WARN, t);
		remove(response);
	}

}
