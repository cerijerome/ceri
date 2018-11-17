package ceri.common.collection;

import java.util.Map;
import java.util.function.Function;

/**
 * Enum lookup helper.
 */
public class EnumLookup<K, T extends Enum<T>> {
	private final Map<K, T> map;

	public static class ByInt<T extends Enum<T>> extends EnumLookup<Integer, T> {
		ByInt(Map<Integer, T> map) {
			super(map);
		}

		public T find(int key) {
			return super.find(key);
		}
	}

	public static <T extends Enum<T>> EnumLookup.ByInt<T> ofInt(Function<T, Number> fn,
		Class<T> cls) {
		Function<T, Integer> intFn = t -> fn.apply(t).intValue();
		return new EnumLookup.ByInt<>(ImmutableUtil.enumMap(intFn, cls));
	}

	public static <K, T extends Enum<T>> EnumLookup<K, T> of(Function<T, K> fn, Class<T> cls) {
		return new EnumLookup<>(ImmutableUtil.enumMap(fn, cls)) {
			public T find(K key) {
				return super.find(key);
			}
		};
	}

	EnumLookup(Map<K, T> map) {
		this.map = map;
	}

	protected T find(K key) {
		return map.get(key);
	}

}
