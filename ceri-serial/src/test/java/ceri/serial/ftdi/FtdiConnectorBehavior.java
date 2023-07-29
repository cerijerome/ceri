package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;

public class FtdiConnectorBehavior {

	@Test
	public void shouldProvideNullConnector() throws IOException {
		try (var con = Ftdi.NULL) {
			assertThrown(() -> con.broken());
			con.listeners().listen(t -> {});
			con.connect();
			con.bitmode(FtdiBitMode.BITBANG);
			con.flowControl(FtdiFlowControl.dtrDsr);
			con.dtr(true);
			con.rts(true);
			con.write(1, 2, 3);
			assertEquals(con.readPins(), 0);
			assertEquals(con.read(), 0);
			assertEquals(con.read(new byte[3]), 3);
		}
	}

	@Test
	public void shouldReturnEofIfUnableToRead() throws IOException {
		try (var con = new Ftdi.Null() {
			@Override
			public int read(byte[] buffer, int offset, int length) {
				return 0;
			}
		}) {
			assertEquals(con.read(), -1);
		}
	}

}
