package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;

public class LineReaderBehavior {

	@Test
	public void shouldProvideNoOpImplementation() throws IOException {
		assertEquals(LineReader.NULL.readLine(), "");
		assertEquals(LineReader.NULL.ready(), false);
	}

}
