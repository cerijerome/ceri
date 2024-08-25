package ceri.common.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import ceri.common.collection.StreamUtil;
import ceri.common.util.BasicUtil;
import ceri.common.validation.ValidationUtil;

/**
 * Helper to convert between object types and integer values. Integers can map to a single instance
 * or to a set of instances. Useful for converting between integers and enums.
 */
public class TypeTranscoder<T> {
	final MaskTranscoder mask;
	final ToLongFunction<T> valueFn;
	final Map<Long, T> lookup;

	public static record Remainder<T>(long diff, Set<T> types) {
		@SafeVarargs
		public static <T> Remainder<T> of(long diff, T... types) {
			return new Remainder<>(diff, Set.of(types));
		}

		public int intDiff() {
			return Math.toIntExact(diff());
		}

		public boolean isEmpty() {
			return types.isEmpty() && isExact();
		}

		public boolean isExact() {
			return diff == 0L;
		}

		@Override
		public String toString() {
			return types + "+" + diff;
		}
	}

	public static <T extends Enum<T>> TypeTranscoder<T> of(ToLongFunction<T> valueFn,
		Class<T> cls) {
		return of(valueFn, MaskTranscoder.NULL, cls);
	}

	public static <T extends Enum<T>> TypeTranscoder<T> of(ToLongFunction<T> valueFn,
		MaskTranscoder mask, Class<T> cls) {
		return of(valueFn, mask, cls.getEnumConstants());
	}

	@SafeVarargs
	public static <T> TypeTranscoder<T> of(ToLongFunction<T> valueFn, T... ts) {
		return of(valueFn, MaskTranscoder.NULL, ts);
	}

	@SafeVarargs
	public static <T> TypeTranscoder<T> of(ToLongFunction<T> valueFn, MaskTranscoder mask,
		T... ts) {
		return of(valueFn, mask, Arrays.asList(ts));
	}

	public static <T> TypeTranscoder<T> of(ToLongFunction<T> valueFn, MaskTranscoder mask,
		Iterable<T> ts) {
		return of(valueFn, mask, ts, StreamUtil.mergeError());
	}

	public static <T> TypeTranscoder<T> of(ToLongFunction<T> valueFn, MaskTranscoder mask,
		Iterable<T> ts, BinaryOperator<T> mergeFn) {
		return new TypeTranscoder<>(valueFn, mask, ts, mergeFn);
	}

	protected TypeTranscoder(ToLongFunction<T> valueFn, MaskTranscoder mask, Iterable<T> ts,
		BinaryOperator<T> mergeFn) {
		this.valueFn = valueFn;
		this.mask = mask;
		this.lookup = Collections.unmodifiableMap(StreamUtil.stream(ts).collect(Collectors
			.toMap(t -> valueFn.applyAsLong(t), t -> t, mergeFn, () -> new LinkedHashMap<>())));
	}

	public Collection<T> all() {
		return lookup.values();
	}

	public <E extends Exception> FieldTranscoder<E, T> field(ValueField<E> accessor) {
		return FieldTranscoder.of(accessor, this);
	}

	public <E extends Exception, U> FieldTranscoder.Typed<E, U, T>
		field(ValueField.Typed<E, U> accessor) {
		return FieldTranscoder.Typed.of(accessor, this);
	}

	public long encodeAll() {
		return encode(all());
	}

	@SafeVarargs
	public final long encode(T... ts) {
		if (ts == null || ts.length == 0) return 0;
		if (ts.length == 1) return mask.encodeInt(encodeType(ts[0]));
		return encode(Arrays.asList(ts));
	}

	public long encode(Iterable<T> ts) {
		return mask.encode(encodeTypes(ts));
	}

	public long encode(Remainder<T> rem) {
		if (rem == null) return 0;
		return mask.encodeInt(encodeTypes(rem.types) | rem.diff);
	}

	public int encodeAllInt() {
		return (int) encodeAll();
	}

	@SafeVarargs
	public final int encodeInt(T... ts) {
		return (int) encode(ts);
	}

