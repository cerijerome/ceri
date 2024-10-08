package ceri.common.property;

import static ceri.common.exception.ExceptionUtil.exceptionf;
import static ceri.common.text.StringUtil.COMMA_SPLIT_REGEX;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import ceri.common.collection.CollectionUtil;
import ceri.common.exception.ExceptionUtil;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
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
	private static final ExceptionFunction<RuntimeException, java.lang.String, Integer> INT =
		NumberParser::decodeInt;
	private static final ExceptionFunction<RuntimeException, java.lang.String, Long> LONG =
		NumberParser::decodeLong;
	private static final ExceptionFunction<RuntimeException, java.lang.String, Double> DOUBLE =
		Double::parseDouble;

	private Parser() {}

	private interface ArrayConsumer<E extends Exception, A, T> {
		void accept(A array, int i, T t) throws E;
	}

	/**
	 * Provides access to a typed value.
	 */
	public interface Type<T> extends ExceptionSupplier<RuntimeException, T> {

		/**
		 * Creates an instance using the fixed value.
		 */
		static <T> Type<T> of(T value) {
			return () -> value;
		}

		/**
		 * Creates an instance using the value supplier.
		 */
		static <T> Type<T> from(ExceptionSupplier<RuntimeException, T> supplier) {
			return supplier::get;
		}

		/**
		 * Access the value or use default supplier if null.
		 */
		default T get(T def) {
			return get(() -> def);
		}

		/**
		 * Access the value or use default supplier if null.
		 */
		default <E extends Exception> T get(ExceptionSupplier<E, T> defSupplier) throws E {
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
			return () -> get(def);
		}

		/**
		 * Provides access with a default value supplier if the supplied value is null.
		 */
		default Type<T> def(ExceptionSupplier<RuntimeException, T> defSupplier) {
			return () -> get(defSupplier);
		}

		/**
		 * Flattens the accessors to use the evaluated value.
		 */
		default Type<T> flat() {
			return of(get());
		}

		/**
		 * Converts the value using a constructor. Returns null if the value is null.
		 */
		default <E extends Exception, R> R to(ExceptionFunction<E, T, R> constructor) throws E {
			return to(constructor, null);
		}

		/**
		 * Converts the value using a constructor. Returns default if the value is null.
		 */
		default <E extends Exception, R> R to(ExceptionFunction<E, T, R> constructor, R def)
			throws E {
			return parseValue(get(), constructor, def);
		}

		/**
		 * Converts the accessor using a constructor.
		 */
		default <R> Type<R> as(ExceptionFunction<RuntimeException, T, R> constructor) {
			return () -> parseValue(get(), constructor, null);
		}

		/**
		 * Converts the evaluated value and provides an accessor.
		 */
		default <E extends Exception, R> Type<R> asFlat(ExceptionFunction<E, T, R> constructor)
			throws E {
			return of(to(constructor, null));
		}

		/**
		 * Returns the accessor for the value converted into a list. The list is null if the value
		 * is null. Consider calling flat() if the accessor does not need dynamic access to the
		 * original value.
		 */
		default <R> Types<R> split(ExceptionFunction<RuntimeException, T, List<R>> splitter) {
			return () -> splitValue(get(), splitter::apply);
		}

		/**
		 * Returns the accessor for the value converted into a list via an array. The list is null
		 * if the value is null. Consider calling flat() if the accessor does not need dynamic
		 * access to the original value.
		 */
		default <R> Types<R> splitArray(ExceptionFunction<RuntimeException, T, R[]> splitter) {
			return split(t -> FunctionUtil.safeApply(splitter.apply(t), Arrays::asList));
		}

		/**
		 * Consume the value if not null.
		 */
		default <E extends Exception> void accept(ExceptionConsumer<E, T> consumer) throws E {
			FunctionUtil.safeAccept(get(), consumer);
		}
	}

	/**
	 * Provides access to a list of typed values.
	 */
	public interface Types<T> extends Type<List<T>> {

		/**
		 * Creates an instance using the fixed values.
		 */
		@SafeVarargs
		static <T> Types<T> of(T... values) {
			return of(Arrays.asList(values));
		}

		/**
		 * Creates an instance using the value list.
		 */
		static <T> Types<T> of(List<T> values) {
			return () -> values;
		}

		/**
		 * Creates an instance using the value list supplier.
		 */
		static <T> Types<T> from(ExceptionSupplier<RuntimeException, List<T>> type) {
			return type::get;
		}

		/**
		 * Returns the value list, or default values as a list if null.
		 */
		default Types<T> def(@SuppressWarnings("unchecked") T... defs) {
			return def(Arrays.asList(defs));
		}

		@Override
		default Types<T> def(List<T> def) {
			return () -> BasicUtil.defaultValue(get(), def);
		}

		@Override
		default Types<T> def(ExceptionSupplier<RuntimeException, List<T>> defSupplier) {
			return () -> BasicUtil.defaultValue(get(), defSupplier::get);
		}

		@Override
		default Types<T> flat() {
			return of(get());
		}

		/**
		 * Returns true if the list is null or empty.
		 */
		default boolean empty() {
			return CollectionUtil.empty(get());
		}

		/**
		 * Returns an accessor to the value at given index, or null if out of range, or if list is
		 * null.
		 */
		default Type<T> at(int index) {
			return () -> FunctionUtil.safeApply(get(),
				list -> CollectionUtil.getOrDefault(list, index, null));
		}

		/**
		 * Returns the values as an array, which is empty if the value list is null.
		 */
		default T[] array(IntFunction<T[]> arrayFn) {
			return FunctionUtil.safeApplyGet(get(), list -> list.toArray(arrayFn),
				() -> arrayFn.apply(0));
		}

		/**
		 * Returns the values as a stream, which is empty if the value list is null.
		 */
		default Stream<T> stream() {
			return FunctionUtil.safeApply(get(), List::stream, Stream.empty());
		}

		/**
		 * Transforms the list to a boolean array, with default array if the value list is null.
		 */
		default <E extends Exception> boolean[]
			toBoolArray(ExceptionToBooleanFunction<E, T> constructor, boolean... def) throws E {
			ExceptionFunction<E, T, Boolean> fn = constructor::applyAsBoolean;
			return toPrimitiveArray(get(), boolean[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Transforms the list to an int array, with default array if the value list is null.
		 */
		default <E extends Exception> int[] toIntArray(ExceptionToIntFunction<E, T> constructor,
			int... def) throws E {
			ExceptionFunction<E, T, Integer> fn = constructor::applyAsInt;
			return toPrimitiveArray(get(), int[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Transforms the list to a long array, with default array if the value list is null.
		 */
		default <E extends Exception> long[] toLongArray(ExceptionToLongFunction<E, T> constructor,
			long... def) throws E {
			ExceptionFunction<E, T, Long> fn = constructor::applyAsLong;
			return toPrimitiveArray(get(), long[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Transforms the list to a double array, with default array if the value list is null.
		 */
		default <E extends Exception> double[]
			toDoubleArray(ExceptionToDoubleFunction<E, T> constructor, double... def) throws E {
			ExceptionFunction<E, T, Double> fn = constructor::applyAsDouble;
			return toPrimitiveArray(get(), double[]::new,
				(array, i, value) -> array[i] = parseValue(value, fn, null), def);
		}

		/**
		 * Transforms each value to a new list, or null if the list is null.
		 */
		default <E extends Exception, R> List<R> toEach(ExceptionFunction<E, T, R> constructor)
			throws E {
			return toEach(constructor, null);
		}

		/**
		 * Transforms each value to a new list, or default if the list is null.
		 */
		default <E extends Exception, R> List<R> toEachDef(ExceptionFunction<E, T, R> constructor,
			@SuppressWarnings("unchecked") R... defs) throws E {
			return toEach(constructor, Arrays.asList(defs));
		}

		/**
		 * Transforms each value to a new list, or default if the list is null.
		 */
		default <E extends Exception, R> List<R> toEach(ExceptionFunction<E, T, R> constructor,
			List<R> def) throws E {
			return parseValues(get(), def, constructor);
		}

		/**
		 * Converts the accessor using a constructor for each value.
		 */
		default <R> Types<R> asEach(ExceptionFunction<RuntimeException, T, R> constructor) {
			return () -> parseValues(get(), null, constructor);
		}

		/**
		 * Converts the accessor using a constructor for each flattened value.
		 */
		default <E extends Exception, R> Types<R> asEachFlat(ExceptionFunction<E, T, R> constructor)
			throws E {
			return of(toEach(constructor, null));
		}

		/**
		 * Iterates the value list, calling the consumer, if the value list is not null. Any null
		 * list items will be passed to the consumer.
		 */
		default <E extends Exception> void each(ExceptionConsumer<E, T> consumer) throws E {
			FunctionUtil.safeAccept(get(), list -> {
				for (var t : list)
					consumer.accept(t);
			});
		}
	}

	/**
	 * Provides access to a string value.
	 */
	@FunctionalInterface
	public interface String extends Type<java.lang.String> {

		static Parser.String of(java.lang.String value) {
			return () -> value;
		}

		static Parser.String from(ExceptionSupplier<RuntimeException, java.lang.String> type) {
			return type::get;
		}

		@Override
		default Parser.String def(java.lang.String def) {
			return () -> BasicUtil.defaultValue(get(), def);
		}

		@Override
		default Parser.String
			def(ExceptionSupplier<RuntimeException, java.lang.String> defSupplier) {
			return () -> BasicUtil.defaultValue(get(), defSupplier);
		}

		/**
		 * Flattens the accessors to use the current value.
		 */
		@Override
		default Parser.String flat() {
			return of(get());
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
			return () -> FunctionUtil.safeApply(get(), v -> StringUtil.split(v, splitter), null);
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
			return BasicUtil.conditional(toBool(), trueVal, falseVal, null);
		}

		/**
		 * Converts the value to int from -0xffffffff to 0xffffffff, or null if the value is null.
		 */
		default Integer toInt() {
			return asInt().get();
		}

		/**
		 * Converts the value to int from -0xffffffff to 0xffffffff, or default if the value is
		 * null.
		 */
		default int toInt(int def) {
			return asInt().get(def);
		}

		/**
		 * Converts the value to long from -0xffffffff_ffffffff to 0xffffffff_ffffffff, or null if
		 * the value is null.
		 */
		default Long toLong() {
			return asLong().get();
		}

		/**
		 * Converts the value to long from -0xffffffff_ffffffff to 0xffffffff_ffffffff, or default
		 * if the value is null.
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
		 * Converts the value to enum, or default if the value is null. The default enum cannot be
		 * null.
		 */
		default <T extends Enum<T>> T toEnum(T def) {
			Objects.requireNonNull(def);
			return asEnum(BasicUtil.<Class<T>>uncheckedCast(def.getClass())).get(def);
		}

		/**
		 * Converts the value to enum, or default if the value is null.
		 */
		default <T extends Enum<T>> T toEnum(Class<T> cls) {
			return asEnum(cls).get();
		}

		/**
		 * Modify the value if not null, without changing type.
		 */
		default Parser.String
			mod(ExceptionFunction<RuntimeException, java.lang.String, java.lang.String> modFn) {
			return () -> to(modFn);
		}

		/**
		 * Provide a new typed accessor. Converts the value to a boolean, matching 'true' in any
		 * case.
		 */
		default Type<Boolean> asBool() {
			return as(BOOL);
		}

		/**
		 * Provides a new typed accessor. Converts the value to a boolean, and returns a
		 * corresponding true or false value, or null if the original value is null.
		 */
		default <U> Type<U> asBool(U trueVal, U falseVal) {
			return () -> toBool(trueVal, falseVal);
		}

		/**
		 * Provide a new typed accessor. Converts the value to int from -0xffffffff to 0xffffffff.
		 */
		default Type<Integer> asInt() {
			return as(INT);
		}

		/**
		 * Provide a new typed accessor. Converts the value to long from -0xffffffff_ffffffff to
		 * 0xffffffff_ffffffff.
		 */
		default Type<Long> asLong() {
			return as(LONG);
		}

		/**
		 * Provide a new typed accessor. Converts the value to double.
		 */
		default Type<Double> asDouble() {
			return as(DOUBLE);
		}

		/**
		 * Provide a new typed accessor for the converted value.
		 */
		default <T extends Enum<T>> Type<T> asEnum(Class<T> cls) {
			Objects.requireNonNull(cls);
			return as(v -> Enum.valueOf(cls, v));
		}
	}

	/**
	 * Provides access to a list of strings.
	 */
	@FunctionalInterface
	public interface Strings extends Types<java.lang.String> {

		/**
		 * Creates an instance using the fixed values.
		 */
		@SafeVarargs
		static Strings of(java.lang.String... values) {
			return of(Arrays.asList(values));
		}

		/**
		 * Creates an instance using the value list.
		 */
		static Strings of(List<java.lang.String> values) {
			return () -> values;
		}

		/**
		 * Creates an instance using the value list supplier.
		 */
		static Strings from(ExceptionSupplier<RuntimeException, List<java.lang.String>> type) {
			return type::get;
		}

		/**
		 * Returns the value list, or default values as a list if null.
		 */
		@Override
		default Strings def(java.lang.String... defs) {
			return def(Arrays.asList(defs));
		}

		@Override
		default Strings def(List<java.lang.String> def) {
			return () -> BasicUtil.defaultValue(get(), def);
		}

		@Override
		default Strings
			def(ExceptionSupplier<RuntimeException, List<java.lang.String>> defSupplier) {
			return () -> BasicUtil.defaultValue(get(), defSupplier::get);
		}

		@Override
		default Strings flat() {
			return of(get());
		}

		@Override
		default Parser.String at(int index) {
			return () -> CollectionUtil.getOrDefault(get(), index, null);
		}

		/**
		 * Returns the values as an array, which is empty if the value list is null.
		 */
		default java.lang.String[] array() {
			return array(java.lang.String[]::new);
		}

		/**
		 * Transforms the list to a boolean array, with default array if the value list is null.
		 */
		default boolean[] toBoolArray(boolean... def) {
			return toBoolArray(BOOL::apply, def);
		}

		/**
		 * Transforms the list to an int array, with default array if the value list is null.
		 */
		default int[] toIntArray(int... def) {
			return toIntArray(INT::apply, def);
		}

		/**
		 * Transforms the list to a long array, with default array if the value list is null.
		 */
		default long[] toLongArray(long... def) {
			return toLongArray(LONG::apply, def);
		}

		/**
		 * Transforms the list to a double array, with default array if the value list is null.
		 */
		default double[] toDoubleArray(double... def) {
			return toDoubleArray(DOUBLE::apply, def);
		}

		/**
		 * Modify each value if the value list is not null, without changing type. Null list values
		 * will be passed to the modifier function.
		 */
		default Strings
			modEach(ExceptionFunction<RuntimeException, java.lang.String, java.lang.String> modFn) {
			return () -> toEach(modFn);
		}

		/**
		 * Provide a new typed accessor. Converts the values to booleans.
		 */
		default Types<Boolean> asBools() {
			return asEach(BOOL);
		}

		/**
		 * Provides a new typed accessor. Converts the value to a boolean, and returns a
		 * corresponding true or false value, or null if the original value is null.
		 */
		default <U> Types<U> asBools(U trueVal, U falseVal) {
			return asEach(s -> BasicUtil.conditional(BOOL.apply(s), trueVal, falseVal, null));
		}
		
		/**
		 * Provide a new typed accessor. Converts the values to ints from -0xffffffff to 0xffffffff.
		 */
		default Types<Integer> asInts() {
			return asEach(INT);
		}

		/**
		 * Provide a new typed accessor. Converts the values to longs from -0xffffffff_ffffffff to
		 * 0xffffffff_ffffffff.
		 */
		default Types<Long> asLongs() {
			return asEach(LONG);
		}

		/**
		 * Provide a new typed accessor. Converts the values to doubles.
		 */
		default Types<Double> asDoubles() {
			return asEach(DOUBLE);
		}

		/**
		 * Provide a new typed accessor. Converts the values to enums.
		 */
		default <T extends Enum<T>> Types<T> asEnums(Class<T> cls) {
			return asEach(v -> Enum.valueOf(cls, v));
		}
	}

	/* support */

	private static <E extends Exception, A, T> A toPrimitiveArray(List<T> values,
		IntFunction<A> arrayFn, ArrayConsumer<E, A, T> arraySetFn, A def) throws E {
		if (values == null) return def;
		var array = arrayFn.apply(values.size());
		for (int i = 0; i < values.size(); i++)
			arraySetFn.accept(array, i, values.get(i));
		return array;
	}

	private static <E extends Exception, T, R> R parseValue(T value,
		ExceptionFunction<E, T, R> constructor, R def) throws E {
		try {
			return FunctionUtil.safeApply(value, constructor, def);
		} catch (RuntimeException e) {
			throw ExceptionUtil.initCause(exceptionf("Invalid format: %s", value), e);
		}
	}

	private static <E extends Exception, T, R> List<R> parseValues(List<T> values, List<R> def,
		ExceptionFunction<E, T, R> constructor) throws E {
		if (values == null) return def;
		if (values.isEmpty()) return List.of();
		var list = new ArrayList<R>();
		for (var value : values)
			list.add(parseValue(value, constructor, null));
		return Collections.unmodifiableList(list);
	}

	private static <E extends Exception, T, R> List<R> splitValue(T value,
		ExceptionFunction<E, T, List<R>> splitter) throws E {
		try {
			return FunctionUtil.safeApply(value, splitter);
		} catch (RuntimeException e) {
			throw ExceptionUtil.initCause(exceptionf("Invalid list format: %s", value), e);
		}
	}
}
