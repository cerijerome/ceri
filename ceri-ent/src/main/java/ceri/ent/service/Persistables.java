package ceri.ent.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collect.Immutable;
import ceri.common.function.Functions;

public class Persistables implements Functions.Closeable, Persistable {
	private static final Logger logger = LogManager.getLogger();
	private final List<? extends Persistable> persistables;

	public static Persistables create(Persistable... persistables) {
		return create(Arrays.asList(persistables));
	}

	public static Persistables create(Collection<? extends Persistable> persistables) {
		return new Persistables(persistables);
	}

	Persistables(Collection<? extends Persistable> persistables) {
		this.persistables = Immutable.list(persistables);
	}

	@Override
	public void close() {
		save();
	}

	@Override
	public void load() throws IOException {
		for (Persistable persistable : persistables)
			persistable.load();
	}

	@Override
	public void save() {
		for (Persistable persistable : persistables)
			save(logger, persistable);
	}

	public static void save(Logger logger, Persistable persistable) {
		try {
			persistable.save();
		} catch (IOException e) {
			logger.catching(e);
		}
	}

}
