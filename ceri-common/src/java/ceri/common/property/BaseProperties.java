package ceri.common.property;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

public class BaseProperties {
	private final String prefix;
	private final Properties properties;
	
	protected BaseProperties(Properties properties, String prefix) {
		this.prefix = prefix;
		this.properties = properties;
	}
	
	protected String key(String...keys) {
		return Key.createWithPrefix(prefix, keys).value;
	}
	
	protected String value(String...keys) {
		return properties.getProperty(key(keys));
	}
	
	protected Integer intValue(String...keys) {
		String value = value(keys);
		if (value == null) return null;
		return Integer.valueOf(value);
	}
	
	protected Collection<String> keys() {
		Collection<String> keys = new LinkedHashSet<>();
		for (Object o : properties.keySet()) {
			String key = String.valueOf(o);
			if (prefix == null || prefix.isEmpty() || key.startsWith(prefix)) keys.add(key);
		}
		return keys;
	}
	
}
