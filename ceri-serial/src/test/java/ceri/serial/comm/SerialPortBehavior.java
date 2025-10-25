package ceri.serial.comm;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertTrue;
import static ceri.common.test.Assert.assertUnordered;
import static ceri.jna.test.JnaTestUtil.assertRef;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.ptr.IntByReference;
import ceri.common.function.Closeables;
import ceri.jna.clib.ErrNo;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.CtlArgs;
import ceri.jna.clib.test.TestCLibNative.TcArgs;
import ceri.jna.util.JnaLibrary;

public class SerialPortBehavior {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private SerialPort serial;

	@After
	public void after() {
		Closeables.close(serial, ref);
		serial = null;
	}

	@Test
	public void testIsFatal() {
		assertFalse(SerialPort.isFatal(null));
		assertFalse(SerialPort.isFatal(new IOException()));
		assertFalse(SerialPort.isFatal(CException.general("test")));
		assertTrue(SerialPort.isFatal(ErrNo.ENOENT.error("test")));
		assertTrue(SerialPort.isFatal(ErrNo.ENXIO.error("test")));
		assertTrue(SerialPort.isFatal(ErrNo.EBADF.error("test")));
		assertTrue(SerialPort.isFatal(ErrNo.EACCES.error("test")));
		assertTrue(SerialPort.isFatal(ErrNo.ENODEV.error("test")));
	}

	@Test
	public void shouldConfigurePort() throws IOException {
		initSerial();
		var params = SerialParams.from("1200,6,2,e");
		serial.params(params);
		serial.flowControls(FlowControl.xonXoffIn);
		serial.inBufferSize(111);
		serial.outBufferSize(222);
		assertEquals(serial.params(), params);
		assertUnordered(serial.flowControl(), FlowControl.xonXoffIn);
		assertEquals(serial.inBufferSize(), 111);
		assertEquals(serial.outBufferSize(), 222);
	}

	@Test
	public void shouldSetSerialState() throws IOException {
		var lib = initSerial();
		serial.brk(true);
		assertEquals(lib.ioctl.awaitAuto(), CtlArgs.of(lib.lastFd(), CIoctl.TIOCSBRK));
		serial.rts(false);
		assertIoctlIntRef(lib.lastFd(), CIoctl.TIOCMBIC, CIoctl.TIOCM_RTS);
		serial.dtr(true);
		assertIoctlIntRef(lib.lastFd(), CIoctl.TIOCMBIS, CIoctl.TIOCM_DTR);
	}

	@Test
	public void shouldGetSerialState() throws IOException {
		initSerial();
		ioctlAutoIntRef(CIoctl.TIOCM_RTS | CIoctl.TIOCM_CD | CIoctl.TIOCM_DSR);
		assertEquals(serial.rts(), true);
		assertEquals(serial.dtr(), false);
		assertEquals(serial.cd(), true);
		assertEquals(serial.cts(), false);
		assertEquals(serial.dsr(), true);
		assertEquals(serial.ri(), false);
		ioctlAutoIntRef(CIoctl.TIOCM_DTR | CIoctl.TIOCM_CTS | CIoctl.TIOCM_RI);
		assertEquals(serial.rts(), false);
		assertEquals(serial.dtr(), true);
		assertEquals(serial.cd(), false);
		assertEquals(serial.cts(), true);
		assertEquals(serial.dsr(), false);
		assertEquals(serial.ri(), true);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFlushOutputStream() throws IOException {
		var lib = initSerial();
		lib.write.autoResponses(1);
		serial.out().write(1);
		serial.out().flush();
		lib.tc.assertAuto(TcArgs.of("tcdrain", lib.lastFd()));
	}

	private TestCLibNative initSerial() throws IOException {
		var lib = ref.init();
		serial = SerialPort.open("test");
		return lib;
	}

	private void ioctlAutoIntRef(int value) {
		ref.lib().ioctl.autoResponse(args -> args.<IntByReference>arg(0).setValue(value), 0);
	}

	private void assertIoctlIntRef(int fd, int request, int value) {
		var args = ref.lib().ioctl.awaitAuto();
		assertEquals(args.fd(), fd);
		assertEquals(args.request(), request);
		assertRef(args.arg(0), value);
	}
}
