package ceri.common.data;

import static ceri.common.collection.ImmutableUtil.enumMap;
import java.util.Map;

public enum DataTestType {
	one(1),
	two(2),
	hundred(100),
	twoFiftyFive(0xff);

	private static final Map<Byte, DataTestType> byteLookup =
		enumMap(t -> t.byteValue, DataTestType.class);
	private static final Map<Short, DataTestType> shortLookup =
		enumMap(t -> t.shortValue, DataTestType.class);
	private static final Map<Integer, DataTestType> intLookup =
		enumMap(t -> t.intValue, DataTestType.class);
	public final byte byteValue;
	public final short shortValue;
	public final int intValue;

	public static class ByteValue extends ByteTypeValue<DataTestType> {
		ByteValue(DataTestType type, String name, byte value) {
			super(type, name, value);
		}
	}

	public static class ShortValue extends ShortTypeValue<DataTestType> {
		ShortValue(DataTestType type, String name, short value) {
			super(type, name, value);
		}
	}

	public static class IntValue extends IntTypeValue<DataTestType> {
		IntValue(DataTestType type, String name, int value) {
			super(type, name, value);
		}
	}

	public static ByteValue byteValue(int value) {
		DataTestType type = fromByte(value);
		if (type != null) return new ByteValue(type, null, type.byteValue);
		return new ByteValue(null, "single", (byte) value);
	}

	public static ShortValue shortValue(int value) {
		DataTestType type = fromShort(value);
		if (type != null) return new ShortValue(type, null, type.shortValue);
		return new ShortValue(null, "single", (short) value);
	}

	public static IntValue intValue(int value) {
		DataTestType type = fromInt(value);
		if (type != null) return new IntValue(type, null, type.intValue);
		return new IntValue(null, "single", value);
	}

	public static DataTestType fromByte(int value) {
		return byteLookup.get((byte) value);
	}

	public static DataTestType fromShort(int value) {
		return shortLookup.get((short) value);
	}

	public static DataTestType fromInt(int value) {
		return intLookup.get(value);
	}

	private DataTestType(int value) {
		this.byteValue = (byte) value;
		this.shortValue = (short) value;
		this.intValue = value;
	}

	public ByteValue toByteValue() {
		return new ByteValue(this, null, byteValue);
	}

	public ShortValue toShortValue() {
		return new ShortValue(this, null, shortValue);
	}

	public IntValue toIntValue() {
		return new IntValue(this, null, intValue);
	}

}
