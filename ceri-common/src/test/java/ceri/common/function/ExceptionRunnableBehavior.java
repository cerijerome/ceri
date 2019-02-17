package ceri.common.function;

import static ceri.common.test.TestUtil.assertException;
import java.io.IOException;
import org.junit.Test;

public class ExceptionRunnableBehavior {

	@Test
	public void shouldConvertToRunnable() {
		int[] i = { 1 };
		ExceptionRunnable<IOException> runnable = () -> {
			if (i[0] < 0) throw new IOException();
			if (i[0] == 0) throw new RuntimeException();
		};
		Runnable r = runnable.asRunnable();
		r.run();
		i[0] = 0;
		assertException(r::run);
		i[0] = -1;
		assertException(r::run);
	}

	@Test
	public void shouldConvertFromRunnable() {
		int[] i = { 1 };
		Runnable runnable = () -> {
			if (i[0] == 0) throw new RuntimeException();
		};
		ExceptionRunnable<RuntimeException> r = ExceptionRunnable.of(runnable);
		r.run();
		i[0] = 0;
		assertException(r::run);
	}

}
