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
 * Methods for converting between types and bytes for receiving from the CM11A controller.
 */
public class Receive {
	private static final int DIM_MAX = 210;

	private Receive() {}

	public static int size(Entry entry) {
		if (entry.isGroup(FunctionGroup.ext)) return 3;
		if (entry.isGroup(FunctionGroup.dim)) return 2;
		return 1;
	}

	/* Decoding support */

	public static Entry decode(boolean function, ByteReader r) {
		int code = r.readUbyte();
		House house = Data.decodeHouse(code);
		if (!function) return Entry.address(house, Data.decodeUnit(code));
		FunctionType type = Data.decodeFunctionType(code);
		if (type.group == FunctionGroup.ext) return Entry.ext(house, r.readUbyte(), r.readUbyte());
		if (type.group == FunctionGroup.dim)
			return Entry.dim(house, type, toDimPercent(r.readUbyte()));
		return Entry.function(house, type);
	}

	private static int toDimPercent(int data) {
		return limit(roundDiv(data * DIM_MAX_PERCENT, DIM_MAX), 0, DIM_MAX_PERCENT);
	}

	/* Encoding support */

	public static ByteProvider encode(Entry entry) {
		return Encoder.fixed(size(entry)).apply(encoder -> encode(entry, encoder)).immutable();
	}

	public static void encode(Entry entry, ByteWriter<?> w) {
		if (entry.isAddress()) w.writeByte(Data.encode(entry.house, entry.unit));
		else if (entry.isGroup(FunctionGroup.ext))
			w.writeBytes(Data.encode(entry.house, entry.type), entry.data, entry.command);
		else if (entry.isGroup(FunctionGroup.dim))
			w.writeBytes(Data.encode(entry.house, entry.type), fromDimPercent(entry.data));
		else w.writeByte(Data.encode(entry.house, entry.type));
	}

	private static int fromDimPercent(int percent) {
		return limit(roundDiv(percent * DIM_MAX, DIM_MAX_PERCENT), 0, DIM_MAX);
	}

}