	public int encodeInt(Iterable<T> ts) {
		return (int) encode(ts);
	}

	public int encodeInt(Remainder<T> rem) {
		return (int) encode(rem);
	}

	/**
	 * Simple bitwise check if value contains the type. May not match decodeAll if type values
	 * overlap.
	 */
	public boolean has(long value, T t) {
		var mask = valueFn.applyAsLong(t);
		return (value & mask) == mask;
	}

	/**
	 * Simple bitwise check if value contains the types. May not match decodeAll if type values
	 * overlap.
	 */
	@SafeVarargs
	public final boolean hasAny(long value, T... ts) {
		return hasAny(value, Arrays.asList(ts));
	}

	/**
	 * Simple bitwise check if value contains the types. May not match decodeAll if type values
	 * overlap.
	 */
	public boolean hasAny(long value, Iterable<T> ts) {
		return StreamUtil.stream(ts).mapToLong(valueFn).filter(v -> (value & v) == v).findAny()
			.isPresent();
	}

	/**
	 * Simple bitwise check if value contains the types. May not match decodeAll if type values
	 * overlap.
	 */
	@SafeVarargs
	public final boolean hasAll(long value, T... ts) {
		return hasAll(value, Arrays.asList(ts));
	}

	/**
	 * Simple bitwise check if value contains the types. May not match decodeAll if type values
	 * overlap.
	 */
	public boolean hasAll(long value, Iterable<T> ts) {
		var mask = StreamUtil.bitwiseOr(StreamUtil.stream(ts).mapToLong(valueFn));
		return (value & mask) == mask;
	}

	public boolean isValid(long value) {
		value = mask.decodeInt(value);
		if (value == 0) return true;
		if (lookup.containsKey(value)) return true;
		for (var k : lookup.keySet()) {
			if ((k & value) != k) continue;
			value -= k;
			if (value == 0) break;
		}
		return value == 0;
	}

	/**
	 * Decode the value to return a single type. Returns null if not found.
	 */
	public T decode(long value) {
		return lookup.get(mask.decode(value));
	}

	/**
	 * Decode the value to return a single type. Returns default if not found.
	 */
	public T decode(long value, T def) {
		return BasicUtil.defaultValue(decode(value), def);
	}

	/**
	 * Decode the value to return a single type. Throws IllegalArgumentException if not found.
	 */
	public T decodeValid(long value) {
		return decodeValid(value, ValidationUtil.VALUE);
	}

	/**
	 * Decode the value to return a single type. Throws IllegalArgumentException if not found.
	 */
	public T decodeValid(long value, String name) {
		return ValidationUtil.<T>validateLongLookup(this::decode, value, name);
	}

	/**
	 * Decode the value into multiple types. Iteration over the types is in lookup entry order. Any
	 * remainder is discarded.
	 */
	public Set<T> decodeAll(long value) {
		Set<T> set = new LinkedHashSet<>();
		decodeWithRemainder(set, value);
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Decode the value into multiple types. Iteration over the types is in lookup entry order.
	 */
	public Remainder<T> decodeWithRemainder(long value) {
		Set<T> set = new LinkedHashSet<>();
		long remainder = decodeWithRemainder(set, value);
		return new Remainder<>(remainder, Collections.unmodifiableSet(set));
	}

	private long decodeWithRemainder(Set<T> receiver, long value) {
		value = mask.decode(value);
		for (var entry : lookup.entrySet()) {
			var k = entry.getKey();
			T t = entry.getValue();
			if ((k & value) != k) continue;
			value -= k;
			receiver.add(t);
			if (value == 0) break;
		}
		return value;
	}

	private long encodeTypes(Iterable<T> ts) {
		if (ts == null) return 0;
		long value = 0;
		for (T t : ts)
			value |= encodeType(t);
		return value;
	}

	private long encodeType(T t) {
		var value = valueFn.applyAsLong(t);
		return lookup.containsKey(value) ? value : 0L;
	}
}
