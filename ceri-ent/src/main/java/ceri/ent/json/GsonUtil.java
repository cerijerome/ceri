package ceri.ent.json;

import java.util.List;
import java.util.Map;
import ceri.common.property.Key;
import ceri.common.util.BasicUtil;
import ceri.common.util.PrimitiveUtil;

public class GsonUtil {

	private GsonUtil() {}
	
	public static Object extract(Object gsonObject, String path) {
		List<String> parts = Key.create(path).asParts();
		Object value = gsonObject;
		for (String part : parts) {
			if (!(value instanceof Map)) return null;
			Map<?, ?> map = (Map<?, ?>)value;
			value = map.get(part);
		}
		return value;
	}
	
	public static String extractString(Object gsonObject, String path) {
		return BasicUtil.castOrNull(String.class, extract(gsonObject, path));
	}
	
	public static Character extractChar(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Character)null);
	}
	
	public static Boolean extractBoolean(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Boolean)null);
	}
	
	public static Byte extractByte(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Byte)null);
	}
	
	public static Short extractShort(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Short)null);
	}
	
	public static Integer extractInt(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Integer)null);
	}
	
	public static Long extractLong(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Long)null);
	}
	
	public static Float extractFloat(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Float)null);
	}
	
	public static Double extractDouble(Object gsonObject, String path) {
		return PrimitiveUtil.valueOf(extractString(gsonObject, path), (Double)null);
	}
	
}
