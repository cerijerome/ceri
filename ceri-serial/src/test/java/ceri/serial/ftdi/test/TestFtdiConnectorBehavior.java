package ceri.serial.ftdi.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;

public class TestFtdiConnectorBehavior {

	@Test
	public void shouldEchoOutputToInputAndPins() throws IOException {
		try (var con = TestFtdiConnector.echoPins()) {
			con.connect();
			assertEquals(con.write(), 0);
			assertArray(con.read(4));
			assertEquals(con.write(0xff, 0x80, 0x7f), 3);
			assertArray(con.read(4), 0xff, 0x80, 0x7f);
			assertEquals(con.readPins(), 0x7f);
		}
	}

	@Test
	public void shouldReset() throws IOException {
		try (var con = TestFtdiConnector.of()) {
			con.connect();
			con.connect.assertValues(true);
			con.reset();
			con.connect.assertValues();
		}
	}

}
