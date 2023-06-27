package ceri.common.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.util.CloseableUtil;

public class SocketUtil {

	private SocketUtil() {}

	public static void main(String[] args) throws IOException {
		listen(0, socket -> CloseableUtil.close(socket));
		// try (var t = TestUtil.threadRun(() -> listen)) {
		// t.get();
		// }
	}

	public static void verifyConnected(Socket socket) {
		if (!socket.isConnected()) throw new IllegalStateException("Socket is not connected");
	}

	public static HostPort remoteHostPort(Socket socket) {
		verifyConnected(socket);
		return HostPort.from((InetSocketAddress) socket.getRemoteSocketAddress());
	}
	
	public static HostPort localHostPort(Socket socket) {
		return HostPort.localhost(socket.getLocalPort());
	}
	
	public static void listen(int port, Consumer<Socket> consumer) throws IOException {
		try (var serverSocket = new ServerSocket(port)) {
			listen(serverSocket, consumer);
		}
	}

	public static void listen(IntConsumer portListener, Consumer<Socket> consumer)
		throws IOException {
		try (var serverSocket = new ServerSocket(0)) {
			portListener.accept(serverSocket.getLocalPort());
			listen(serverSocket, consumer);
		}
	}

	@SuppressWarnings("resource")
	private static void listen(ServerSocket serverSocket, Consumer<Socket> consumer)
		throws IOException {
		while (true) {
			ConcurrentUtil.checkRuntimeInterrupted();
			Socket socket = serverSocket.accept();
			try {
				consumer.accept(socket);
			} catch (RuntimeException e) {
				CloseableUtil.close(socket);
				throw e;
			}
		}
	}

}
