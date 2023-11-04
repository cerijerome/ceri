package ceri.serial.comm.test;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertString;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.FileTestHelper;
import ceri.common.test.SystemIoCaptor;
import ceri.jna.clib.test.TestCLibNative;
import ceri.serial.comm.DataBits;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Parity;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.StopBits;

public class SerialTesterBehavior {

	@Test
	public void shouldTestEcho() throws IOException {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			sys.in.print("otesting\n\n!\n");
			SerialTester.testEcho();
			assertFind(sys.out, "(?s)IN <<<.*testing");
			assertString(sys.err, "");
		}
	}

	@Test
	public void shouldTestPair() throws IOException {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			sys.in.print("otesting\n+\n!\n");
			SerialTester.testPair();
			assertFind(sys.out, "(?s)IN <<<.*testing");
			assertString(sys.err, "");
		}
	}

	@Test
	public void shouldTestUsbPorts() throws IOException {
		var lib = TestCLibNative.of();
		try (var f = FileTestHelper.builder().file("tty.usb0", "").build();
			SystemIoCaptor sys = SystemIoCaptor.of(); var enc = TestCLibNative.register(lib)) {
			sys.in.print("!\n");
			SerialTester.testUsbPorts(f.root);
			assertString(sys.err, "");
		}
	}

	@Test
	public void shouldTestPorts() throws IOException {
		var lib = TestCLibNative.of();
		try (SystemIoCaptor sys = SystemIoCaptor.of(); var enc = TestCLibNative.register(lib)) {
			sys.in.print("!\n");
			SerialTester.testPorts("test0", "test1");
			assertString(sys.err, "");
		}
	}

	@Test
	public void shouldConfigureSerial() throws IOException {
		try (SystemIoCaptor sys = SystemIoCaptor.of(); var s = TestSerial.of()) {
			sys.in.print("P\np1200,5,2,e\np\nfN\nfrRxX\nf\nBi111\nBi\nBo222\nBo\n!\n");
			SerialTester.test(s);
			assertEquals(s.params(), SerialParams.of(1200, DataBits._5, StopBits._2, Parity.even));
			assertCollection(s.flowControl(), FlowControl.rtsCtsIn, FlowControl.rtsCtsOut,
				FlowControl.xonXoffIn, FlowControl.xonXoffOut);
			assertEquals(s.inBufferSize(), 111);
			assertEquals(s.outBufferSize(), 222);
			assertString(sys.err, "");
		}
	}

	@Test
	public void shouldSetSerialState() throws IOException {
		try (SystemIoCaptor sys = SystemIoCaptor.of(); var s = TestSerial.of()) {
			sys.in.print("b1\nr0\nd1\nl\nb0\nr1\nd0\nl\n!\n");
			s.cd.autoResponses(false, true);
			s.cts.autoResponses(true, false);
			s.dsr.autoResponses(false, true);
			s.ri.autoResponses(true, false);
			SerialTester.test(s);
			assertFind(sys.out, "(?s)\\[DTR, CTS, RI\\].*\\[RTS, CD, DSR\\]");
			assertString(sys.err, "");
		}
	}
}
