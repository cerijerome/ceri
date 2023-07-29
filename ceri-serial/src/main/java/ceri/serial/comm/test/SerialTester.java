package ceri.serial.comm.test;

import static ceri.common.test.ManualTester.Parse.b;
import static ceri.common.test.ManualTester.Parse.c;
import static ceri.common.test.ManualTester.Parse.d;
import static ceri.common.test.ManualTester.Parse.i;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import ceri.common.test.ConnectorTester;
import ceri.common.test.ManualTester;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosed;
import ceri.serial.comm.DataBits;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Parity;
import ceri.serial.comm.Serial;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.StopBits;
import ceri.serial.comm.util.SelfHealingSerial;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.serial.comm.util.SerialPortLocator;

/**
 * Utility to manually test serial ports.
 */
public class SerialTester {

	private SerialTester() {}

	public static void main(String[] args) throws IOException {
		testEcho();
	}

	/**
	 * Create and manually test a simulated serial port that echoes output to input.
	 */
	public static void testEcho() throws IOException {
		try (TestSerial serial = TestSerial.ofEcho()) {
			test(serial);
		}
	}

	/**
	 * Create and manually test two simulated serial ports that connect to each other.
	 */
	public static void testPair() throws IOException {
		try (var serials = Enclosed.ofAll(TestSerial.pairOf())) {
			test(serials.subject);
		}
	}

	/**
	 * Create and manually test available USB serial ports.
	 */
	public static void testUsbPorts() throws IOException {
		testPorts(SerialPortLocator.of().usbPorts());
	}

	/**
	 * Create and manually test a list of serial ports.
	 */
	public static void testPorts(String... ports) throws IOException {
		testPorts(Arrays.asList(ports));
	}

	/**
	 * Create and manually test a list of serial ports.
	 */
	public static void testPorts(Collection<String> ports) throws IOException {
		var serials = CloseableUtil
			.create(port -> SelfHealingSerial.of(SelfHealingSerialConfig.of(port)), ports);
		try {
			test(serials);
		} finally {
			CloseableUtil.close(serials);
		}
	}

	/**
	 * Manually test a list of serial ports.
	 */
	public static void test(Serial... serials) throws IOException {
		test(Arrays.asList(serials));
	}

	/**
	 * Manually test a list of serial ports.
	 */
	public static void test(List<? extends Serial> serials) throws IOException {
		manual(serials).build().run();
	}

	/**
	 * Initialize a ManualTester builder for a list of serial ports.
	 */
	public static ManualTester.Builder manual(List<? extends Serial> serials) throws IOException {
		var b = ConnectorTester.manual(serials);
		buildCommands(b);
		return b;
	}

	private static void buildCommands(ManualTester.Builder b) {
		b.command(Serial.class,
			"p(?i)(?:(\\d+)\\,\\s*([5678])\\,\\s*(1|1\\.5|2)\\,\\s*"
				+ "([noems]|none|odd|even|mark|space))?",
			SerialTester::setParams,
			"pN,N,N,[n|o|e|m|s] = set params baud, data bits, stop bits, parity");
		b.command(Serial.class, "f([rRxX]*|[nN])", (m, s, t) -> setFlowControl(m, s, t),
			"f[rRxX|n] = set flow control RTS/CTS in/out, XON/XOFF in/out, none");
		b.command(Serial.class, "B(i|o)(\\d*)", (m, s, t) -> setBufferSize(m, s, t),
			"B[i|o]N = set in/out buffer size");
		b.command(Serial.class, "b(0|1)", (m, s, t) -> s.brk(b(m)), "b[0|1] = break off/on");
		b.command(Serial.class, "r(0|1)", (m, s, t) -> s.rts(b(m)), "r[0|1] = RTS off/on");
		b.command(Serial.class, "d(0|1)", (m, s, t) -> s.dtr(b(m)), "d[0|1] = DTR off/on");
		b.command(Serial.class, "l", (m, s, t) -> t.out(lineState(s)),
			"l = line state [RTS, DTR, CD, CTS, DSR, RI]");
	}

	private static void setBufferSize(Matcher m, Serial serial, ManualTester tester) {
		boolean in = c(m, 1) == 'i';
		Integer size = i(m, 2);
		if (size != null) {
			if (in) serial.inBufferSize(size);
			else serial.outBufferSize(size);
		}
		tester.out(in ? serial.inBufferSize() : serial.outBufferSize());
	}

	private static void setParams(Matcher m, Serial serial, ManualTester tester)
		throws IOException {
		if (m.group(1) != null) serial.params(parseParams(m));
		tester.out(serial.params());
	}

	private static SerialParams parseParams(Matcher m) {
		int i = 1;
		int baud = i(m, i++);
		DataBits dataBits = DataBits.from(i(m, i++));
		StopBits stopBits = StopBits.fromBits(d(m, i++));
		Parity parity = Parity.from(c(m, i++));
		return SerialParams.builder().baud(baud).dataBits(dataBits).stopBits(stopBits)
			.parity(parity).build();
	}

	private static void setFlowControl(Matcher m, Serial serial, ManualTester tester)
		throws IOException {
		String s = m.group(1);
		if (!s.isEmpty()) serial.flowControl(parseFlowControl(s));
		tester.out(serial.flowControl());
	}

	private static Set<FlowControl> parseFlowControl(String s) {
		Set<FlowControl> flowControl = new HashSet<>();
		for (int i = 0; i < s.length(); i++) {
			switch (s.charAt(i)) {
				case 'r' -> flowControl.add(FlowControl.rtsCtsIn);
				case 'R' -> flowControl.add(FlowControl.rtsCtsOut);
				case 'x' -> flowControl.add(FlowControl.xonXoffIn);
				case 'X' -> flowControl.add(FlowControl.xonXoffOut);
				case 'n', 'N' -> flowControl.clear();
				default -> {}
			}
		}
		return flowControl;
	}

	private static List<String> lineState(Serial serial) throws IOException {
		var list = new ArrayList<String>();
		if (serial.rts()) list.add("RTS");
		if (serial.dtr()) list.add("DTR");
		if (serial.cd()) list.add("CD");
		if (serial.cts()) list.add("CTS");
		if (serial.dsr()) list.add("DSR");
		if (serial.ri()) list.add("RI");
		return list;
	}
}
