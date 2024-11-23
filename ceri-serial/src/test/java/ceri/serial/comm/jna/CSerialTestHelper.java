package ceri.serial.comm.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import ceri.jna.clib.jna.CIoctl;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.jna.CTermios.speed_t;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.clib.test.TestCLibNative.CfArgs;
import ceri.jna.clib.test.TestCLibNative.CtlArgs;
import ceri.jna.clib.test.TestCLibNative.TcArgs;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.Struct;

/**
 * Provides support for low-level serial testing. Simulates some interactions of serial calls using
 * test CLib.
 */
public abstract class CSerialTestHelper {
	private final TestCLibNative lib;

	public static Mac mac(TestCLibNative lib) {
		return new Mac(lib);
	}

	public static Linux linux(TestCLibNative lib) {
		return new Linux(lib);
	}

	public static class Mac extends CSerialTestHelper {
		public final CTermios.Mac.termios termios = new CTermios.Mac.termios();
		private long speed;

		private Mac(TestCLibNative lib) {
			super(lib);
			lib.ioctl.autoResponse(args -> handleIos(args), 0);
			lib.tc.autoResponse(args -> handleTc(args), 0);
			lib.cf.autoResponse(args -> handleCfSpeed(args, termios));
		}

		public void assertSpeed(int speed) {
			CSerialTestHelper.assertSpeed(termios, speed);
		}

		public void assertIossiospeed(int fd, int speed) {
			var args = super.lib.ioctl.awaitAuto();
			assertEquals(args.fd(), fd);
			assertEquals(args.request(), CIoctl.Mac.IOSSIOSPEED);
			JnaTestUtil.assertRef(args.arg(0), new speed_t(speed));
		}

		private void handleIos(CtlArgs args) {
			if (args.request() == CIoctl.Mac.IOSSIOSPEED) speed = JnaUtil.unlong(args.arg(0), 0);
		}

		private void handleTc(TcArgs args) {
			if (args.name().equals("tcgetattr") && speed >= 0) {
				termios.c_ispeed.setValue(speed);
				termios.c_ospeed.setValue(speed);
			}
			CSerialTestHelper.handleTc(args, termios);
		}
	}

	public static class Linux extends CSerialTestHelper {
		public final CIoctl.Linux.serial_struct serial = new CIoctl.Linux.serial_struct();
		public final CTermios.Linux.termios termios = new CTermios.Linux.termios();

		private Linux(TestCLibNative lib) {
			super(lib);
			lib.cf.autoResponse(args -> handleCfSpeed(args, termios));
			lib.ioctl.autoResponse(args -> handleTio(args), 0);
			lib.tc.autoResponse(args -> handleTc(args, termios), 0);
		}

		public void initSerial(int type, int flags, int baudBase, int customDivisor) {
			serial.type = type;
			serial.flags = flags;
			serial.baud_base = baudBase;
			serial.custom_divisor = customDivisor;
		}

		public void assertSerial(int flags, int customDivisor) {
			assertEquals(serial.flags, flags);
			assertEquals(serial.custom_divisor, customDivisor);
		}

		public void assertSpeed(int speed) {
			CSerialTestHelper.assertSpeed(termios, speed);
		}

		private void handleTio(CtlArgs args) {
			if (args.request() == CIoctl.Linux.TIOCGSERIAL) Struct.copy(serial, args.arg(0));
			if (args.request() == CIoctl.Linux.TIOCSSERIAL) Struct.copy(args.arg(0), serial);
		}
	}

	private CSerialTestHelper(TestCLibNative lib) {
		this.lib = lib;
	}

	private static void assertSpeed(CTermios.termios termios, int speed) {
		assertEquals(termios.c_ispeed, new speed_t(speed));
		assertEquals(termios.c_ospeed, new speed_t(speed));
	}

	private static <T extends CTermios.termios> void handleTc(TcArgs args, T termios) {
		if (args.name().equals("tcgetattr")) Struct.copyTo(termios, args.arg(0));
		if (args.name().equals("tcsetattr")) Struct.copyFrom(args.arg(1), termios);
	}

	private static <T extends CTermios.termios> int handleCfSpeed(CfArgs args, T termios) {
		Struct.copyFrom(args.termios(), termios);
		if (args.name().equals("cfgetispeed")) return termios.c_ispeed.intValue();
		if (args.name().equals("cfgetospeed")) return termios.c_ospeed.intValue();
		if (args.name().equals("cfsetispeed")) termios.c_ispeed.setValue(args.<Integer>arg(0));
		if (args.name().equals("cfsetospeed")) termios.c_ospeed.setValue(args.<Integer>arg(0));
		Struct.copyTo(termios, args.termios());
		return 0;
	}
}
