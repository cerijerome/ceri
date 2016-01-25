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
import java.util.Properties;

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
		}
		return properties;
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
			if (in == null) throw new FileNotFoundException(cls.getName() + ":" + name);
			properties.load(in);
		}
		return properties;
	}

	/**
	 * Returns the String property for given Key.
	 */
	public static String property(Properties properties, Key key) {
		return properties.getProperty(key.value);
	}

}
