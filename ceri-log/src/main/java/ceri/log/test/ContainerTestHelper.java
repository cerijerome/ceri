package ceri.log.test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.property.TypedProperties;
import ceri.common.util.BasicUtil;
import ceri.log.util.LogUtil;

/**
 * Test helper to create containers from a property file and close after use. Containers are cached
 * by id so multiples are not created.
 */
public class ContainerTestHelper implements Functions.Closeable {
	private static final Logger logger = LogManager.getLogger();
	protected final TypedProperties properties;
	protected final String name;
	private final Map<Object, AutoCloseable> cache = new ConcurrentHashMap<>();

	public ContainerTestHelper(String name) throws IOException {
		this.name = name;
		properties = TypedProperties.load(getClass(), name + ".properties");
	}

	protected String name(Object id) {
		return name + "." + id;
	}

	@SuppressWarnings("resource")
	protected <T extends AutoCloseable> T get(Object id,
		Excepts.Function<IOException, TypedProperties, T> supplier) {
		var name = name(id);
		return BasicUtil.unchecked(cache.computeIfAbsent(name, _ -> {
			logger.info("Creating container: {}", name);
			return ExceptionAdapter.runtimeIo.get(() -> supplier.apply(properties(name)));
		}));
	}

	protected TypedProperties properties(String... groups) {
		return properties.sub(groups);
	}

	@Override
	public void close() {
		LogUtil.close(cache.values());
	}

}
