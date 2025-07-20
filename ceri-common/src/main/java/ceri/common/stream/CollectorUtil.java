package ceri.common.stream;

import static ceri.common.exception.Exceptions.unsupportedOp;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import ceri.common.function.Functions;
import ceri.common.util.BasicUtil;

/**
 * Stream collector support.
 */
public class CollectorUtil {
	private static final BinaryOperator<Object> UNSUPPORTED_COMBINER = (_, _) -> {
		throw unsupportedOp("Joining cannot be combined");
	};

	private CollectorUtil() {}

	/**
	 * A combiner that throws unsupported exception.
	 */
	public static <T> BinaryOperator<T> unsupportedCombiner() {
		return BasicUtil.unchecked(UNSUPPORTED_COMBINER);
	}

	/**
	 * A collector composed of method responses.
	 */
	public record Composed<T, A, R>(Supplier<A> supplier, BinaryOperator<A> combiner,
		BiConsumer<A, T> accumulator, Function<A, R> finisher, Set<Characteristics> characteristics)
		implements Collector<T, A, R> {

		public static <T, A, R> Composed<T, A, R> of(Supplier<A> supplier,
			BiConsumer<A, T> accumulator, Function<A, R> finisher) {
			return new Composed<>(supplier, null, accumulator, finisher, Set.of());
		}
	}

	public record IntCollector<T>(Supplier<T> supplier, ObjIntConsumer<T> accumulator,
		BiConsumer<T, T> combiner) {
		public T apply(java.util.stream.IntStream stream) {
			return stream.collect(supplier(), accumulator(), combiner());
		}

		// public <E extends Exception> T apply(IntStream<E> stream) throws E {
		// return stream.collect(supplier(), accumulator(), combiner());
		// }
	}

	/**
	 * A joining collector that acts on an object's string representation.
	 */
	public static Collector<Object, ?, String> joining() {
		return adapt(String::valueOf, Collectors.joining());
	}

	/**
	 * A joining collector that acts on an object's string representation.
	 */
	public static Collector<Object, ?, String> joining(CharSequence delimiter) {
		return joining(delimiter, "", "");
	}

	/**
	 * A joining collector that acts on an object's string representation.
	 */
	public static Collector<Object, ?, String> joining(CharSequence delimiter, CharSequence prefix,
		CharSequence suffix) {
		return adapt(String::valueOf, Collectors.joining(delimiter, prefix, suffix));
	}

	/**
	 * Adapts the collector to accept a different type.
	 */
	public static <T, U, A, R> Collector<T, A, R> adapt(Functions.Function<T, U> adapter,
		Collector<U, A, R> collector) {
		Objects.requireNonNull(adapter);
		Objects.requireNonNull(collector);
		var accumulator = collector.accumulator();
		return new Composed<>(collector.supplier(), collector.combiner(),
			(a, t) -> accumulator.accept(a, t == null ? null : adapter.apply(t)),
			collector.finisher(), collector.characteristics());
		// return new Composed<>(collector.supplier(), collector.combiner(),
		// (a, t) -> accumulator.accept(a, t == null ? null : adapter.apply(t)),
		// collector.finisher(), collector.characteristics());
	}
}
