package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.biConsumer;
import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.function.FunctionTestUtil.function;
import static ceri.common.function.FunctionTestUtil.intPredicate;
import static ceri.common.function.FunctionTestUtil.intSupplier;
import static ceri.common.function.FunctionTestUtil.intUnaryOperator;
import static ceri.common.function.FunctionTestUtil.predicate;
import static ceri.common.function.FunctionTestUtil.runnable;
import static ceri.common.function.FunctionTestUtil.supplier;
import static ceri.common.function.FunctionTestUtil.toIntFunction;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class FunctionWrapperBehavior {
	private final FunctionWrapper<IOException> w = FunctionWrapper.create();

	@Test
	public void shouldAllowMixedWrappersIfNoExceptionIsThrown() throws IOException {
		FunctionWrapper<IOException> w0 = FunctionWrapper.create();
		w0.unwrap(w.wrap(runnable(2))::run);
	}

	@Test
	public void shouldFailWhenMixingWrappersIfExceptionIsThrown() throws IOException {
		FunctionWrapper<IOException> w0 = FunctionWrapper.create();
		assertThrown(RuntimeException.class, () -> w0.unwrap(w.wrap(runnable(1))::run));
	}

	@Test
	public void shouldUnwrapExceptionWithForEach() throws IOException {
		ExceptionConsumer<IOException, Integer> consumer = consumer();
		w.unwrap(() -> List.of(2, 3, 4).forEach(w.wrap(consumer)));
		assertThrown(IOException.class, () -> //
			w.unwrap(() -> List.of(3, 2, 1).forEach(w.wrap(consumer))));
		assertThrown(RuntimeException.class, () -> //
			w.unwrap(() -> List.of(0).forEach(w.wrap(consumer))));
	}

	@Test
	public void shouldHandleRunnables() throws IOException {
		w.unwrap(runnable(2));
		assertThrown(IOException.class, () -> w.unwrap(runnable(1)));
		assertThrown(RuntimeException.class, () -> w.unwrap(runnable(0)));
		w.unwrap(w.wrap(runnable(2))::run);
		assertThrown(IOException.class, () -> w.unwrap(w.wrap(runnable(1))::run));
		assertThrown(RuntimeException.class, () -> w.unwrap(w.wrap(runnable(0))::run));
	}

	@Test
	public void shouldHandleConsumers() throws IOException {
		w.unwrap(consumer(), 2);
		assertThrown(IOException.class, () -> w.unwrap(consumer(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrap(consumer(), 0));
		w.unwrap(w.wrap(consumer())::accept, 2);
		assertThrown(IOException.class, () -> w.unwrap(w.wrap(consumer())::accept, 1));
		assertThrown(RuntimeException.class, () -> w.unwrap(w.wrap(consumer())::accept, 0));
	}

	@Test
	public void shouldHandleBiConsumers() throws IOException {
		w.unwrap(biConsumer(), 2, 3);
		assertThrown(IOException.class, () -> w.unwrap(biConsumer(), 2, 1));
		assertThrown(RuntimeException.class, () -> w.unwrap(biConsumer(), 0, 3));
		w.unwrap(w.wrap(biConsumer())::accept, 3, 2);
		assertThrown(IOException.class, () -> w.unwrap(w.wrap(biConsumer())::accept, 1, 3));
		assertThrown(RuntimeException.class, () -> w.unwrap(w.wrap(biConsumer())::accept, 2, 0));
	}

	@Test
	public void shouldHandleSuppliers() throws IOException {
		assertThat(w.unwrap(supplier(2)), is(2));
		assertThrown(IOException.class, () -> w.unwrap(supplier(1)));
		assertThrown(RuntimeException.class, () -> w.unwrap(supplier(0)));
		assertThat(w.unwrap(w.wrap(supplier(2))::get), is(2));
		assertThrown(IOException.class, () -> w.unwrap(w.wrap(supplier(1))::get));
		assertThrown(RuntimeException.class, () -> w.unwrap(w.wrap(supplier(0))::get));
	}

	@Test
	public void shouldHandleIntSuppliers() throws IOException {
		assertThat(w.unwrap(intSupplier(2)), is(2));
		assertThrown(IOException.class, () -> w.unwrap(intSupplier(1)));
		assertThrown(RuntimeException.class, () -> w.unwrap(intSupplier(0)));
		assertThat(w.unwrap(w.wrap(intSupplier(2))::getAsInt), is(2));
		assertThrown(IOException.class, () -> w.unwrap(w.wrap(intSupplier(1))::getAsInt));
		assertThrown(RuntimeException.class, () -> w.unwrap(w.wrap(intSupplier(0))::getAsInt));
	}

	@Test
	public void shouldHandleFunctions() throws IOException {
		assertThat(w.unwrap(function(), 2), is(2));
		assertThrown(IOException.class, () -> w.unwrap(function(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrap(function(), 0));
		assertThat(w.unwrap(w.wrap(function())::apply, 2), is(2));
		assertThrown(IOException.class, () -> w.unwrap(w.wrap(function())::apply, 1));
		assertThrown(RuntimeException.class, () -> w.unwrap(w.wrap(function())::apply, 0));
	}

	@Test
	public void shouldHandleToIntFunctions() throws IOException {
		assertThat(w.unwrap(toIntFunction(), 2), is(2));
		assertThrown(IOException.class, () -> w.unwrap(toIntFunction(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrap(toIntFunction(), 0));
		assertThat(w.unwrap(w.wrap(toIntFunction())::applyAsInt, 2), is(2));
		assertThrown(IOException.class, () -> w.unwrap(w.wrap(toIntFunction())::applyAsInt, 1));
		assertThrown(RuntimeException.class,
			() -> w.unwrap(w.wrap(toIntFunction())::applyAsInt, 0));
	}

	@Test
	public void shouldHandleIntUnaryOperators() throws IOException {
		assertThat(w.unwrap(intUnaryOperator(), 2), is(2));
		assertThrown(IOException.class, () -> w.unwrap(intUnaryOperator(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrap(intUnaryOperator(), 0));
		assertThat(w.unwrap(w.wrap(intUnaryOperator())::applyAsInt, 2), is(2));
		assertThrown(IOException.class, () -> w.unwrap(w.wrap(intUnaryOperator())::applyAsInt, 1));
		assertThrown(RuntimeException.class,
			() -> w.unwrap(w.wrap(intUnaryOperator())::applyAsInt, 0));
	}

	@Test
	public void shouldHandlePredicates() throws IOException {
		assertThat(w.unwrap(predicate(), 2), is(true));
		assertThrown(IOException.class, () -> w.unwrap(predicate(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrap(predicate(), 0));
		assertThat(w.unwrap(w.wrap(predicate())::test, 2), is(true));
		assertThrown(IOException.class, () -> w.unwrap(w.wrap(predicate())::test, 1));
		assertThrown(RuntimeException.class, () -> w.unwrap(w.wrap(predicate())::test, 0));
	}

	@Test
	public void shouldHandleIntPredicates() throws IOException {
		assertThat(w.unwrap(intPredicate(), 2), is(true));
		assertThrown(IOException.class, () -> w.unwrap(intPredicate(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrap(intPredicate(), 0));
		assertThat(w.unwrap(w.wrap(intPredicate())::test, 2), is(true));
		assertThrown(IOException.class, () -> w.unwrap(w.wrap(intPredicate())::test, 1));
		assertThrown(RuntimeException.class, () -> w.unwrap(w.wrap(intPredicate())::test, 0));
	}

	@Test
	public void shouldWrapBiFunctions() throws IOException {
		assertThat(w.wrap((s, i) -> s + "=" + i, "value", 3), is("value=3"));
		assertThat(w.unwrap((s, i) -> s + "=" + i, "x", -1), is("x=-1"));
	}

}
