package ceri.common.property;

import static ceri.common.exception.ExceptionAdapter.shouldNotThrow;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
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
	 * Parse properties from text.
	 */
	public static Properties parse(String text) {
		try (var r = new StringReader(text)) {
			Properties properties = new Properties();
			shouldNotThrow.run(() -> properties.load(r));
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

}
