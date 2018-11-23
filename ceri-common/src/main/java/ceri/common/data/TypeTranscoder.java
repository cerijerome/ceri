package ceri.common.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import ceri.common.collection.ImmutableUtil;
import ceri.common.collection.StreamUtil;

/**
 * Helper to convert between object types and integer values.
 */
public abstract class TypeTranscoder<T> {
	final ToIntFunction<T> valueFn;
	final Map<Integer, T> lookup;

	public static class Single<T> extends TypeTranscoder<T> {
		Single(ToIntFunction<T> valueFn, Collection<T> ts) {
			super(valueFn, ts);
		}

		public FieldMapper.Single<T> mapper(IntSupplier getFn, IntConsumer setFn) {
			return FieldMapper.single(getFn, setFn, this);
		}
		
		public int encode(T t) {
			if (t == null) return 0;
			return valueFn.applyAsInt(t);
		}

		public boolean isValid(int value) {
			return decode(value) != null;
		}
		
		public T decode(int value) {
			return lookup.get(value);
		}		
	}

	public static class Flag<T> extends TypeTranscoder<T> {
		Flag(ToIntFunction<T> valueFn, Collection<T> ts) {
			super(valueFn, ts);
		}

		public FieldMapper.Flag<T> mapper(IntSupplier getFn, IntConsumer setFn) {
			return FieldMapper.flag(getFn, setFn, this);
		}
		
		@SafeVarargs
		public final int encode(T... ts) {
			return encode(Arrays.asList(ts));
		}

		public final int encode(Collection<T> ts) {
			return StreamUtil.bitwiseOr(ts.stream().mapToInt(valueFn));
		}

		public boolean isValid(int value) {
			if (value == 0) return true;
			for (int i : lookup.keySet()) {
				int v = value ^ i;
				if (v + i != value) continue;
				value = v;
				if (value == 0) return true;
			}
			return value == 0;
		}
		
		public Set<T> decode(int value) {
			if (value == 0) return Set.of();
			Set<T> set = new LinkedHashSet<>();
			for (Map.Entry<Integer, T> entry : lookup.entrySet()) {
				int i = entry.getKey().intValue();
				T t = entry.getValue();
				int v = value ^ i;
				if (v + i != value) continue;
				value = v;
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
		return new Flag<>(valueFn, ts) {};
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
		return new TypeTranscoder.Single<>(valueFn, ts) {};
	}

	TypeTranscoder(ToIntFunction<T> valueFn, Collection<T> ts) {
		this.valueFn = valueFn;
		lookup = ImmutableUtil.convertAsMap(t -> valueFn.applyAsInt(t), ts);
	}

}
