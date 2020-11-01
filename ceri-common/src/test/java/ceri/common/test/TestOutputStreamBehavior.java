package ceri.common.test;

import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;

public class TestOutputStreamBehavior {
	private TestOutputStream out;

	@Before
	public void before() {
		out = TestOutputStream.of();
	}

	@After
	public void after() throws IOException {
		out.close();
	}

	@Test
	public void shouldSinkBytes() throws IOException {
		out.write(ArrayUtil.bytes(1, 2, 3));
		assertEquals(out.from.available(), 3);
		assertRead(out.from, 1, 2, 3);
		assertEquals(out.from.available(), 0);
	}

	@Test
	public void should() throws IOException {
		out.assertAvailable(0);
		out.write(ArrayUtil.bytes(1, 2, 3));
		out.flush();
		assertAssertion(() -> out.assertAvailable(2));
		out.assertAvailable(3);
	}

	@Test
	public void shouldGenerateWriteError() throws IOException {
		out.write(1);
		out.write.error.setFrom(IOX);
		assertThrown(() -> out.write(2));
		assertRead(out.from, 1, 2);
	}

}
