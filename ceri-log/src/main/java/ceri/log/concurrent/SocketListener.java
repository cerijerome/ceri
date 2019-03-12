package ceri.log.concurrent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.log.util.LogUtil;

/**
 * Server socket handler with listener notification. Can be used as a shutdown listening mechanism.
 */
public class SocketListener extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	private static final int BUFFER_SIZE_DEF = 1024;
	private final int bufferSize;
	private final ServerSocket serverSocket;
	private final Listeners<ImmutableByteArray> listeners = new Listeners<>();

	public static SocketListener create(int port, Runnable listenable) throws IOException {
		return create(port, listenable, null);
	}

	public static SocketListener create(int port, Runnable listenable, Predicate<String> tester)
		throws IOException {
		SocketListener socketListener = create(port);
		socketListener.listeners().listen(data -> notifyIfMatch(data, tester, listenable));
		return socketListener;
	}

	private static void notifyIfMatch(ImmutableByteArray data, Predicate<String> tester,
		Runnable listenable) {
		if (tester != null) {
			String s = new String(data.copy());
			if (!tester.test(s)) return;
		}
		listenable.run();
	}

	public static SocketListener create(int port) throws IOException {
		return create(port, BUFFER_SIZE_DEF);
	}

	public static SocketListener create(int port, int bufferSize) throws IOException {
		return new SocketListener(port, bufferSize);
	}

	SocketListener(int port, int bufferSize) throws IOException {
		this.bufferSize = bufferSize;
		serverSocket = new ServerSocket(port);
		start();
	}

	@Override
	public void close() {
		LogUtil.close(logger, serverSocket);
		super.close();
	}

	public Listenable<ImmutableByteArray> listeners() {
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
		byte[] buffer = new byte[bufferSize];
		int count = socket.getInputStream().read(buffer);
		if (count < 0) return;
		ImmutableByteArray data = ImmutableByteArray.wrap(buffer, 0, count);
		listeners.accept(data);
	}

}
