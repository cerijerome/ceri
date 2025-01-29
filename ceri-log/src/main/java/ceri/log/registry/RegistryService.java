package ceri.log.registry;

import static ceri.common.time.TimeSupplier.millis;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.SequencedMap;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.Locker;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.exception.ExceptionTracker;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.property.PropertySource;
import ceri.common.property.TypedProperties;
import ceri.common.time.DateUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

/**
 * A registry persistence service, allowing direct access and queued updates.
 */
public class RegistryService extends LoopingExecutor {
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final int DELAY_MS_DEF = 5000;
	private static final int ERROR_DELAY_MS_DEF = 5000;
	private final Locker locker = Locker.of();
	private final BooleanCondition sync = BooleanCondition.of(locker.lock);
	private final ExceptionConsumer<IOException, Properties> loadFn;
	private final ExceptionConsumer<IOException, Properties> saveFn;
	private final int delayMs;
	private final int errorDelayMs;
	private final SequencedMap<Object, Runnable> updates = new LinkedHashMap<>();
	private final ExceptionTracker exceptions = ExceptionTracker.of();
	private final Properties properties = new Properties();
	private final PropertySource source = PropertySource.Properties.of(properties);
	public final Registry registry = registry(TypedProperties.of(source));

	public static RegistryService of(String name, Path path) throws IOException {
		return of(name, path, DELAY_MS_DEF, ERROR_DELAY_MS_DEF);
	}

	public static RegistryService of(String name, Path path, int delayMs, int errorDelayMs)
		throws IOException {
		return of(p -> loadPath(p, path), p -> savePath(p, path, name), delayMs, errorDelayMs);
	}

	public static RegistryService of(ExceptionConsumer<IOException, Properties> loadFn,
		ExceptionConsumer<IOException, Properties> saveFn) throws IOException {
		return of(loadFn, saveFn, DELAY_MS_DEF, ERROR_DELAY_MS_DEF);
	}

	public static RegistryService of(ExceptionConsumer<IOException, Properties> loadFn,
		ExceptionConsumer<IOException, Properties> saveFn, int delayMs, int errorDelayMs)
		throws IOException {
		return new RegistryService(loadFn, saveFn, delayMs, errorDelayMs);
	}

	private RegistryService(ExceptionConsumer<IOException, Properties> loadFn,
		ExceptionConsumer<IOException, Properties> saveFn, int delayMs, int errorDelayMs)
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
			millis.delay(errorDelayMs);
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
				apply(ExceptionFunction<E, TypedProperties, T> function) throws E {
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

	private static void loadPath(Properties properties, Path path) throws IOException {
		if (path == null || !Files.exists(path)) return;
		try (InputStream in = Files.newInputStream(path)) {
			properties.load(in);
		}
	}

	private static void savePath(Properties properties, Path path, String name) throws IOException {
		if (path == null || properties.isEmpty()) return;
		var comment = String.format("# Written by %s %s", name, DateUtil.nowSec());
		Files.createDirectories(path.getParent());
		try (OutputStream out = Files.newOutputStream(path, CREATE, WRITE)) {
			properties.store(out, comment);
		}
	}
}
