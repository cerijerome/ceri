package ceri.log.test;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.ExceptionSupplier;
import ceri.common.property.PropertyUtil;
import ceri.common.util.BasicUtil;
import ceri.log.util.LogCloseableTracker;
import ceri.log.util.LogUtil;

/**
 * Test helper to create containers from a property file and close after use.
 * Containers are cached by id so multiples are not created.
 */
public class ContainerTestHelper implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	protected final Properties properties;
	protected final String name;
	private final Map<Object, Object> cache = new HashMap<>();
	private final LogCloseableTracker tracker = LogCloseableTracker.of();

	public ContainerTestHelper(String name) throws IOException {
		this.name = name;
		properties = PropertyUtil.load(getClass(), name + ".properties");
	}

	protected String idName(int id) {
		return name + "." + id;
	}

	@SuppressWarnings("resource")
	protected <T extends Closeable> T get(Object id, ExceptionSupplier<IOException, T> supplier)
		throws IOException {
		T t = BasicUtil.uncheckedCast(cache.get(id));
		if (t != null) return t;
		logger.info("Creating container: {}", id);
		t = supplier.get();
		cache.put(id, t);
		return tracker.add(t);
	}

	@Override
	public void close() {
		LogUtil.execute(logger, tracker::close);
	}

}
