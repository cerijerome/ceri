package ceri.log.registry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.SequencedMap;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.Maps;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.Locker;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.exception.ExceptionTracker;
import ceri.common.function.Excepts;
import ceri.common.property.PropertySource;
import ceri.common.property.TypedProperties;
import ceri.common.time.DateUtil;
import ceri.common.time.TimeSupplier;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

/**
 * A registry persistence service, allowing direct access and queued updates.
 */
public class RegistryService extends LoopingExecutor {
	private static final Logger logger = LogManager.getFormatterLogger();
	private final Locker locker = Locker.of();
	private final BoolCondition sync = BoolCondition.of(locker.lock);
	private final Excepts.Consumer<IOException, java.util.Properties> loadFn;
	private final Excepts.Consumer<IOException, java.util.Properties> saveFn;
	private final int delayMs;
	private final int errorDelayMs;
	private final SequencedMap<Object, Runnable> updates = Maps.link();
	private final ExceptionTracker exceptions = ExceptionTracker.of();
	private final java.util.Properties properties = new java.util.Properties();
	private final PropertySource source = PropertySource.Properties.of(properties);
	public final Registry registry = registry(TypedProperties.of(source));

	public record Config(String name, Path path, int delayMs, int errorDelayMs) {

		public static final int DELAY_MS = 5000;
		public static final int ERROR_DELAY_MS = 5000;

		public static Config of(String name, Path path) {
			return new Config(name, path, DELAY_MS, ERROR_DELAY_MS);
		}
	}

	/**
	 * Instantiates config from properties.
	 */
	public static class Properties extends TypedProperties.Ref {
		private static final String NAME_KEY = "name";
		private static final String PATH_KEY = "path";
		private static final String DELAY_MS_KEY = "delay.ms";
		private static final String ERROR_DELAY_MS_KEY = "error.delay.ms";

		public Properties(TypedProperties properties, String... groups) {
			super(properties, groups);
		}

		public Config config() {
			return new Config(parse(NAME_KEY).get(), parse(PATH_KEY).to(Path::of),
				parse(DELAY_MS_KEY).toInt(Config.DELAY_MS),
				parse(ERROR_DELAY_MS_KEY).toInt(Config.ERROR_DELAY_MS));
		}
	}

	public static RegistryService of(Config config) throws IOException {
		return of(p -> loadPath(p, config.path()), p -> savePath(p, config.path(), config.name()),
			config.delayMs(), config.errorDelayMs());
	}

	public static RegistryService of(Excepts.Consumer<IOException, java.util.Properties> loadFn,
		Excepts.Consumer<IOException, java.util.Properties> saveFn) throws IOException {
		return of(loadFn, saveFn, Config.DELAY_MS, Config.ERROR_DELAY_MS);
	}

	public static RegistryService of(Excepts.Consumer<IOException, java.util.Properties> loadFn,
		Excepts.Consumer<IOException, java.util.Properties> saveFn, int delayMs, int errorDelayMs)
		throws IOException {
		return new RegistryService(loadFn, saveFn, delayMs, errorDelayMs);
	}

	private RegistryService(Excepts.Consumer<IOException, java.util.Properties> loadFn,
		Excepts.Consumer<IOException, java.util.Properties> saveFn, int delayMs, int errorDelayMs)
		throws IOException {
		this.loadFn = loadFn;
		this.saveFn = saveFn;
		this.delayMs = delayMs;
		this.errorDelayMs = errorDelayMs;
		load();
		if (saveFn != null) start();
	}

	public void persist(boolean urgent) {
		if (urgent) sync.signal();
		else sync.set();
	}

	@Override
	public void close() {
		super.close();
		if (processUpdates()) LogUtil.close(this::save);
	}

	@Override
	protected void loop() throws InterruptedException {
		try {
			if (exceptions.isEmpty()) sync.await(delayMs);
			if (processUpdates() || !exceptions.isEmpty()) save();
			exceptions.clear();
		} catch (InterruptedException | RuntimeInterruptedException e) {
			throw e;
		} catch (Exception e) {
			if (exceptions.add(e)) logger.catching(e);
			TimeSupplier.millis.delay(errorDelayMs);
		}
	}

	private boolean processUpdates() {
		try (var _ = locker.lock()) {
			while (true) {
				var entry = updates.pollFirstEntry();
				if (entry == null) break;
				entry.getValue().run();
			}
			return source.modified();
		}
	}

	private Registry registry(TypedProperties properties) {
		return new Registry() {
			@Override
			public void queue(Object source, Consumer<TypedProperties> update) {
				try (var _ = locker.lock()) {
					updates.put(source, () -> update.accept(properties));
				}
			}

			@Override
			public <E extends Exception, T> T
				apply(Excepts.Function<E, TypedProperties, T> function) throws E {
				try (var _ = locker.lock()) {
					return function.apply(properties);
				}
			}

			@Override
			public Registry sub(String... subs) {
				return registry(properties.sub(subs));
			}
		};
	}

	private void load() throws IOException {
		if (loadFn != null) loadFn.accept(properties);
	}

	private void save() throws IOException {
		if (saveFn != null) saveFn.accept(properties);
	}

	private static void loadPath(java.util.Properties properties, Path path) throws IOException {
		if (path == null || !Files.exists(path)) return;
		try (var in = Files.newInputStream(path)) {
			properties.load(in);
		}
	}

	private static void savePath(java.util.Properties properties, Path path, String name)
		throws IOException {
		if (path == null || properties.isEmpty()) return;
		var comment = String.format("# Written by %s %s", name, DateUtil.nowSec());
		Files.createDirectories(path.getParent());
		try (var out =
			Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			properties.store(out, comment);
		}
	}
}
