package ceri.common.function;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;

/**
 * Utilities to help test functions.
 */
public class FunctionTestUtil {

	public static void assertIoEx(String message, ExceptionRunnable<IOException> runnable) {
		try {
			runnable.run();
		} catch (IOException e) {
			assertThat(e.getMessage(), is(message));
		}
	}

	public static void assertRtEx(String message, ExceptionRunnable<IOException> runnable)
		throws IOException {
		try {
			runnable.run();
		} catch (RuntimeException e) {
			assertThat(e.getMessage(), is(message));
		}
	}

	public static ExceptionFunction<IOException, Integer, Integer> function(int value) {
		return i -> {
			if (i == value) throw new IOException("" + value);
			if (i == 0) throw new RuntimeException("0");
			return i;
		};
	}

	public static ExceptionRunnable<IOException> runnable(int i, int value) {
		return () -> {
			if (i == value) throw new IOException("" + value);
			if (i == 0) throw new RuntimeException("0");
		};
	}

	public static ExceptionConsumer<IOException, Integer> consumer(int value) {
		return i -> {
			if (i == value) throw new IOException("" + value);
			if (i == 0) throw new RuntimeException("0");
		};
	}

	public static ExceptionSupplier<IOException, Integer> supplier(int i, int value) {
		return () -> {
			if (i == value) throw new IOException("" + value);
			if (i == 0) throw new RuntimeException("0");
			return i;
		};
	}

}
