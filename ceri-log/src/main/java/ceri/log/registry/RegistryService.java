package ceri.log.registry;

import static ceri.common.time.TimeSupplier.millis;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.exception.ExceptionTracker;
import ceri.common.function.ExceptionConsumer;
import ceri.common.property.BaseProperties;
import ceri.common.time.DateUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

public class RegistryService extends LoopingExecutor {
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final int DELAY_MS_DEF = 5000;
	private static final int ERROR_DELAY_MS_DEF = 5000;
	private final Lock lock = new ReentrantLock();
	private final BooleanCondition sync = BooleanCondition.of(lock);
	private final ExceptionConsumer<IOException, Properties> loadFn;
	private final ExceptionConsumer<IOException, Properties> saveFn;
	private final int delayMs;
	private final int errorDelayMs;
	private final ExceptionTracker exceptions = ExceptionTracker.of();
	private final Properties properties = new Properties();

	public static RegistryService of(String name, Path path) throws IOException {
		return of(name, path, DELAY_MS_DEF, ERROR_DELAY_MS_DEF);
	}

	public static RegistryService of(String name, Path path, int delayMs, int errorDelayMs)
		throws IOException {
		return of(p -> loadPath(p, path), p -> savePath(p, path, name), delayMs,
			errorDelayMs);
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

	private class Opened extends Registry.Opened {
		private boolean modified = false;
		public final Registry registry = () -> open();

		private Opened(String... groups) {
			super(BaseProperties.from(properties), groups);
		}

		@Override
		public void setValue(Object value, String... keyParts) {
			super.setValue(value, keyParts);
			modified = true;
		}

		@Override
		public void close() {
			if (modified) persist();
			modified = false;
			lock.unlock();
		}

		private Opened open() {
			lock.lock();
			modified = false;
			return this;
		}
	}

	@SuppressWarnings("resource")
	public Registry registry(String... prefix) {
		return new Opened(prefix).registry;
	}

	public void persist() {
		sync.signal();
	}

	@Override
	public void close() {
		super.close();
		if (sync.isSet()) LogUtil.close(this::save);
	}

	@Override
	protected void loop() throws InterruptedException {
		try {
			if (exceptions.isEmpty()) sync.await();
			save();
			exceptions.clear();
			millis.delay(delayMs);
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (IOException | RuntimeException e) {
			if (exceptions.add(e)) logger.catching(e);
			millis.delay(errorDelayMs);
		}
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
		var comment = String.format("Written by %s %s", name, DateUtil.nowSec());
		Files.createDirectories(path.getParent());
		try (OutputStream out = Files.newOutputStream(path, CREATE, WRITE)) {
			properties.store(out, comment);
		}
	}
}
