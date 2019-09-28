package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.biConsumer;
import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.function.FunctionTestUtil.function;
import static ceri.common.function.FunctionTestUtil.intConsumer;
import static ceri.common.function.FunctionTestUtil.intFunction;
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
	public void shouldFailWhenMixingWrappersIfExceptionIsThrown() {
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
		w.unwrapConsumer(consumer(), 2);
		assertThrown(IOException.class, () -> w.unwrapConsumer(consumer(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrapConsumer(consumer(), 0));
		w.unwrapConsumer(w.wrap(consumer())::accept, 2);
		assertThrown(IOException.class, () -> w.unwrapConsumer(w.wrap(consumer())::accept, 1));
		assertThrown(RuntimeException.class, () -> w.unwrapConsumer(w.wrap(consumer())::accept, 0));
	}

	@Test
	public void shouldHandleIntConsumers() throws IOException {
		w.unwrapIntConsumer(intConsumer(), 2);
		assertThrown(IOException.class, () -> w.unwrapIntConsumer(intConsumer(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrapIntConsumer(intConsumer(), 0));
		w.unwrapConsumer(w.wrap(intConsumer())::accept, 2);
		assertThrown(IOException.class,
			() -> w.unwrapIntConsumer(w.wrap(intConsumer())::accept, 1));
		assertThrown(RuntimeException.class,
			() -> w.unwrapIntConsumer(w.wrap(intConsumer())::accept, 0));
	}

	@Test
	public void shouldHandleBiConsumers() throws IOException {
		w.unwrapBiConsumer(biConsumer(), 2, 3);
		assertThrown(IOException.class, () -> w.unwrapBiConsumer(biConsumer(), 2, 1));
		assertThrown(RuntimeException.class, () -> w.unwrapBiConsumer(biConsumer(), 0, 3));
		w.unwrapBiConsumer(w.wrap(biConsumer())::accept, 3, 2);
		assertThrown(IOException.class,
			() -> w.unwrapBiConsumer(w.wrap(biConsumer())::accept, 1, 3));
		assertThrown(RuntimeException.class,
			() -> w.unwrapBiConsumer(w.wrap(biConsumer())::accept, 2, 0));
	}

	@Test
	public void shouldHandleSuppliers() throws IOException {
		assertThat(w.unwrapSupplier(supplier(2)), is(2));
		assertThrown(IOException.class, () -> w.unwrapSupplier(supplier(1)));
		assertThrown(RuntimeException.class, () -> w.unwrapSupplier(supplier(0)));
		assertThat(w.unwrapSupplier(w.wrap(supplier(2))::get), is(2));
		assertThrown(IOException.class, () -> w.unwrapSupplier(w.wrap(supplier(1))::get));
		assertThrown(RuntimeException.class, () -> w.unwrapSupplier(w.wrap(supplier(0))::get));
	}

	@Test
	public void shouldHandleIntSuppliers() throws IOException {
		assertThat(w.unwrapIntSupplier(intSupplier(2)), is(2));
		assertThrown(IOException.class, () -> w.unwrapIntSupplier(intSupplier(1)));
		assertThrown(RuntimeException.class, () -> w.unwrapIntSupplier(intSupplier(0)));
		assertThat(w.unwrapIntSupplier(w.wrap(intSupplier(2))::getAsInt), is(2));
		assertThrown(IOException.class,
			() -> w.unwrapIntSupplier(w.wrap(intSupplier(1))::getAsInt));
		assertThrown(RuntimeException.class,
			() -> w.unwrapIntSupplier(w.wrap(intSupplier(0))::getAsInt));
	}

	@Test
	public void shouldHandleFunctions() throws IOException {
		assertThat(w.unwrapFunction(function(), 2), is(2));
		assertThrown(IOException.class, () -> w.unwrapFunction(function(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrapFunction(function(), 0));
		assertThat(w.unwrapFunction(w.wrap(function())::apply, 2), is(2));
		assertThrown(IOException.class, () -> w.unwrapFunction(w.wrap(function())::apply, 1));
		assertThrown(RuntimeException.class, () -> w.unwrapFunction(w.wrap(function())::apply, 0));
	}

	@Test
	public void shouldHandleIntFunctions() throws IOException {
		assertThat(w.unwrapIntFunction(intFunction(), 2), is(2));
		assertThrown(IOException.class, () -> w.unwrapIntFunction(intFunction(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrapIntFunction(intFunction(), 0));
		assertThat(w.unwrapIntFunction(w.wrap(intFunction())::apply, 2), is(2));
		assertThrown(IOException.class, () -> w.unwrapIntFunction(w.wrap(intFunction())::apply, 1));
		assertThrown(RuntimeException.class,
			() -> w.unwrapIntFunction(w.wrap(intFunction())::apply, 0));
	}

	@Test
	public void shouldHandleToIntFunctions() throws IOException {
		assertThat(w.unwrapToIntFunction(toIntFunction(), 2), is(2));
		assertThrown(IOException.class, () -> w.unwrapToIntFunction(toIntFunction(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrapToIntFunction(toIntFunction(), 0));
		assertThat(w.unwrapToIntFunction(w.wrap(toIntFunction())::applyAsInt, 2), is(2));
		assertThrown(IOException.class,
			() -> w.unwrapToIntFunction(w.wrap(toIntFunction())::applyAsInt, 1));
		assertThrown(RuntimeException.class,
			() -> w.unwrapToIntFunction(w.wrap(toIntFunction())::applyAsInt, 0));
	}

	@Test
	public void shouldHandleIntUnaryOperators() throws IOException {
		assertThat(w.unwrapIntUnaryOperator(intUnaryOperator(), 2), is(2));
		assertThrown(IOException.class, () -> w.unwrapIntUnaryOperator(intUnaryOperator(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrapIntUnaryOperator(intUnaryOperator(), 0));
		assertThat(w.unwrapIntUnaryOperator(w.wrap(intUnaryOperator())::applyAsInt, 2), is(2));
		assertThrown(IOException.class,
			() -> w.unwrapIntUnaryOperator(w.wrap(intUnaryOperator())::applyAsInt, 1));
		assertThrown(RuntimeException.class,
			() -> w.unwrapIntUnaryOperator(w.wrap(intUnaryOperator())::applyAsInt, 0));
	}

	@Test
	public void shouldHandlePredicates() throws IOException {
		assertThat(w.unwrapPredicate(predicate(), 2), is(true));
		assertThrown(IOException.class, () -> w.unwrapPredicate(predicate(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrapPredicate(predicate(), 0));
		assertThat(w.unwrapPredicate(w.wrap(predicate())::test, 2), is(true));
		assertThrown(IOException.class, () -> w.unwrapPredicate(w.wrap(predicate())::test, 1));
		assertThrown(RuntimeException.class, () -> w.unwrapPredicate(w.wrap(predicate())::test, 0));
	}

	@Test
	public void shouldHandleIntPredicates() throws IOException {
		assertThat(w.unwrapIntPredicate(intPredicate(), 2), is(true));
		assertThrown(IOException.class, () -> w.unwrapIntPredicate(intPredicate(), 1));
		assertThrown(RuntimeException.class, () -> w.unwrapIntPredicate(intPredicate(), 0));
		assertThat(w.unwrapIntPredicate(w.wrap(intPredicate())::test, 2), is(true));
		assertThrown(IOException.class,
			() -> w.unwrapIntPredicate(w.wrap(intPredicate())::test, 1));
		assertThrown(RuntimeException.class,
			() -> w.unwrapIntPredicate(w.wrap(intPredicate())::test, 0));
	}

	@Test
	public void shouldWrapBiFunctions() throws IOException {
		assertThat(w.wrap((s, i) -> s + "=" + i, "value", 3), is("value=3"));
		assertThat(w.unwrapBiFunction((s, i) -> s + "=" + i, "x", -1), is("x=-1"));
	}

}
