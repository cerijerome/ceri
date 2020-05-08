package ceri.x10.cm11a.protocol;

import static ceri.common.validation.ValidationUtil.validateEqualL;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteWriter;
import ceri.x10.cm11a.Entry;
import ceri.x10.type.Address;
import ceri.x10.type.BaseFunction;
import ceri.x10.type.DimFunction;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.Function;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;

/**
 * Methods for converting between types and byte arrays for sending to the CM11A controller.
 */
public class WriteData {
	private static final byte ADDRESS_PREFIX = 0x4;
	private static final byte FUNCTION_PREFIX_MASK = 0x7;
	private static final byte FUNCTION_PREFIX = 0x6;
	private static final byte EXT_FUNCTION_PREFIX = 0x7;
	private static final int DIM_MAX = 22;

	WriteData() {}

	public Address readAddressFrom(ByteReader r) {
		validateEqualL(r.readByte(), ADDRESS_PREFIX, "Header");
		return Data.toAddress(r.readByte());
	}

	public void writeAddressTo(Address address, ByteWriter<?> w) {
		w.writeBytes(ADDRESS_PREFIX, Data.fromAddress(address));
	}

	public Function readFunctionFrom(ByteReader r) {
		validateEqualL(r.readByte(), FUNCTION_PREFIX, "Header");
		byte b = r.readByte();
		House house = Data.toHouse(b >> 4 & 0xf);
		FunctionType type = Data.toFunctionType(b & 0xf);
		return new Function(house, type);
	}

	public void writeFunctionTo(Function function, ByteWriter<?> w) {
		w.writeByte(FUNCTION_PREFIX);
		writeBaseFunctionTo(function, w);
	}

	public DimFunction readDimFunctionFrom(ByteReader r) {
		byte b = r.readByte();
		validateEqualL(b & FUNCTION_PREFIX_MASK, FUNCTION_PREFIX, "Header");
		int percent = toDim(b >> 3);
		b = r.readByte();
		House house = Data.toHouse(b >> 4 & 0xf);
		FunctionType type = Data.toFunctionType(b & 0xf);
		return new DimFunction(house, type, percent);
	}

	public void writeDimFunctionTo(DimFunction function, ByteWriter<?> w) {
		int header = fromDim(function.percent) << 3 | FUNCTION_PREFIX;
		w.writeByte(header);
		writeBaseFunctionTo(function, w);
	}

	public int toDim(int data) {
		double percent = (double) (data) * 100 / DIM_MAX;
		return (int) percent;
	}

	public int fromDim(int percent) {
		double data = (double) percent * DIM_MAX / 100;
		return (int) data;
	}

	public ExtFunction readExtFunctionFrom(ByteReader r) {
		validateEqualL(r.readByte(), EXT_FUNCTION_PREFIX, "Header");
		byte b = r.readByte();
		House house = Data.toHouse(b >> 4 & 0xf);
		FunctionType type = Data.toFunctionType(b & 0xf);
		if (!ExtFunction.isAllowed(type))
			throw new IllegalArgumentException("Type is not extended: " + type);
		byte data = r.readByte();
		byte command = r.readByte();
		return new ExtFunction(house, data, command);
	}

	public void writeExtFunctionTo(ExtFunction function, ByteWriter<?> w) {
		w.writeByte(EXT_FUNCTION_PREFIX);
		writeBaseFunctionTo(function, w);
		w.writeByte(function.data);
		w.writeByte(function.command);
	}

	public Entry readEntryFrom(ByteReader r) {
		byte header = r.readByte();
		if (header == ADDRESS_PREFIX) return new Entry(Data.toAddress(r.readByte()));
		byte b = r.readByte();
		House house = Data.toHouse(b >> 4);
		FunctionType type = Data.toFunctionType(b);
		if (DimFunction.isAllowed(type)) {
			int percent = toDim(header >> 3);
			return new Entry(new DimFunction(house, type, percent));
		}
		if (ExtFunction.isAllowed(type)) {
			byte data = r.readByte();
			byte command = r.readByte();
			return new Entry(new ExtFunction(house, data, command));
		}
		return new Entry(new Function(house, type));
	}

	public ByteProvider fromEntry(Entry entry) {
		return ByteArray.encoder().apply(w -> writeEntryTo(entry, w)).immutable();
	}

	public void writeEntryTo(Entry entry, ByteWriter<?> w) {
		switch (entry.type) {
		case address:
			writeAddressTo(entry.asAddress(), w);
			break;
		case function:
			writeFunctionTo(entry.asFunction(), w);
			break;
		case dim:
			writeDimFunctionTo(entry.asDimFunction(), w);
			break;
		case ext:
			writeExtFunctionTo(entry.asExtFunction(), w);
			break;
		}
	}

	private void writeBaseFunctionTo(BaseFunction function, ByteWriter<?> w) {
		int house = Data.fromHouse(function.house);
		int type = Data.fromFunctionType(function.type);
		w.writeByte(house << 4 | type);
	}

}
