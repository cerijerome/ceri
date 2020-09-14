package ceri.x10.cm11a.protocol;

import static ceri.common.math.MathUtil.limit;
import static ceri.common.math.MathUtil.roundDiv;
import static ceri.x10.util.X10Util.DIM_MAX_PERCENT;
import ceri.common.data.ByteArray.Encoder;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteWriter;
import ceri.x10.command.FunctionGroup;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;

/**
 * Methods for converting between types and bytes for transmitting to the CM11A controller.
 */
public class Transmit {
	private static final int DIM_START_BIT = 3;
	private static final int DIM_MASK = 0x1f;
	private static final int HEADER_MASK = 0x04;
	private static final int FUNCTION_MASK = 0x2;
	private static final int EXTENDED_MASK = 0x1;
	private static final int DIM_MAX = 22;

	private Transmit() {}

	public static int size(Entry entry) {
		if (entry.isGroup(FunctionGroup.ext)) return 4;
		return 2;
	}

	/* Decoding support */

	public static Entry decode(ByteReader r) {
		int header = r.readUbyte();
		int code = r.readUbyte();
		House house = Data.decodeHouse(code);
		if (!function(header)) return Entry.address(house, Data.decodeUnit(code));
		if (extended(header)) return Entry.ext(house, r.readUbyte(), r.readUbyte());
		FunctionType type = Data.decodeFunctionType(code);
		if (type.group == FunctionGroup.dim) return Entry.dim(house, type, dimPercent(header));
		return Entry.function(house, type);
	}

	private static int dimPercent(int header) {
		int value = limit((header >>> DIM_START_BIT) & DIM_MASK, 0, DIM_MAX);
		return roundDiv(value * DIM_MAX_PERCENT, DIM_MAX);
	}

	private static boolean function(int header) {
		return (header & FUNCTION_MASK) != 0;
	}

	private static boolean extended(int header) {
		return (header & EXTENDED_MASK) != 0;
	}

	/* Encoding support */

	public static ByteProvider encode(Entry entry) {
		return Encoder.fixed(size(entry)).apply(encoder -> encode(entry, encoder)).immutable();
	}

	public static void encode(Entry entry, ByteWriter<?> w) {
		if (entry.isAddress())
			w.writeBytes(header(0, false, false), Data.encode(entry.house, entry.unit));
		else if (entry.isGroup(FunctionGroup.ext)) w.writeBytes(header(0, true, true),
			Data.encode(entry.house, entry.type), entry.data, entry.command);
		else w.writeBytes(header(entry.data, true, false), Data.encode(entry.house, entry.type));
	}

	private static int header(int dimPercent, boolean function, boolean extended) {
		int dim = limit(roundDiv(dimPercent * DIM_MAX, DIM_MAX_PERCENT), 0, DIM_MAX);
		return (dim << DIM_START_BIT) | HEADER_MASK | (function ? FUNCTION_MASK : 0) |
			(extended ? EXTENDED_MASK : 0);
	}

}
