package ceri.serial.comm.jna;

import static ceri.jna.clib.jna.CTermios.CMSPAR;
import static ceri.jna.clib.jna.CTermios.PARENB;
import static ceri.jna.clib.jna.CTermios.PARODD;
import static ceri.serial.comm.jna.CSerial.DATABITS_5;
import static ceri.serial.comm.jna.CSerial.DATABITS_6;
import static ceri.serial.comm.jna.CSerial.DATABITS_7;
import static ceri.serial.comm.jna.CSerial.DATABITS_8;
import static ceri.serial.comm.jna.CSerial.FLOWCONTROL_RTSCTS_IN;
import static ceri.serial.comm.jna.CSerial.FLOWCONTROL_RTSCTS_OUT;
import static ceri.serial.comm.jna.CSerial.PARITY_EVEN;
import static ceri.serial.comm.jna.CSerial.PARITY_MARK;
import static ceri.serial.comm.jna.CSerial.PARITY_NONE;
import static ceri.serial.comm.jna.CSerial.PARITY_SPACE;
import static ceri.serial.comm.jna.CSerial.STOPBITS_1;
import static ceri.serial.comm.jna.CSerial.STOPBITS_1_5;
import static ceri.serial.comm.jna.CSerial.STOPBITS_2;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CLib;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.CtlArgs;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaLibrary;
import ceri.jna.util.JnaOs;

public class CSerialTest {
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();

	@After
	public void after() {
		Closeables.close(ref);
	}

	@Test
	public void testCloseAfterOpenFailure() {
		var lib = ref.init();
		lib.fcntl.error.set(CException.general("test"));
		Assert.thrown(() -> CSerial.open("test"));
		lib.open.assertCalls(1);
		lib.close.assertCalls(1);
	}

	@Test
	public void shouldBreak() throws CException {
		var lib = ref.init();
		int fd = CSerial.open("test");
		CSerial.brk(fd, true);
		lib.ioctl.assertAuto(CtlArgs.of(fd, CIoctl.TIOCSBRK));
		CSerial.brk(fd, false);
		lib.ioctl.assertAuto(CtlArgs.of(fd, CIoctl.TIOCCBRK));
	}

	@Test
	public void shouldFailToSetInvalidParameters() throws CException {
		ref.init();
		int fd = CSerial.open("test");
		Assert.thrown(() -> CSerial.setParams(fd, 9600, 4, STOPBITS_1, PARITY_NONE));
		Assert.thrown(() -> CSerial.setParams(fd, 9600, DATABITS_8, 4, PARITY_NONE));
		Assert.thrown(() -> CSerial.setParams(fd, 9600, DATABITS_8, STOPBITS_1, 5));
	}

	@Test
	public void shouldSetParityForLinux() {
		JnaTestUtil.testAsOs(JnaOs.linux, SetParityForLinux.class, CSerial.class, CTermios.class,
			CFcntl.class, CIoctl.class, CLib.class, CSerialTestHelper.class, TestCLibNative.class);
	}

	public static class SetParityForLinux {
		static {
			try (var enc = TestCLibNative.register()) {
				var helper = CSerialTestHelper.linux(enc.ref);
				int fd = CSerial.open("test");
				CSerial.setParams(fd, 9600, DATABITS_8, STOPBITS_1, PARITY_MARK);
				Assert.mask(helper.termios.c_cflag.intValue(), PARENB | CMSPAR | PARODD);
				CSerial.setParams(fd, 9600, DATABITS_8, STOPBITS_1, PARITY_SPACE);
				Assert.mask(helper.termios.c_cflag.intValue(), PARENB | CMSPAR);
			} catch (Exception e) {
				Assert.fail(e);
			}
		}
	}

	@Test
	public void shouldSetStandardBaudForLinux() throws CException {
		JnaOs.linux.accept(_ -> {
			var helper = CSerialTestHelper.linux(ref.init());
			helper.initSerial(0x111, 0xabc0030, 16000000, 3);
			int fd = CSerial.open("test");
			CSerial.setParams(fd, 115200, DATABITS_7, STOPBITS_2, PARITY_EVEN);
			helper.assertSpeed(CTermios.B115200);
			helper.assertSerial(0xabc0000, 3); // custom flags removed
		});
	}

