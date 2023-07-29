package ceri.log.test;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.CollectionUtil;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.RuntimeCloseable;
import ceri.common.property.PropertyUtil;
import ceri.common.util.BasicUtil;
import ceri.log.util.LogUtil;

/**
 * Test helper to create containers from a property file and close after use. Containers are cached
 * by id so multiples are not created.
 */
public class ContainerTestHelper implements RuntimeCloseable {
	private static final Logger logger = LogManager.getLogger();
	protected final Properties properties;
	protected final String name;
	private final Map<Object, AutoCloseable> cache = new ConcurrentHashMap<>();

	public ContainerTestHelper(String name) throws IOException {
		this.name = name;
		properties = PropertyUtil.load(getClass(), name + ".properties");
	}

	protected String idName(int id) {
		return name + "." + id;
	}

	@SuppressWarnings("resource")
	protected <T extends AutoCloseable> T get(Object id, ExceptionSupplier<IOException, T> supplier)
		throws IOException {
		return BasicUtil.uncheckedCast(CollectionUtil.computeIfAbsent(cache, id, x -> {
			logger.info("Creating container: {}", id);
			return supplier.get();
		}));
	}

	@Override
	public void close() {
		LogUtil.close(cache.values());
	}

}
