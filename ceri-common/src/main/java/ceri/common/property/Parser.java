package ceri.common.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.Collectable;
import ceri.common.collection.Enums;
import ceri.common.collection.Immutable;
import ceri.common.collection.Lists;
import ceri.common.exception.Exceptions;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.stream.DoubleStream;
import ceri.common.stream.IntStream;
import ceri.common.stream.LongStream;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;
import ceri.common.text.Parse;
import ceri.common.text.Regex;
import ceri.common.util.Basics;
import ceri.common.util.Validate;

/**
 * Encapsulates conversion between types.
 */
public class Parser {
	private static final Parse.Function<Boolean> BOOL = Parse::parseBool;
	private static final Parse.Function<Integer> DINT = Parse::decodeInt;
	private static final Parse.Function<Long> DLONG = Parse::decodeLong;
	private static final Parse.Function<Double> DOUBLE = Parse::parseDouble;

	private Parser() {}

	/**
	 * Returns a string parser for the value.
	 */
	public static <T> Parser.Type<T> type(T value) {
		return () -> value;
	}

	/**
	 * Returns an instance using the fixed values.
	 */
	@SafeVarargs
	public static <T> Types<T> types(T... values) {
		return types(Arrays.asList(values));
	}

	/**
	 * Returns an instance using the value collection.
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
	 * Returns a string parser for the regex group.
	 */
	public static Parser.String string(Matcher m, int group) {
		return string(Regex.group(m, group));
	}

	/**
	 * Returns a string parser for the regex find group.
	 */
	public static Parser.String findString(Pattern regex, java.lang.String s, int group) {
		return string(Regex.find(regex, s), group);
	}

	/**
	 * Returns a string collection parser for the values.
	 */
	@SafeVarargs
	public static Strings strings(java.lang.String... values) {
		return strings(Arrays.asList(values));
	}

	/**
	 * Returns a string collection parser for the values.
	 */
	public static Strings strings(Collection<java.lang.String> values) {
		return () -> values;
	}

