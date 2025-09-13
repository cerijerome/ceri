package ceri.x10.cm17a.device;

import java.util.List;
import ceri.common.collection.Lists;
import ceri.common.math.Maths;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;
import ceri.x10.util.X10Util;

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
	private static final int DIM_PERCENT_PER_SEND = 5;
	public static final int HEADER1 = 0xd5;
	public static final int HEADER2 = 0xaa;
	public static final int FOOTER = 0xad;

	private Data() {}

	public static int toDimCount(int percent) {
		if (percent == 0) return 0;
		return Math.max(1, Maths.roundDiv(Maths.limit(percent, 0, X10Util.DIM_MAX_PERCENT),
			DIM_PERCENT_PER_SEND));
	}

	public static int fromDimCount(int count) {
		return Maths.limit(count * DIM_PERCENT_PER_SEND, 0, X10Util.DIM_MAX_PERCENT);
	}

	/**
	 * Returns the transmission code for an on/off command.
	 */
	public static int code(House house, Unit unit, FunctionType type) {
		return encodeHouse(house) | encodeUnit(unit) | encodeFunction(type);
	}

	/**
	 * Returns the transmission code for a dim command.
	 */
	public static int code(House house, FunctionType type) {
		return encodeHouse(house) | encodeFunction(type);
	}

	private static int encodeHouse(House house) {
		if (house == null) return 0;
		return Lists.at(houseValues, house.id - 1, 0) << HOUSE_BIT_OFFSET;
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
			if ((value & 1) != 0) encoded |= Lists.at(unitMasks, i, 0);
		return encoded;
	}
}
