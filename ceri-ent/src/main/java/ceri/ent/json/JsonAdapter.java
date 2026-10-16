package ceri.ent.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.function.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import ceri.common.reflect.Reflect;

/**
 * Json adapter using JsonReader and JsonWriter. For fine-detailed sequential processing.
 */
public class JsonAdapter<T> extends TypeAdapter<T> implements Json.Registrant {
	public static final JsonAdapter<LocalDateTime> LOCAL_DATE_TIME =
		ofString(LocalDateTime.class, LocalDateTime::parse);
	public static final JsonAdapter<LocalDate> LOCAL_DATE =
		ofString(LocalDate.class, LocalDate::parse);
	public static final JsonAdapter<LocalTime> LOCAL_TIME =
		ofString(LocalTime.class, LocalTime::parse);
	public static final JsonAdapter<ZoneId> ZONE_ID = ofString(ZoneId.class, ZoneId::of);
	public final Type type;
	public final Read<T> read;
	public final Write<T> write;

	public interface Read<T> {
		Read<?> NULL = _ -> null;

		static <T> Read<T> ofNull() {
			return Reflect.unchecked(NULL);
		}

		T read(JsonReader in) throws IOException;
	}

	public interface Write<T> {
		Write<?> NULL = (_, _) -> {};

		static <T> Write<T> ofNull() {
			return Reflect.unchecked(NULL);
		}

		void write(JsonWriter out, T value) throws IOException;
	}

	/**
	 * Json adapter that can override and/or use the default serialization adapter. Useful? for
	 * implementing special cases of non-trivial objects.
	 */
	public static class Wrapper<T> implements Json.Registrant {
		public final Type type;
		public final Read<T> read;
		public final Write<T> write;
		private final TypeAdapterFactory factory;

		public interface Read<T> {
			T read(TypeAdapter<T> adapter, JsonReader in);
		}

		public interface Write<T> {
			void write(TypeAdapter<T> adapter, JsonWriter out, T value);
		}

		public static <T> Wrapper<T> read(Type type, Read<T> read) {
			return of(type, read, null);
		}

		public static <T> Wrapper<T> write(Type type, Write<T> write) {
			return of(type, null, write);
		}

		public static <T> Wrapper<T> of(Type type, Read<T> read, Write<T> write) {
			return new Wrapper<>(type, read, write);
		}

		private Wrapper(Type type, Read<T> read, Write<T> write) {
			this.type = type;
			this.read = read;
			this.write = write;
			factory = this::factoryCreate;
		}

		@Override
		public GsonBuilder registerWith(GsonBuilder builder) {
			return builder.registerTypeAdapterFactory(factory);
		}

		private <U> TypeAdapter<U> factoryCreate(Gson gson, TypeToken<U> type) {
			if (type.getType() != this.type) return null;
			TypeAdapter<T> delegate = Reflect.unchecked(gson.getDelegateAdapter(factory, type));
			if (delegate == null)
				throw new IllegalStateException("No delegate registered for " + type);
			return Reflect.unchecked(createAdapter(delegate, read, write));
		}
	}

	public static <T> JsonAdapter<T> ofBool(Type type, Function<Boolean, T> deserializer,
		Function<T, Boolean> serializer) {
		return of(type, in -> deserializer.apply(in.nextBoolean()),
			(out, t) -> out.value(serializer.apply(t)));
	}

	public static <T> JsonAdapter<T> ofInt(Type type, Function<Integer, T> deserializer,
		Function<T, Integer> serializer) {
		return of(type, in -> deserializer.apply(in.nextInt()),
			(out, t) -> out.value(serializer.apply(t)));
	}

	public static <T> JsonAdapter<T> ofLong(Type type, Function<Long, T> deserializer,
		Function<T, Long> serializer) {
		return of(type, in -> deserializer.apply(in.nextLong()),
			(out, t) -> out.value(serializer.apply(t)));
	}

	public static <T> JsonAdapter<T> ofDouble(Type type, Function<Double, T> deserializer,
		Function<T, Double> serializer) {
		return of(type, in -> deserializer.apply(in.nextDouble()),
			(out, t) -> out.value(serializer.apply(t)));
	}

	public static <T> JsonAdapter<T> ofString(Type type, Function<String, T> deserializer) {
		return ofString(type, deserializer, String::valueOf);
	}

	public static <T> JsonAdapter<T> ofString(Type type, Function<String, T> deserializer,
		Function<T, String> serializer) {
		return of(type, in -> deserializer.apply(in.nextString()),
			(out, t) -> out.value(serializer.apply(t)));
	}

	public static <T> JsonAdapter<T> of(Type type, Read<T> read, Write<T> write) {
		return new JsonAdapter<>(type, read, write);
	}

	public static <T> JsonAdapter<T> of(Type type, TypeAdapter<T> adapter) {
		return of(type, adapter::read, adapter::write);
	}

	private JsonAdapter(Type type, Read<T> read, Write<T> write) {
		this.type = type;
		this.read = read;
		this.write = write;
	}

	@Override
	public GsonBuilder registerWith(GsonBuilder builder) {
		return builder.registerTypeAdapter(type, this);
	}

	@Override
	public T read(JsonReader in) throws IOException {
		if (nullIn(in)) return null;
		return read.read(in);
	}

	@Override
	public void write(JsonWriter out, T value) throws IOException {
		if (nullOut(out, value)) return;
		write.write(out, value);
	}

	private boolean nullIn(JsonReader in) throws IOException {
		if (in.peek() != JsonToken.NULL) return false;
		in.nextNull();
		return true;
	}

	@SuppressWarnings("resource")
	private boolean nullOut(JsonWriter out, T value) throws IOException {
		if (value != null) return false;
		out.nullValue();
		return true;
	}

	private static <T> TypeAdapter<T> createAdapter(TypeAdapter<T> delegate, Wrapper.Read<T> read,
		Wrapper.Write<T> write) {
		return new TypeAdapter<>() {
			@Override
			public T read(JsonReader in) throws IOException {
				if (read == null) return delegate.read(in);
				return read.read(delegate, in);
			}

			@Override
			public void write(JsonWriter out, T value) throws IOException {
				if (write == null) delegate.write(out, value);
				else write.write(delegate, out, value);
			}
		};
	}
}