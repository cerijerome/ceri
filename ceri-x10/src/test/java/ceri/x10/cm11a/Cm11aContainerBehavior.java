package ceri.x10.cm11a;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import ceri.common.io.Connector;
import ceri.common.io.DeviceMode;
import ceri.common.test.TestInputStream;
import ceri.serial.comm.test.TestSerial;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.x10.cm11a.device.Cm11aDeviceConfig;
import ceri.x10.cm11a.device.Cm11aEmulator;
import ceri.x10.command.Command;
import ceri.x10.command.House;

public class Cm11aContainerBehavior {
	private static final Cm11aDeviceConfig cm11aConfig =
		Cm11aDeviceConfig.builder().readPollMs(0).build();

	@Test
	public void shouldCreateFromSerialConfig() throws IOException {
		try (var serial = TestSerial.of();
			var con = Cm11aContainer.of(Cm11aConfig.builder().device(cm11aConfig)
				.serial(SelfHealingSerialConfig.builder("test").factory(serial.factory()).build())
				.build())) {
			initInputStream(serial.in, 0x6c, 0x55); // checksum, ready
			con.cm11a.command(Command.allLightsOff(House.A)); // waits for completion
		}
	}

	@Test
	public void shouldCreateFromTestConfig() throws IOException {
		try (var con = Cm11aContainer
			.of(Cm11aConfig.builder().mode(DeviceMode.test).device(cm11aConfig).build())) {
			con.cm11a.command(Command.allLightsOff(House.A));
		}
	}

	@Test
	public void shouldCreateFromDisabledConfig() throws IOException {
		try (var con = Cm11aContainer.of(Cm11aConfig.builder().mode(DeviceMode.disabled).build())) {
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
		assertThrown(() -> Cm11aContainer.of(1, new Connector.Null() {
			@Override
			public InputStream in() {
				throw new RuntimeException("test");
			}
		}));
	}

	@SuppressWarnings("resource")
	private void initInputStream(TestInputStream in, int... bytes) {
		in.available.autoResponses(0, bytes.length, 0);
		in.to.writeBytes(bytes);
	}

}
