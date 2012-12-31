package ceri.common.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil {
	
	private PropertyUtil() {
	}
	
	public static Properties load(File file) throws IOException {
		Properties properties = new Properties();
		try (InputStream in = new FileInputStream(file)) {
			properties.load(in);
		}
		return properties;
	}
	
	public static Properties load(Class<?> cls) throws IOException {
		return load(cls, cls.getSimpleName() + ".properties");
	}
	
	public static Properties load(Class<?> cls, String name) throws IOException {
		Properties properties = new Properties();
		try (InputStream in = cls.getResourceAsStream(name)) {
			properties.load(in);
		}
		return properties;
	}
	
	public static String property(Properties properties, Key key) {
		return properties.getProperty(key.value);
	}


}
