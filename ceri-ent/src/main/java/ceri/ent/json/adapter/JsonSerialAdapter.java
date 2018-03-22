package ceri.ent.json.adapter;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public class JsonSerialAdapter<T> {
	private final Class<T> cls;
	private final JsonDeserializer<T> deserializer;
	private final JsonSerializer<T> serializer;

	public static <T> JsonSerialAdapter<T> of(Class<T> cls, JsonDeserializer<T> deserializer,
		JsonSerializer<T> serializer) {
		return new JsonSerialAdapter<>(cls, deserializer, serializer);
	}

	private JsonSerialAdapter(Class<T> cls, JsonDeserializer<T> deserializer,
		JsonSerializer<T> serializer) {
		this.cls = cls;
		this.deserializer = deserializer;
		this.serializer = serializer;
	}

	public GsonBuilder registerWith(GsonBuilder builder) {
		builder.registerTypeAdapter(cls, serializer);
		builder.registerTypeAdapter(cls, deserializer);
		return builder;
	}

}
