package ceri.serial.comm.test;

import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;

public class TestSerialBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldEchoOutputToInput() throws IOException {
		try (var serial = TestSerial.ofEcho()) {
			serial.open();
			serial.out().write(ArrayUtil.bytes(1, 2, 3));
			assertRead(serial.in(), 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldConnectAPairOfConnectors() throws IOException {
		var serials = TestSerial.pairOf();
		serials[0].open();
		serials[1].open();
		serials[0].out().write(ArrayUtil.bytes(1, 2, 3));
		assertRead(serials[1].in(), 1, 2, 3);
		serials[1].out().write(ArrayUtil.bytes(4, 5, 6));
		assertRead(serials[0].in(), 4, 5, 6);
	}

	@Test
	public void shouldResetState() throws IOException {
		try (var serial = TestSerial.of()) {
			serial.open();
			serial.brk(true);
			serial.reset();
			assertThrown(() -> serial.brk(true)); // not open
		}
	}

}
