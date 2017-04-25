package ceri.log.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ExceptionRunnable;
import ceri.common.util.BasicUtil;
import ceri.log.io.SelfHealingSocket;
import ceri.log.util.LogUtil;

public class SelfHealingSocketTest implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final ExecutorService exec = Executors.newCachedThreadPool();
	private volatile boolean pause = false;
	private volatile boolean abort = false;

	public static void main(String[] args) {
		int port = 7890;
		try (SelfHealingSocketTest test = new SelfHealingSocketTest(port)) {
			BasicUtil.delay(60 * 60 * 1000);
		}
	}

	private SelfHealingSocketTest(int port) {
		execute(() -> inputs());
		execute(() -> startServer(port));
		execute(() -> startClient(port));
	}

	private void startClient(int port) throws IOException {
		SelfHealingSocket socket =
			SelfHealingSocket.builder("localhost", port).tcpNoDelay(true).build();
		socket.connect();
		String name = "client";
		BufferedReader in = in(socket);
		OutputStreamWriter out = out(socket);
		execute(() -> recv(in, name));
		while (true) {
			try {
				send(out, name, "hello");
			} catch (IOException e) {
				log(e);
			}
			BasicUtil.delay(1000);
		}
	}

	private void log(Exception e) {
		logger.error("{}: {}\n\tat {}", e.getClass().getName(), e.getMessage(),
			e.getStackTrace()[0]);
	}

	private void inputs() throws IOException {
		while (true) {
			String s = readString(System.in);
			if (s.equals("p")) pause = !pause;
			if (s.equals("x")) abort = !abort;
		}
	}

	@Override
	public void close() {
		exec.shutdownNow();
	}

	private void startServer(int port) throws IOException {
		Socket socket = null;
		BufferedReader in = null;
		OutputStreamWriter out = null;
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (true) {
				socket = serverSocket.accept();
				in = in(socket);
				out = out(socket);
				Socket socket0 = socket;
				BufferedReader in0 = in;
				OutputStreamWriter out0 = out;
				String name = name("server", socket);
				execute(() -> {
					while (true) {
						checkAbort(socket0);
						recv(in0, name);
						pause();
						send(out0, name, "ok");
					}
				});
			}
		} finally {
			LogUtil.close(logger, socket, out, in);
		}
	}

	private void execute(ExceptionRunnable<?> runnable) {
		exec.execute(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				log(e);
			}
		});
	}

	private void checkAbort(Socket socket) throws IOException {
		if (!abort) return;
		socket.close();
		throw new IOException("abort!");
	}

	private void pause() {
		while (pause)
			BasicUtil.delay(50);
	}

	private static void send(OutputStreamWriter out, String name, String text) throws IOException {
		logger.info("{} tx: {}", name, text);
		out.write(text);
		out.write("\n");
		out.flush();
	}

	private static void recv(BufferedReader in, String name) throws IOException {
		String text = in.readLine();
		logger.info("{} rx: {}", name, text);
	}

	private static BufferedReader in(SelfHealingSocket socket) {
		return new BufferedReader(new InputStreamReader(socket.in()));
	}

	private static OutputStreamWriter out(SelfHealingSocket socket) {
		return new OutputStreamWriter(socket.out());
	}

	private static BufferedReader in(Socket socket) throws IOException {
		return new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	private static OutputStreamWriter out(Socket socket) throws IOException {
		return new OutputStreamWriter(socket.getOutputStream());
	}

	private static String name(String prefix, Socket socket) {
		return prefix + "[" + socket.getLocalPort() + "-" + socket.getPort() + "]";
	}

	private static String readString(InputStream in) throws IOException {
		byte[] buffer = new byte[1000];
		int n = in.read(buffer);
		if (n < 1) return "";
		return new String(buffer, 0, n).trim();
	}

}
