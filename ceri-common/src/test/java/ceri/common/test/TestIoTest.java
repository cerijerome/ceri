package ceri.common.test;

import static ceri.common.data.Bytes.toAscii;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.concurrent.ValueCondition;
import ceri.common.data.ByteStream;
import ceri.common.data.Bytes;
import ceri.common.io.StateChange;
import ceri.common.io.SystemIo;
import ceri.common.net.HostPort;
import ceri.common.reflect.Reflect;
import ceri.common.text.Utf8;

public class TestIoTest {
	private TestIo.In in;
	private TestIo.Out out;
	private TestIo.Connector con;
	private TestIo.Connector con2;
	private TestIo.Fixable fixable;
	private TestIo.Socket socket;
	private TestIo.TcpSocket tcpSocket;

	@After
	public void after() {
		in = Testing.close(in);
		out = Testing.close(out);
		con = Testing.close(con);
		con2 = Testing.close(con2);
		fixable = Testing.close(fixable);
		socket = Testing.close(socket);
		tcpSocket = Testing.close(tcpSocket);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(TestIo.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFeedInputBytes() throws IOException {
		in = TestIo.in();
		in.to.writeBytes(1, 2, 3);
		Assert.equal(in.available(), 3);
		Assert.read(in, 1, 2, 3);
		Assert.equal(in.available(), 0);
	}

	@Test
	public void shouldWaitForInputFeedToBeEmpty() throws IOException {
		in = TestIo.inBytes(1, 2, 3);
		try (var exec = TestConcurrent.threadCall(() -> in.readNBytes(3))) {
			in.awaitFeed();
			Assert.array(exec.get(), 1, 2, 3);
		}
	}

	@Test
	public void shouldProvideInputForEof() throws IOException {
		in = TestIo.inBytes(1, 2, 3);
		Assert.equal(in.read(), 1);
		in.eof(true);
		Assert.equal(in.read(), -1); // returns -1 but still reads byte
		in.eof(false);
		Assert.equal(in.read(), 3);
	}

	@Test
	public void shouldMarkInputAndReset() throws IOException {
		in = TestIo.inChars("testing");
		Assert.read(in, Utf8.encode("test"));
		in.markSupported.autoResponses(true);
		Assert.yes(in.markSupported());
		in.mark(10);
		in.mark.assertAuto(10);
		Assert.read(in, Utf8.encode("ing"));
		in.reset();
		in.reset.awaitAuto();
	}

	@Test
	public void shouldGenerateInputReadError() throws IOException {
		in = TestIo.inBytes(0);
		in.read.error.setFrom(ErrorGen.IOX);
		Assert.equal(in.available(), 1);
		Assert.thrown(() -> in.read());
		Assert.equal(in.available(), 0);
	}

	@Test
	public void shouldGenerateInputAvailableError() throws IOException {
		in = TestIo.in();
		Assert.equal(in.available(), 0);
		in.available.error.setFrom(ErrorGen.IOX);
		Assert.thrown(() -> in.available());
	}

	@Test
	public void shouldSinkOutputBytes() throws IOException {
		out = TestIo.out();
		out.write(Array.BYTE.of(1, 2, 3));
		Assert.equal(out.from.available(), 3);
		Assert.read(out.from, 1, 2, 3);
		Assert.equal(out.from.available(), 0);
	}

	@Test
	public void shouldFlushOutput() throws IOException {
		out = TestIo.out();
		out.assertAvailable(0);
		out.write(Array.BYTE.of(1, 2, 3));
		out.flush();
		Assert.assertion(() -> out.assertAvailable(2));
		out.assertAvailable(3);
	}

	@Test
	public void shouldGenerateOutputWriteError() throws IOException {
		out = TestIo.out();
		out.write(1);
		out.write.error.setFrom(ErrorGen.IOX);
		Assert.thrown(() -> out.write(2));
		Assert.read(out.from, 1, 2);
	}

	@Test
	public void shouldMatchOutputAsText() throws IOException {
		out = TestIo.out();
		try (var run = TestConcurrent.threadRun(() -> {
			out.awaitMatch("(?s).*\nx");
		})) {
			out.write(Bytes.toAsciiBytes("test\0"));
			out.write(Bytes.toAsciiBytes("\n"));
			out.write(Bytes.toAsciiBytes("x"));
			run.get();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideFixableName() {
		Assert.match(TestIo.fixable().name(), Reflect.name(TestIo.Fixable.class) + "@.*");
		Assert.match(TestIo.fixable("test").name(), "test");
	}

	@Test
	public void shouldProvideFixableStringRepresentation() throws IOException {
		fixable = TestIo.fixable();
		Assert.find(fixable, "fixed,closed");
		fixable.open();
		Assert.find(fixable, "fixed,open");
		fixable.broken();
		Assert.find(fixable, "broken,closed");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOpenFixableOnCreation() throws IOException {
		TestIo.openFixable().open.assertAuto(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldEchoConnectorToInput() throws IOException {
		con = TestIo.connector();
		con.open();
		con.echoOn();
		con.out().write(Array.BYTE.of(1, 2, 3, 4, 5), 1, 3);
		Assert.equal(con.in().available(), 3);
		Assert.read(con.in(), 2, 3, 4);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldPairConnectors() throws IOException {
		con = TestIo.connector();
		con2 = TestIo.connector();
		con.pairWith(con2);
		con.open();
		con2.open();
		con.out().write(Array.BYTE.of(1, 2, 3));
		Assert.read(con2.out.from, 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailToReadIfConnectorBroken() {
		con = TestIo.connector();
		con.broken();
		con.in.to.writeBytes(0, 0);
		Assert.thrown(con.in()::read);
		Assert.thrown(() -> con.in().read(new byte[1]));
		Assert.thrown(con.in()::available);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailToReadIfNotConnected() {
		con = TestIo.connector();
		con.in.to.writeBytes(0, 0);
		Assert.thrown(con.in()::read);
		Assert.thrown(() -> con.in().read(new byte[1]));
		Assert.thrown(con.in()::available);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailToWriteIfConnectorBroken() {
		con = TestIo.connector();
		con.broken();
		Assert.thrown(() -> con.out().write(0));
		Assert.thrown(() -> con.out().write(new byte[3]));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailToWriteIfNotConnected() {
		con = TestIo.connector();
		Assert.thrown(() -> con.out().write(0));
		Assert.thrown(() -> con.out().write(new byte[3]));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBreakConnector() throws InterruptedException {
		con = TestIo.connector();
		con.in.to.writeBytes(1, 2, 3);
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var _ = con.listeners().enclose(sync::signal)) {
			con.broken();
			Assert.equal(sync.await(), StateChange.broken);
			Assert.thrown(() -> con.in().read());
			con.broken();
			Assert.isNull(sync.value());
			Assert.thrown(() -> con.in().read());
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFixConnector() throws InterruptedException, IOException {
		con = TestIo.connector();
		con.in.to.writeBytes(1, 2, 3);
		con.broken();
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var _ = con.listeners().enclose(sync::signal)) {
			con.fixed();
			Assert.equal(sync.await(), StateChange.fixed);
			con.fixed();
			Assert.isNull(sync.value());
			Assert.read(con.in(), 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldResetConnectorState() throws IOException {
		con = TestIo.connector();
		con.broken();
		con.in.read.error.setFrom(ErrorGen.IOX);
		con.out.write.error.setFrom(ErrorGen.IOX);
		con.reset();
		con.open();
		con.in.to.writeBytes(0);
		con.in().available();
		con.in().read();
		con.out().write(0);
	}

	@Test
	public void shouldProvideConnectorStringRepresentation() {
		con = TestIo.connector();
		Assert.find(con.toString(), ".+");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldConnect() throws IOException {
		socket = TestIo.socket();
		socket.connect("test", 123);
		socket.remote.assertAuto(HostPort.of("test", 123));
		Assert.equal(socket.getPort(), 123);
	}

	@Test
	public void shouldProvideLocalPort() {
		socket = TestIo.socket();
		socket.localPort.autoResponses(456);
		Assert.equal(socket.getLocalPort(), 456);
		socket = TestIo.socket();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideInputStream() throws IOException {
		socket = TestIo.socket();
		socket.in.to.writeAscii("test");
		Assert.read(socket.getInputStream(), toAscii("test"));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideOutputStream() throws IOException {
		socket = TestIo.socket();
		socket.getOutputStream().write(toAscii("test").copy(0));
		Assert.read(socket.out.from, toAscii("test"));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldEchoTcpSocket() throws IOException {
		tcpSocket = TestIo.TcpSocket.ofEcho();
		tcpSocket.open();
		var in = ByteStream.reader(tcpSocket.in());
		var out = ByteStream.writer(tcpSocket.out());
		out.writeAscii("test");
		Assert.ascii(in, "test");
		tcpSocket.out().write(Array.BYTE.of(1, 2, 3));
		Assert.read(tcpSocket.in(), 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideTcpSocketConnector() {
		tcpSocket = TestIo.tcpSocket();
		Assert.equal(tcpSocket.hostPort(), HostPort.NULL);
		tcpSocket.in.read.error.setFrom(ErrorGen.IOX);
		tcpSocket.in.to.writeBytes(0);
		Assert.io(tcpSocket.in()::read);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideTcpSocketPair() throws IOException {
		var ss = TestIo.TcpSocket.pairOf();
		try {
			ss[0].open();
			ss[1].open();
			ss[0].out().write(Array.BYTE.of(1, 2, 3));
			ss[1].out().write(Array.BYTE.of(4, 5, 6));
			Assert.read(ss[0].in(), 4, 5, 6);
			Assert.read(ss[1].in(), 1, 2, 3);
		} finally {
			Testing.close(ss[0]);
			Testing.close(ss[1]);
		}
	}

	@Test
	public void shouldProvideTcpSocketHostPort() {
		tcpSocket = TestIo.tcpSocket(HostPort.of("test", 123), 456);
		Assert.equal(tcpSocket.hostPort(), HostPort.of("test", 123));
		tcpSocket.hostPort.autoResponses(HostPort.LOCALHOST);
		Assert.equal(tcpSocket.hostPort(), HostPort.LOCALHOST);
		tcpSocket.reset();
		Assert.equal(tcpSocket.hostPort(), HostPort.of("test", 123));
	}

	@Test
	public void shouldProvideTcpSocketLocalPort() {
		tcpSocket = TestIo.tcpSocket(HostPort.of("test", 123), 456);
		Assert.equal(tcpSocket.localPort(), 456);
		tcpSocket.localPort.autoResponses(123);
		Assert.equal(tcpSocket.localPort(), 123);
		tcpSocket.reset();
		Assert.equal(tcpSocket.localPort(), 456);
	}

	@Test
	public void testReadString() {
		try (var sys = SystemIo.of()) {
			sys.in(new ByteArrayInputStream("test".getBytes()));
			Assert.equal(TestIo.readString(), "test");
			Assert.equal(TestIo.readString(), "");
		}
	}

	@Test
	public void testReadStringWithBadInputStream() throws IOException {
		try (var sys = SystemIo.of()) {
			try (var badIn = new InputStream() {
				@Override
				public int read() throws IOException {
					throw new IOException();
				}
			}) {
				sys.in(badIn);
				Assert.thrown(TestIo::readString);
			}
		}
	}

}
