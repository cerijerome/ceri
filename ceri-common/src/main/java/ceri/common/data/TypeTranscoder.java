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
import ceri.common.collection.EnumUtil;
import ceri.common.collection.ImmutableUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.math.MathUtil;
import ceri.common.util.BasicUtil;
import ceri.common.validation.ValidationUtil;

/**
 * Helper to convert between object types and long values. Longs can map to a single instance or to
 * a set of instances. Useful for converting between integers and enums.
 */
public class TypeTranscoder<T> {
	private final ToLongFunction<T> valueFn;
	private final Map<Long, T> lookup;

	public static record Remainder<T>(long diff, Set<T> types) {
		@SafeVarargs
		public static <T> Remainder<T> of(long diff, T... types) {
			return new Remainder<>(diff, ImmutableUtil.asSet(types));
		}

		public int diffInt() {
			return Math.toIntExact(diff());
		}

		public boolean isEmpty() {
			return types.isEmpty() && isExact();
		}

		public boolean isExact() {
			return diff() == 0L;
		}

		@Override
		public String toString() {
			return types() + "+" + diff();
		}
	}

	/**
	 * Creates an encoder for unique type values.
	 */
	@SafeVarargs
	public static <T extends Enum<T>> TypeTranscoder<T> of(ToLongFunction<T> valueFn, Class<T> cls,
		T... ignore) {
		if (ignore.length == 0) return of(valueFn, EnumUtil.enums(cls));
		var set = Set.of(ignore);
		return of(valueFn, EnumUtil.enums(cls).stream().filter(t -> !set.contains(t))::iterator);
	}

	/**
	 * Creates an encoder for unique type values.
	 */
	public static <T> TypeTranscoder<T> of(ToLongFunction<T> valueFn, Iterable<T> ts) {
		return new TypeTranscoder<>(valueFn, ts, null);
	}

	/**
	 * Creates an encoder that allows duplicate type values.
	 */
	@SafeVarargs
	public static <T extends Enum<T>> TypeTranscoder<T> ofDup(ToLongFunction<T> valueFn,
		Class<T> cls, T... ignore) {
		if (ignore.length == 0) return ofDup(valueFn, EnumUtil.enums(cls));
		var set = Set.of(ignore);
		return ofDup(valueFn, EnumUtil.enums(cls).stream().filter(t -> !set.contains(t))::iterator);
	}

	/**
	 * Creates an encoder that allows duplicate type values.
	 */
	public static <T extends Enum<T>> TypeTranscoder<T> ofDup(ToLongFunction<T> valueFn,
		Iterable<T> ts) {
		return new TypeTranscoder<>(valueFn, ts, StreamUtil.mergeFirst());
	}

	protected TypeTranscoder(ToLongFunction<T> valueFn, Iterable<T> ts, BinaryOperator<T> mergeFn) {
		this.valueFn = valueFn;
		this.lookup = lookup(valueFn, ts, mergeFn);
	}

	public Collection<T> all() {
		return lookup.values();
	}

	public long encodeAll() {
		return encode(all());
	}

	@SafeVarargs
	public final long encode(T... ts) {
		if (ts == null || ts.length == 0) return 0;
		if (ts.length == 1) return encodeType(ts[0]);
		return encode(Arrays.asList(ts));
	}

	/**
	 * Encodes the types to a value.
	 */
	public long encode(Iterable<T> ts) {
		if (ts == null) return 0;
		long value = 0;
		for (T t : ts)
			value |= encodeType(t);
		return value;
	}

	/**
	 * Encodes all types to an int value.
	 */
	public int encodeAllInt() {
		return (int) encodeAll();
	}

	/**
	 * Encodes the types to an int value.
	 */
	@SafeVarargs
	public final int encodeInt(T... ts) {
		return (int) encode(ts);
	}

	/**
	 * Encodes the types to an int value.
	 */
	public int encodeInt(Iterable<T> ts) {
		return (int) encode(ts);
	}

	/**
	 * Decode the value to return a single type matching the value exactly. Returns null if not
	 * found.
	 */
	public T decode(long value) {
		return lookup.get(value);
	}

	/**
	 * Decode the value to return a single type matching the value exactly. Returns default if not
	 * found.
	 */
	public T decode(long value, T def) {
		return BasicUtil.def(decode(value), def);
	}

	/**
	 * Decodes the value to find the first type that matches, with possible remainder. Returns null
	 * if not found.
	 */
	public T decodeFirst(long value) {
		T t = decode(value);
		if (t != null) return t;
		return findFirst(value);
	}

	/**
	 * Decode the value to return a single type matching the value exactly. Throws
	 * IllegalArgumentException if not found.
	 */
	public T decodeValid(long value) {
		return decodeValid(value, ValidationUtil.VALUE);
	}

	/**
	 * Decode the value to return a single type matching the value exactly. Throws
	 * IllegalArgumentException if not found.
	 */
	public T decodeValid(long value, String name) {
		return ValidationUtil.<T>validateLongLookup(this::decode, value, name);
	}

	/**
	 * Decode the value into multiple types. Iteration over the types is in lookup entry order. Any
	 * remainder is discarded.
	 */
	public Set<T> decodeAll(long value) {
		var set = new LinkedHashSet<T>();
		decodeRemainder(set, value);
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Decode the value into multiple types with remainder value. Iteration over the types is in
	 * lookup entry order.
	 */
	public Remainder<T> decodeRemainder(long value) {
		Set<T> set = new LinkedHashSet<>();
		long remainder = decodeRemainder(set, value);
		return new Remainder<>(remainder, Collections.unmodifiableSet(set));
	}

	/**
	 * Decode the value into multiple types, and add to the given collection. Iteration over the
	 * types is in lookup entry order. Any remainder is returned.
	 */
	public long decodeRemainder(Collection<T> receiver, long value) {
		for (var entry : lookup.entrySet()) {
			var k = entry.getKey();
			if ((k & value) != k) continue;
			if (receiver != null) receiver.add(entry.getValue());
			value -= k;
			if (value == 0) break;
		}
		return value;
	}

	/**
	 * Decode the value into multiple types, and add to the given collection. Iteration over the
	 * types is in lookup entry order. Any remainder is returned.
	 */
	public int decodeRemainderInt(Collection<T> receiver, int value) {
		return (int) decodeRemainder(receiver, MathUtil.uint(value));
	}

	/**
	 * Returns true if the value contains types with no remainder.
	 */
	public boolean isValid(long value) {
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
		for (T t : ts)
			if (has(value, t)) return true;
		return false;
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
		long mask = 0L;
		for (var t : ts)
			mask |= valueFn.applyAsLong(t);
		return (value & mask) == mask;
	}

	private T findFirst(long unmasked) {
		if (unmasked != 0) for (var entry : lookup.entrySet()) {
			var k = entry.getKey();
			if ((k & unmasked) == k) return entry.getValue();
		}
		return null;
	}

	private long encodeType(T t) {
		var value = valueFn.applyAsLong(t);
		return lookup.containsKey(value) ? value : 0L;
	}

	private static <T> Map<Long, T> lookup(ToLongFunction<T> valueFn, Iterable<T> ts,
		BinaryOperator<T> mergeFn) {
		mergeFn = BasicUtil.def(mergeFn, StreamUtil.mergeError());
		return Collections.unmodifiableMap(StreamUtil.stream(ts).collect(Collectors
			.toMap(t -> valueFn.applyAsLong(t), t -> t, mergeFn, () -> new LinkedHashMap<>())));
	}
}
