package ceri.common.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import ceri.common.array.Array;
import ceri.common.array.RawArray;
import ceri.common.concurrent.Listenable;
import ceri.common.concurrent.Listeners;
import ceri.common.data.ByteStream;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Fluent;
import ceri.common.function.Functional;
import ceri.common.io.Io;
import ceri.common.io.IoStream;
import ceri.common.io.IoStream.Write;
import ceri.common.io.PipedStream;
import ceri.common.io.StateChange;
import ceri.common.net.HostPort;
import ceri.common.net.TcpSocketOption;
import ceri.common.net.TcpSocketOptions;
import ceri.common.reflect.Reflect;
import ceri.common.text.Chars;
import ceri.common.text.Strings;
import ceri.common.text.ToString;
import ceri.common.util.Basics;
import ceri.common.util.Validate;

/**
 * I/O testing support types.
 */
public class TestIo {
	private static final int DEFAULT_SIZE = 1024;

	private TestIo() {}

	/**
	 * An InputStream that wraps a PipedStream for testing. Read calls read from the pipe. CallSync
	 * fields can be used to override behavior and generate errors.
	 */
	public static class In extends InputStream implements Fluent<In> {
		private final PipedStream piped;
		public final CallSync.Function<RawArray.Sub<byte[]>, Integer> read =
			CallSync.function(null, (Integer) null);
		public final CallSync.Supplier<Integer> available = CallSync.supplier((Integer) null);
		public final CallSync.Consumer<Integer> mark = CallSync.consumer(null, true);
		public final CallSync.Runnable reset = CallSync.runnable(true);
		public final CallSync.Runnable close = CallSync.runnable(true);
		public final CallSync.Supplier<Boolean> markSupported = CallSync.supplier((Boolean) null);
		public final ByteStream.Writer to; // write to input

		public static In of(int pipeSize) {
			return new In(pipeSize);
		}

		@SuppressWarnings("resource")
		protected In(int pipeSize) {
			piped = PipedStream.of(pipeSize);
			to = ByteStream.writer(piped.out());
		}

		public void resetState() {
			CallSync.resetAll(read, available, mark, reset, close);
			Functional.muteRun(piped::clear);
		}

		/**
		 * Wait for PipedInputStream to read available bytes.
		 */
		public void awaitFeed() throws IOException {
			piped.awaitRead(1);
		}

		/**
		 * Set read auto-response to EOF or delegate (null). This replaces any currently configured
		 * auto-response function.
		 */
		public void eof(boolean enabled) {
			if (enabled) read.autoResponses(-1);
			else read.autoResponses((Integer) null);
		}

		@Override
		public int read() throws IOException {
			byte[] bytes = new byte[1];
			int n = read(bytes);
			if (n < 0) return n;
			Validate.equal(n, 1);
			return bytes[0] & 0xff;
		}

		@SuppressWarnings("resource")
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int n = piped.in().read(b, off, len);
			return Basics.def(read.apply(RawArray.Sub.of(b, off, len), ExceptionAdapter.io), n);
		}

		@SuppressWarnings("resource")
		@Override
		public int available() throws IOException {
			int n = piped.in().available();
			return Basics.def(available.get(ExceptionAdapter.io), n);
		}

		@Override
		public void mark(int readLimit) {
			// Not supported by PipedInputStream
			mark.accept(readLimit);
		}

		@Override
		public void reset() throws IOException {
			// Not supported by PipedInputStream
			reset.run(ExceptionAdapter.io);
		}

		@Override
		public boolean markSupported() {
			return markSupported.get();
		}

		@Override
		public void close() throws IOException {
			piped.close();
			close.run(ExceptionAdapter.io);
		}

