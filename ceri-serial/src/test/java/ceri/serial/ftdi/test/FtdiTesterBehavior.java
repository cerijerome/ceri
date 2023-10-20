package ceri.serial.ftdi.test;

import static ceri.common.test.AssertUtil.assertThrown;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
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
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.libusb.jna.LibUsbSampleData;
import ceri.serial.libusb.jna.TestLibUsbNative;

public class FtdiConnectorTesterBehavior {
	private LogModifier logMod;
	private SystemIo sys;
	private TestOutputStream out;
	private TestInputStream in;
	private TestFtdi con;

	@Before
	public void before() throws IOException {
		logMod = LogModifier.of(Level.OFF, FtdiTester.class, LoopingExecutor.class);
		sys = SystemIo.of();
		in = TestInputStream.of();
		out = TestOutputStream.of();
		sys.in(in);
		sys.out(new PrintStream(out));
		con = TestFtdi.of();
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

	@SuppressWarnings("resource")
	@Test
	public void shouldFindDevice() throws IOException {
		try (var enc = TestLibUsbNative.register()) {
			enc.ref.data.deviceConfigs.add(LibUsbSampleData.ftdiConfig());
			try (var run = TestUtil.threadRun(() -> FtdiTester.test("0x0403:0x6001"))) {
				awaitHelp();
				in.to.writeString("x\n");
				run.get();
			}
		}
	}

	@Test
	public void shouldInterrupt() throws IOException {
		try (var run = TestUtil.threadRun(() -> FtdiTester.test(con, null))) {
			awaitHelp();
			run.cancel();
			assertThrown(run::get);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteToFtdi() throws IOException {
		try (var tester = FtdiTester.of(con, null, 0)) {
			awaitHelp();
			in.to.writeString("o\n");
			awaitPrompt();
			in.to.writeString("otest\\0\\xff\n");
			con.out.awaitMatch("test\0\u00ff", ISO_8859_1);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadFromFtdi() throws IOException {
		try (var tester = FtdiTester.of(con, null, 0)) {
			awaitHelp();
			con.in.to.writeString("test\0");
			in.to.writeString("i0\n");
			awaitPrompt();
			in.to.writeString("i5\n");
			out.awaitMatch("(?s)IN <<<.*74 65 73 74 00.*> ");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadPins() throws IOException {
		try (var tester = FtdiTester.of(con, null, 0)) {
			awaitHelp();
			con.pins.autoResponses(0xff96a55a);
			in.to.writeString("p\n");
			out.awaitMatch("(?s)PINS <<<.*ff 96 a5 5a.*> ");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBreakAndFix() throws IOException {
		try (var tester = FtdiTester.of(con, con::fixed, 0)) {
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
		try (var tester = FtdiTester.of(con, null, 0)) {
			awaitHelp();
			in.to.writeString("r1\n");
			con.rts.assertAuto(true);
			in.to.writeString("b0\n");
			con.bitmode.assertAuto(FtdiBitMode.OFF);
			in.to.writeString("b1\n");
			con.bitmode.assertAuto(FtdiBitMode.BITBANG);
			in.to.writeString("d1\n");
			con.dtr.assertAuto(true);
			in.to.writeString("fn\n");
			con.flowControl.assertAuto(FtdiFlowControl.disabled);
			in.to.writeString("fr\n");
			con.flowControl.assertAuto(FtdiFlowControl.rtsCts);
			in.to.writeString("fd\n");
			con.flowControl.assertAuto(FtdiFlowControl.dtrDsr);
			in.to.writeString("fx\n");
			con.flowControl.assertAuto(FtdiFlowControl.xonXoff);
			in.to.writeString("x\n");
			tester.waitUntilStopped();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAcceptInvalidCommands() throws IOException {
		try (var tester = FtdiTester.of(con, null, 0)) {
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
		try (var tester = FtdiTester.of(con, null, 0)) {
			awaitHelp();
			in.to.writeString("?\n");
			awaitHelp();
		}
	}

	private void awaitHelp() throws IOException {
		out.awaitMatch("(?s).*x = exit\n> ");
	}

	private void awaitPrompt() throws IOException {
		out.awaitMatch("(?s).*> ");
	}
}
