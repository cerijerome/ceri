package ceri.common.io;

import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Assert;

public class LineReaderBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		Assert.equal(LineReader.NULL.readLine(), "");
		Assert.equal(LineReader.NULL.ready(), false);
	}
}
