package ceri.ent.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.text.Strings;

/**
 * Replaces values by key and sub-field. Useful for correcting fields retrieved by services.
 * {@link PropertyParser} is used to load fixes from property files.
 */
public class Fixer<K, V> {
	public final Map<K, V> map;

	private static class Receiver<K, V> {
		public final Fixer.Builder<K, V> builder;
		public final Function<String, V> valueFn;

		public Receiver(Fixer.Builder<K, V> builder, Function<String, V> valueFn) {
			this.builder = builder;
			this.valueFn = valueFn;
		}

		public void receive(K key, String value) {
			builder.add(key, valueFn.apply(value));
		}
	}

	/**
	 * Used to create fixers from property files. Add fixer builders for each field, then process
	 * the properties to populate the builders.
	 */
	public static class PropertyParser<K> {
		private static final Logger logger = LogManager.getLogger();
		private static final String NO_FIELD = "";
		private final Function<String, K> keyFn;
		private final Map<String, Receiver<K, ?>> receivers = Maps.of();

		private PropertyParser(Function<String, K> keyFn) {
			this.keyFn = keyFn;
		}

		public PropertyParser<K> add(Fixer.Builder<K, String> builder) {
			return add(NO_FIELD, builder);
		}

		public PropertyParser<K> add(String field, Fixer.Builder<K, String> builder) {
			return add(field, builder, Function.identity());
		}

		public <V> PropertyParser<K> add(Fixer.Builder<K, V> builder, Function<String, V> valueFn) {
			return add(NO_FIELD, builder, valueFn);
		}

		public <V> PropertyParser<K> add(String field, Fixer.Builder<K, V> builder,
			Function<String, V> valueFn) {
			receivers.put(field, new Receiver<>(builder, valueFn));
			return this;
		}

		public PropertyParser<K> process(Properties properties) {
			var missedFields = Sets.<String>of();
			for (var name : properties.stringPropertyNames()) {
				if (name.startsWith("#") || Strings.isBlank(name)) continue;
				var value = properties.getProperty(name);
				int i = name.indexOf('.');
				K key = key(name, i);
				var field = field(name, i);
				var receiver = receivers.get(field);
				if (receiver != null) receiver.receive(key, value);
				else missedField(missedFields, key, field);
			}
			return this;
		}

		private void missedField(Set<String> missedFields, K key, String field) {
			if (missedFields.contains(field)) return;
			logger.warn("Field ignored for key {}: {}", key, field);
			missedFields.add(field);
		}

		private K key(String name, int i) {
			if (i == -1) return keyFn.apply(name);
			return keyFn.apply(name.substring(0, i));
		}

		private String field(String name, int i) {
			if (i == -1) return NO_FIELD;
			return name.substring(i + 1);
		}
	}

	/**
	 * Collects fixer map entries.
	 */
	public static class Builder<K, V> {
		final Map<K, V> map = new LinkedHashMap<>();

		Builder() {}

		public Builder<K, V> add(K key, V value) {
			map.put(key, value);
			return this;
		}

		public Fixer<K, V> build() {
			return new Fixer<>(this);
		}
	}

	public static <K, V> Builder<K, V> builder() {
		return new Builder<>();
	}

	public static <K> PropertyParser<K> propertyParser(Function<String, K> keyFn) {
		return new PropertyParser<>(keyFn);
	}

	Fixer(Builder<K, V> builder) {
		map = Map.copyOf(builder.map);
	}

	public V value(K key) {
		return map.get(key);
	}

	public V fix(K key, V value) {
		V fixedValue = value(key);
		if (fixedValue != null) return fixedValue;
		return value;
	}
}
