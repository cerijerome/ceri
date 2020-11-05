package ceri.log.io.test;

import static ceri.common.test.AssertUtil.assertAscii;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import org.junit.Test;
import ceri.common.data.ByteStream;

public class TestSocketConnectorBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldEcho() throws IOException {
		try (var s = TestSocketConnector.echo()) {
			s.connect();
			var in = ByteStream.reader(s.in());
			var out = ByteStream.writer(s.out());
			out.writeAscii("test");
			assertAscii(in, "test");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideConnector() throws IOException {
		try (var s = TestSocketConnector.of()) {
			s.in.read.error.setFrom(IOX);
			s.in.to.writeBytes(0);
			assertThrown(IOException.class, s.in()::read);
		}
	}

}
