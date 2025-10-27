package ceri.serial.comm.test;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.FileTestHelper;
import ceri.common.test.ManualTester;
import ceri.common.test.SystemIoCaptor;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.util.JnaLibrary;
import ceri.serial.comm.DataBits;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Parity;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.StopBits;

public class SerialTesterBehavior {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private Functions.Closeable fastMode;
	private SystemIoCaptor sys;
	private TestSerial serial;
	private FileTestHelper files;

	@Before
	public void before() {
		fastMode = ManualTester.fastMode();
	}

	@After
	public void after() {
		Closeables.close(serial, sys, ref, files, fastMode);
		fastMode = null;
		files = null;
		sys = null;
		serial = null;
	}

	@Test
	public void shouldTestEcho() throws IOException {
		sys = SystemIoCaptor.of();
		sys.in.print("otesting\n\n!\n");
		SerialTester.testEcho();
		Assert.find(sys.out, "(?s)IN <<<.*testing");
		Assert.string(sys.err, "");
	}

	@Test
	public void shouldTestPair() throws IOException {
		sys = SystemIoCaptor.of();
		sys.in.print("otesting\n+\n!\n");
		SerialTester.testPair();
		Assert.find(sys.out, "(?s)IN <<<.*testing");
		Assert.string(sys.err, "");
	}

	@Test
	public void shouldTestUsbPorts() throws IOException {
		files = FileTestHelper.builder().file("tty.usb0", "").build();
		init(true);
		sys.in.print("!\n");
		SerialTester.testUsbPorts(files.root);
		Assert.string(sys.err, "");
	}

	@Test
	public void shouldTestPorts() throws IOException {
		init(true);
		sys.in.print("!\n");
		SerialTester.testPorts("test0", "test1");
		Assert.string(sys.err, "");
	}

	@Test
	public void shouldConfigureSerial() throws IOException {
		init(false);
		sys.in.print("P\np1200,5,2,e\np\nfN\nfrRxX\nf\nBi111\nBi\nBo222\nBo\n!\n");
		SerialTester.test(serial);
		Assert.equal(serial.params(), SerialParams.of(1200, DataBits._5, StopBits._2, Parity.even));
		Assert.unordered(serial.flowControl(), FlowControl.rtsCtsIn, FlowControl.rtsCtsOut,
			FlowControl.xonXoffIn, FlowControl.xonXoffOut);
		Assert.equal(serial.inBufferSize(), 111);
		Assert.equal(serial.outBufferSize(), 222);
		Assert.string(sys.err, "");
	}

	@Test
	public void shouldSetSerialState() throws IOException {
		init(false);
		sys.in.print("b1\nr0\nd1\nl\nb0\nr1\nd0\nl\n!\n");
		serial.cd.autoResponses(false, true);
		serial.cts.autoResponses(true, false);
		serial.dsr.autoResponses(false, true);
		serial.ri.autoResponses(true, false);
		SerialTester.test(serial);
		Assert.find(sys.out, "(?s)\\[DTR, CTS, RI\\].*\\[RTS, CD, DSR\\]");
		Assert.string(sys.err, "");
	}

	private void init(boolean initLib) {
		sys = SystemIoCaptor.of();
		if (initLib) ref.init();
		else serial = TestSerial.of();
	}
}
