package ceri.ent.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JsonCoder<T> {
	public final Gson gson;
	public final TypeToken<T> typeToken;

	public static <T> JsonCoder<T> create(Gson gson, Class<T> cls) {
		return create(gson, TypeToken.get(cls));
	}

	public static <T> JsonCoder<T> create(Gson gson, TypeToken<T> typeToken) {
		return new JsonCoder<>(gson, typeToken);
	}

	private JsonCoder(Gson gson, TypeToken<T> typeToken) {
		this.gson = gson;
		this.typeToken = typeToken;
	}

	public T fromJson(String json) {
		return gson.fromJson(json, typeToken.getType());
	}

	public String toJson(T t) {
		return gson.toJson(t, typeToken.getType());
	}

}
