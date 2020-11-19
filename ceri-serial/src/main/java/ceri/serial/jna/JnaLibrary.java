package ceri.serial.jna;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Native;
import ceri.common.util.BasicUtil;
import ceri.common.util.Enclosed;

/**
 * Encapsulates cached access to a native library. Can be used to override a library for testing.
 */
public class JnaLibrary<T> {
	private static final Logger logger = LogManager.getLogger();
	public final Class<T> cls;
	public final String name;
	private volatile T loaded;
	private volatile T override;

	public static <T> JnaLibrary<T> of(String name, Class<T> cls) {
		return new JnaLibrary<>(name, cls);
	}

	private JnaLibrary(String name, Class<T> cls) {
		this.name = name;
		this.cls = cls;
	}

	/**
	 * Loads typed native library, or returns override if set.
	 */
	public T get() {
		if (override != null) return override;
		if (loaded == null) loadNative();
		return loaded;
	}

	/**
	 * Temporarily override the native library.
	 */
	public <U extends T> Enclosed<U> enclosed(U override) {
		set(override);
		return Enclosed.of(override, l -> set(null));
	}
	
	/**
	 * Overrides the library; use null to clear. Useful for testing.
	 */
	private void set(T override) {
		this.override = override;
	}

	private void loadNative() {
		logger.debug("Loading {} started [{}]", name, cls.getSimpleName());
		loaded = BasicUtil.uncheckedCast(Native.loadLibrary(name, cls));
		logger.info("Loading {} complete [{}]", name, cls.getSimpleName());
	}

}
