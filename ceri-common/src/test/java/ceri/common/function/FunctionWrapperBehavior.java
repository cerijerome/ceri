package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.assertIoEx;
import static ceri.common.function.FunctionTestUtil.assertRtEx;
import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.function.FunctionTestUtil.function;
import static ceri.common.function.FunctionTestUtil.runnable;
import static ceri.common.function.FunctionTestUtil.supplier;
import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class FunctionWrapperBehavior {
	private final FunctionWrapper<IOException> wrapper = FunctionWrapper.create();

	@Test
	public void shouldFailWhenMixingWrappers() throws IOException {
		FunctionWrapper<IOException> w1 = FunctionWrapper.create();
		FunctionWrapper<IOException> w2 = FunctionWrapper.create();
		w1.unwrap(w2.wrap(runnable(1, 2))::run);
		try {
			w1.unwrap(w2.wrap(runnable(2, 2))::run);
			fail();
		} catch (RuntimeException e) {
			// mis-matched agent
		}
	}

	@Test
	public void shouldWrapAndUnwrapForEachExceptions() throws IOException {
		ExceptionConsumer<IOException, String> consumer = s -> {
			if (s.isEmpty()) throw new IOException();
		};
		FunctionWrapper<IOException> w = FunctionWrapper.create();
		w.unwrap(() -> List.of("a", "b", "c").forEach(w.wrap(consumer)));
		assertException(IOException.class, () -> //
			w.unwrap(() -> List.of("").forEach(w.wrap(consumer))));
	}

	@Test
	public void shouldHandleRunnable() throws IOException {
		FunctionWrapper<IOException> wrapper = FunctionWrapper.create();
		wrapper.unwrap(runnable(1, 2));
		assertIoEx("2", () -> wrapper.unwrap(runnable(2, 2)));
		assertRtEx("0", () -> wrapper.unwrap(runnable(0, 2)));
		wrapper.unwrap(wrapper.wrap(runnable(1, 2))::run);
		assertIoEx("2", () -> wrapper.unwrap(wrapper.wrap(runnable(2, 2))::run));
		assertRtEx("0", () -> wrapper.unwrap(wrapper.wrap(runnable(0, 2))::run));
	}

	@Test
	public void shouldHandleConsumer() throws IOException {
		FunctionWrapper<IOException> wrapper = FunctionWrapper.create();
		wrapper.unwrap(consumer(2), 1);
		assertIoEx("2", () -> wrapper.unwrap(consumer(2), 2));
		assertRtEx("0", () -> wrapper.unwrap(consumer(2), 0));
		wrapper.unwrap(wrapper.wrap(consumer(2))::accept, 1);
		assertIoEx("2", () -> wrapper.unwrap(wrapper.wrap(consumer(2))::accept, 2));
		assertRtEx("0", () -> wrapper.unwrap(wrapper.wrap(consumer(2))::accept, 0));
	}

	@Test
	public void shouldHandleSupplier() throws IOException {
		FunctionWrapper<IOException> wrapper = FunctionWrapper.create();
		assertThat(wrapper.unwrap(supplier(1, 2)), is(1));
		assertIoEx("2", () -> wrapper.unwrap(supplier(2, 2)));
		assertRtEx("0", () -> wrapper.unwrap(supplier(0, 2)));
		assertThat(wrapper.unwrap(wrapper.wrap(supplier(1, 2))::get), is(1));
		assertIoEx("2", () -> wrapper.unwrap(wrapper.wrap(supplier(2, 2))::get));
		assertRtEx("0", () -> wrapper.unwrap(wrapper.wrap(supplier(0, 2))::get));
	}

	@Test
	public void shouldHandleFunction() throws IOException {
		FunctionWrapper<IOException> wrapper = FunctionWrapper.create();
		assertThat(wrapper.unwrap(function(2), 1), is(1));
		assertIoEx("2", () -> wrapper.unwrap(function(2), 2));
		assertRtEx("0", () -> wrapper.unwrap(function(2), 0));
		assertThat(wrapper.unwrap(wrapper.wrap(function(2))::apply, 1), is(1));
		assertIoEx("2", () -> wrapper.unwrap(wrapper.wrap(function(2))::apply, 2));
		assertRtEx("0", () -> wrapper.unwrap(wrapper.wrap(function(2))::apply, 0));
	}

	@Test
	public void shouldWrapBiFunctions() throws IOException {
		assertThat(wrapper.wrap((s, i) -> s + "=" + i, "value", 3), is("value=3"));
		assertThat(wrapper.unwrap((s, i) -> s + "=" + i, "x", -1), is("x=-1"));
	}

	@Test
	public void shouldWrapBiFunctionCheckedExceptions() {
		try {
			wrapper.wrap((s, i) -> {
				throw new IOException();
			}, "value", 3);
			fail();
		} catch (WrapperException e) {
			assertSame(e.getCause().getClass(), IOException.class);
		}
	}

	@Test
	public void shouldNotWrapBiFunctionRuntimeExceptions() {
		try {
			wrapper.wrap((s, i) -> {
				throw new IllegalArgumentException();
			}, "value", 3);
			fail();
		} catch (IllegalArgumentException e) {
			//
		}
	}

	@Test
	public void shouldRethrowBiFunctionWrappedExceptions() {
		IOException ex = new IOException();
		try {
			wrapper.unwrap(exceptionBiFn(wrapper, ex), "value", 3);
			fail();
		} catch (IOException e) {
			assertThat(e, is(ex));
		}
	}

	@Test
	public void shouldFailRethrowWithMismatchedAgent() throws IOException {
		IOException ex = new IOException();
		try {
			wrapper.unwrap(exceptionBiFn(FunctionWrapper.create(), ex), "value", 3);
			fail();
		} catch (RuntimeException e) {
			// IOException not thrown
		}
	}

//	private <E extends Exception, T, U> ExceptionBiConsumer<E, T, U> exceptionBiCon(
//		FunctionWrapper<E> wrapper, E e) {
//		return (t, u) -> {
//			throw new WrapperException(wrapper, e);
//		};
//	}

	private <E extends Exception, T, U, R> ExceptionBiFunction<E, T, U, R> exceptionBiFn(
		FunctionWrapper<E> wrapper, E e) {
		return (t, u) -> {
			throw new WrapperException(wrapper, e);
		};
	}

}
