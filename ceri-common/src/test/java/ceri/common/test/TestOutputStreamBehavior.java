package ceri.common.test;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertThrown;
import java.io.IOException;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;

public class TestOutputStreamBehavior {

	@Test
	public void shouldCollectWrittenBytes() throws IOException {
		try (var out = TestOutputStream.builder().dataSize(5).build()) {
			out.write(ArrayUtil.bytes(1, 2, 3));
			assertThrown(() -> out.write(ArrayUtil.bytes(4, 5, 6)));
			assertThrown(() -> out.write(ArrayUtil.bytes(7)));
			assertArray(out.written(), 1, 2, 3, 4, 5);
		}
	}

	@Test
	public void shouldDisableCollector() throws IOException {
		try (var out = TestOutputStream.builder().collect(false).build()) {
			out.write(ArrayUtil.bytes(1, 2, 3));
			assertArray(out.written());
		}
	}

	@Test
	public void shouldSetCloseBehavior() throws IOException {
		Capturer.Int captor = Capturer.ofInt();
		try (var out = TestOutputStream.builder().close(() -> captor.accept(-1)).build()) {
			out.close();
		}
		captor.verifyInt(-1, -1);
	}

	@Test
	public void shouldGenerateExceptions() throws IOException {
		try (var out = TestOutputStream.of(0, -3, 0, -2, 0, -1, 0)) {
			out.write(1);
			assertThrown(() -> out.write(2));
			out.write(3);
			assertThrown(() -> out.write(4));
			out.write(5);
			assertThrown(() -> out.write(6));
			out.write(7);
			assertThrown(() -> out.write(8));
			assertArray(out.written(), 1, 3, 5, 7);
		}
	}

}
