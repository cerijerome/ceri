package ceri.jna.util;

import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import ceri.common.io.IoUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.util.Enclosed;
import ceri.common.util.OsUtil;
import ceri.common.util.SystemVars;

/**
 * Encapsulates cached access to a native library. Can be used to override a library for testing.
 */
public class JnaLibrary<T extends Library> {
	private static final Logger logger = LogManager.getLogger();
	private static final String PLATFORM_LIB_PATH = "jna.platform.library.path";
	private static final String LIB_PATH = "jna.library.path";
	private static final String MAC_LOCAL_LIB = "/usr/local/lib";
	private static final String MAC_HOMEBREW_LIB = "/opt/homebrew/lib";
	public final Class<T> cls;
	public final String name;
	private volatile T loaded;
	private volatile T override;

	/**
	 * Return the platform library search path system property.
	 */
	public static String platformPath() {
		return SystemVars.sys(PLATFORM_LIB_PATH, "");
	}

	/**
	 * Return the library search path system property.
	 */
	public static String path() {
		return SystemVars.sys(LIB_PATH, "");
	}

	/**
	 * Add entries to JNA library path system variable.
	 */
	public static void addPaths(String... paths) {
		addPropertyPaths(LIB_PATH, paths);
	}

	/**
	 * Create instance with library name and native interface.
	 */
	public static <T extends Library> JnaLibrary<T> of(String name, Class<T> cls) {
		return new JnaLibrary<>(name, cls);
	}

	private JnaLibrary(String name, Class<T> cls) {
		this.name = name;
		this.cls = cls;
	}

	/**
	 * Add a search path specific to the library.
	 */
	public JnaLibrary<T> addPath(String path) {
		NativeLibrary.addSearchPath(name, path);
		return this;
	}

	/**
	 * Loads typed native library, or returns override if set.
	 */
	public T get() {
		if (override != null) return override;
		if (loaded == null) synchronized (this) {
			loaded = Objects.requireNonNullElseGet(loaded, this::loadNative);
		}
		return loaded;
	}

	/**
	 * Temporarily override the native library.
	 */
	public <U extends T> Enclosed<RuntimeException, U> enclosed(U override) {
		set(override);
		return Enclosed.of(override, l -> set(null));
	}

	/**
	 * Overrides the library; use null to clear. Useful for testing.
	 */
	private void set(T override) {
		this.override = override;
	}

	private T loadNative() {
		logger.debug("Loading {} [{}]", name, ReflectUtil.name(cls));
		var loaded = Native.load(name, cls);
		logger.info("Loaded {} [{}]", name, ReflectUtil.name(cls));
		return loaded;
	}

	private static void addPropertyPaths(String property, String... paths) {
		String path = IoUtil.pathVariable(SystemVars.sys(property, ""), IoUtil.pathVariable(paths));
		SystemVars.setProperty(property, path.isEmpty() ? null : path);
	}

	/* os-specific settings */

	static {
		if (OsUtil.os().mac) addPaths(MAC_LOCAL_LIB, MAC_HOMEBREW_LIB);
	}
}