		/**
		 * Prints state; useful for debugging tests.
		 */
		@SuppressWarnings("resource")
		@Override
		public String toString() {
			return ToString
				.ofClass(this, Functional.muteGet(piped.in()::available)).children("read=" + read,
					"available=" + available, "mark=" + mark, "reset=" + reset, "close=" + close)
				.toString();
		}
	}

	/**
	 * An OutputStream that wraps a PipedStream for testing. Write calls write to the pipe. CallSync
	 * fields can be used to override behavior and generate errors.
	 */
	public static class Out extends OutputStream {
		private final PipedStream piped;
		public final CallSync.Consumer<RawArray.Sub<byte[]>> write = CallSync.consumer(null, true);
		public final CallSync.Runnable flush = CallSync.runnable(true);
		public final CallSync.Runnable close = CallSync.runnable(true);
		public final ByteStream.Reader from; // read from output

		public static Out of(int pipeSize) {
			return new Out(pipeSize);
		}

		@SuppressWarnings("resource")
		protected Out(int pipeSize) {
			piped = PipedStream.of(pipeSize);
			from = ByteStream.reader(piped.in());
		}

		public void resetState() {
			CallSync.resetAll(write, flush, close);
			Functional.muteRun(piped::clear);
		}

		public void assertAvailable(int n) throws IOException {
			Assert.equal(from.available(), n);
		}

		@Override
		public void write(int b) throws IOException {
			write(Array.BYTE.of(b));
		}

		@SuppressWarnings("resource")
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			piped.out().write(b, off, len);
			write.accept(RawArray.Sub.of(b, off, len), ExceptionAdapter.io);
		}

		@SuppressWarnings("resource")
		@Override
		public void flush() throws IOException {
			piped.out().flush();
			flush.run(ExceptionAdapter.io);
		}

		@Override
		public void close() throws IOException {
			piped.close();
			close.run(ExceptionAdapter.io);
		}

		/**
		 * Prints state; useful for debugging tests.
		 */
		@SuppressWarnings("resource")
		@Override
		public String toString() {
			return ToString.ofClass(this, Functional.muteGet(piped.in()::available))
				.children("write=" + write, "flush=" + flush, "close=" + close).toString();
		}

		/**
		 * Capture output until it matches the text pattern. Use (?s) for dot-all matches.
		 */
		public String awaitMatch(String pattern) throws IOException {
			return awaitMatch(pattern, Charset.defaultCharset());
		}

		/**
		 * Capture output until it matches the text pattern. Use (?s) for dot-all matches.
		 */
		public String awaitMatch(String pattern, Charset charset) throws IOException {
			StringBuilder b = new StringBuilder();
			Pattern p = Pattern.compile(pattern);
			while (true) {
				write.awaitAuto();
				b.append(Io.availableString(from, charset));
				if (p.matcher(b).matches()) return b.toString();
			}
		}
	}

	/**
	 * A connector implementation for tests, using piped streams.
	 */
	public static class Fixable implements ceri.common.io.Fixable {
		public final Listeners<StateChange> listeners = Listeners.of();
		public final CallSync.Consumer<Boolean> open = CallSync.consumer(false, true);
		public final CallSync.Consumer<Boolean> broken = CallSync.consumer(false, true);
		public final CallSync.Runnable close = CallSync.runnable(true);
		private final String name;

		/**
		 * Constructor with optional name override. Use null for the default name.
		 */
		protected Fixable(String name) {
			this.name = name;
		}

		/**
		 * Clear state.
		 */
		public void reset() {
			listeners.clear();
			CallSync.resetAll(broken, open, close);
		}

		@Override
		public String name() {
			return name == null ? ceri.common.io.Fixable.super.name() : name;
		}

		@Override
		public Listenable<StateChange> listeners() {
			return listeners;
		}

		@Override
		public void broken() {
			if (!broken.value()) listeners.accept(StateChange.broken);
			broken.accept(true);
			open.value(false);
		}

		/**
		 * Manually mark the connector as fixed.
		 */
		public void fixed() {
			open.value(true); // don't signal call
			if (broken.value()) listeners.accept(StateChange.fixed);
			broken.accept(false);
		}

		@Override
		public void open() throws IOException {
			open.accept(true, ExceptionAdapter.io);
			verifyUnbroken();
		}

		@Override
		public void close() throws IOException {
			open.value(false);
			close.run(ExceptionAdapter.io);
		}

		/**
		 * Prints state; useful for debugging tests.
		 */
		@Override
		public String toString() {
			return asString().toString();
		}

		protected ToString asString() {
			return ToString.ofName(name(), listeners.size(), broken.value() ? "broken" : "fixed",
				open.value() ? "open" : "closed");
		}

		protected void verifyConnected() throws IOException {
			verifyUnbroken();
			if (!open.value()) throw new IOException("Not connected");
		}

		protected void verifyUnbroken() throws IOException {
			if (broken.value()) throw new IOException("Connector is broken");
		}
	}

	/**
	 * A connector implementation for tests, using piped streams.
	 */
	public static class Connector extends Fixable implements ceri.common.io.Connector.Fixable {
		public final ErrorGen error = ErrorGen.of(); // for generating general errors
		public final In in;
		public final Out out;
		private final InputStream wrappedIn;
		private final OutputStream wrappedOut;
		private volatile Write writeOverride = null;

		/**
		 * Convenience method to enable echo from input to output streams.
		 */
		public static <T extends Connector> T echoOn(T connector) {
			connector.echoOn();
			return connector;
		}

		/**
		 * Connects outputs to inputs for consecutive connectors.
		 */
		@SafeVarargs
		public static <T extends Connector> T[] chain(T... connectors) {
			for (int i = 0; i < connectors.length; i++)
				connectors[i].pairWithTest(connectors[(i + 1) % connectors.length]);
			return connectors;
		}

		/**
		 * Constructor with optional name override. Use null for the default name.
		 */
		protected Connector(String name) {
			super(name);
			in = TestIo.in();
			out = TestIo.out();
			wrappedIn = IoStream.filterIn(in, this::read, this::available);
			wrappedOut = IoStream.filterOut(out, this::writeWithReturn);
		}

		/**
		 * Clear state.
		 */
		@Override
		public void reset() {
			super.reset();
			in.resetState();
			out.resetState();
		}

		/**
		 * Enable echo; input data is written to output.
		 */
		public void echoOn() {
			pairWithTest(this);
		}

		/**
		 * Enable pairing; input data is written to another connector.
		 */
		public void pairWithTest(Connector other) {
			writeOverride((b, off, len) -> other.in.to.write(b, off, len));
		}

		/**
		 * Enable pairing; input data is written to another connector.
		 */
		@SuppressWarnings("resource")
		public void pairWith(ceri.common.io.Connector other) {
			writeOverride((b, off, len) -> other.out().write(b, off, len));
		}

		/**
		 * Override writing, instead of writing to the test output stream.
		 */
		public void writeOverride(Write writeOverride) {
			this.writeOverride = writeOverride;
		}

		@Override
		public InputStream in() {
			error.call();
			return wrappedIn;
		}

		@Override
		public OutputStream out() {
			error.call();
			return wrappedOut;
		}

		@Override
		public void close() throws IOException {
			in.close();
			out.close();
			super.close();
		}

		@Override
		protected ToString asString() {
			return super.asString().children(in, out);
		}

		/**
		 * Calls available before error generation logic.
		 */
		private int available(InputStream in) throws IOException {
			int n = in.available();
			verifyConnected();
			return n;
		}

		/**
		 * Calls read before error generation logic. EOF overrides read response.
		 */
		private int read(InputStream in, byte[] b, int offset, int length) throws IOException {
			int n = in.read(b, offset, length);
			verifyConnected();
			return n;
		}

		/**
		 * Calls write before error generation logic.
		 */
		private void write(OutputStream out, byte[] b, int offset, int length) throws IOException {
			verifyConnected();
			if (!Functional.accept(w -> w.write(b, offset, length), writeOverride))
				out.write(b, offset, length);
		}

		private boolean writeWithReturn(OutputStream out, byte[] b, int offset, int length)
			throws IOException {
			write(out, b, offset, length);
			return true;
		}
	}

	/**
	 * A subclassed jdk socket that delegates to test streams for i/o.
	 */
	public static class Socket extends java.net.Socket {
		public final CallSync.Consumer<HostPort> remote = CallSync.consumer(HostPort.NULL, true);
		public final CallSync.Supplier<Integer> localPort = CallSync.supplier(0);
		public final In in;
		public final Out out;

		protected Socket() {
			in = TestIo.in();
			out = TestIo.out();
		}

		public Socket connect(String host, int port) throws IOException {
			remote.accept(HostPort.of(host, port), ExceptionAdapter.io);
			return this;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return in;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return out;
		}

		@Override
		public int getPort() {
			return remote.value().port(0);
		}

		@Override
		public int getLocalPort() {
			return localPort.get();
		}

		@Override
		public void close() throws IOException {
			in.close();
			out.close();
		}
	}

	/**
	 * A connector for testing logic against serial connectors.
	 */
	public static class TcpSocket extends Connector implements ceri.common.net.TcpSocket.Fixable {
		private static final String NAME = Reflect.name(TcpSocket.class);
		public final CallSync.Supplier<HostPort> hostPort;
		public final CallSync.Supplier<Integer> localPort;
		public final CallSync.Runnable optionSync = CallSync.runnable(true);
		public final TcpSocketOptions.Mutable options = TcpSocketOptions.of(ConcurrentHashMap::new);

		/**
		 * Provide a test socket that echoes output to input.
		 */
		@SuppressWarnings("resource")
		public static TcpSocket ofEcho() {
			return Connector.echoOn(new TcpSocket(NAME + ":echo", HostPort.NULL, 0));
		}

		/**
		 * Provide a pair of test sockets that write to each other.
		 */
		@SuppressWarnings("resource")
		public static TcpSocket[] pairOf() {
			return Connector.chain(new TcpSocket(NAME + "[0->1]", HostPort.NULL, 0),
				new TcpSocket(NAME + "[1->0]", HostPort.NULL, 1));
		}

		protected TcpSocket(String name, HostPort hostPort, int localPort) {
			super(name);
			this.hostPort = CallSync.supplier(hostPort);
			this.localPort = CallSync.supplier(localPort);
		}

		@Override
		public void reset() {
			super.reset();
			CallSync.resetAll(hostPort, localPort, optionSync);
		}

		@Override
		public HostPort hostPort() {
			return hostPort.get();
		}

		@Override
		public int localPort() {
			return localPort.get();
		}

		@Override
		public <T> void option(TcpSocketOption<T> option, T value) throws IOException {
			options.set(option, value);
			optionSync.run(ExceptionAdapter.io);
		}

		@Override
		public <T> T option(TcpSocketOption<T> option) throws IOException {
			optionSync.run(ExceptionAdapter.io);
			return options.get(option);
		}
	}

	/**
	 * Returns a test input stream with default pipe size.
	 */
	public static In in() {
		return In.of(DEFAULT_SIZE);
	}

	/**
	 * Returns a test input stream with default pipe size, initialized with bytes.
	 */
	@SuppressWarnings("resource")
	public static In inBytes(int... bytes) {
		return in().apply(in -> in.to.writeBytes(bytes));
	}

	/**
	 * Returns a test input stream with default pipe size, initialized with encoded string bytes.
	 */
	@SuppressWarnings("resource")
	public static In inChars(String format, Object... args) {
		return in().apply(in -> in.to.writeUtf8(Strings.format(format, args)));
	}

	/**
	 * Returns a test output stream with default pipe size.
	 */
	public static Out out() {
		return Out.of(DEFAULT_SIZE);
	}

	/**
	 * Creates an input stream with given bytes.
	 */
	public static ByteArrayInputStream inputStream(int... bytes) {
		return new ByteArrayInputStream(Array.BYTE.of(bytes));
	}

	/**
	 * Creates an input stream based on encoded string.
	 */
	public static ByteArrayInputStream inputStream(String format, Object... args) {
		return inputStream(StandardCharsets.UTF_8, format, args);
	}

	/**
	 * Creates an input stream based on encoded string.
	 */
	public static ByteArrayInputStream inputStream(Charset charset, String format, Object... args) {
		return new ByteArrayInputStream(Strings.format(format, args).getBytes(Chars.safe(charset)));
	}

	/**
	 * Create a new fixable with default name.
	 */
	public static Fixable fixable() {
		return fixable(null);
	}

	/**
	 * Create a new fixable with given name.
	 */
	public static Fixable fixable(String name) {
		return new Fixable(name);
	}

	/**
	 * Create a new open fixable with default name.
	 */
	public static Fixable openFixable() throws IOException {
		var fixable = fixable();
		fixable.open();
		return fixable;
	}

	/**
	 * Create a new connector with default name.
	 */
	public static Connector connector() {
		return new Connector(null);
	}

	public static Socket socket() {
		return new Socket();
	}

	public static TcpSocket tcpSocket() {
		return tcpSocket(HostPort.NULL, 0);
	}

	public static TcpSocket tcpSocket(HostPort hostPort, int localPort) {
		return new TcpSocket(null, hostPort, localPort);
	}

	/**
	 * Reads a string from stdin.
	 */
	public static String readString() {
		return ExceptionAdapter.shouldNotThrow.get(() -> readString(System.in, null));
	}

	/**
	 * Reads a string from given input stream.
	 */
	public static String readString(InputStream in, Charset charset) throws IOException {
		return readString(in, charset, DEFAULT_SIZE);
	}

	/**
	 * Reads a string from given input stream.
	 */
	public static String readString(InputStream in, Charset charset, int maxBytes)
		throws IOException {
		byte[] buffer = new byte[maxBytes];
		int n = in.read(buffer);
		if (n < 1) return "";
		return Chars.decode(charset, buffer, 0, n).trim();
	}
}
