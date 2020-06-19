package ceri.ent.service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public class PersistentIdService<V> extends PersistentService<Long, V> {
	private final LocalId localId = new LocalId();

	public PersistentIdService(PersistentStore<Collection<V>> store, Function<V, Long> idFn) {
		super(store, idFn);
	}

	protected long takeId() {
		return localId.takeId();
	}

	@Override
	protected void safeAdd(Map<Long, V> map) {
		super.safeAdd(map);
		for (Long id : map.keySet())
			localId.taken(id);
	}

}
