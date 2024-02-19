package ceri.serial.ftdi.test;

import static ceri.common.test.ManualTester.Parse.b;
import static ceri.common.test.ManualTester.Parse.c;
import static ceri.common.test.ManualTester.Parse.d;
import static ceri.common.test.ManualTester.Parse.i;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.IntStream;
import ceri.common.data.ByteUtil;
import ceri.common.test.ConnectorTester;
import ceri.common.test.ManualTester;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosed;
import ceri.serial.ftdi.Ftdi;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.jna.LibFtdi;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.ftdi.util.SelfHealingFtdi;
import ceri.serial.libusb.jna.LibUsbFinder;

/**
 * Utility to manually test FTDI devices.
 */
public class FtdiTester {

	private FtdiTester() {}

	public static void main(String[] args) throws IOException {
		testAll();
	}

	/**
	 * Create and manually test a simulated ftdi device that echoes output to input, and saves the
	 * last byte as pin status.
	 */
	public static void testPinEcho() throws IOException {
		try (var ftdi = TestFtdi.ofPinEcho()) {
			test(ftdi);
		}
	}

	/**
	 * Create and manually test two simulated ftdi devices that connect to each other.
	 */
	public static void testPair() throws IOException {
		try (var ftdis = Enclosed.ofAll(TestFtdi.pairOf())) {
			test(ftdis.ref);
		}
	}

	public static void test() throws IOException {
		try (var ftdi = SelfHealingFtdi.of(SelfHealingFtdi.Config.DEFAULT)) {
			test(ftdi);
		}
	}

	/**
	 * Create and manually test a list of serial ports.
	 */
	public static void testAll() throws IOException {
		var ftdis = openFtdis();
		try {
			test(ftdis);
		} finally {
			CloseableUtil.close(ftdis);
		}
	}

	/**
	 * Attempt to open multiple self-healing ftdi devices based on vendor id and increasing index.
	 */
	private static List<SelfHealingFtdi> openFtdis() throws IOException {
		var count = LibFtdiUtil.FINDER.matchCount();
		var configs = IntStream.range(0, count)
			.mapToObj(i -> LibUsbFinder.builder().vendor(LibFtdi.FTDI_VENDOR_ID).index(i).build())
			.map(SelfHealingFtdi.Config::of).toList();
		return CloseableUtil.create(SelfHealingFtdi::of, configs);
	}

	/**
	 * Manually test a list of FTDI devices.
	 */
	public static void test(Ftdi... ftdis) throws IOException {
		test(Arrays.asList(ftdis));
	}

	/**
	 * Manually test a list of FTDI devices.
	 */
	public static void test(List<? extends Ftdi> ftdis) throws IOException {
		try (var m = manual(ftdis).build()) {
			m.run();
		}
	}

	/**
	 * Initialize a ManualTester builder for a list of FTDI devices.
	 */
	public static ManualTester.Builder manual(List<? extends Ftdi> ftdis) throws IOException {
		var b = ConnectorTester.manual(ftdis);
		buildCommands(b);
		return b;
	}

	private static void buildCommands(ManualTester.Builder b) {
		b.command(Ftdi.class, "D", (t, m, s) -> showDescriptor(t, s), "D = show device descriptor");
		b.command(Ftdi.class, "R", (t, m, s) -> s.usbReset(), "R = USB reset");
		b.command(Ftdi.class, "b([01])", (t, m, s) -> s.bitBang(b(m)),
			"b[0|1] = set bitbang on/off");
		b.command(Ftdi.class, "B(\\d+)", (t, m, s) -> s.baud(i(m)), "BN = set baud");
		b.command(Ftdi.class,
			"L(?i)(?:(7|8)\\,\\s*(1|1\\.5|2)\\,\\s*([noems]|none|odd|even|mark|space)\\,\\s*(0|1))",
			FtdiTester::setLine,
			"LN,N,[n|o|e|m|s],[0|1] = set line params data bits, stop bits, parity, break off/on");
		b.command(Ftdi.class, "f([nrdx])", (t, m, s) -> setFlowControl(m, s),
			"F[n|r|d|x] = set flow control: none, RTS/CTS, DTR/DSR, XON/XOFF");
		b.command(Ftdi.class, "d(0|1)", (t, m, s) -> s.dtr(b(m)), "d[0|1] = DTR off/on");
		b.command(Ftdi.class, "r(0|1)", (t, m, s) -> s.rts(b(m)), "r[0|1] = RTS off/on");
		b.command(Ftdi.class, "p", (t, m, s) -> showBits(t, s.readPins(), Byte.SIZE),
			"p = read pins");
		b.command(Ftdi.class, "m", (t, m, s) -> showBits(t, s.pollModemStatus(), Short.SIZE),
			"m = modem status");
	}

	private static void showBits(ManualTester tester, int value, int bits) {
		StringBuilder b = new StringBuilder("H ");
		for (int i = bits - 1; i >= 0; i--) {
			b.append(ByteUtil.bit(value, i) ? '1' : '0');
			if (i > 0 && (i % Byte.SIZE == 0)) b.append('-');
		}
		tester.out(b.append(" L").toString());
	}

	private static void showDescriptor(ManualTester tester, Ftdi ftdi) throws IOException {
		var descriptor = ftdi.descriptor();
		tester.out(descriptor.manufacturer() + " : " + descriptor.description() + " : "
			+ descriptor.serial());
	}

	private static void setLine(ManualTester tester, Matcher m, Ftdi ftdi) throws IOException {
		var b = FtdiLineParams.builder();
		int i = 1;
		b.dataBits(ftdi_data_bits_type.xcoder.decodeValid(i(m, i++)));
		b.stopBits(stopBits(d(m, i++)));
		b.parity(parity(c(m, i++)));
		b.breakType(ftdi_break_type.xcoder.decodeValid(i(m, i++)));
		var params = b.build();
		ftdi.line(params);
		tester.out(params);
	}

	private static ftdi_stop_bits_type stopBits(double d) {
		if (d == 1.0) return ftdi_stop_bits_type.STOP_BIT_1;
		if (d == 1.5) return ftdi_stop_bits_type.STOP_BIT_15;
		return ftdi_stop_bits_type.STOP_BIT_2;
	}

	private static ftdi_parity_type parity(char c) {
		return switch (c) {
			case 'n', 'N' -> ftdi_parity_type.NONE;
			case 'o', 'O' -> ftdi_parity_type.ODD;
			case 'e', 'E' -> ftdi_parity_type.EVEN;
			case 'm', 'M' -> ftdi_parity_type.MARK;
			case 's', 'S' -> ftdi_parity_type.SPACE;
			default -> null;
		};
	}

	private static void setFlowControl(Matcher m, Ftdi ftdi) throws IOException {
		ftdi.flowControl(flowControlType(c(m)));
	}

	private static FtdiFlowControl flowControlType(char c) {
		return switch (c) {
			case 'n' -> FtdiFlowControl.disabled;
			case 'r' -> FtdiFlowControl.rtsCts;
			case 'd' -> FtdiFlowControl.dtrDsr;
			case 'x' -> FtdiFlowControl.xonXoff;
			default -> null;
		};
	}
}
