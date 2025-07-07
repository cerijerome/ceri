package ceri.ent.json.adapter;

import java.io.IOException;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import ceri.common.util.BasicUtil;

/**
 * Json adapter that can override and/or use the default serialization adapter. Useful? for
 * implementing special cases of non-trivial objects.
 */
public class JsonOverrideAdapter<T> {
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

	public static <T> JsonOverrideAdapter<T> read(Type type, Read<T> read) {
		return of(type, read, null);
	}

	public static <T> JsonOverrideAdapter<T> write(Type type, Write<T> write) {
		return of(type, null, write);
	}

	public static <T> JsonOverrideAdapter<T> of(Type type, Read<T> read, Write<T> write) {
		return new JsonOverrideAdapter<>(type, read, write);
	}

	private JsonOverrideAdapter(Type type, Read<T> read, Write<T> write) {
		this.type = type;
		this.read = read;
		this.write = write;
		factory = this::factoryCreate;
	}

	public GsonBuilder registerWith(GsonBuilder builder) {
		return builder.registerTypeAdapterFactory(factory);
	}

	private <U> TypeAdapter<U> factoryCreate(Gson gson, TypeToken<U> type) {
		if (type.getType() != this.type) return null;
		TypeAdapter<T> delegate = BasicUtil.unchecked(gson.getDelegateAdapter(factory, type));
		if (delegate == null) throw new IllegalStateException("No delegate registered for " + type);
		return BasicUtil.unchecked(createAdapter(delegate));
	}

	private TypeAdapter<T> createAdapter(TypeAdapter<T> delegate) {
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