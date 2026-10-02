package ceri.ent.service;

public record Entry<K, V>(K key, V value, long expiration) {

	public boolean expired(long t) {
		return expiration() < t;
	}

	public boolean expired() {
		return expired(System.currentTimeMillis());
	}
}
