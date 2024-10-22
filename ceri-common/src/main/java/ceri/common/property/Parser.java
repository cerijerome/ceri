package ceri.common.property;

import static ceri.common.exception.ExceptionUtil.exceptionf;
import static ceri.common.text.StringUtil.COMMA_SPLIT_REGEX;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.regex.Pattern;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import ceri.common.collection.CollectionUtil;
import ceri.common.collection.ImmutableUtil;
import ceri.common.exception.ExceptionUtil;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionPredicate;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.ExceptionToBooleanFunction;
import ceri.common.function.ExceptionToDoubleFunction;
import ceri.common.function.ExceptionToIntFunction;
import ceri.common.function.ExceptionToLongFunction;
import ceri.common.function.FunctionUtil;
import ceri.common.text.NumberParser;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * Encapsulates conversion between types.
 */
public class Parser {
	private static final ExceptionFunction<RuntimeException, java.lang.String, Boolean> BOOL =
		Boolean::parseBoolean;
	private static final ExceptionFunction<RuntimeException, java.lang.String, Integer> DINT =
		NumberParser::decodeInt;
	private static final ExceptionFunction<RuntimeException, java.lang.String, Long> DLONG =
		NumberParser::decodeLong;
	private static final ExceptionFunction<RuntimeException, java.lang.String, Double> DOUBLE =
		Double::parseDouble;

	private Parser() {}

	/**
	 * Returns a string parser for the value.
	 */
	public static <T> Parser.Type<T> type(T value) {
		return () -> value;
	}

	/**
	 * Creates an instance using the fixed values.
	 */
	@SafeVarargs
	public static <T> Types<T> types(T... values) {
		return types(Arrays.asList(values));
	}

	/**
	 * Creates an instance using the value collection.
	 */
	public static <T> Types<T> types(Collection<T> values) {
		return () -> values;
	}

	/**
	 * Returns a string parser for the value.
	 */
	public static Parser.String string(java.lang.String value) {
		return () -> value;
	}

	/**
	 * Creates a string collection parser for the values.
	 */
	@SafeVarargs
	public static Strings strings(java.lang.String... values) {
		return strings(Arrays.asList(values));
	}

	/**
	 * Creates a string collection parser for the values.
	 */
	public static Strings strings(Collection<java.lang.String> values) {
		return () -> values;
	}

	/**
	 * Interface for setting primitive array values.
	 */
	private interface ArrayConsumer<E extends Exception, A, T> {
		void accept(A array, int i, T t) throws E;
	}

	/**
	 * Provides access to a typed value.
	 */
	public interface Type<T> extends ExceptionSupplier<RuntimeException, T>, Supplier<T> {

		/**
		 * Creates an instance using the value supplier.
		 */
		static <T> Type<T> from(Supplier<? extends T> supplier) {
			return supplier::get;
		}

		/**
		 * Access the value or use default supplier if null.
		 */
		default T get(T def) {
			return BasicUtil.defaultValue(get(), def);
		}

		/**
		 * Access the value or use default supplier if null.
		 */
		default <E extends Exception> T get(ExceptionSupplier<E, ? extends T> defSupplier)
			throws E {
			return BasicUtil.defaultValue(get(), defSupplier);
		}

		/**
		 * Access the value, or throw validation exception if null.
		 */
		default T getValid() {
			return getValid(null);
		}

		/**
		 * Access the value, or throw named validation exception if null.
		 */
		default T getValid(java.lang.String name) {
			return validateNotNull(get(), name);
		}

		/**
		 * Returns an optional instance.
		 */
		default Optional<T> optional() {
			return Optional.ofNullable(get());
		}

		/**
		 * Returns true if the value is null.
		 */
		default boolean isNull() {
			return get() == null;
		}

		/**
		 * Provides access with a default value if the supplied value is null.
		 */
		default Type<T> def(T def) {
			return Parser.type(get(def));
		}

		/**
		 * Provides access with a default value supplier if the supplied value is null.
		 */
		default <E extends Exception> Type<T> def(ExceptionSupplier<E, ? extends T> defSupplier)
			throws E {
			return Parser.type(get(defSupplier));
		}

		/**
		 * Converts the value using a constructor. Returns null if the value is null.
		 */
		default <E extends Exception, R> R to(ExceptionFunction<E, ? super T, R> constructor)
			throws E {
			return to(constructor, null);
		}

