package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.assertIoEx;
import static ceri.common.function.FunctionTestUtil.assertRtEx;
import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.function.FunctionTestUtil.function;
import static ceri.common.function.FunctionTestUtil.runnable;
import static ceri.common.function.FunctionTestUtil.supplier;
import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class FunctionWrapperBehavior {

	@Test
	public void shouldFailWhenMixingWrappers() throws IOException {
		FunctionWrapper<IOException> w1 = FunctionWrapper.create();
		FunctionWrapper<IOException> w2 = FunctionWrapper.create();
		w1.handle(w2.wrap(runnable(1, 2))::run);
		try {
			w1.handle(w2.wrap(runnable(2, 2))::run);
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
		w.handle(() -> List.of("a", "b", "c").forEach(w.wrap(consumer)));
		assertException(IOException.class, () -> //
		w.handle(() -> List.of("").forEach(w.wrap(consumer))));
	}

	@Test
	public void shouldHandleRunnable() throws IOException {
		FunctionWrapper<IOException> wrapper = FunctionWrapper.create();
		wrapper.handle(runnable(1, 2));
		assertIoEx("2", () -> wrapper.handle(runnable(2, 2)));
		assertRtEx("0", () -> wrapper.handle(runnable(0, 2)));
		wrapper.handle(wrapper.wrap(runnable(1, 2))::run);
		assertIoEx("2", () -> wrapper.handle(wrapper.wrap(runnable(2, 2))::run));
		assertRtEx("0", () -> wrapper.handle(wrapper.wrap(runnable(0, 2))::run));
	}

	@Test
	public void shouldHandleConsumer() throws IOException {
		FunctionWrapper<IOException> wrapper = FunctionWrapper.create();
		wrapper.handle(consumer(2), 1);
		assertIoEx("2", () -> wrapper.handle(consumer(2), 2));
		assertRtEx("0", () -> wrapper.handle(consumer(2), 0));
		wrapper.handle(wrapper.wrap(consumer(2))::accept, 1);
		assertIoEx("2", () -> wrapper.handle(wrapper.wrap(consumer(2))::accept, 2));
		assertRtEx("0", () -> wrapper.handle(wrapper.wrap(consumer(2))::accept, 0));
	}

	@Test
	public void shouldHandleSupplier() throws IOException {
		FunctionWrapper<IOException> wrapper = FunctionWrapper.create();
		assertThat(wrapper.handle(supplier(1, 2)), is(1));
		assertIoEx("2", () -> wrapper.handle(supplier(2, 2)));
		assertRtEx("0", () -> wrapper.handle(supplier(0, 2)));
		assertThat(wrapper.handle(wrapper.wrap(supplier(1, 2))::get), is(1));
		assertIoEx("2", () -> wrapper.handle(wrapper.wrap(supplier(2, 2))::get));
		assertRtEx("0", () -> wrapper.handle(wrapper.wrap(supplier(0, 2))::get));
	}

	@Test
	public void shouldHandleFunction() throws IOException {
		FunctionWrapper<IOException> wrapper = FunctionWrapper.create();
		assertThat(wrapper.handle(function(2), 1), is(1));
		assertIoEx("2", () -> wrapper.handle(function(2), 2));
		assertRtEx("0", () -> wrapper.handle(function(2), 0));
		assertThat(wrapper.handle(wrapper.wrap(function(2))::apply, 1), is(1));
		assertIoEx("2", () -> wrapper.handle(wrapper.wrap(function(2))::apply, 2));
		assertRtEx("0", () -> wrapper.handle(wrapper.wrap(function(2))::apply, 0));
	}

}
