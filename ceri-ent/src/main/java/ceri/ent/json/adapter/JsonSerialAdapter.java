package ceri.ent.json.adapter;

import java.lang.reflect.Type;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * Json adapter using JsonDeserializer and JsonSerializer.
 */
public class JsonSerialAdapter<T> {
	private final Type type;
	private final JsonDeserializer<T> deserializer;
	private final JsonSerializer<T> serializer;

	public static <T> JsonSerialAdapter<T> of(Type type, JsonDeserializer<T> deserializer,
		JsonSerializer<T> serializer) {
		return new JsonSerialAdapter<>(type, deserializer, serializer);
	}

	private JsonSerialAdapter(Type type, JsonDeserializer<T> deserializer,
		JsonSerializer<T> serializer) {
		this.type = type;
		this.deserializer = deserializer;
		this.serializer = serializer;
	}

	public GsonBuilder registerWith(GsonBuilder builder) {
		builder.registerTypeAdapter(type, serializer);
		builder.registerTypeAdapter(type, deserializer);
		return builder;
	}

}
