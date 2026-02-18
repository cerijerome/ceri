package ceri.common.test;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.data.Bytes;

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
		out.write(Array.BYTE.of(1, 2, 3));
		Assert.equal(out.from.available(), 3);
		Assert.read(out.from, 1, 2, 3);
		Assert.equal(out.from.available(), 0);
	}

	@Test
	public void should() throws IOException {
		out.assertAvailable(0);
		out.write(Array.BYTE.of(1, 2, 3));
		out.flush();
		Assert.assertion(() -> out.assertAvailable(2));
		out.assertAvailable(3);
	}

	@Test
	public void shouldGenerateWriteError() throws IOException {
		out.write(1);
		out.write.error.setFrom(ErrorGen.IOX);
		Assert.thrown(() -> out.write(2));
		Assert.read(out.from, 1, 2);
	}

	@Test
	public void shouldMatchOutputAsText() throws IOException {
		try (var run = Testing.threadRun(() -> {
			out.awaitMatch("(?s).*\nx");
		})) {
			out.write(Bytes.toAsciiBytes("test\0"));
			out.write(Bytes.toAsciiBytes("\n"));
			out.write(Bytes.toAsciiBytes("x"));
			run.get();
		}
	}
}
