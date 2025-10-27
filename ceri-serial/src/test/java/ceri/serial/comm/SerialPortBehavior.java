package ceri.serial.comm;

import static ceri.jna.test.JnaTestUtil.assertRef;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.ptr.IntByReference;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
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
		Assert.no(SerialPort.isFatal(null));
		Assert.no(SerialPort.isFatal(new IOException()));
		Assert.no(SerialPort.isFatal(CException.general("test")));
		Assert.yes(SerialPort.isFatal(ErrNo.ENOENT.error("test")));
		Assert.yes(SerialPort.isFatal(ErrNo.ENXIO.error("test")));
		Assert.yes(SerialPort.isFatal(ErrNo.EBADF.error("test")));
		Assert.yes(SerialPort.isFatal(ErrNo.EACCES.error("test")));
		Assert.yes(SerialPort.isFatal(ErrNo.ENODEV.error("test")));
	}

	@Test
	public void shouldConfigurePort() throws IOException {
		initSerial();
		var params = SerialParams.from("1200,6,2,e");
		serial.params(params);
		serial.flowControls(FlowControl.xonXoffIn);
		serial.inBufferSize(111);
		serial.outBufferSize(222);
		Assert.equal(serial.params(), params);
		Assert.unordered(serial.flowControl(), FlowControl.xonXoffIn);
		Assert.equal(serial.inBufferSize(), 111);
		Assert.equal(serial.outBufferSize(), 222);
	}

	@Test
	public void shouldSetSerialState() throws IOException {
		var lib = initSerial();
		serial.brk(true);
		Assert.equal(lib.ioctl.awaitAuto(), CtlArgs.of(lib.lastFd(), CIoctl.TIOCSBRK));
		serial.rts(false);
		assertIoctlIntRef(lib.lastFd(), CIoctl.TIOCMBIC, CIoctl.TIOCM_RTS);
		serial.dtr(true);
		assertIoctlIntRef(lib.lastFd(), CIoctl.TIOCMBIS, CIoctl.TIOCM_DTR);
	}

	@Test
	public void shouldGetSerialState() throws IOException {
		initSerial();
		ioctlAutoIntRef(CIoctl.TIOCM_RTS | CIoctl.TIOCM_CD | CIoctl.TIOCM_DSR);
		Assert.equal(serial.rts(), true);
		Assert.equal(serial.dtr(), false);
		Assert.equal(serial.cd(), true);
		Assert.equal(serial.cts(), false);
		Assert.equal(serial.dsr(), true);
		Assert.equal(serial.ri(), false);
		ioctlAutoIntRef(CIoctl.TIOCM_DTR | CIoctl.TIOCM_CTS | CIoctl.TIOCM_RI);
		Assert.equal(serial.rts(), false);
		Assert.equal(serial.dtr(), true);
		Assert.equal(serial.cd(), false);
		Assert.equal(serial.cts(), true);
		Assert.equal(serial.dsr(), false);
		Assert.equal(serial.ri(), true);
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
		Assert.equal(args.fd(), fd);
		Assert.equal(args.request(), request);
		assertRef(args.arg(0), value);
	}
}
