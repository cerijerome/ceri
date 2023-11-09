package ceri.x10.cm17a;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import ceri.common.io.DeviceMode;
import ceri.serial.comm.test.TestSerial;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.x10.cm17a.device.Cm17aDeviceConfig;
import ceri.x10.cm17a.device.Cm17aEmulator;
import ceri.x10.command.Command;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class Cm17aContainerBehavior {

	@Test
	public void shouldCreateFromSerialConfig() throws IOException {
		try (var serial = TestSerial.of();
			var con = Cm17aContainer.of(Cm17aConfig.builder().device(Cm17aDeviceConfig.NULL)
				.serial(SelfHealingSerialConfig.builder("test").factory(serial.factory()).build())
				.build())) {
			con.cm17a.command(Command.dim(House.A, 50, Unit._1));
		}
	}

	@Test
	public void shouldCreateFromTestConfig() throws IOException {
		try (var con = Cm17aContainer.of(
			Cm17aConfig.builder().mode(DeviceMode.test).device(Cm17aDeviceConfig.NULL).build())) {
			con.cm17a.command(Command.dim(House.A, 50, Unit._1));
		}
	}

	@Test
	public void shouldCreateFromDisabledConfig() throws IOException {
		try (var con = Cm17aContainer.of(Cm17aConfig.builder().mode(DeviceMode.disabled).build())) {
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
			try (var con = Cm17aContainer.of(1, serial, Cm17aDeviceConfig.NULL)) {
				con.cm17a.command(Command.dim(House.A, 50, Unit._1));
			}
		}
	}

	@Test
	public void shouldFailOnBadConfig() throws IOException {
		try (var serial = TestSerial.of()) {
			var config = Cm17aConfig.builder().device(null)
				.serial(SelfHealingSerialConfig.builder("test").factory(serial.factory()).build())
				.build();
			assertThrown(() -> Cm17aContainer.of(config));
		}
	}
}
