package ceri.log.io.test;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.log.util.LogUtil;

/**
 * A server socket that echoes input as output for each connection.
 */
public class EchoServerSocket implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int BUFFER_SIZE_DEF = 1024;
	private final int bufferSize;
	private final ExecutorService exec;
	private final ServerSocket serverSocket;
	private final int localPort;

	public static EchoServerSocket of() throws IOException {
		return of(0);
	}

	public static EchoServerSocket of(int port) throws IOException {
		return of(port, BUFFER_SIZE_DEF);
	}

	public static EchoServerSocket of(int port, int bufferSize) throws IOException {
		return new EchoServerSocket(port, bufferSize);
	}

	private EchoServerSocket(int port, int bufferSize) throws IOException {
		this.bufferSize = bufferSize;
		exec = Executors.newCachedThreadPool();
		serverSocket = new ServerSocket(port);
		localPort = serverSocket.getLocalPort();
		ConcurrentUtil.submit(exec, this::listen);
		logger.info("Server socket open: port {}", localPort);
	}

	public int port() {
		return localPort;
	}

	@Override
	public void close() {
		LogUtil.close(logger, serverSocket);
		logger.info("Server socket closed: port {}", localPort);
		LogUtil.close(logger, exec);
	}

	private void listen() throws IOException {
		while (true) {
			@SuppressWarnings("resource")
			Socket socket = serverSocket.accept();
			ConcurrentUtil.submit(exec, () -> process(socket));
		}
	}

	@SuppressWarnings("resource")
	private void process(Socket socket) throws IOException {
		int port = 0;
		try {
			port = socket.getPort();
			logger.info("Socket open: port {}", port);
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			byte[] buffer = new byte[bufferSize];
			while (true) {
				int n = in.read(buffer);
				if (n < 0) break;
				out.write(buffer, 0, n);
			}
		} finally {
			LogUtil.close(logger, socket);
			logger.info("Socket closed: port {}", port);
		}
	}

}
