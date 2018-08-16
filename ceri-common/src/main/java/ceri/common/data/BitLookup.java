package ceri.common.data;

import static ceri.common.collection.ImmutableUtil.convertAsMap;
import static ceri.common.collection.ImmutableUtil.enumMap;
import java.util.Collection;
import java.util.Map;
import java.util.function.ToIntFunction;

public class BitLookup<T> {
	private final ToIntFunction<T> valueFn;
	private final Map<Integer, T> lookup;
	private final int startBit;
	private final int mask;

	public static <T extends Enum<T>> BitLookup<T> of(Class<T> cls, ToIntFunction<T> valueFn,
		int startBit, int bits) {
		Map<Integer, T> lookup = enumMap(valueFn::applyAsInt, cls);
		return new BitLookup<>(lookup, valueFn, startBit, bits);
	}

	public static <T> BitLookup<T> of(Collection<T> values, ToIntFunction<T> valueFn, int startBit,
		int bits) {
		Map<Integer, T> lookup = convertAsMap(valueFn::applyAsInt, values);
		return new BitLookup<>(lookup, valueFn, startBit, bits);
	}

	private BitLookup(Map<Integer, T> lookup, ToIntFunction<T> valueFn, int startBit, int bits) {
		this.valueFn = valueFn;
		this.lookup = lookup;
		this.startBit = startBit;
		this.mask = (int) ByteUtil.mask(bits);
	}

	public T from(int value) {
		return lookup.get((value >>> startBit) & mask);
	}

	public int value(T t) {
		return valueFn.applyAsInt(t) << startBit;
	}

}
