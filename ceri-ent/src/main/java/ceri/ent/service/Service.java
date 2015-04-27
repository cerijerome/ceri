package ceri.ent.service;

public interface Service<K, V> {
	V retrieve(K k) throws ServiceException;
}
