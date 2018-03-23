package ceri.ent.json.adapter;

import java.io.IOException;
import java.lang.reflect.Type;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Json adapter using JsonReader and JsonWriter.
 */
public class JsonAdapter<T> extends TypeAdapter<T> {
	public final Type type;
	public final Read<T> read;
	public final Write<T> write;

	public static interface Read<T> {
		static <T> Read<T> _null() {
			return in -> null;
		}

		T read(JsonReader in) throws IOException;
	}

	public static interface Write<T> {
		static <T> Write<T> _null() {
			return (out, value) -> {};
		}

		void write(JsonWriter out, T value) throws IOException;
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
	public T read(JsonReader in) throws IOException {
		if (processNull(in)) return null;
		return read.read(in);
	}

	@Override
	public void write(JsonWriter out, T value) throws IOException {
		if (processNull(out, value)) return;
		write.write(out, value);
	}

	public GsonBuilder registerWith(GsonBuilder builder) {
		return builder.registerTypeAdapter(type, this);
	}

	private boolean processNull(JsonReader in) throws IOException {
		if (in.peek() != JsonToken.NULL) return false;
		in.nextNull();
		return true;
	}

	private boolean processNull(JsonWriter out, T value) throws IOException {
		if (value != null) return false;
		out.nullValue();
		return true;
	}

}