package ceri.x10.cm17a.device;

import static ceri.common.collection.CollectionUtil.getOrDefault;
import java.util.List;
import ceri.common.data.ByteArray.Encoder;
import ceri.common.data.ByteProvider;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

/**
 * Creates the transmission sequences for device commands.
 */
public class Data {
	private static final int HOUSE_BIT_OFFSET = 12;
	private static final int OFF_MASK = 0x20;
	private static final int DIM_MASK = 0x98;
	private static final int BRIGHT_MASK = 0x88;
	private static final List<Integer> houseValues = // MNOPCDABEFGHKLIJ order
		List.of(6, 7, 4, 5, 8, 9, 10, 11, 14, 15, 12, 13, 0, 1, 2, 3);
	private static final List<Integer> unitMasks = List.of(0x10, 0x08, 0x40, 0x400); // bits 0..3
	private static final byte HEADER1 = (byte) 0xd5;
	private static final byte HEADER2 = (byte) 0xaa;
	private static final byte FOOTER = (byte) 0xad;
	private static final byte TRANSMISSION_LEN = 5;

	private Data() {}

	/**
	 * Returns the binary transmission sequence for an on/off command.
	 */
	public static ByteProvider transmission(House house, Unit unit, FunctionType type) {
		int value = encodeHouse(house) | encodeUnit(unit) | encodeFunction(type);
		return transmission(value);
	}

	/**
	 * Returns the binary transmission sequence for a dim command.
	 */
	public static ByteProvider transmission(House house, FunctionType type) {
		int value = encodeHouse(house) | encodeFunction(type);
		return transmission(value);
	}

	private static ByteProvider transmission(int code) {
		return Encoder.fixed(TRANSMISSION_LEN).writeBytes(HEADER1, HEADER2).writeShortMsb(code)
			.writeByte(FOOTER).immutable();
	}

	private static int encodeHouse(House house) {
		if (house == null) return 0;
		return getOrDefault(houseValues, house.id - 1, 0) << HOUSE_BIT_OFFSET;
	}

	private static int encodeFunction(FunctionType type) {
		if (type == null) return 0;
		if (type == FunctionType.off) return OFF_MASK;
		if (type == FunctionType.dim) return DIM_MASK;
		if (type == FunctionType.bright) return BRIGHT_MASK;
		return 0;
	}

	private static int encodeUnit(Unit unit) {
		if (unit == null) return 0;
		int encoded = 0;
		for (int i = 0, value = unit.value - 1; value > 0; i++, value >>>= 1)
			if ((value & 1) != 0) encoded |= getOrDefault(unitMasks, i, 0);
		return encoded;
	}

}
