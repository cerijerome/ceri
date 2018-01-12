package ceri.common.property;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Utility methods for property files.
 */
public class PropertyUtil {
	private static final String PROPERTIES_FILE_EXT = ".properties";

	private PropertyUtil() {}

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
		properties.forEach(p -> merged.putAll(p));
		return merged;
	}

	/**
	 * Stores properties in given file.
	 */
	public static void store(Properties properties, File file) throws IOException {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
			properties.store(out, "Saving state");
		}
	}

	/**
	 * Creates properties from given file.
	 */
	public static Properties load(File file) throws IOException {
		Properties properties = new Properties();
		try (InputStream in = new FileInputStream(file)) {
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
		for (Locator locator : locators) {
			try (InputStream in = locator.cls.getResourceAsStream(locator.filename())) {
				if (in == null) continue;
				properties.load(in);
			}
		}
		return properties;
	}

	/**
	 * Returns the String property for given Key.
	 */
	public static String property(Properties properties, Path key) {
		return properties.getProperty(key.value);
	}

}