		/**
		 * Converts the value using a constructor. Returns default if the value is null.
		 */
		default <E extends Exception, R> R
			to(ExceptionFunction<E, ? super T, ? extends R> constructor, R def) throws E {
			return parseValue(get(), constructor, def);
		}

		/**
		 * Converts the accessor using a constructor.
		 */
		default <E extends Exception, R> Type<R> as(ExceptionFunction<E, ? super T, R> constructor)
			throws E {
			return Parser.type(to(constructor, null));
		}

		/**
		 * Returns the accessor for the value converted into a collection. The collection is null if
		 * the value is null.
		 */
		default <E extends Exception, R> Types<R>
			split(ExceptionFunction<E, ? super T, ? extends Collection<R>> splitter) throws E {
			return Parser.types(splitValues(get(), splitter));
		}

		/**
		 * Returns the accessor for the value converted into a collection via an array. The
		 * collection is null if the value is null.
		 */
		default <E extends Exception, R> Types<R>
			splitArray(ExceptionFunction<E, ? super T, R[]> splitter) throws E {
			return split(t -> Arrays.asList(splitter.apply(t)));
		}

		/**
		 * Consume the value if not null.
		 */
		default <E extends Exception> void accept(ExceptionConsumer<E, ? super T> consumer)
			throws E {
			FunctionUtil.safeAccept(get(), consumer);
		}
	}

	/**
	 * Provides access to a collection of typed values.
	 */
	public interface Types<T> extends Type<Collection<T>> {

		/**
		 * Creates an instance using the value collection supplier.
		 */
		static <T> Types<T> from(Supplier<? extends Collection<T>> type) {
			return type::get;
		}

		/**
		 * Access the value or use default supplier if null.
		 */
		default Collection<T> get(@SuppressWarnings("unchecked") T... defs) {
			return get(() -> Arrays.asList(defs));
		}

		/**
		 * Returns the value collection, or default values as a list if null.
		 */
		default Types<T> def(@SuppressWarnings("unchecked") T... defs) {
			return Parser.types(get(defs));
		}

		@Override
		default Types<T> def(Collection<T> def) {
			return Parser.types(get(def));
		}

		@Override
		default <E extends Exception> Types<T>
			def(ExceptionSupplier<E, ? extends Collection<T>> defSupplier) throws E {
			return Parser.types(get(defSupplier));
		}

		/**
		 * Returns true if the collection is null or empty.
		 */
		default boolean empty() {
			return CollectionUtil.empty(get());
		}

		/**
		 * Returns the values as an array, or returns null if the value collection is null. Null
		 * values in the collection are retained.
		 */
		default T[] array(IntFunction<T[]> arrayFn) {
			return FunctionUtil.safeApply(get(), values -> values.toArray(arrayFn));
		}

		/**
		 * Returns the values as an array, or returns default if the value collection is null. Null
		 * values in the collection are retained.
		 */
		default T[] arrayDef(IntFunction<T[]> arrayFn, @SuppressWarnings("unchecked") T... defs) {
			return FunctionUtil.safeApply(get(), list -> list.toArray(arrayFn), defs);
		}

		/**
		 * Returns the values as a stream, which is empty if the value collection is null. Null
		 * values in the collection are retained.
		 */
		default Stream<T> stream() {
			return FunctionUtil.safeApply(get(), Collection::stream, Stream.empty());
		}

