package ceri.common.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import ceri.common.collection.ImmutableUtil;
import ceri.common.collection.StreamUtil;

/**
 * Helper to convert between object types and integer values. Integers can map to a single instance
 * (Single), or to a set of instances (Flag). Useful for converting between integers and enums.
 */
public abstract class TypeTranscoder<T> {
	final MaskTranscoder mask;
	final ToIntFunction<T> valueFn;
	final Map<Integer, T> lookup;

	/**
	 * Transcoder for types stored as single values.
	 */
	public static class Single<T> extends TypeTranscoder<T> {
		Single(ToIntFunction<T> valueFn, Collection<T> ts, MaskTranscoder mask) {
			super(valueFn, ts, mask);
		}

		Single(ToIntFunction<T> valueFn, Map<Integer, T> lookup, MaskTranscoder mask) {
			super(valueFn, lookup, mask);
		}

		public Flag<T> flag() {
			return new Flag<>(valueFn, lookup, mask);
		}

		public FieldTranscoder.Single<T> field(IntAccessor accessor) {
			return FieldTranscoder.single(accessor, this);
		}

		public Single<T> mask(MaskTranscoder mask) {
			return new Single<>(valueFn, lookup, mask);
		}

		public int encode(T t) {
			if (t == null) return 0;
			int value = valueFn.applyAsInt(t);
			if (!lookup.containsKey(value)) return 0;
			return mask.encodeInt(value);
		}

		public boolean isValid(int value) {
			//if (value == 0) return true;
			return decode(value) != null;
		}

		public T decode(int value) {
			//if (value == 0) return null;
			return lookup.get(mask.decodeInt(value));
		}
	}

	/**
	 * Transcoder for types stored as multiple values, such as flags.
	 */
	public static class Flag<T> extends TypeTranscoder<T> {
		Flag(ToIntFunction<T> valueFn, Collection<T> ts, MaskTranscoder mask) {
			super(valueFn, ts, mask);
		}

		Flag(ToIntFunction<T> valueFn, Map<Integer, T> lookup, MaskTranscoder mask) {
			super(valueFn, lookup, mask);
		}

		public Single<T> single() {
			return new Single<>(valueFn, lookup, mask);
		}

		public FieldTranscoder.Flag<T> field(IntAccessor accessor) {
			return FieldTranscoder.flag(accessor, this);
		}

		public Flag<T> mask(MaskTranscoder mask) {
			return new Flag<>(valueFn, lookup, mask);
		}

		@SafeVarargs
		public final int encode(T... ts) {
			if (ts == null || ts.length == 0) return 0;
			return encode(Arrays.asList(ts));
		}

		public int encode(Collection<T> ts) {
			if (ts == null || ts.isEmpty()) return 0;
			return mask.encodeInt(
				StreamUtil.bitwiseOr(ts.stream().mapToInt(valueFn).filter(lookup::containsKey)));
		}

		public boolean isValid(int value) {
			value = mask.decodeInt(value);
			if (value == 0) return true;
			for (int i : lookup.keySet()) {
				if ((i & value) != i) continue;
				value -= i;
				if (value == 0) break;
			}
			return value == 0;
		}

		public Set<T> decode(int value) {
			value = mask.decodeInt(value);
			if (value == 0) return Set.of();
			Set<T> set = new LinkedHashSet<>();
			for (Map.Entry<Integer, T> entry : lookup.entrySet()) {
				int i = entry.getKey();
				T t = entry.getValue();
				if ((i & value) != i) continue;
				value -= i;
				set.add(t);
				if (value == 0) break;
			}
			return set;
		}

	}

	public static <T extends Enum<T>> TypeTranscoder.Flag<T> flag(ToIntFunction<T> valueFn,
		Class<T> cls) {
		return flag(valueFn, EnumSet.allOf(cls));
	}

	@SafeVarargs
	public static <T> TypeTranscoder.Flag<T> flag(ToIntFunction<T> valueFn, T... ts) {
		return flag(valueFn, Arrays.asList(ts));
	}

	public static <T> TypeTranscoder.Flag<T> flag(ToIntFunction<T> valueFn, Collection<T> ts) {
		return new Flag<>(valueFn, ts, MaskTranscoder.NULL) {};
	}

	public static <T extends Enum<T>> TypeTranscoder.Single<T> single(ToIntFunction<T> valueFn,
		Class<T> cls) {
		return single(valueFn, EnumSet.allOf(cls));
	}

	@SafeVarargs
	public static <T> TypeTranscoder.Single<T> single(ToIntFunction<T> valueFn, T... ts) {
		return single(valueFn, Arrays.asList(ts));
	}

	public static <T> TypeTranscoder.Single<T> single(ToIntFunction<T> valueFn, Collection<T> ts) {
		return new TypeTranscoder.Single<>(valueFn, ts, MaskTranscoder.NULL) {};
	}

	TypeTranscoder(ToIntFunction<T> valueFn, Collection<T> ts, MaskTranscoder mask) {
		this(valueFn, ImmutableUtil.convertAsMap(valueFn::applyAsInt, ts), mask);
	}

	TypeTranscoder(ToIntFunction<T> valueFn, Map<Integer, T> lookup, MaskTranscoder mask) {
		this.valueFn = valueFn;
		this.lookup = lookup;
		this.mask = mask;
	}

	public Collection<T> all() {
		return lookup.values();
	}

}
