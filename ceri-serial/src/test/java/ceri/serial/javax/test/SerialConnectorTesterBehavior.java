package ceri.serial.javax.test;

import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.time.TimeSupplier.millis;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.io.SystemIo;
import ceri.common.test.TestInputStream;
import ceri.common.test.TestOutputStream;
import ceri.common.test.TestUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.test.LogModifier;
import ceri.serial.javax.FlowControl;

public class SerialConnectorTesterBehavior {
	private LogModifier logMod;
	private SystemIo sys;
	private TestOutputStream out;
	private TestInputStream in;
	private TestSerialConnector con;

	@Before
	public void before() throws IOException {
		logMod = LogModifier.of(Level.OFF, SerialConnectorTester.class, LoopingExecutor.class);
		sys = SystemIo.of();
		in = TestInputStream.of();
		out = TestOutputStream.of();
		sys.in(in);
		sys.out(new PrintStream(out));
		con = TestSerialConnector.of();
		con.connect();
	}

	@After
	public void afterClass() throws IOException {
		con.close();
		out.close();
		in.close();
		sys.close();
		logMod.close();
	}

	@Test
	public void shouldInterrupt() {
		try (var run = TestUtil.threadRun(() -> SerialConnectorTester.test(con, null))) {
			millis.delay(1);
			run.cancel();
			assertThrown(run::get);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteToSerialPort() throws IOException {
		try (var tester = SerialConnectorTester.of(con, null, 0)) {
			awaitHelp();
			in.to.writeString("o\n");
			awaitPrompt();
			in.to.writeString("otest\\0\\xff\n");
			con.out.awaitMatchAscii("test\0\u00ff");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadFromSerialPort() throws IOException {
		try (var tester = SerialConnectorTester.of(con, null, 0)) {
			awaitHelp();
			con.in.to.writeString("test\0");
			in.to.writeString("\n");
			out.awaitMatchAscii("(?s)IN <<<.*74 65 73 74 00.*> ");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBreakAndFix() throws IOException {
		try (var tester = SerialConnectorTester.of(con, con::fixed, 0)) {
			awaitHelp();
			in.to.writeString("z\n");
			awaitPrompt();
			con.broken.assertAuto(true);
			in.to.writeString("otest\n");
			awaitPrompt();
			in.to.writeString("Z\n");
			con.broken.assertAuto(false);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAcceptCommands() throws IOException {
		try (var tester = SerialConnectorTester.of(con, null, 0)) {
			awaitHelp();
			in.to.writeString("r1\n");
			con.rts.assertAuto(true);
			in.to.writeString("b0\n");
			con.breakBit.assertAuto(false);
			in.to.writeString("d1\n");
			con.dtr.assertAuto(true);
			in.to.writeString("fn\n");
			con.flowControl.assertAuto(FlowControl.none);
			in.to.writeString("fr0\n");
			con.flowControl.assertAuto(FlowControl.rtsCtsIn);
			in.to.writeString("fr1\n");
			con.flowControl.assertAuto(FlowControl.rtsCtsOut);
			in.to.writeString("fx0\n");
			con.flowControl.assertAuto(FlowControl.xonXoffIn);
			in.to.writeString("fx1\n");
			con.flowControl.assertAuto(FlowControl.xonXoffOut);
			in.to.writeString("x\n");
			tester.waitUntilStopped();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAcceptInvalidCommands() throws IOException {
		try (var tester = SerialConnectorTester.of(con, null, 0)) {
			awaitHelp();
			in.to.writeString("f\n");
			awaitPrompt();
			in.to.writeString("ff\n");
			awaitPrompt();
			in.to.writeString("r2\n");
			awaitPrompt();
			in.to.writeString("Z\n");
			awaitPrompt();
			in.to.writeString("@\n");
			awaitPrompt();
			in.to.writeString("\n");
			awaitPrompt();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldshowHelp() throws IOException {
		try (var tester = SerialConnectorTester.of(con, null, 0)) {
			awaitHelp();
			in.to.writeString("?\n");
			awaitHelp();
		}
	}

	private void awaitHelp() throws IOException {
		out.awaitMatchAscii("(?s).*x = exit\n> ");
	}

	private void awaitPrompt() throws IOException {
		out.awaitMatchAscii("(?s).*> ");
	}
}
