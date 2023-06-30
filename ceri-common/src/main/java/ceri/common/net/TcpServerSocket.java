package ceri.common.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.ExceptionConsumer;
import ceri.common.util.CloseableUtil;

/**
 * A server socket that passes newly connected sockets to a listener.
 */
public class TcpServerSocket implements Closeable {
	private final ExecutorService exec;
	private final ServerSocket serverSocket;
	private final int port;

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
	public Future<?> listenAndClose(ExceptionConsumer<IOException, TcpSocket> listener) {
		return listen(socket -> {
			listener.accept(socket);
			socket.close();
		});
	}

	/**
	 * Listens for connections and passes the new socket to the listener. The listener is expected
	 * to close the socket if no exception is thrown. The returned future can be used to interrupt
	 * listening.
	 */
	public Future<?> listen(ExceptionConsumer<IOException, TcpSocket> listener) {
		return ConcurrentUtil.submit(exec, () -> listenAndNotify(listener));
	}

	public HostPort hostPort() {
		return HostPort.localhost(port());
	}

	public int port() {
		return port;
	}

	@Override
	public void close() {
		CloseableUtil.closeAll(exec, serverSocket);
	}

	@SuppressWarnings("resource")
	private void listenAndNotify(ExceptionConsumer<IOException, TcpSocket> listener)
		throws IOException {
		while (true) {
			Socket socket = serverSocket.accept();
			try {
				listener.accept(TcpSocket.wrap(socket));
			} catch (RuntimeException | IOException e) {
				CloseableUtil.close(socket);
				throw e;
			}
		}
	}

}
