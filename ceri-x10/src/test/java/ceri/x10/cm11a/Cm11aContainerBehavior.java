package ceri.x10.cm11a;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFind;
import static ceri.common.test.Assert.assertMatch;
import java.io.IOException;
import org.junit.Test;
import ceri.common.io.DeviceMode;
import ceri.common.test.Assert;
import ceri.common.test.TestInputStream;
import ceri.serial.comm.Serial;
import ceri.serial.comm.test.TestSerial;
import ceri.serial.comm.util.SelfHealingSerial;
import ceri.x10.cm11a.device.Cm11a;
import ceri.x10.cm11a.device.Cm11aDevice;
import ceri.x10.cm11a.device.Cm11aEmulator;
import ceri.x10.command.Command;
import ceri.x10.command.House;

public class Cm11aContainerBehavior {
	private static final Cm11aDevice.Config cm11aConfig =
		Cm11aDevice.Config.builder().readPollMs(0).build();

	@Test
	public void shouldDetermineContainerType() {
		assertEquals(Cm11aContainer.Config.of("com").type(null, null),
			Cm11aContainer.Type.connector);
		assertEquals(Cm11aContainer.Config.of("com").type(null, Serial.NULL),
			Cm11aContainer.Type.connectorRef);
		assertEquals(Cm11aContainer.Config.of("com").type(Cm11a.NULL, null),
			Cm11aContainer.Type.cm11aRef);
		assertEquals(Cm11aContainer.Config.builder().mode(DeviceMode.test).build().type(null, null),
			Cm11aContainer.Type.test);
		assertEquals(
			Cm11aContainer.Config.builder().mode(DeviceMode.disabled).build().type(null, null),
			Cm11aContainer.Type.noOp);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertMatch(Cm11aContainer.Config.builder().id(777)
			.serial(SelfHealingSerial.Config.of("com")).build().toString(),
			".*\\b777\\b.*\\bcom\\b.*");
	}

	@Test
	public void shouldSetDeviceConfig() {
		var config = Cm11aContainer.Config.builder()
			.device(Cm11aDevice.Config.builder().errorDelayMs(111).build()).build();
		assertEquals(config.device.errorDelayMs, 111);
	}

	@Test
	public void shouldCreateFromSerialConfig() throws IOException {
		try (var serial = TestSerial.of(); var con = Cm11aContainer.of(Cm11aContainer.Config
			.builder().device(cm11aConfig).serial(serial.selfHealingConfig()).build())) {
			initInputStream(serial.in, 0x6c, 0x55); // checksum, ready
			con.cm11a.command(Command.allLightsOff(House.A)); // waits for completion
		}
	}

	@Test
	public void shouldCreateFromTestConfig() throws IOException {
		try (var con = Cm11aContainer.of(
			Cm11aContainer.Config.builder().mode(DeviceMode.test).device(cm11aConfig).build())) {
			con.cm11a.command(Command.allLightsOff(House.A));
		}
	}

	@Test
	public void shouldCreateFromDisabledConfig() throws IOException {
		try (var con =
			Cm11aContainer.of(Cm11aContainer.Config.builder().mode(DeviceMode.disabled).build())) {
			con.cm11a.command(Command.allLightsOff(House.A));
			assertFind(con, "noOp");
		}
	}

	@Test
	public void shouldCreateFromCm11a() throws IOException {
		try (var cm11a = Cm11aEmulator.of(0); var con = Cm11aContainer.of(3, cm11a)) {
			assertEquals(con.id, 3);
			con.cm11a.command(Command.allLightsOff(House.A));
		}
	}

	@Test
	public void shouldCreateFromConnector() throws IOException {
		try (var serial = TestSerial.of()) {
			serial.open();
			try (var con = Cm11aContainer.of(1, serial)) {
				initInputStream(serial.in, 0x6c, 0x55); // checksum, ready
				con.cm11a.command(Command.allLightsOff(House.A)); // waits for completion
			}
		}
	}

	@Test
	public void shouldCreateFromConnectorAndConfig() throws IOException {
		try (var serial = TestSerial.of()) {
			serial.open();
			try (var con = Cm11aContainer.of(1, serial, cm11aConfig)) {
				initInputStream(serial.in, 0x6c, 0x55); // checksum, ready
				con.cm11a.command(Command.allLightsOff(House.A)); // waits for completion
			}
		}
	}

	@Test
	public void shouldFailOnConnectorError() {
		var config = Cm11aContainer.Config.builder().serial(TestSerial.errorConfig()).build();
		Assert.thrown(() -> Cm11aContainer.of(config));
	}

	@SuppressWarnings("resource")
	private void initInputStream(TestInputStream in, int... bytes) {
		in.available.autoResponses(0, bytes.length, 0);
		in.to.writeBytes(bytes);
	}
}
