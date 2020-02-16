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
 * or to a set of instances. Useful for converting between integers and enums.
 */
public class TypeTranscoder<T> {
	final MaskTranscoder mask;
	final ToIntFunction<T> valueFn;
	final Map<Integer, T> lookup;

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

	@SafeVarargs
	public final int encode(T... ts) {
		if (ts == null || ts.length == 0) return 0;
		if (ts.length == 1) return mask.encodeInt(encodeType(ts[0]));
		return encode(Arrays.asList(ts));
	}

	public int encode(Collection<T> ts) {
		if (ts == null || ts.isEmpty()) return 0;
		return mask.encodeInt(StreamUtil.bitwiseOr(ts.stream().mapToInt(this::encodeType)));
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

	public T decode(int value) {
		return lookup.get(mask.decodeInt(value));
	}

	public Set<T> decodeAll(int value) {
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
		return set;
	}

	private int encodeType(T t) {
		int value = valueFn.applyAsInt(t);
		return lookup.containsKey(value) ? value : 0;
	}

}
