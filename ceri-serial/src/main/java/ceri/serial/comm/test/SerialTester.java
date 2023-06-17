package ceri.serial.comm.test;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.Connector;
import ceri.common.io.IoUtil;
import ceri.common.io.StateChange;
import ceri.common.math.MathUtil;
import ceri.common.test.ManualTester;
import ceri.common.test.TestConnector;
import ceri.common.util.Enclosed;
import ceri.serial.comm.DataBits;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Parity;
import ceri.serial.comm.Serial;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.StopBits;
import ceri.serial.comm.util.SelfHealingSerial;
import ceri.serial.comm.util.SelfHealingSerialConfig;

/**
 * Class to test serial ports. Takes commands via System.in and calls methods on the serial port.
 * Logs data received from the port.
 */
public class SerialTester implements RuntimeCloseable {
	private final ManualTester tester;
	private final List<Serial> serials;
	private volatile Enclosed<RuntimeException, ?> listener = null;
	private volatile int index;
	private volatile Serial serial;

	public static void main(String[] args) throws IOException {
		var pair = TestSerial.pair();
		pair[0].open();
		pair[1].open();
		test(pair);
	}

	public static void testEcho() throws IOException {
		try (TestSerial serial0 = TestSerial.echo()) {
			try (TestSerial serial1 = TestSerial.echo()) {
				serial0.open();
				serial1.open();
				test(serial0, serial1);
			}
		}
	}

	public static void test(String port) throws IOException {
		SelfHealingSerialConfig config = SelfHealingSerialConfig.of(port);
		try (SelfHealingSerial serial = SelfHealingSerial.of(config)) {
			serial.open();
			test(serial);
		}
	}

	public static void test(Serial... serials) {
		test(Arrays.asList(serials));
	}
	
	public static void test(Collection<Serial> serials) {
		validateMin(serials.size(), 1, "serial port count");
		try (var tester = new SerialTester(serials)) {
			tester.run();
		}
	}

	private SerialTester(Collection<Serial> serials) {
		this.serials = List.copyOf(serials);
		tester = tester();
		index(0);
	}

	public void run() {
		tester.showHelp();
		while (!tester.exitRequested()) {
			tester.execute(() -> tester.readBytes(serial.in()), false);
			tester.out.print(Serial.port(serial) + "[" + index + "]> ");
			tester.execute();
		}
	}

	@Override
	public void close() {
		IoUtil.close(listener);
	}

	private void index(int i) {
		index = MathUtil.limit(i, 0, serials.size() - 1);
		serial = serials.get(index);
		IoUtil.close(listener);
		listener = listener(serial);
		tester.subject(serial);
	}

	private Enclosed<RuntimeException, ?> listener(Serial serial) {
		if (serial instanceof Connector.Fixable fixable)
			return fixable.listeners().enclose(this::event);
		return Enclosed.empty();
	}

	private void event(StateChange state) {
		tester.out("Event: " + state);
	}

	private ManualTester tester() {
		var b = ManualTester.builder();
		if (serials.size() > 1) b.command("@(\\d+)", (m, t) -> index(Integer.parseInt(m.group(1))),
			"@0.." + (serials.size() - 1) + " = set current serial[] index");
		b.command(Serial.class,
			"(?i)p(?:(\\d+)\\,\\s*([5678])\\,\\s*(1|1\\.5|2)\\,\\s*"
				+ "([noems]|none|odd|even|mark|space))?",
			(m, s, t) -> setParams(m, s, t),
			"pN,N,N,[n|o|e|m|s] = set params baud, data bits, stop bits, parity");
		b.command(Serial.class, "f([rRxX]*|[nN])", (m, s, t) -> setFlowControl(m.group(1), s, t),
			"f[rRxX|n] = set flow control RTS/CTS in/out, XON/XOFF in/out, none");
		b.command(Serial.class, "b(0|1)", (m, s, t) -> s.brk(bool(m)), "b[0|1] = break off/on");
		b.command(Serial.class, "r(0|1)", (m, s, t) -> s.rts(bool(m)), "r[0|1] = RTS off/on");
		b.command(Serial.class, "d(0|1)", (m, s, t) -> s.dtr(bool(m)), "d[0|1] = DTR off/on");
		b.command(Serial.class, "l", (m, s, t) -> t.out(lineState(s)),
			"l = line state [RTS, DTR, CD, CTS, DSR, RI]");
		b.command(Connector.class, "o(.*)", (m, s, t) -> t.writeAscii(s.out(), m.group(1)),
			"o... = write literal char bytes to output (e.g. \\xff for 0xff)");
		b.command(Connector.Fixable.class, "z", (m, s, t) -> s.broken(),
			"z = mark connector as broken");
		b.command(TestConnector.class, "Z", (m, s, t) -> s.fixed(), "Z = fix the connector");
		return b.build();
	}

	private void setParams(Matcher m, Serial serial, ManualTester tester) throws IOException {
		if (m.group(1) != null) serial.params(parseParams(m));
		tester.out(serial.params());
	}

	private SerialParams parseParams(Matcher m) {
		int i = 1;
		int baud = Integer.parseInt(m.group(i++));
		DataBits dataBits = DataBits.from(Integer.parseInt(m.group(i++)));
		StopBits stopBits = StopBits.fromBits(Double.parseDouble(m.group(i++)));
		Parity parity = Parity.from(m.group(i++).charAt(0));
		return SerialParams.builder().baud(baud).dataBits(dataBits).stopBits(stopBits)
			.parity(parity).build();
	}

	private void setFlowControl(String s, Serial serial, ManualTester tester) throws IOException {
		if (!s.isEmpty()) serial.flowControl(parseFlowControl(s));
		tester.out(serial.flowControl());
	}

	private Set<FlowControl> parseFlowControl(String s) {
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

	private static boolean bool(Matcher m) {
		return m.group(1).charAt(0) == '1';
	}

	private List<String> lineState(Serial serial) throws IOException {
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
