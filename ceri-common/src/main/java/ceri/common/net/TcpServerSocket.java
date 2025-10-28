package ceri.common.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import ceri.common.concurrent.Concurrent;
import ceri.common.function.Closeables;
import ceri.common.function.Excepts;

/**
 * A server socket that passes newly connected sockets to a listener.
 */
public class TcpServerSocket implements Closeable {
	private final ExecutorService exec;
	private final ServerSocket serverSocket;
	private final int port;
	private final AtomicBoolean closed = new AtomicBoolean(false);

	public static TcpServerSocket of() throws IOException {
		return of(0);
	}

	public static TcpServerSocket of(int port) throws IOException {
		return new TcpServerSocket(port);
	}

	private TcpServerSocket(int port) throws IOException {
		exec = Executors.newSingleThreadExecutor();
		serverSocket = new ServerSocket(port);
		this.port = serverSocket.getLocalPort();
	}

	/**
	 * Listens for connections and passes the new socket to the listener. The socket is closed on
	 * returning from the listener callback. The returned future can be used to interrupt listening.
	 */
	public Future<?> listenAndClose(Excepts.Consumer<IOException, TcpSocket> listener) {
		return listen(socket -> Closeables.acceptOrClose(socket, listener));
	}

	/**
	 * Listens for connections and passes the new socket to the listener. The listener is expected
	 * to close the socket if no exception is thrown. The returned future can be used to interrupt
	 * listening.
	 */
	public Future<?> listen(Excepts.Consumer<IOException, TcpSocket> listener) {
		return Concurrent.submit(exec, () -> listenAndNotify(listener));
	}

	public HostPort hostPort() {
		return HostPort.localhost(port());
	}

	public int port() {
		return port;
	}

	@Override
	public void close() {
		if (closed.getAndSet(true)) return;
		Closeables.close(serverSocket, exec); // must shut down socket first
	}

	@SuppressWarnings("resource")
	private void listenAndNotify(Excepts.Consumer<IOException, TcpSocket> listener)
		throws IOException {
		Socket socket = null;
		try {
			while (!closed.get()) {
				Concurrent.checkRuntimeInterrupted();
				socket = serverSocket.accept();
				listener.accept(TcpSocket.wrap(socket));
			}
		} catch (RuntimeException | IOException e) {
			Closeables.close(socket);
			if (!closed.get()) throw e;
		}
	}
}
