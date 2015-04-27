package ceri.ent.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ImmutableUtil;

public class Persistables implements Closeable, Persistable {
	private static final Logger logger = LogManager.getLogger();
	private final List<Persistable> persistables;

	public static Persistables create(Persistable...persistables) {
		return create(Arrays.asList(persistables));
	}
	
	public static Persistables create(Collection<Persistable> persistables) {
		return new Persistables(persistables);
	}
	
	Persistables(Collection<Persistable> persistables) {
		this.persistables = ImmutableUtil.copyAsList(persistables);
	}

	@Override
	public void close() {
		save();
	}

	@Override
	public void load() {
		for (Persistable persistable : persistables)
			load(persistable);
	}

	@Override
	public void save() {
		for (Persistable persistable : persistables)
			save(persistable);
	}

	private void load(Persistable persistable) {
		try {
			persistable.load();
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	private void save(Persistable persistable) {
		try {
			persistable.save();
		} catch (IOException e) {
			logger.catching(e);
		}
	}

}
