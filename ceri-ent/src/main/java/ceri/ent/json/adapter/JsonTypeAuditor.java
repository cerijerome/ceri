package ceri.ent.json.adapter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * Checks when a type matching predicates is (de)serialized. Can be configured to log and/or throw
 * exception. Useful to audit the presence of library type adapters, and when Gson warns of missing
 * java type adapters. Register with gson builder BEFORE other adapters - for some reason the order
 * is reversed during Gson construction.
 */
public class JsonTypeAuditor implements TypeAdapterFactory {
	private static final Logger logger = LogManager.getLogger();
	private static final List<Class<?>> JAVA_COLLECTIONS =
		List.of(Collection.class, Set.class, List.class, Map.class);
	private static final Pattern JAVA_LANG_REGEX = Pattern.compile("java\\.lang\\..*");
	private static final Pattern JAVA_REGEX = Pattern.compile("javax?\\..*");
	private final boolean enabled;
	private final boolean exception;
	private final Predicate<TypeToken<?>> allow;
	private final Predicate<TypeToken<?>> deny;

	public static GsonBuilder auditForJava(GsonBuilder gsonBuilder, boolean exception) {
		return builder().exception(exception).allowClasses(JAVA_COLLECTIONS)
			.allowClass(JAVA_LANG_REGEX).denyClass(JAVA_REGEX).build().registerWith(gsonBuilder);
	}

	public static class Builder {
		boolean enabled = true;
		boolean exception = false;
		Predicate<TypeToken<?>> allow = t -> false;
		Predicate<TypeToken<?>> deny = t -> false;

		Builder() {}

		/**
		 * Flag to quickly enable/disable factory.
		 */
		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public Builder exception(boolean exception) {
			this.exception = exception;
			return this;
		}

		public Builder allowClass(Pattern pattern) {
			return allow(classMatches(pattern));
		}

		public Builder allowClasses(Class<?>... classes) {
			return allowClasses(Arrays.asList(classes));
		}

		public Builder allowClasses(Collection<Class<?>> classes) {
			classes.forEach(cls -> allow(classIs(cls)));
			return this;
		}

		public final Builder allowSubClassesOf(Class<?>... classes) {
			return allowSubClassesOf(Arrays.asList(classes));
		}

		public final Builder allowSubClassesOf(Collection<Class<?>> classes) {
			classes.forEach(cls -> allow(subClassOf(cls)));
			return this;
		}

		public final Builder denyClass(Pattern pattern) {
			return deny(classMatches(pattern));
		}

		public Builder denyClasses(Class<?>... classes) {
			return denyClasses(Arrays.asList(classes));
		}

		public Builder denyClasses(Collection<Class<?>> classes) {
			classes.forEach(cls -> deny(classIs(cls)));
			return this;
		}

		public final Builder denySubClassesOf(Class<?>... classes) {
			return denySubClassesOf(Arrays.asList(classes));
		}

		public final Builder denySubClassesOf(Collection<Class<?>> classes) {
			classes.forEach(cls -> deny(subClassOf(cls)));
			return this;
		}

		public final Builder allow(Predicate<TypeToken<?>> allow) {
			this.allow = this.allow.or(allow);
			return this;
		}

		public final Builder deny(Predicate<TypeToken<?>> deny) {
			this.deny = this.deny.or(deny);
			return this;
		}

		public JsonTypeAuditor build() {
			return new JsonTypeAuditor(this);
		}

		private static Predicate<TypeToken<?>> classMatches(Pattern pattern) {
			// return typeToken -> pattern.matcher(typeToken.getType().getTypeName()).matches();
			return typeToken -> {
				String name = typeToken.getRawType().getTypeName();
				boolean result = pattern.matcher(name).matches();
				return result;
			};
		}

		private static Predicate<TypeToken<?>> classIs(Class<?> cls) {
			// return typeToken -> cls == typeToken.getType().getClass();
			return typeToken -> {
				Class<?> actual = typeToken.getRawType();
				boolean result = cls == actual;
				return result;
			};
		}

		private static Predicate<TypeToken<?>> subClassOf(Class<?> cls) {
			// return typeToken -> cls.isAssignableFrom(typeToken.getType().getClass());
			return typeToken -> {
				Class<?> actual = typeToken.getRawType();
				boolean result = cls.isAssignableFrom(actual);
				return result;
			};
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	JsonTypeAuditor(Builder builder) {
		enabled = builder.enabled;
		exception = builder.exception;
		allow = builder.allow;
		deny = builder.deny;
	}

	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		if (!enabled || allow.test(type) || !deny.test(type)) return null;
		IllegalArgumentException e = new IllegalArgumentException("No adapter for: " + type);
		if (exception) throw e;
		logger.catching(Level.WARN, e);
		return null;
	}

	public GsonBuilder registerWith(GsonBuilder b) {
		if (!enabled) return b;
		return b.registerTypeAdapterFactory(this);
	}

}
