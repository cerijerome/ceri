package ceri.common.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import ceri.common.collection.Enums;
import ceri.common.collection.Immutable;
import ceri.common.collection.Maps;
import ceri.common.collection.Sets;
import ceri.common.function.Functions;
import ceri.common.math.MathUtil;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;
import ceri.common.util.BasicUtil;
import ceri.common.validation.ValidationUtil;

/**
 * Helper to convert between object types and long values. Longs can map to a single instance or to
 * a set of instances. Useful for converting between integers and enums.
 */
public class TypeTranscoder<T> {
	private final Functions.ToLongFunction<T> valueFn;
	private final Map<Long, T> lookup;

	public static record Remainder<T>(long diff, Set<T> types) {
		@SafeVarargs
		public static <T> Remainder<T> of(long diff, T... types) {
			return new Remainder<>(diff, Immutable.setOf(types));
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
	public static <T extends Enum<T>> TypeTranscoder<T> of(Functions.ToLongFunction<T> valueFn,
		Class<T> cls, T... ignore) {
		return of(valueFn, Enums.of(cls), ignore);
	}

	/**
	 * Creates an encoder for unique type values.
	 */
	@SafeVarargs
	public static <T> TypeTranscoder<T> of(Functions.ToLongFunction<T> valueFn,
		Iterable<T> iterable, T... ignore) {
		var stream = Streams.from(iterable);
		if (ignore.length == 0) return new TypeTranscoder<>(valueFn, stream);
		var set = Set.of(ignore);
		return new TypeTranscoder<>(valueFn, stream.filter(t -> !set.contains(t)));
	}

	protected TypeTranscoder(Functions.ToLongFunction<T> valueFn,
		Stream<RuntimeException, T> stream) {
		this.valueFn = valueFn;
		this.lookup = stream
			.collect(Stream.Collect.map(Maps.Put.first, Maps::link, valueFn::applyAsLong, t -> t));
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
		var set = Sets.<T>link();
		decodeRemainder(set, value);
		return Immutable.wrap(set);
	}

	/**
	 * Decode the value into multiple types with remainder value. Iteration over the types is in
	 * lookup entry order.
	 */
	public Remainder<T> decodeRemainder(long value) {
		var set = Sets.<T>link();
		long remainder = decodeRemainder(set, value);
		return new Remainder<>(remainder, Immutable.wrap(set));
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
}
