package ceri.ent.json;

import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;
import ceri.common.function.Functions;
import ceri.common.property.Separator;
import ceri.common.reflect.Reflect;
import ceri.common.text.Chars;
import ceri.common.text.Parse;

public class JsonUtil {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private JsonUtil() {}

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

	public static <T> JsonCoder<T> coder(TypeToken<T> typeToken) {
		return JsonCoder.create(GSON, typeToken);
	}

	public static <T> JsonDeserializer<T>
		stringDeserializer(Functions.Function<String, T> constructor) {
		return (json, _, _) -> constructor.apply(json.getAsString());
	}

	public static <T> JsonDeserializer<T>
		deserializer(Functions.Function<JsonElement, T> constructor) {
		return (json, _, _) -> constructor.apply(json);
	}

	/**
	 * Given a simple object structure created from gson such as
	 * <code>gson.fromJson(json, Object.class)</code>, this method extracts a value using
	 * dot-notated path where each text part is a map key name, and each number is an array index,
	 * such as "abc.def.1.ghi".
	 */
	public static Object extract(Object gsonObject, String path) {
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

	public static String extractString(Object gsonObject, String path) {
		return Reflect.castOrNull(String.class, extract(gsonObject, path));
	}

	public static Character extractChar(Object gsonObject, String path) {
		var obj = extract(gsonObject, path);
		return Chars.at(Reflect.castOrNull(String.class, obj), 0);
	}

	public static Boolean extractBoolean(Object gsonObject, String path) {
		var obj = extract(gsonObject, path);
		if (obj == null) return null;
		var b = Reflect.castOrNull(Boolean.class, obj);
		if (b != null) return b;
		return Parse.parseBool(Reflect.castOrNull(String.class, obj), null);
	}

	public static Byte extractByte(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::byteValue, Parse.BYTE);
	}

	public static Short extractShort(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::shortValue, Parse.SHORT);
	}

	public static Integer extractInt(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::intValue, Parse.INT);
	}

	public static Long extractLong(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::longValue, Parse.LONG);
	}

	public static Float extractFloat(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::floatValue, Parse.FLOAT);
	}

	public static Double extractDouble(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::doubleValue, Parse.DOUBLE);
	}

	private static <T extends Number> T extractNumber(Object gsonObject, String path,
		Functions.Function<? super Number, T> nFn, Functions.Function<? super String, T> sFn) {
		var obj = extract(gsonObject, path);
		if (obj == null) return null;
		var n = Reflect.castOrNull(Number.class, obj);
		if (n != null) return nFn.apply(n);
		return sFn.apply(Reflect.castOrNull(String.class, obj));
	}
}
