package ceri.common.property;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import ceri.common.exception.ExceptionUtil;

/**
 * Utility methods for property files.
 */
public class PropertyUtil {
	private static final String PROPERTIES_FILE_EXT = ".properties";

	private PropertyUtil() {}

	/**
	 * Parse properties from text.
	 */
	public static Properties parse(String text) {
		try (var r = new StringReader(text)) {
			Properties properties = new Properties();
			ExceptionUtil.shouldNotThrow(() -> properties.load(r));
			return properties;
		}
	}
	
	/**
	 * Merges multiple properties. Latter properties override the former.
	 */
	public static Properties merge(Properties... properties) {
		return merge(Arrays.asList(properties));
	}

	/**
	 * Merges multiple properties. Latter properties override the former.
	 */
	public static Properties merge(Collection<Properties> properties) {
		Properties merged = new Properties();
		properties.forEach(merged::putAll);
		return merged;
	}

	/**
	 * Stores properties in given file.
	 */
	public static void store(Properties properties, java.nio.file.Path file) throws IOException {
		try (Writer out = Files.newBufferedWriter(file)) {
			properties.store(out, "Saving state");
		}
	}

	/**
	 * Creates properties from given file.
	 */
	public static Properties load(String filename) throws IOException {
		return load(java.nio.file.Path.of(filename));
	}

	/**
	 * Creates properties from given file.
	 */
	public static Properties load(java.nio.file.Path file) throws IOException {
		Properties properties = new Properties();
		try (Reader in = Files.newBufferedReader(file)) {
			properties.load(in);
			return properties;
		}
	}

	/**
	 * Creates properties from resource file. Location is same package as the class, file name is
	 * <simple-class-name>.properties
	 */
	public static Properties load(Class<?> cls) throws IOException {
		return load(cls, cls.getSimpleName() + PROPERTIES_FILE_EXT);
	}

	/**
	 * Creates properties from resource file. Location is same package as the class.
	 */
	public static Properties load(Class<?> cls, String name) throws IOException {
		Properties properties = new Properties();
		try (InputStream in = cls.getResourceAsStream(name)) {
			if (in == null) throw new FileNotFoundException(cls.getName() + ": " + name);
			properties.load(in);
			return properties;
		}
	}

	/**
	 * Creates properties from resource file locators and their ancestors. Properties are merged
	 * starting with top level ancestor for each locator, in the order given.
	 */
	public static Properties loadPaths(Locator... locators) throws IOException {
		return loadPaths(Arrays.asList(locators));
	}

	/**
	 * Creates properties from resource file locators and their ancestors. Properties are merged
	 * starting with top level ancestor for each locator, in the order given.
	 */
	public static Properties loadPaths(Collection<Locator> locators) throws IOException {
		Set<Locator> locs = new LinkedHashSet<>();
		locators.forEach(locator -> locs.addAll(locator.ancestors()));
		return load(locs);
	}

	/**
	 * Creates properties from resource file locators. Properties are merged in the order given.
	 */
	public static Properties load(Locator... locators) throws IOException {
		return load(Arrays.asList(locators));
	}

	/**
	 * Creates properties from resource file locators. Properties are merged in the order given.
	 */
	public static Properties load(Collection<Locator> locators) throws IOException {
		Properties properties = new Properties();
		for (Locator locator : locators)
			load(properties, locator);
		return properties;
	}

	/**
	 * Returns the String property for given Key.
	 */
	public static String property(Properties properties, Path key) {
		return properties.getProperty(key.value);
	}

	private static void load(Properties properties, Locator locator) throws IOException {
		try (InputStream in = locator.resourceAsStream()) {
			if (in == null) return;
			properties.load(in);
		}
	}
}
