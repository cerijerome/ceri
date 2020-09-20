package ceri.common.util;

import java.util.Objects;
import ceri.common.text.ToString;

/**
 * Useful for storing key values in a list where multiple values exist for a key.
 */
public class KeyValue<K, V> {
	public final K key;
	public final V value;

	public static class Named<T> extends KeyValue<String, T> {
		protected Named(String name, T value) {
			super(name, value);
		}
	}

	public static <T> Named<T> named(String name, T value) {
		return new Named<>(name, value);
	}

	public static <K, V> KeyValue<K, V> of(K key, V value) {
		return new KeyValue<>(key, value);
	}

	protected KeyValue(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof KeyValue)) return false;
		KeyValue<?, ?> other = (KeyValue<?, ?>) obj;
		if (!Objects.equals(key, other.key)) return false;
		if (!Objects.equals(value, other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, key, value);
	}

}
