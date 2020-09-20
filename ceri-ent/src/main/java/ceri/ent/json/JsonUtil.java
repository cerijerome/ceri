package ceri.ent.json;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;
import ceri.common.property.PathFactory;
import ceri.common.util.BasicUtil;
import ceri.common.util.PrimitiveUtil;

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
		JsonObject obj = json.getAsJsonObject(memberName);
		if (obj != null) return obj;
		throw new IllegalStateException("No json object named " + memberName + ": " + json);
	}

	/**
	 * Iterates over names and elements of a json object.
	 */
	public static void forEach(JsonObject obj,
		BiConsumer<? super String, ? super JsonElement> action) {
		for (Map.Entry<String, JsonElement> entry : obj.entrySet())
			action.accept(entry.getKey(), entry.getValue());
	}

	/**
	 * Serializes value and adds to json object if non-null.
	 */
	public static JsonElement addTo(JsonObject obj, JsonSerializationContext context, String name,
		Object value) {
		if (obj == null || context == null || name == null || value == null) return null;
		JsonElement element = context.serialize(value);
		obj.add(name, element);
		return element;
	}

	public static <T> JsonCoder<T> coder(TypeToken<T> typeToken) {
		return JsonCoder.create(GSON, typeToken);
	}

	public static <T> JsonDeserializer<T> stringDeserializer(Function<String, T> constructor) {
		return (json, typeOfT, context) -> constructor.apply(json.getAsString());
	}

	public static <T> JsonDeserializer<T> deserializer(Function<JsonElement, T> constructor) {
		return (json, typeOfT, context) -> constructor.apply(json);
	}

	/**
	 * Given a simple object structure created from gson such as
	 * <code>gson.fromJson(json, Object.class)</code>, this method extracts a value using
	 * dot-notated path where each text part is a map key name, and each number is an array index,
	 * such as "abc.def.1.ghi".
	 */
	public static Object extract(Object gsonObject, String path) {
		List<String> parts = PathFactory.dot.split(path);
		Object value = gsonObject;
		for (String part : parts) {
			Map<?, ?> map = BasicUtil.castOrNull(Map.class, value);
			if (map != null) value = map.get(part);
			else {
				List<?> list = BasicUtil.castOrNull(List.class, value);
				if (list == null) return null;
				Integer index = PrimitiveUtil.valueOf(part, (Integer) null);
				if (index == null || list.size() <= index) return null;
				value = list.get(index);
			}
		}
		return value;
	}

	public static String extractString(Object gsonObject, String path) {
		return BasicUtil.castOrNull(String.class, extract(gsonObject, path));
	}

	public static Character extractChar(Object gsonObject, String path) {
		Object obj = extract(gsonObject, path);
		return PrimitiveUtil.charValue(BasicUtil.castOrNull(String.class, obj));
	}

	public static Boolean extractBoolean(Object gsonObject, String path) {
		Object obj = extract(gsonObject, path);
		if (obj == null) return null;
		Boolean b = BasicUtil.castOrNull(Boolean.class, obj);
		if (b != null) return b;
		return PrimitiveUtil.booleanValue(BasicUtil.castOrNull(String.class, obj));
	}

	public static Byte extractByte(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::byteValue, PrimitiveUtil::byteValue);
	}

	public static Short extractShort(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::shortValue, PrimitiveUtil::shortValue);
	}

	public static Integer extractInt(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::intValue, PrimitiveUtil::intValue);
	}

	public static Long extractLong(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::longValue, PrimitiveUtil::longValue);
	}

	public static Float extractFloat(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::floatValue, PrimitiveUtil::floatValue);
	}

	public static Double extractDouble(Object gsonObject, String path) {
		return extractNumber(gsonObject, path, Number::doubleValue, PrimitiveUtil::doubleValue);
	}

	private static <T extends Number> T extractNumber(Object gsonObject, String path,
		Function<Number, T> nFn, Function<String, T> sFn) {
		Object obj = extract(gsonObject, path);
		if (obj == null) return null;
		Number n = BasicUtil.castOrNull(Number.class, obj);
		if (n != null) return nFn.apply(n);
		return sFn.apply(BasicUtil.castOrNull(String.class, obj));
	}

}
