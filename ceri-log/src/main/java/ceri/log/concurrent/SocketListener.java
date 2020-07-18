package ceri.log.concurrent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteProvider;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.log.util.LogUtil;

/**
 * Server socket handler with listener notification. Can be used as a shutdown listening mechanism.
 */
public class SocketListener extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	private final ServerSocket serverSocket;
	private final Listeners<ByteProvider> listeners = new Listeners<>();

	public static SocketListener of(int port, Runnable listenable) throws IOException {
		return of(port, listenable, null);
	}

	public static SocketListener of(int port, Runnable listenable, Predicate<String> tester)
		throws IOException {
		SocketListener socketListener = of(port);
		socketListener.listeners().listen(data -> notifyIfMatch(data, tester, listenable));
		return socketListener;
	}

	private static void notifyIfMatch(ByteProvider data, Predicate<String> tester,
		Runnable listenable) {
		if (tester != null) {
			String s = data.getUtf8(0);
			if (!tester.test(s)) return;
		}
		listenable.run();
	}

	public static SocketListener of(int port) throws IOException {
		return new SocketListener(port);
	}

	private SocketListener(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		start();
	}

	public int port() {
		return serverSocket.getLocalPort();
	}

	@Override
	public void close() {
		LogUtil.close(logger, serverSocket);
		super.close();
	}

	public Listenable<ByteProvider> listeners() {
		return listeners;
	}

	@Override
	protected void loop() throws Exception {
		try (Socket socket = serverSocket.accept()) {
			process(socket);
		} catch (SocketException e) {
			logger.info(e.getMessage());
			throw new RuntimeInterruptedException("Stopping");
		}
	}

	private void process(Socket socket) throws IOException {
		@SuppressWarnings("resource")
		byte[] data = socket.getInputStream().readAllBytes();
		listeners.accept(Immutable.wrap(data));
	}

}
