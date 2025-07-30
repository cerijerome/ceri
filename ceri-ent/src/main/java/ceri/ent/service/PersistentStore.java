package ceri.ent.service;

import java.io.IOException;

public interface PersistentStore<T> {

	T load() throws IOException;

	void save(T t) throws IOException;
}
