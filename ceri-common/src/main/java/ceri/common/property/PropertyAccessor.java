package ceri.common.property;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import ceri.common.collection.CollectionUtil;

public interface PropertyAccessor {

	Set<String> keys();

	String property(String key);

	default Map<? super String, ? super String> properties() {
		return CollectionUtil.toMap(Function.identity(), this::property, keys());
	}

	static PropertyAccessor from(ResourceBundle bundle) {
		return new PropertyAccessor() {
			@Override
			public Set<String> keys() {
				return bundle.keySet();
			}

			@Override
			public String property(String key) {
				try {
					return bundle.getString(key);
				} catch (MissingResourceException e) {
					return null;
				}
			}

			@Override
			public String toString() {
				return bundle.toString();
			}
		};
	}

	static PropertyAccessor from(Properties properties) {
		return new PropertyAccessor() {
			@Override
			public String property(String key) {
				return properties.getProperty(key);
			}

			@Override
			public Set<String> keys() {
				return properties.stringPropertyNames();
			}

			@Override
			public Map<? super String, ? super String> properties() {
				return properties;
			}

			@Override
			public String toString() {
				return properties.toString();
			}

		};

	}

}
