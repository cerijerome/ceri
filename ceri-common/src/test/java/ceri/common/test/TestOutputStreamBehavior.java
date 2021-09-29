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
import ceri.common.data.ByteUtil;

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

	@Test
	public void shouldMatchOutputAsText() throws IOException {
		try (var run = TestUtil.threadRun(() -> {
			out.awaitMatch("(?s).*\nx");
		})) {
			out.write(ByteUtil.toAsciiBytes("test\0"));
			out.write(ByteUtil.toAsciiBytes("\n"));
			out.write(ByteUtil.toAsciiBytes("x"));
			run.get();
		}
	}

}
