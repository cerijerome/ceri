package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateLongLookup;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import ceri.common.collection.EnumUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.validation.ValidationUtil;

/**
 * Helper to convert between object types and integer values. Integers can map to a single instance
 * or to a set of instances. Useful for converting between integers and enums.
 */
public class TypeTranscoder<T> {
	final MaskTranscoder mask;
	final ToLongFunction<T> valueFn;
	final Map<Long, T> lookup;

	public static class Remainder<T> {
		public final Set<T> types;
		private final long diff;

		@SafeVarargs
		public static <T> Remainder<T> of(long diff, T... types) {
			return new Remainder<>(diff, Set.of(types));
		}

		private Remainder(long diff, Set<T> types) {
			this.types = types;
			this.diff = diff;
		}

		public int intDiff() {
			return Math.toIntExact(diff());
		}

		public long diff() {
			return diff;
		}

		public boolean isEmpty() {
			return types.isEmpty() && isExact();
		}

		public boolean isExact() {
			return diff == 0L;
		}

		@Override
		public int hashCode() {
			return Objects.hash(types, diff);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Remainder<?> other)) return false;
			if (!Objects.equals(types, other.types)) return false;
			if (diff != other.diff) return false;
			return true;
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
		return of(valueFn, mask, EnumUtil.enums(cls));
	}

	public static <T extends Enum<T>> TypeTranscoder<T> ofDups(ToLongFunction<T> valueFn,
		Class<T> cls) {
		return ofDups(valueFn, MaskTranscoder.NULL, cls);
	}

	public static <T extends Enum<T>> TypeTranscoder<T> ofDups(ToLongFunction<T> valueFn,
		MaskTranscoder mask, Class<T> cls) {
		return of(valueFn, mask, EnumUtil.enums(cls), StreamUtil.mergeFirst());
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
		Collection<T> ts) {
		return of(valueFn, mask, ts, StreamUtil.mergeError());
	}

	private static <T> TypeTranscoder<T> of(ToLongFunction<T> valueFn, MaskTranscoder mask,
		Collection<T> ts, BinaryOperator<T> mergeFn) {
		return new TypeTranscoder<>(valueFn, mask, Collections.unmodifiableMap(
			ts.stream().collect(Collectors.toMap(t -> valueFn.applyAsLong(t), t -> t, mergeFn))));
	}

	private TypeTranscoder(ToLongFunction<T> valueFn, MaskTranscoder mask, Map<Long, T> lookup) {
		this.valueFn = valueFn;
		this.lookup = lookup;
		this.mask = mask;
	}

	public Collection<T> all() {
		return lookup.values();
	}

	public FieldTranscoder<T> field(ValueField accessor) {
		return FieldTranscoder.of(accessor, this);
	}

	public <U> FieldTranscoder.Typed<U, T> field(ValueField.Typed<U> accessor) {
		return FieldTranscoder.Typed.of(accessor, this);
	}

	@SafeVarargs
	public final long encode(T... ts) {
		if (ts == null || ts.length == 0) return 0;
		if (ts.length == 1) return mask.encodeInt(encodeType(ts[0]));
		return encode(Arrays.asList(ts));
	}

	public long encode(Collection<T> ts) {
		return mask.encodeInt(encodeTypes(ts));
	}

	public long encode(Remainder<T> rem) {
		if (rem == null) return 0;
		return mask.encodeInt(encodeTypes(rem.types) | rem.diff);
	}

	@SafeVarargs
	public final int encodeInt(T... ts) {
		return (int) encode(ts);
	}

	public int encodeInt(Collection<T> ts) {
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
	public boolean hasAny(long value, Collection<T> ts) {
		return ts.stream().mapToLong(valueFn).filter(v -> (value & v) == v).findAny().isPresent();
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
	public boolean hasAll(long value, Collection<T> ts) {
		var mask = StreamUtil.bitwiseOr(ts.stream().mapToLong(valueFn));
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
	 * Decode the value to return a single type. Throws IllegalArgumentException if not found.
	 */
	public T decodeValid(long value) {
		return validateLongLookup(this::decode, value);
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
		return decodeWithRemainder(value).types;
	}

	/**
	 * Decode the value into multiple types. Iteration over the types is in lookup entry order.
	 */
	public Remainder<T> decodeWithRemainder(long value) {
		value = mask.decodeInt(value);
		Set<T> set = new LinkedHashSet<>();
		for (var entry : lookup.entrySet()) {
			var k = entry.getKey();
			T t = entry.getValue();
			if ((k & value) != k) continue;
			value -= k;
			set.add(t);
			if (value == 0) break;
		}
		return new Remainder<>(value, set);
	}

	private long encodeTypes(Collection<T> ts) {
		if (ts == null || ts.isEmpty()) return 0;
		return StreamUtil.bitwiseOr(ts.stream().mapToLong(this::encodeType));
	}

	private long encodeType(T t) {
		var value = valueFn.applyAsLong(t);
		return lookup.containsKey(value) ? value : 0L;
	}

}
