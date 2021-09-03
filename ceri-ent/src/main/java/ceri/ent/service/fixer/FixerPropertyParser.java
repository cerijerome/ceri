package ceri.ent.service.fixer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.text.StringUtil;

/**
 * Used to create fixers from property files. Add fixer builders for each field, then process the
 * properties to populate the builders.
 */
public class FixerPropertyParser<K> {
	private static final Logger logger = LogManager.getLogger();
	private static final String NO_FIELD = "";
	private final Function<String, K> keyFn;
	private final Map<String, Receiver<K, ?>> receivers = new HashMap<>();

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

	public FixerPropertyParser(Function<String, K> keyFn) {
		this.keyFn = keyFn;
	}

	public FixerPropertyParser<K> add(Fixer.Builder<K, String> builder) {
		return add(NO_FIELD, builder);
	}

	public FixerPropertyParser<K> add(String field, Fixer.Builder<K, String> builder) {
		return add(field, builder, Function.identity());
	}

	public <V> FixerPropertyParser<K> add(Fixer.Builder<K, V> builder,
		Function<String, V> valueFn) {
		return add(NO_FIELD, builder, valueFn);
	}

	public <V> FixerPropertyParser<K> add(String field, Fixer.Builder<K, V> builder,
		Function<String, V> valueFn) {
		receivers.put(field, new Receiver<>(builder, valueFn));
		return this;
	}

	public FixerPropertyParser<K> process(Properties properties) {
		Set<String> missedFields = new HashSet<>();
		for (String name : properties.stringPropertyNames()) {
			if (name.startsWith("#") || StringUtil.blank(name)) continue;
			String value = properties.getProperty(name);
			int i = name.indexOf('.');
			K key = key(name, i);
			String field = field(name, i);
			Receiver<K, ?> receiver = receivers.get(field);
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
