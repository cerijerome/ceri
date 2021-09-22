package ceri.serial.javax.test;

import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;

public class TestSerialConnectorBehavior {

	@Test
	public void shouldResetFields() throws IOException {
		try (var con = TestSerialConnector.of()) {
			con.connect();
			con.dtr.error.set(new IOException("test"));
			assertThrown(() -> con.dtr(false));
			con.reset();
			con.connect();
			con.dtr(false);
		}
	}

}