	@Test
	public void shouldSetCustomBaudForLinux() throws CException {
		JnaOs.linux.accept(_ -> {
			var helper = CSerialTestHelper.linux(ref.init());
			helper.initSerial(0x111, 0xabc0000, 16000000, 0);
			int fd = CSerial.open("test");
			CSerial.setParams(fd, 250000, DATABITS_6, STOPBITS_2, PARITY_EVEN);
			helper.assertSpeed(CTermios.B38400);
			helper.assertSerial(0xabc0030, 64); // 16000000/250000 = 64
			CSerial.setParams(fd, 350000, DATABITS_5, STOPBITS_1_5, PARITY_NONE);
			helper.assertSpeed(CTermios.B38400);
			helper.assertSerial(0xabc0030, 46); // 16000000/350000 = 45.7
		});
	}

	@Test
	public void shouldFailToSetIncompatibleBaudForLinux() throws CException {
		JnaOs.linux.accept(_ -> {
			var helper = CSerialTestHelper.linux(ref.init());
			helper.initSerial(0x111, 0, 16000000, 0);
			int fd = CSerial.open("test");
			Assert
				.thrown(() -> CSerial.setParams(fd, 3750000, DATABITS_6, STOPBITS_2, PARITY_EVEN));
		});
	}

	@Test
	public void shouldSetReadParametersForLinux() throws CException {
		JnaOs.linux.accept(_ -> {
			var helper = CSerialTestHelper.linux(ref.init());
			int fd = CSerial.open("test");
			CSerial.setReadParams(fd, 11, 22);
			Assert.equals(helper.termios.c_cc[CTermios.VMIN], 11);
			Assert.equals(helper.termios.c_cc[CTermios.VTIME], 22);
		});
	}

	@Test
	public void shouldSetFlowControlForLinux() throws CException {
		JnaOs.linux.accept(_ -> {
			var helper = CSerialTestHelper.linux(ref.init());
			int fd = CSerial.open("test");
			CSerial.setFlowControl(fd, FLOWCONTROL_RTSCTS_IN);
			Assert.mask(helper.termios.c_cflag.intValue(), CTermios.CRTSCTS);
		});
	}

	@Test
	public void shouldSetStandardBaudForMac() throws CException {
		JnaOs.mac.accept(_ -> {
			var helper = CSerialTestHelper.mac(ref.init());
			int fd = CSerial.open("test");
			CSerial.setParams(fd, 115200, DATABITS_7, STOPBITS_2, PARITY_EVEN);
			helper.assertSpeed(CTermios.B115200);
		});
	}

	@Test
	public void shouldSetCustomBaudForMac() throws CException {
		JnaOs.mac.accept(_ -> {
			var helper = CSerialTestHelper.mac(ref.init());
			int fd = CSerial.open("test");
			CSerial.setParams(fd, 250000, DATABITS_6, STOPBITS_2, PARITY_EVEN);
			helper.assertSpeed(CTermios.B9600);
			helper.assertIossiospeed(fd, 250000);
			CSerial.setParams(fd, 3750000, DATABITS_6, STOPBITS_2, PARITY_EVEN);
			helper.assertSpeed(CTermios.B9600);
			helper.assertIossiospeed(fd, 3750000);
		});
	}

	@Test
	public void shouldSetReadParametersForMac() throws CException {
		JnaOs.mac.accept(_ -> {
			var helper = CSerialTestHelper.mac(ref.init());
			int fd = CSerial.open("test");
			CSerial.setReadParams(fd, 11, 22);
			Assert.equals(helper.termios.c_cc[CTermios.VMIN], 11);
			Assert.equals(helper.termios.c_cc[CTermios.VTIME], 22);
		});
	}

	@Test
	public void shouldSetReadParametersWithCustomBaudForMac() throws CException {
		JnaOs.mac.accept(_ -> {
			var helper = CSerialTestHelper.mac(ref.init());
			int fd = CSerial.open("test");
			CSerial.setParams(fd, 250000, DATABITS_8, STOPBITS_1, PARITY_NONE);
			CSerial.setReadParams(fd, 11, 22);
			Assert.equals(helper.termios.c_cc[CTermios.VMIN], 11);
			Assert.equals(helper.termios.c_cc[CTermios.VTIME], 22);
			helper.assertSpeed(CTermios.B9600);
			helper.assertIossiospeed(fd, 250000);
		});
	}

	@Test
	public void shouldSetFlowControlForMac() throws CException {
		JnaOs.mac.accept(_ -> {
			var helper = CSerialTestHelper.mac(ref.init());
			int fd = CSerial.open("test");
			CSerial.setFlowControl(fd, FLOWCONTROL_RTSCTS_OUT);
			Assert.mask(helper.termios.c_cflag.intValue(), CTermios.CRTSCTS);
		});
	}
}
