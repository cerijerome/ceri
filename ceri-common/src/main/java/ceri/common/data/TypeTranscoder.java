package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateIntLookup;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import ceri.common.collection.ImmutableUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.validation.ValidationUtil;

/**
 * Helper to convert between object types and integer values. Integers can map to a single instance
 * or to a set of instances. Useful for converting between integers and enums.
 */
public class TypeTranscoder<T> {
	final MaskTranscoder mask;
	final ToIntFunction<T> valueFn;
	final Map<Integer, T> lookup;

	public static class Remainder<T> {
		public final Set<T> types;
		public final int remainder;

		@SafeVarargs
		public static <T> Remainder<T> of(int remainder, T... types) {
			return of(remainder, Arrays.asList(types));
		}

		public static <T> Remainder<T> of(int remainder, Collection<T> types) {
			return new Remainder<>(remainder, ImmutableUtil.copyAsSet(types));
		}

		private Remainder(int remainder, Set<T> types) {
			this.types = types;
			this.remainder = remainder;
		}

		public boolean isEmpty() {
			return types.isEmpty() && isExact();
		}

		public boolean isExact() {
			return remainder == 0;
		}

		@Override
		public int hashCode() {
			return HashCoder.hash(types, remainder);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Remainder)) return false;
			Remainder<?> other = (Remainder<?>) obj;
			if (!EqualsUtil.equals(types, other.types)) return false;
			if (remainder != other.remainder) return false;
			return true;
		}

		@Override
		public String toString() {
			return types + "+" + remainder;
		}
	}

	public static <T extends Enum<T>> TypeTranscoder<T> of(ToIntFunction<T> valueFn, Class<T> cls) {
		return of(valueFn, EnumSet.allOf(cls));
	}

	@SafeVarargs
	public static <T> TypeTranscoder<T> of(ToIntFunction<T> valueFn, T... ts) {
		return of(valueFn, Arrays.asList(ts));
	}

	public static <T> TypeTranscoder<T> of(ToIntFunction<T> valueFn, Collection<T> ts) {
		return of(valueFn, MaskTranscoder.NULL, ts);
	}

	public static <T extends Enum<T>> TypeTranscoder<T> of(ToIntFunction<T> valueFn,
		MaskTranscoder mask, Class<T> cls) {
		return of(valueFn, mask, EnumSet.allOf(cls));
	}

	@SafeVarargs
	public static <T> TypeTranscoder<T> of(ToIntFunction<T> valueFn, MaskTranscoder mask, T... ts) {
		return of(valueFn, mask, Arrays.asList(ts));
	}

	public static <T> TypeTranscoder<T> of(ToIntFunction<T> valueFn, MaskTranscoder mask,
		Collection<T> ts) {
		return new TypeTranscoder<>(valueFn, mask, ts);
	}

	private TypeTranscoder(ToIntFunction<T> valueFn, MaskTranscoder mask, Collection<T> ts) {
		this(valueFn, mask, ImmutableUtil.convertAsMap(valueFn::applyAsInt, ts));
	}

	private TypeTranscoder(ToIntFunction<T> valueFn, MaskTranscoder mask, Map<Integer, T> lookup) {
		this.valueFn = valueFn;
		this.lookup = lookup;
		this.mask = mask;
	}

	public Collection<T> all() {
		return lookup.values();
	}

	public FieldTranscoder<T> field(IntAccessor accessor) {
		return FieldTranscoder.of(accessor, this);
	}

	public <U> FieldTranscoder.Typed<U, T> field(IntAccessor.Typed<U> accessor) {
		return FieldTranscoder.Typed.of(accessor, this);
	}

	@SafeVarargs
	public final int encode(T... ts) {
		if (ts == null || ts.length == 0) return 0;
		if (ts.length == 1) return mask.encodeInt(encodeType(ts[0]));
		return encode(Arrays.asList(ts));
	}

	public int encode(Collection<T> ts) {
		return mask.encodeInt(encodeTypes(ts));
	}

	public int encode(Remainder<T> rem) {
		if (rem == null) return 0;
		return mask.encodeInt(encodeTypes(rem.types) | rem.remainder);
	}

	/**
	 * Simple bitwise check if value contains the type. May not match decodeAll if type values
	 * overlap.
	 */
	public boolean has(int value, T t) {
		int mask = valueFn.applyAsInt(t);
		return (value & mask) == mask;
	}

	/**
	 * Simple bitwise check if value contains the types. May not match decodeAll if type values
	 * overlap.
	 */
	@SafeVarargs
	public final boolean hasAny(int value, T... ts) {
		return hasAny(value, Arrays.asList(ts));
	}

	/**
	 * Simple bitwise check if value contains the types. May not match decodeAll if type values
	 * overlap.
	 */
	public boolean hasAny(int value, Collection<T> ts) {
		return ts.stream().mapToInt(valueFn).filter(v -> (value & v) == v).findAny().isPresent();
	}

	/**
	 * Simple bitwise check if value contains the types. May not match decodeAll if type values
	 * overlap.
	 */
	@SafeVarargs
	public final boolean hasAll(int value, T... ts) {
		return hasAll(value, Arrays.asList(ts));
	}

	/**
	 * Simple bitwise check if value contains the types. May not match decodeAll if type values
	 * overlap.
	 */
	public boolean hasAll(int value, Collection<T> ts) {
		int mask = StreamUtil.bitwiseOr(ts.stream().mapToInt(valueFn));
		return (value & mask) == mask;
	}

	public boolean isValid(int value) {
		value = mask.decodeInt(value);
		if (value == 0) return true;
		if (lookup.containsKey(value)) return true;
		for (int i : lookup.keySet()) {
			if ((i & value) != i) continue;
			value -= i;
			if (value == 0) break;
		}
		return value == 0;
	}

	/**
	 * Decode the value to return a single type. Returns null if not found.
	 */
	public T decode(int value) {
		return lookup.get(mask.decodeInt(value));
	}

	/**
	 * Decode the value to return a single type. Throws IllegalArgumentException if not found.
	 */
	public T decodeValid(int value) {
		return validateIntLookup(this::decode, value);
	}

	/**
	 * Decode the value to return a single type. Throws IllegalArgumentException if not found.
	 */
	public T decodeValid(int value, String name) {
		return ValidationUtil.<T>validateIntLookup(this::decode, value, name);
	}

	/**
	 * Decode the value into multiple types. Iteration over the types is in lookup entry order. Any
	 * remainder is discarded.
	 */
	public Set<T> decodeAll(int value) {
		return decodeWithRemainder(value).types;
	}

	/**
	 * Decode the value into multiple types. Iteration over the types is in lookup entry order.
	 */
	public Remainder<T> decodeWithRemainder(int value) {
		value = mask.decodeInt(value);
		Set<T> set = new LinkedHashSet<>();
		for (Map.Entry<Integer, T> entry : lookup.entrySet()) {
			int i = entry.getKey();
			T t = entry.getValue();
			if ((i & value) != i) continue;
			value -= i;
			set.add(t);
			if (value == 0) break;
		}
		return new Remainder<>(value, set);
	}

	private int encodeTypes(Collection<T> ts) {
		if (ts == null || ts.isEmpty()) return 0;
		return StreamUtil.bitwiseOr(ts.stream().mapToInt(this::encodeType));
	}

	private int encodeType(T t) {
		int value = valueFn.applyAsInt(t);
		return lookup.containsKey(value) ? value : 0;
	}

}
