package ceri.x10.cm17a;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.x10.cm17a.Cm17aContainer.Type.cm17aRef;
import static ceri.x10.cm17a.Cm17aContainer.Type.noOp;
import static ceri.x10.cm17a.Cm17aContainer.Type.serial;
import static ceri.x10.cm17a.Cm17aContainer.Type.serialRef;
import static ceri.x10.cm17a.Cm17aContainer.Type.test;
import java.io.IOException;
import org.junit.Test;
import ceri.common.io.DeviceMode;
import ceri.serial.comm.Serial;
import ceri.serial.comm.test.TestSerial;
import ceri.serial.comm.util.SelfHealingSerial;
import ceri.x10.cm17a.device.Cm17a;
import ceri.x10.cm17a.device.Cm17aDevice;
import ceri.x10.cm17a.device.Cm17aEmulator;
import ceri.x10.command.Command;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class Cm17aContainerBehavior {

	@Test
	public void shouldDetermineContainerType() {
		assertEquals(Cm17aContainer.Config.of("com").type(null, null), serial);
		assertEquals(Cm17aContainer.Config.of("com").type(null, Serial.NULL), serialRef);
		assertEquals(Cm17aContainer.Config.of("com").type(Cm17a.NULL, null), cm17aRef);
		assertEquals(Cm17aContainer.Config.builder().mode(DeviceMode.test).build().type(null, null),
			test);
		assertEquals(
			Cm17aContainer.Config.builder().mode(DeviceMode.disabled).build().type(null, null),
			noOp);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertMatch(Cm17aContainer.Config.builder().id(777)
			.serial(SelfHealingSerial.Config.of("com")).build().toString(),
			".*\\b777\\b.*\\bcom\\b.*");
	}

	@Test
	public void shouldSetDeviceConfig() {
		var config = Cm17aContainer.Config.builder()
			.device(Cm17aDevice.Config.builder().errorDelayMs(111).build()).build();
		assertEquals(config.device.errorDelayMs, 111);
	}

	@Test
	public void shouldCreateFromSerialConfig() throws IOException {
		try (var serial = TestSerial.of();
			var con =
				Cm17aContainer.of(Cm17aContainer.Config.builder().device(Cm17aDevice.Config.NULL)
					.serial(serial.selfHealingConfig("test")).build())) {
			con.cm17a.command(Command.dim(House.A, 50, Unit._1));
		}
	}

	@Test
	public void shouldCreateFromTestConfig() throws IOException {
		try (var con = Cm17aContainer.of(Cm17aContainer.Config.builder().mode(DeviceMode.test)
			.device(Cm17aDevice.Config.NULL).build())) {
			con.cm17a.command(Command.dim(House.A, 50, Unit._1));
		}
	}

	@Test
	public void shouldCreateFromDisabledConfig() throws IOException {
		try (var con =
			Cm17aContainer.of(Cm17aContainer.Config.builder().mode(DeviceMode.disabled).build())) {
			con.cm17a.command(Command.dim(House.A, 50, Unit._1));
			assertFind(con, "noOp");
		}
	}

	@Test
	public void shouldCreateFromCm17a() throws IOException {
		try (var cm17a = Cm17aEmulator.of(0); var con = Cm17aContainer.of(3, cm17a)) {
			assertEquals(con.id, 3);
			con.cm17a.command(Command.dim(House.A, 50, Unit._1));
		}
	}

	@Test
	public void shouldCreateFromConnector() throws IOException {
		try (var serial = TestSerial.of()) {
			serial.open();
			try (var con = Cm17aContainer.of(1, serial)) {}
		}
	}

	@Test
	public void shouldCreateFromConnectorAndConfig() throws IOException {
		try (var serial = TestSerial.of()) {
			serial.open();
			try (var con = Cm17aContainer.of(1, serial, Cm17aDevice.Config.NULL)) {
				con.cm17a.command(Command.dim(House.A, 50, Unit._1));
			}
		}
	}

	@Test
	public void shouldFailOnBadConfig() throws IOException {
		try (var serial = TestSerial.of()) {
			var config = Cm17aContainer.Config.builder().device(null)
				.serial(serial.selfHealingConfig("test")).build();
			assertThrown(() -> Cm17aContainer.of(config));
		}
	}
}
