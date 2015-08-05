package ceri.common.util;

import ceri.common.text.ToStringHelper;

/**
 * Useful for storing key values in a list where multiple values exist for a key.
 */
public class KeyValue<K, V> {
	public final K key;
	public final V value;

	public KeyValue(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(key, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof KeyValue)) return false;
		KeyValue<?, ?> other = (KeyValue<?, ?>) obj;
		if (!EqualsUtil.equals(key, other.key)) return false;
		if (!EqualsUtil.equals(value, other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, key, value).toString();
	}

}
