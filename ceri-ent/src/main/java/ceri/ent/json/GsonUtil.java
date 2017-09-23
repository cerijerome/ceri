package ceri.ent.json;

import java.util.List;
import java.util.Map;
import ceri.common.property.Key;
import ceri.common.util.BasicUtil;
import ceri.common.util.PrimitiveUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	private GsonUtil() {}

	/**
	 * Given a simple object structure created from gson such as
	 * <code>gson.fromJson(json, Object.class)</code>, this method extracts a value using
	 * dot-notated path where each text part is a map key name, and each number is an array index,
	 * such as "abc.def.1.ghi".
	 */
	public static Object extract(Object gsonObject, String path) {
		List<String> parts = Key.create(path).asParts();
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
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Character) null);
	}

	public static Boolean extractBoolean(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Boolean) null);
	}

	public static Byte extractByte(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Byte) null);
	}

	public static Short extractShort(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Short) null);
	}

	public static Integer extractInt(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Integer) null);
	}

	public static Long extractLong(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Long) null);
	}

	public static Float extractFloat(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Float) null);
	}

	public static Double extractDouble(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Double) null);
	}

}