		/**
		 * Transforms as each value for an int stream, which is empty if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default IntStream intStream(ToIntFunction<? super T> constructor) {
			return stream().filter(Objects::nonNull).mapToInt(constructor);
		}

		/**
		 * Transforms as each value for a long stream, which is empty if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default LongStream longStream(ToLongFunction<? super T> constructor) {
			return stream().filter(Objects::nonNull).mapToLong(constructor);
		}

		/**
		 * Transforms as each value for a double stream, which is empty if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default DoubleStream doubleStream(ToDoubleFunction<? super T> constructor) {
			return stream().filter(Objects::nonNull).mapToDouble(constructor);
		}

		/**
		 * Transforms the collection to a boolean array, with default array if the value collection
		 * is null. Null values in the collection are dropped.
		 */
		default <E extends Exception> boolean[] toBoolArray(
			ExceptionToBooleanFunction<E, ? super T> constructor, boolean... def) throws E {
			ExceptionFunction<E, T, Boolean> fn = constructor::applyAsBoolean;
			return toPrimitiveArray(get(), boolean[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Transforms the collection to an int array, with default array if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default <E extends Exception> int[]
			toIntArray(ExceptionToIntFunction<E, ? super T> constructor, int... def) throws E {
			ExceptionFunction<E, T, Integer> fn = constructor::applyAsInt;
			return toPrimitiveArray(get(), int[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Transforms the collection to a long array, with default array if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default <E extends Exception> long[]
			toLongArray(ExceptionToLongFunction<E, ? super T> constructor, long... def) throws E {
			ExceptionFunction<E, T, Long> fn = constructor::applyAsLong;
			return toPrimitiveArray(get(), long[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Transforms the collection to a double array, with default array if the value collection
		 * is null. Null values in the collection are dropped.
		 */
		default <E extends Exception> double[]
			toDoubleArray(ExceptionToDoubleFunction<E, T> constructor, double... def) throws E {
			ExceptionFunction<E, T, Double> fn = constructor::applyAsDouble;
			return toPrimitiveArray(get(), double[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Collects each value into a new collection using a supplier, or returns null if the value
		 * collection is null. Null values in the collection are retained.
		 */
		default <C extends Collection<T>> C collect(Supplier<C> supplier) {
			return FunctionUtil.safeApply(get(),
				values -> CollectionUtil.collect(values, supplier));
		}

		/**
		 * Collects each value into a new unmodifiable list, or returns null if the value collection
		 * is null. Null values in the collection are retained.
		 */
		default List<T> toList() {
			return FunctionUtil.safeApply(get(), ImmutableUtil::collectAsList);
		}

		/**
		 * Collects each value into a new unmodifiable set, or returns null if the value collection
		 * is null. Null values in the collection are retained.
		 */
		default Set<T> toSet() {
			return FunctionUtil.safeApply(get(), ImmutableUtil::collectAsSet);
		}

		/**
		 * Transforms each non-null value to a new unmodifiable list, or null if the collection is
		 * null. Null values in the collection are retained.
		 */
		default <E extends Exception, R> List<R> toEach(ExceptionFunction<E, T, R> constructor)
			throws E {
			return toEach(constructor, null);
		}

		/**
		 * Transforms each non-null value to a new unmodifiable list, or default if the collection
		 * is null. Null values in the collection are retained.
		 */
		default <E extends Exception, R> List<R> toEachDef(ExceptionFunction<E, T, R> constructor,
			@SuppressWarnings("unchecked") R... defs) throws E {
			return toEach(constructor, Arrays.asList(defs));
		}

		/**
		 * Transforms each non-null value to a new unmodifiable list, or default if the collection
		 * is null. Null values in the collection are retained.
		 */
		default <E extends Exception, R> List<R> toEach(ExceptionFunction<E, T, R> constructor,
			List<R> def) throws E {
			return parseValues(get(), def, constructor);
		}

		/**
		 * Removes null values.
		 */
		default Types<T> filter() {
			return filter(v -> true);
		}

		/**
		 * Removes values that are null or that do not match the filter.
		 */
		default <E extends Exception> Types<T> filter(ExceptionPredicate<E, ? super T> filter)
			throws E {
			return Parser.types(filterValues(get(), filter));
		}

		/**
		 * Sorts the values in natural order. Throws cast exception if type is not comparable. Null
		 * values in the collection are retained.
		 */
		default Types<T> sort() {
			return sort(null);
		}

		/**
		 * Sorts the values using the comparator. Null values in the collection are retained.
		 */
		default Types<T> sort(Comparator<? super T> comparator) {
			return Parser.types(sortValues(get(), comparator));
		}

		/**
		 * Converts the accessor using a constructor for each non-null value. Null values in the
		 * collection are retained.
		 */
		default <R> Types<R> asEach(ExceptionFunction<RuntimeException, T, R> constructor) {
			return Parser.types(parseValues(get(), null, constructor));
		}

		/**
		 * Iterates the value collection, calling the consumer, if the value collection is not null.
		 * Null values in the collection are passed to the consumer.
		 */
		default <E extends Exception> void each(ExceptionConsumer<E, T> consumer) throws E {
			FunctionUtil.safeAccept(get(), collection -> {
				for (var t : collection)
					consumer.accept(t);
			});
		}
	}

	/**
	 * Provides access to a string value.
	 */
	@FunctionalInterface
	public interface String extends Type<java.lang.String> {

		static Parser.String from(Supplier<java.lang.String> type) {
			return type::get;
		}

		@Override
		default Parser.String def(java.lang.String def) {
			return Parser.string(get(def));
		}

		@Override
		default <E extends Exception> Parser.String
			def(ExceptionSupplier<E, ? extends java.lang.String> defSupplier) throws E {
			return Parser.string(get(defSupplier));
		}

		/**
		 * Returns the accessor for values split by comma.
		 */
		default Strings split() {
			return split(COMMA_SPLIT_REGEX);
		}

		/**
		 * Returns an accessor for values split by regex.
		 */
		default Strings split(Pattern splitter) {
			return Parser
				.strings(FunctionUtil.safeApply(get(), v -> StringUtil.split(v, splitter)));
		}

		/**
		 * Returns an accessor for values split by separator.
		 */
		default Strings split(Separator separator) {
			return Parser.strings(FunctionUtil.safeApply(get(), v -> separator.split(v)));
		}

		/**
		 * Converts the value to a boolean, matching 'true' in any case, or null if the value is
		 * null.
		 */
		default Boolean toBool() {
			return asBool().get();
		}

		/**
		 * Converts the value to a boolean, matching 'true' in any case, or default if the value is
		 * null.
		 */
		default boolean toBool(boolean def) {
			return asBool().get(def);
		}

		/**
		 * Converts the value to a boolean, and returns a corresponding true or false value, or null
		 * if the original value is null.
		 */
		default <U> U toBool(U trueVal, U falseVal) {
			return toBool(trueVal, falseVal, null);
		}

		/**
		 * Converts the value to a boolean, and returns a corresponding true or false value, or null
		 * if the original value is null.
		 */
		default <U> U toBool(U trueVal, U falseVal, U nullVal) {
			return BasicUtil.conditional(toBool(), trueVal, falseVal, nullVal);
		}

		/**
		 * Decodes the value to int from -0xffffffff to 0xffffffff, or null if the value is null.
		 */
		default Integer toInt() {
			return asInt().get();
		}

		/**
		 * Decodes the value to int from -0xffffffff to 0xffffffff, or default if the value is null.
		 */
		default int toInt(int def) {
			return asInt().get(def);
		}

		/**
		 * Decodes the value to long from -0xffffffff_ffffffff to 0xffffffff_ffffffff, or null if
		 * the value is null.
		 */
		default Long toLong() {
			return asLong().get();
		}

		/**
		 * Decodes the value to long from -0xffffffff_ffffffff to 0xffffffff_ffffffff, or default if
		 * the value is null.
		 */
		default long toLong(long def) {
			return asLong().get(def);
		}

		/**
		 * Converts the value to double, or null if the value is null.
		 */
		default Double toDouble() {
			return asDouble().get();
		}

		/**
		 * Converts the value to double, or default if the value is null.
		 */
		default double toDouble(double def) {
			return asDouble().get(def);
		}

		/**
		 * Converts non-null value to an enum, or default if the value is null. Fails if no match.
		 * The default enum cannot be null.
		 */
		default <T extends Enum<T>> T toEnum(T def) {
			Objects.requireNonNull(def);
			return FunctionUtil.safeApply(get(),
				s -> Enum.valueOf(BasicUtil.<Class<T>>uncheckedCast(def.getClass()), s), def);
		}

		/**
		 * Converts non-null value to an enum; fails if no match.
		 */
		default <T extends Enum<T>> T toEnum(Class<T> cls) {
			return FunctionUtil.safeApply(get(), s -> Enum.valueOf(cls, s));
		}

		/**
		 * Modify the value if not null, without changing type.
		 */
		default <E extends Exception> Parser.String
			mod(ExceptionFunction<E, java.lang.String, java.lang.String> modFn) throws E {
			return Parser.string(to(modFn));
		}

		/**
		 * Provide a new typed accessor. Converts non-null value to a boolean, matching 'true' in
		 * any case.
		 */
		default Type<Boolean> asBool() {
			return as(BOOL);
		}

		/**
		 * Provides a new typed accessor. Converts the value to a boolean, and returns a
		 * corresponding true or false value, or null if the original value is null.
		 */
		default <U> Type<U> asBool(U trueVal, U falseVal) {
			return asBool(trueVal, falseVal, null);
		}

		/**
		 * Provides a new typed accessor. Converts the value to a boolean, and returns a
		 * corresponding true, false, or null value.
		 */
		default <U> Type<U> asBool(U trueVal, U falseVal, U nullVal) {
			return Parser.type(toBool(trueVal, falseVal, nullVal));
		}

		/**
		 * Provide a new typed accessor. Decodes non-null value to int from -0xffffffff to
		 * 0xffffffff.
		 */
		default Type<Integer> asInt() {
			return as(DINT);
		}

		/**
		 * Provide a new typed accessor. Decodes non-null value to long from -0xffffffff_ffffffff to
		 * 0xffffffff_ffffffff.
		 */
		default Type<Long> asLong() {
			return as(DLONG);
		}

		/**
		 * Provide a new typed accessor. Converts non-null value to double.
		 */
		default Type<Double> asDouble() {
			return as(DOUBLE);
		}

		/**
		 * Provide a new typed accessor. Converts non-null value to an enum; fails if no match.
		 */
		default <T extends Enum<T>> Type<T> asEnum(Class<T> cls) {
			return Parser.type(toEnum(cls));
		}
	}

	/**
	 * Provides access to a collection of strings.
	 */
	@FunctionalInterface
	public interface Strings extends Types<java.lang.String> {

		/**
		 * Creates an instance using the value collection supplier.
		 */
		static Strings from(Supplier<? extends Collection<java.lang.String>> type) {
			return type::get;
		}

		/**
		 * Returns the value collection, or default values as a list if null.
		 */
		@Override
		default Strings def(java.lang.String... defs) {
			return Parser.strings(get(defs));
		}

		@Override
		default Strings def(Collection<java.lang.String> def) {
			return Parser.strings(get(def));
		}

		@Override
		default <E extends Exception> Strings
			def(ExceptionSupplier<E, ? extends Collection<java.lang.String>> defSupplier) throws E {
			return Parser.strings(get(defSupplier));
		}

		/**
		 * Returns the values as an array, or returns null if the value collection is null. Null
		 * values in the collection are retained.
		 */
		default java.lang.String[] array() {
			return array(java.lang.String[]::new);
		}

		/**
		 * Returns the values as an array, or returns default if the value collection is null. Null
		 * values in the collection are retained.
		 */
		default java.lang.String[] arrayDef(java.lang.String... def) {
			return arrayDef(java.lang.String[]::new, def);
		}

		/**
		 * Decodes as each value for an int stream, which is empty if the value collection is null.
		 * Null values in the collection are dropped.
		 */
		default IntStream intStream() {
			return intStream(DINT::apply);
		}

		/**
		 * Transforms as each value for a long stream, which is empty if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default LongStream longStream() {
			return longStream(DLONG::apply);
		}

		/**
		 * Transforms as each value for a double stream, which is empty if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default DoubleStream doubleStream() {
			return doubleStream(DOUBLE::apply);
		}

		/**
		 * Transforms the collection to a boolean array, with default array if the value collection
		 * is null. Null values in the collection are dropped.
		 */
		default boolean[] toBoolArray(boolean... def) {
			return toBoolArray(BOOL::apply, def);
		}

		/**
		 * Transforms the collection to an int array, with default array if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default int[] toIntArray(int... def) {
			return toIntArray(DINT::apply, def);
		}

		/**
		 * Transforms the collection to a long array, with default array if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default long[] toLongArray(long... def) {
			return toLongArray(DLONG::apply, def);
		}

		/**
		 * Transforms the collection to a double array, with default array if the value collection
		 * is null. Null values in the collection are dropped.
		 */
		default double[] toDoubleArray(double... def) {
			return toDoubleArray(DOUBLE::apply, def);
		}

		/**
		 * Modify each non-null value if the value collection is not null, without changing type.
		 * Null values in the collection are retained.
		 */
		default <E extends Exception> Strings
			modEach(ExceptionFunction<E, java.lang.String, java.lang.String> modFn) throws E {
			return Parser.strings(toEach(modFn));
		}

		/**
		 * Removes null values.
		 */
		@Override
		default Strings filter() {
			return filter(v -> true);
		}

		/**
		 * Removes values that are null or do not match the filter.
		 */
		@Override
		default <E extends Exception> Strings
			filter(ExceptionPredicate<E, ? super java.lang.String> filter) throws E {
			return Parser.strings(filterValues(get(), filter));
		}

		/**
		 * Removes values that are null or do not match the pattern.
		 */
		default Strings filter(Pattern pattern) {
			return filter(s -> pattern.matcher(s).matches());
		}

		@Override
		default Strings sort() {
			return sort(null);
		}

		@Override
		default Strings sort(Comparator<? super java.lang.String> comparator) {
			return Parser.strings(sortValues(get(), comparator));
		}

		/**
		 * Provide a new typed accessor. Converts the values to booleans. Null values in the
		 * collection are retained.
		 */
		default Types<Boolean> asBools() {
			return asEach(BOOL);
		}

		/**
		 * Provides a new typed accessor. Converts the value to a boolean, and returns a
		 * corresponding true or false value, or null if the original value is null.
		 */
		default <U> Types<U> asBools(U trueVal, U falseVal) {
			return asBools(trueVal, falseVal, null);
		}

		/**
		 * Provides a new typed accessor. Converts the value to a boolean, and returns a
		 * corresponding true, false, or null value.
		 */
		default <U> Types<U> asBools(U trueVal, U falseVal, U nullVal) {
			return asEach(s -> BasicUtil.conditional(BOOL.apply(s), trueVal, falseVal, nullVal));
		}

		/**
		 * Provide a new typed accessor. Decodes the values to ints from -0xffffffff to 0xffffffff.
		 */
		default Types<Integer> asInts() {
			return asEach(DINT);
		}

		/**
		 * Provide a new typed accessor. Decodes the values to longs from -0xffffffff_ffffffff to
		 * 0xffffffff_ffffffff.
		 */
		default Types<Long> asLongs() {
			return asEach(DLONG);
		}

		/**
		 * Provide a new typed accessor. Converts the values to doubles.
		 */
		default Types<Double> asDoubles() {
			return asEach(DOUBLE);
		}

		/**
		 * Provide a new typed accessor. Converts each non-null value to an enum; fails if no match
		 * for a value. Null values in the collection are retained.
		 */
		default <T extends Enum<T>> Types<T> asEnums(Class<T> cls) {
			return asEach(v -> Enum.valueOf(cls, v));
		}
	}

	/* support */

	private static int count(Collection<?> values) {
		int count = 0;
		for (var value : values)
			if (value != null) count++;
		return count;
	}

	private static <E extends Exception, A, T> A toPrimitiveArray(Collection<T> values,
		IntFunction<A> arrayFn, ArrayConsumer<E, A, T> arraySetFn, A def) throws E {
		if (values == null) return def;
		var array = arrayFn.apply(count(values));
		int i = 0;
		for (var value : values)
			if (value != null) arraySetFn.accept(array, i++, value);
		return array;
	}

	private static <E extends Exception, T, R> R parseValue(T value,
		ExceptionFunction<E, ? super T, ? extends R> constructor, R def) throws E {
		try {
			return FunctionUtil.safeApply(value, constructor, def);
		} catch (RuntimeException e) {
			throw ExceptionUtil.initCause(exceptionf("Failed to transform: %s", value), e);
		}
	}

	private static <E extends Exception, T, R> List<R> parseValues(Collection<T> values,
		List<R> def, ExceptionFunction<E, ? super T, ? extends R> constructor) throws E {
		if (values == null) return def;
		if (values.isEmpty()) return List.of();
		var list = new ArrayList<R>();
		for (var value : values) {
			if (value == null) list.add(null);
			else list.add(parseValue(value, constructor, null));
		}
		return Collections.unmodifiableList(list);
	}

	private static <E extends Exception, T> List<T> filterValues(Collection<T> values,
		ExceptionPredicate<E, ? super T> filter) throws E {
		if (values == null) return null;
		if (values.isEmpty()) return List.of();
		var list = new ArrayList<T>();
		for (var value : values)
			if (value != null && filter.test(value)) list.add(value);
		return Collections.unmodifiableList(list);
	}

	private static <E extends Exception, T, R> R splitValues(T value,
		ExceptionFunction<E, ? super T, R> splitter) throws E {
		if (value == null) return null;
		try {
			return splitter.apply(value);
		} catch (RuntimeException e) {
			throw ExceptionUtil.initCause(exceptionf("Failed to split: %s", value), e);
		}
	}

	private static <T> List<T> sortValues(Collection<T> collection,
		Comparator<? super T> comparator) {
		if (collection == null) return null;
		List<T> list = new ArrayList<>(collection);
		list.sort(comparator);
		return Collections.unmodifiableList(list);
	}
}
