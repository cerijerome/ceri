package ceri.ffm.core;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.util.Map;
import ceri.common.collect.Maps;
import ceri.common.except.Exceptions;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;

public class Layouts {
	private static final Map<Class<?>, ValueLayout> VALUE_LAYOUTS = valueLayouts();
	public static final ValueLayout.OfBoolean BOOL = ValueLayout.JAVA_BOOLEAN;
	public static final ValueLayout.OfChar CHAR = ValueLayout.JAVA_CHAR;
	public static final ValueLayout.OfByte BYTE = ValueLayout.JAVA_BYTE;
	public static final ValueLayout.OfShort SHORT = ValueLayout.JAVA_SHORT;
	public static final ValueLayout.OfInt INT = ValueLayout.JAVA_INT;
	public static final ValueLayout.OfLong LONG = ValueLayout.JAVA_LONG;
	public static final ValueLayout.OfFloat FLOAT = ValueLayout.JAVA_FLOAT;
	public static final ValueLayout.OfDouble DOUBLE = ValueLayout.JAVA_DOUBLE;
	public static final AddressLayout POINTER = ValueLayout.ADDRESS;

	private Layouts() {}

	public static ValueLayout ofValue(Class<?> cls) {
		var layout = VALUE_LAYOUTS.get(cls);
		if (layout != null) return layout;
		if (IntType.class.isAssignableFrom(cls)) return IntType.layout(Reflect.unchecked(cls));
		throw Exceptions.illegalArg("Unsupported type: " + Reflect.name(cls));
	}

	public static ValueLayout ofInt(int size) {
		return switch (size) {
			case Byte.BYTES -> BYTE;
			case Short.BYTES -> SHORT;
			case Integer.BYTES -> INT;
			case Long.BYTES -> LONG;
			default -> throw Exceptions.illegalArg("Unsupported size: " + size);
		};
	}

	public static ValueLayout ofInt(int size, long alignment) {
		return ofInt(size).withByteAlignment(alignment);
	}

	public static ValueLayout ofInt(int size, long alignment, ByteOrder order) {
		return ofInt(size, alignment).withOrder(order);
	}

	public static <L extends MemoryLayout> L set(L layout, String name, long align) {
		return align(name(layout, name), align);
	}

	public static <L extends ValueLayout> L set(L layout, String name, long align,
		ByteOrder order) {
		return order(align(name(layout, name), align), order);
	}

	public static <L extends MemoryLayout> L name(L layout, String name) {
		if (layout == null || name == null) return layout;
		return Reflect
			.unchecked(Strings.isEmpty(name) ? layout.withoutName() : layout.withName(name));
	}

	public static <L extends MemoryLayout> L align(L layout, long align) {
		if (layout == null || align <= 0) return layout;
		return Reflect.unchecked(layout.withByteAlignment(align));
	}

	public static <L extends ValueLayout> L order(L layout, ByteOrder order) {
		if (layout == null || order == null) return layout;
		return Reflect.unchecked(layout.withOrder(order));
	}

	// support

	private static Map<Class<?>, ValueLayout> valueLayouts() {
		var b = Maps.Builder.of(Maps.<Class<?>, ValueLayout>of());
		b.putKeys(ValueLayout.JAVA_BOOLEAN, boolean.class, Boolean.class);
		b.putKeys(ValueLayout.JAVA_CHAR, char.class, Character.class);
		b.putKeys(ValueLayout.JAVA_BYTE, byte.class, Byte.class);
		b.putKeys(ValueLayout.JAVA_SHORT, short.class, Short.class);
		b.putKeys(ValueLayout.JAVA_INT, int.class, Integer.class);
		b.putKeys(ValueLayout.JAVA_LONG, long.class, Long.class);
		b.putKeys(ValueLayout.JAVA_FLOAT, float.class, Float.class);
		b.putKeys(ValueLayout.JAVA_DOUBLE, double.class, Double.class);
		b.putKeys(ValueLayout.ADDRESS, boolean[].class, char[].class, byte[].class, short[].class,
			int[].class, long[].class, float.class, double[].class, String.class,
			MemorySegment.class); // Object[] needs special handling
		return b.wrap();
	}
}
