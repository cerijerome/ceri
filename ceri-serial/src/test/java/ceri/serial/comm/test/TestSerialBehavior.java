package ceri.serial.comm.test;

import static ceri.common.test.Assert.assertRead;
import java.io.IOException;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.test.Assert;

public class TestSerialBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldEchoOutputToInput() throws IOException {
		try (var serial = TestSerial.ofEcho()) {
			serial.open();
			serial.out().write(ArrayUtil.bytes.of(1, 2, 3));
			assertRead(serial.in(), 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldConnectAPairOfConnectors() throws IOException {
		var serials = TestSerial.pairOf();
		serials[0].open();
		serials[1].open();
		serials[0].out().write(ArrayUtil.bytes.of(1, 2, 3));
		assertRead(serials[1].in(), 1, 2, 3);
		serials[1].out().write(ArrayUtil.bytes.of(4, 5, 6));
		assertRead(serials[0].in(), 4, 5, 6);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOpenOnCreation() throws IOException {
		TestSerial.ofOpen().open.assertAuto(true);
	}

	@Test
	public void shouldResetState() throws IOException {
		try (var serial = TestSerial.of()) {
			serial.open();
			serial.brk(true);
			serial.reset();
			Assert.thrown(() -> serial.brk(true)); // not open
		}
	}

	@Test
	public void shouldProvideErrorConfig() {
		Assert.thrown("generated", TestSerial.errorConfig()::serial);
	}

}
