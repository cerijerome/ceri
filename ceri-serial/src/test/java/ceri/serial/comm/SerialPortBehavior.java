package ceri.serial.comm;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.jna.test.JnaTestUtil.assertRef;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.ptr.IntByReference;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CError;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.test.TestCLibNative;

public class SerialPortBehavior {
	private TestCLibNative lib;
	private Enclosed<RuntimeException, ?> enc;
	private SerialPort serial;

	@After
	public void after() {
		CloseableUtil.close(serial, enc);
		enc = null;
		lib = null;
		serial = null;
	}

	@Test
	public void testIsFatal() {
		assertFalse(SerialPort.isFatal(null));
		assertFalse(SerialPort.isFatal(new IOException()));
		assertFalse(SerialPort.isFatal(CException.general("test")));
		assertTrue(SerialPort.isFatal(CException.of(CError.ENOENT, "test")));
		assertTrue(SerialPort.isFatal(CException.of(CError.ENXIO, "test")));
		assertTrue(SerialPort.isFatal(CException.of(CError.EBADF, "test")));
		assertTrue(SerialPort.isFatal(CException.of(CError.EACCES, "test")));
		assertTrue(SerialPort.isFatal(CException.of(CError.ENODEV, "test")));
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
		assertCollection(serial.flowControl(), FlowControl.xonXoffIn);
		assertEquals(serial.inBufferSize(), 111);
		assertEquals(serial.outBufferSize(), 222);
	}

	@Test
	public void shouldSetSerialState() throws IOException {
		initSerial();
		serial.brk(true);
		lib.assertIoctl(lib.lastFd().fd(), CIoctl.TIOCSBRK);
		serial.rts(false);
		lib.<IntByReference>assertIoctlArg(lib.lastFd().fd(), CIoctl.TIOCMBIC,
			r -> assertRef(r, CIoctl.TIOCM_RTS));
		serial.dtr(true);
		lib.<IntByReference>assertIoctlArg(lib.lastFd().fd(), CIoctl.TIOCMBIS,
			r -> assertRef(r, CIoctl.TIOCM_DTR));
	}

	@Test
	public void shouldGetSerialState() throws IOException {
		initSerial();
		ioctlAutoResponseArgIntRef(CIoctl.TIOCM_RTS | CIoctl.TIOCM_CD | CIoctl.TIOCM_DSR);
		assertEquals(serial.rts(), true);
		assertEquals(serial.dtr(), false);
		assertEquals(serial.cd(), true);
		assertEquals(serial.cts(), false);
		assertEquals(serial.dsr(), true);
		assertEquals(serial.ri(), false);
		ioctlAutoResponseArgIntRef(CIoctl.TIOCM_DTR | CIoctl.TIOCM_CTS | CIoctl.TIOCM_RI);
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
		initSerial();
		lib.write.autoResponses(1);
		serial.out().write(1);
		serial.out().flush();
		lib.tc.assertAuto(List.of("tcdrain", lib.lastFd()));
	}

	private void initSerial() throws IOException {
		lib = TestCLibNative.of();
		enc = TestCLibNative.register(lib);
		serial = SerialPort.open("test");
	}

	private void ioctlAutoResponseArgIntRef(int value) {
		lib.<IntByReference>ioctlAutoResponseTypedOk(ref -> ref.setValue(value));
	}
}
