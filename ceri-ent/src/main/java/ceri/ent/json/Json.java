package ceri.ent.json;

import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;
import ceri.common.function.Functions;
import ceri.common.property.Separator;
import ceri.common.reflect.Reflect;
import ceri.common.text.Chars;
import ceri.common.text.Parse;

public class Json {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private Json() {}

	/**
	 * An adapter that can register itself with a gson builder.
	 */
	public interface Registrant {
		GsonBuilder registerWith(GsonBuilder builder);
	}

	/**
	 * A json encoder/decoder, based on type token.
	 */
	public static class Coder<T> {
		public final Gson gson;
		public final TypeToken<T> typeToken;

		public static <T> Coder<T> of(Gson gson, Class<T> cls) {
			return of(gson, TypeToken.get(cls));
		}

		public static <T> Coder<T> of(Gson gson, TypeToken<T> typeToken) {
			return new Coder<>(gson, typeToken);
		}

		private Coder(Gson gson, TypeToken<T> typeToken) {
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

	/**
	 * Accesses json elements using dotted-path notation.
	 */
	public static class Paths {
		private Paths() {}

		/**
		 * Given a simple object structure created from gson such as
		 * <code>gson.fromJson(json, Object.class)</code>, this method extracts a value using
		 * dot-notated path where each text part is a map key name, and each number is an array
		 * index, such as "abc.def.1.ghi".
		 */
		public static Object asObj(Object gsonObject, String path) {
			var value = gsonObject;
			for (var part : Separator.DOT.split(path)) {
				var map = Reflect.castOrNull(Map.class, value);
				if (map != null) value = map.get(part);
				else {
					var list = Reflect.castOrNull(List.class, value);
					if (list == null) return null;
					var index = Parse.parseInt(part, null);
					if (index == null || list.size() <= index) return null;
					value = list.get(index);
				}
			}
			return value;
		}

		public static Boolean asBool(Object gsonObject, String path) {
			var obj = asObj(gsonObject, path);
			if (obj == null) return null;
			if (obj instanceof Boolean b) return b;
			return Parse.parseBool(Reflect.castOrNull(String.class, obj), null);
		}

		public static Character asChar(Object gsonObject, String path) {
			var obj = asObj(gsonObject, path);
			return Chars.at(Reflect.castOrNull(String.class, obj), 0);
		}

		public static Byte asByte(Object gsonObject, String path) {
			return asNumber(gsonObject, path, Number::byteValue, Parse.BYTE);
		}

		public static Short asShort(Object gsonObject, String path) {
			return asNumber(gsonObject, path, Number::shortValue, Parse.SHORT);
		}

		public static Integer asInt(Object gsonObject, String path) {
			return asNumber(gsonObject, path, Number::intValue, Parse.INT);
		}

		public static Long asLong(Object gsonObject, String path) {
			return asNumber(gsonObject, path, Number::longValue, Parse.LONG);
		}

		public static Float asFloat(Object gsonObject, String path) {
			return asNumber(gsonObject, path, Number::floatValue, Parse.FLOAT);
		}

		public static Double asDouble(Object gsonObject, String path) {
			return asNumber(gsonObject, path, Number::doubleValue, Parse.DOUBLE);
		}

		public static String asString(Object gsonObject, String path) {
			return Reflect.castOrNull(String.class, asObj(gsonObject, path));
		}
	}

	public static <T> Coder<T> coder(TypeToken<T> typeToken) {
		return Coder.of(GSON, typeToken);
	}

	/**
	 * Decodes then encodes json.
	 */
	public static String format(String json) {
		return format(GSON, json);
	}

	/**
	 * Decodes then encodes json.
	 */
	public static String format(Gson gson, String json) {
		return gson.toJson(JsonParser.parseString(json));
	}

	/**
	 * Convenience method to return string if element can be cast, null otherwise.
	 */
	public static String string(JsonElement json) {
		if (json == null || !json.isJsonPrimitive()) return null;
		return json.getAsString();
	}

	/**
	 * Convenience method to match mechanics of JsonElement getAs...
	 */
	public static JsonObject getAsJsonObject(JsonObject json, String memberName) {
		var obj = json.getAsJsonObject(memberName);
		if (obj != null) return obj;
		throw new IllegalStateException("No json object named " + memberName + ": " + json);
	}

	/**
	 * Iterates over names and elements of a json object.
	 */
	public static void forEach(JsonObject obj,
		Functions.BiConsumer<? super String, ? super JsonElement> action) {
		for (var entry : obj.entrySet())
			action.accept(entry.getKey(), entry.getValue());
	}

	/**
	 * Serializes value and adds to json object if non-null.
	 */
	public static JsonElement addTo(JsonObject obj, JsonSerializationContext context, String name,
		Object value) {
		if (obj == null || context == null || name == null || value == null) return null;
		var element = context.serialize(value);
		obj.add(name, element);
		return element;
	}

	public static <T> JsonDeserializer<T>
		stringDeserializer(Functions.Function<String, T> constructor) {
		return (json, _, _) -> constructor.apply(json.getAsString());
	}

	public static <T> JsonDeserializer<T>
		deserializer(Functions.Function<JsonElement, T> constructor) {
		return (json, _, _) -> constructor.apply(json);
	}

	// support

	private static <T extends Number> T asNumber(Object gsonObject, String path,
		Functions.Function<? super Number, T> nFn, Functions.Function<? super String, T> sFn) {
		var obj = Paths.asObj(gsonObject, path);
		if (obj == null) return null;
		if (obj instanceof Number n) return nFn.apply(n);
		return sFn.apply(Reflect.castOrNull(String.class, obj));
	}
}
