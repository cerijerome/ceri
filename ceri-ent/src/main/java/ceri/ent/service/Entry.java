package ceri.ent.service;

import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

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
		return HashCoder.hash(key, value, expiration);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Entry)) return false;
		Entry<K, V> other = BasicUtil.uncheckedCast(obj);
		return EqualsUtil.equals(key, other.key) && EqualsUtil.equals(value, other.value) &&
			expiration == other.expiration;
	}

}
