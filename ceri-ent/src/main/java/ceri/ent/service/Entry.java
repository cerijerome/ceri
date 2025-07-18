package ceri.ent.service;

import java.util.Objects;
import ceri.common.util.BasicUtil;

public class Entry<K, V> {
	public final K key;
	public final V value;
	public final long expiration;

	public Entry(K key, V value, long expiration) {
		this.key = key;
		this.value = value;
		this.expiration = expiration;
	}

	public boolean expired(long t) {
		return expiration < t;
	}

	public boolean expired() {
		return expired(System.currentTimeMillis());
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value, expiration);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Entry)) return false;
		Entry<K, V> other = BasicUtil.unchecked(obj);
		return Objects.equals(key, other.key) && Objects.equals(value, other.value) &&
			expiration == other.expiration;
	}

}