	/**
	 * Returns a string collection parser from regex find groups.
	 */
	public static Parser.Strings findStrings(Pattern regex, java.lang.String s, int group) {
		return strings(Regex.finds(regex, s, group).toList());
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
	public interface Type<T> extends Functions.Supplier<T> {

		/**
		 * Creates an instance using the value supplier.
		 */
		static <T> Type<T> from(Functions.Supplier<T> supplier) {
			return supplier::get;
		}

		/**
		 * Access the value or use default supplier if null.
		 */
		default T get(T def) {
			return Basics.def(get(), def);
		}

		/**
		 * Access the value or use default supplier if null.
		 */
		default <E extends Exception> T get(Excepts.Supplier<E, ? extends T> defSupplier) throws E {
			return Basics.def(get(), defSupplier);
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
			return Validate.validateNotNull(get(), name);
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
		default <E extends Exception> Type<T> def(Excepts.Supplier<E, ? extends T> defSupplier)
			throws E {
			return Parser.type(get(defSupplier));
		}

		/**
		 * Converts the value using a constructor. Returns null if the value is null.
		 */
		default <E extends Exception, R> R to(Excepts.Function<E, ? super T, R> constructor)
			throws E {
			return to(constructor, null);
		}

		/**
		 * Converts the value using a constructor. Returns default if the value is null.
		 */
		default <E extends Exception, R> R
			to(Excepts.Function<E, ? super T, ? extends R> constructor, R def) throws E {
			return parseValue(get(), constructor, def);
		}

		/**
		 * Converts the accessor using a constructor.
		 */
		default <E extends Exception, R> Type<R> as(Excepts.Function<E, ? super T, R> constructor)
			throws E {
			return Parser.type(to(constructor, null));
		}

		/**
		 * Returns the accessor for the value converted into a collection. The collection is null if
		 * the value is null.
		 */
		default <E extends Exception, R> Types<R>
			split(Excepts.Function<E, ? super T, ? extends Collection<R>> splitter) throws E {
			return Parser.types(splitValues(get(), splitter));
		}

		/**
		 * Returns the accessor for the value converted into a collection via an array. The
		 * collection is null if the value is null.
		 */
		default <E extends Exception, R> Types<R>
			splitArray(Excepts.Function<E, ? super T, R[]> splitter) throws E {
			return split(t -> Arrays.asList(splitter.apply(t)));
		}

		/**
		 * Consume the value if not null. Returns true if consumed.
		 */
		default <E extends Exception> boolean accept(Excepts.Consumer<E, ? super T> consumer)
			throws E {
			var t = get();
			if (t == null) return false;
			consumer.accept(t);
			return true; // FunctionUtil.safeAccept(get(), consumer);
		}

		/**
		 * Apply the function if not null, or return default.
		 */
		default <E extends Exception, R> R
			apply(Excepts.Function<E, ? super T, ? extends R> function, R def) throws E {
			var t = get();
			return t == null ? def : function.apply(t);
		}
	}

	/**
	 * Provides access to a collection of typed values.
	 */
	public interface Types<T> extends Type<Collection<T>> {

		/**
		 * Creates an instance using the value collection supplier.
		 */
		static <T> Types<T> from(Functions.Supplier<? extends Collection<T>> type) {
			return type::get;
		}

		/**
		 * Access the value or use default supplier if null.
		 */
		default Collection<T> get(@SuppressWarnings("unchecked") T... defs) {
			return get(() -> Arrays.asList(defs));
		}

		/**
		 * Provides access with default values if the collection is null.
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
			def(Excepts.Supplier<E, ? extends Collection<T>> defSupplier) throws E {
			return Parser.types(get(defSupplier));
		}

		/**
		 * Returns true if the collection is null or empty.
		 */
		default boolean empty() {
			return Collectable.isEmpty(get());
		}

		/**
		 * Returns the values as an array, or returns null if the value collection is null. Null
		 * values in the collection are retained.
		 */
		default T[] array(Functions.IntFunction<T[]> arrayFn) {
			return apply(values -> values.toArray(arrayFn), null);
		}

		/**
		 * Returns the values as an array, or returns default if the value collection is null. Null
		 * values in the collection are retained.
		 */
		default T[] arrayDef(Functions.IntFunction<T[]> arrayFn,
			@SuppressWarnings("unchecked") T... defs) {
			return apply(list -> list.toArray(arrayFn), defs);
		}

		/**
		 * Returns the values as a stream, which is empty if the value collection is null. Null
		 * values in the collection are retained.
		 */
		default Stream<RuntimeException, T> stream() {
			return apply(Streams::from, Stream.empty());
		}

		/**
		 * Transforms as each value for an int stream, which is empty if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default IntStream<RuntimeException>
			intStream(Functions.ToIntFunction<? super T> constructor) {
			return stream().nonNull().mapToInt(constructor);
		}

		/**
		 * Transforms as each value for a long stream, which is empty if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default LongStream<RuntimeException>
			longStream(Functions.ToLongFunction<? super T> constructor) {
			return stream().nonNull().mapToLong(constructor);
		}

		/**
		 * Transforms as each value for a double stream, which is empty if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default DoubleStream<RuntimeException>
			doubleStream(Functions.ToDoubleFunction<? super T> constructor) {
			return stream().nonNull().mapToDouble(constructor);
		}

		/**
		 * Transforms the collection to a boolean array, with default array if the value collection
		 * is null. Null values in the collection are dropped.
		 */
		default <E extends Exception> boolean[]
			toBoolArray(Excepts.ToBoolFunction<E, ? super T> constructor, boolean... def) throws E {
			Excepts.Function<E, T, Boolean> fn = constructor::applyAsBool;
			return toPrimitiveArray(get(), boolean[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Transforms the collection to an int array, with default array if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default <E extends Exception> int[]
			toIntArray(Excepts.ToIntFunction<E, ? super T> constructor, int... def) throws E {
			Excepts.Function<E, T, Integer> fn = constructor::applyAsInt;
			return toPrimitiveArray(get(), int[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Transforms the collection to a long array, with default array if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default <E extends Exception> long[]
			toLongArray(Excepts.ToLongFunction<E, ? super T> constructor, long... def) throws E {
			Excepts.Function<E, T, Long> fn = constructor::applyAsLong;
			return toPrimitiveArray(get(), long[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Transforms the collection to a double array, with default array if the value collection
		 * is null. Null values in the collection are dropped.
		 */
		default <E extends Exception> double[] toDoubleArray(
			Excepts.ToDoubleFunction<E, ? super T> constructor, double... def) throws E {
			Excepts.Function<E, T, Double> fn = constructor::applyAsDouble;
			return toPrimitiveArray(get(), double[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Collects each value into a new collection using a supplier, or returns null if the value
		 * collection is null. Null values in the collection are retained.
		 */
		default <C extends Collection<T>> C collect(Functions.Supplier<C> supplier) {
			return apply(values -> Collectable.add(supplier.get(), values), null);
		}

		/**
		 * Collects each value into a new unmodifiable list, or returns null if the value collection
		 * is null. Null values in the collection are retained.
		 */
		default List<T> toList() {
			return apply(Immutable::list, null);
		}

		/**
		 * Collects each value into a new unmodifiable set, or returns null if the value collection
		 * is null. Null values in the collection are retained.
		 */
		default Set<T> toSet() {
			return apply(Immutable::set, null);
		}

		/**
		 * Transforms each non-null value to a new unmodifiable list, or null if the collection is
		 * null. Null values in the collection are retained.
		 */
		default <E extends Exception, R> List<R>
			toEach(Excepts.Function<E, ? super T, ? extends R> constructor) throws E {
			return toEach(constructor, null);
		}

		/**
		 * Transforms each non-null value to a new unmodifiable list, or default if the collection
		 * is null. Null values in the collection are retained.
		 */
		default <E extends Exception, R> List<R> toEachDef(
			Excepts.Function<E, ? super T, ? extends R> constructor,
			@SuppressWarnings("unchecked") R... defs) throws E {
			return toEach(constructor, Arrays.asList(defs));
		}

		/**
		 * Transforms each non-null value to a new unmodifiable list, or default if the collection
		 * is null. Null values in the collection are retained.
		 */
		default <E extends Exception, R> List<R>
			toEach(Excepts.Function<E, ? super T, ? extends R> constructor, List<R> def) throws E {
			return parseValues(get(), def, constructor);
		}

		/**
		 * Removes null values.
		 */
		default Types<T> filter() {
			return filter(_ -> true);
		}

		/**
		 * Removes values that are null or that do not match the filter.
		 */
		default <E extends Exception> Types<T> filter(Excepts.Predicate<E, ? super T> filter)
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
		default <E extends Exception, R> Types<R>
			asEach(Excepts.Function<E, ? super T, R> constructor) throws E {
			return Parser.types(parseValues(get(), null, constructor));
		}

		/**
		 * Iterates the value collection, calling the consumer, if the value collection is not null.
		 * Null values in the collection are passed to the consumer.
		 */
		default <E extends Exception> void each(Excepts.Consumer<E, ? super T> consumer) throws E {
			var collection = get();
			if (collection != null) for (var t : collection)
				consumer.accept(t);
		}
	}

	/**
	 * Provides access to a string value.
	 */
	@FunctionalInterface
	public interface String extends Type<java.lang.String> {

		static Parser.String from(Functions.Supplier<java.lang.String> type) {
			return type::get;
		}

		@Override
		default Parser.String def(java.lang.String def) {
			return Parser.string(get(def));
		}

		@Override
		default <E extends Exception> Parser.String
			def(Excepts.Supplier<E, ? extends java.lang.String> defSupplier) throws E {
			return Parser.string(get(defSupplier));
		}

		/**
		 * Returns the accessor for values split by comma.
		 */
		default Strings split() {
			return split(Regex.COMMA);
		}

		/**
		 * Returns an accessor for values split by regex.
		 */
		default Strings split(Pattern splitter) {
			var split = apply(v -> Regex.Split.list(splitter, v, 0, s -> s.trim()), null);
			return Parser.strings(split);
		}

		/**
		 * Returns an accessor for values split by separator.
		 */
		default Strings split(Separator separator) {
			var split = apply(v -> separator.split(v), null);
			return Parser.strings(split);
		}

		/**
		 * Converts the value to a boolean, matching 'true' in any case, or null if the value is
		 * null.
		 */
		default Boolean toBool() {
			return to(BOOL);
		}

		/**
		 * Converts the value to a boolean, matching 'true' in any case, or default if the value is
		 * null.
		 */
		default boolean toBool(boolean def) {
			return to(BOOL, def);
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
			return Basics.ternary(toBool(), trueVal, falseVal, nullVal);
		}

		/**
		 * Decodes the value to int from -0xffffffff to 0xffffffff, or null if the value is null.
		 */
		default Integer toInt() {
			return to(DINT);
		}

		/**
		 * Decodes the value to int from -0xffffffff to 0xffffffff, or default if the value is null.
		 */
		default int toInt(int def) {
			return to(DINT, def);
		}

		/**
		 * Decodes the value to long from -0xffffffff_ffffffff to 0xffffffff_ffffffff, or null if
		 * the value is null.
		 */
		default Long toLong() {
			return to(DLONG);
		}

		/**
		 * Decodes the value to long from -0xffffffff_ffffffff to 0xffffffff_ffffffff, or default if
		 * the value is null.
		 */
		default long toLong(long def) {
			return to(DLONG, def);
		}

		/**
		 * Converts the value to double, or null if the value is null.
		 */
		default Double toDouble() {
			return to(DOUBLE);
		}

		/**
		 * Converts the value to double, or default if the value is null.
		 */
		default double toDouble(double def) {
			return to(DOUBLE, def);
		}

		/**
		 * Converts non-null value to an enum, or default if the value is null. Fails if no match.
		 * The default enum cannot be null.
		 */
		default <T extends Enum<T>> T toEnum(T def) {
			Objects.requireNonNull(def);
			return toEnum(Enums.type(def), def);
		}

		/**
		 * Converts non-null value to an enum; fails if no match.
		 */
		default <T extends Enum<T>> T toEnum(Class<T> cls) {
			return toEnum(cls, null);
		}

		/**
		 * Converts non-null value to an enum; fails if no match.
		 */
		default <T extends Enum<T>> T toEnum(Class<T> cls, T def) {
			return apply(s -> Enum.valueOf(cls, s), def);
		}

		/**
		 * Modify the value if not null, without changing type.
		 */
		default <E extends Exception> Parser.String
			mod(Excepts.Function<E, java.lang.String, java.lang.String> modFn) throws E {
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
		static Strings from(Functions.Supplier<? extends Collection<java.lang.String>> type) {
			return type::get;
		}

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
			def(Excepts.Supplier<E, ? extends Collection<java.lang.String>> defSupplier) throws E {
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
		default IntStream<RuntimeException> intStream() {
			return intStream(DINT::apply);
		}

		/**
		 * Transforms as each value for a long stream, which is empty if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default LongStream<RuntimeException> longStream() {
			return longStream(DLONG::apply);
		}

		/**
		 * Transforms as each value for a double stream, which is empty if the value collection is
		 * null. Null values in the collection are dropped.
		 */
		default DoubleStream<RuntimeException> doubleStream() {
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
			modEach(Excepts.Function<E, java.lang.String, java.lang.String> modFn) throws E {
			return Parser.strings(toEach(modFn));
		}

		/**
		 * Removes null values.
		 */
		@Override
		default Strings filter() {
			return filter(_ -> true);
		}

		/**
		 * Removes values that are null or do not match the filter.
		 */
		@Override
		default <E extends Exception> Strings
			filter(Excepts.Predicate<E, ? super java.lang.String> filter) throws E {
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
			return asEach(s -> Basics.ternary(BOOL.apply(s), trueVal, falseVal, nullVal));
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
		Functions.IntFunction<A> arrayFn, ArrayConsumer<E, A, T> arraySetFn, A def) throws E {
		if (values == null) return def;
		var array = arrayFn.apply(count(values));
		int i = 0;
		for (var value : values)
			if (value != null) arraySetFn.accept(array, i++, value);
		return array;
	}

	private static <E extends Exception, T, R> R parseValue(T value,
		Excepts.Function<E, ? super T, ? extends R> constructor, R def) throws E {
		try {
			return value == null ? def : constructor.apply(value);
		} catch (RuntimeException e) {
			throw Exceptions.initCause(Exceptions.illegalArg("Failed to transform: %s", value), e);
		}
	}

	private static <E extends Exception, T, R> List<R> parseValues(Collection<T> values,
		List<R> def, Excepts.Function<E, ? super T, ? extends R> constructor) throws E {
		if (values == null) return def;
		if (values.isEmpty()) return List.of();

		var list = Lists.<R>of();
		for (var value : values) {
			if (value == null) list.add(null);
			else list.add(parseValue(value, constructor, null));
		}
		return Immutable.wrap(list);
	}

	private static <E extends Exception, T> List<T> filterValues(Collection<T> values,
		Excepts.Predicate<E, ? super T> filter) throws E {
		if (values == null) return null;
		if (values.isEmpty()) return List.of();
		var list = Lists.<T>of();
		for (var value : values)
			if (value != null && filter.test(value)) list.add(value);
		return Immutable.wrap(list);
	}

	private static <E extends Exception, T, R> R splitValues(T value,
		Excepts.Function<E, ? super T, R> splitter) throws E {
		if (value == null) return null;
		try {
			return splitter.apply(value);
		} catch (RuntimeException e) {
			throw Exceptions.initCause(Exceptions.illegalArg("Failed to split: %s", value), e);
		}
	}

	private static <T> List<T> sortValues(Collection<T> collection,
		Comparator<? super T> comparator) {
		if (collection == null) return null;
		var list = Lists.of(collection);
		list.sort(comparator);
		return Immutable.wrap(list);
	}
}
