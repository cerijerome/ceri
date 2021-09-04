package ceri.serial.jna;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;
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
		if (loaded == null) loadNative();
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

	private void loadNative() {
		logger.debug("Loading {} [{}]", name, ReflectUtil.name(cls));
		loaded = BasicUtil.uncheckedCast(Native.loadLibrary(name, cls));
		logger.info("Loaded {} [{}]", name, ReflectUtil.name(cls));
	}

	private static void addPaths(String property, String... paths) {
		String path = join(SystemVars.sys(property, ""), join(paths));
		if (path.isEmpty()) System.clearProperty(property);
		else System.setProperty(property, path);
	}

	private static String join(String... paths) {
		return Stream.of(paths).map(String::trim).filter(StringUtil::nonEmpty)
			.collect(Collectors.joining(File.pathSeparator));
	}

	/* os-specific settings */

	static {
		if (OsUtil.IS_MAC) addPaths(LIB_PATH, "/usr/local/lib");
	}
}
