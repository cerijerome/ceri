package ceri.x10.cm11a.protocol;

import static ceri.common.data.DataUtil.expect;
import static ceri.common.validation.ValidationUtil.validateEqual;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteWriter;
import ceri.x10.cm11a.device.Entry;
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
		expect(r, ADDRESS_PREFIX);
		return Data.toAddress(r.readUbyte());
	}

	public void writeAddressTo(Address address, ByteWriter<?> w) {
		w.writeBytes(ADDRESS_PREFIX, Data.fromAddress(address));
	}

	public Function readFunctionFrom(ByteReader r) {
		expect(r, FUNCTION_PREFIX);
		int b = r.readUbyte();
		House house = Data.toHouse(b >> 4 & 0xf);
		FunctionType type = Data.toFunctionType(b & 0xf);
		return Function.of(house, type);
	}

	public void writeFunctionTo(Function function, ByteWriter<?> w) {
		w.writeByte(FUNCTION_PREFIX);
		writeBaseFunctionTo(function, w);
	}

	public DimFunction readDimFunctionFrom(ByteReader r) {
		int b = r.readUbyte();
		validateEqual(b & FUNCTION_PREFIX_MASK, FUNCTION_PREFIX);
		int percent = toDim(b >> 3);
		b = r.readUbyte();
		House house = Data.toHouse((b >> 4) & 0xf);
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
		expect(r, EXT_FUNCTION_PREFIX);
		int b = r.readUbyte();
		House house = Data.toHouse((b >> 4) & 0xf);
		FunctionType type = Data.toFunctionType(b & 0xf);
		if (!ExtFunction.isAllowed(type))
			throw new IllegalArgumentException("Type is not extended: " + type);
		return Data.decodeExtFunction(house, r);
	}

	public void writeExtFunctionTo(ExtFunction function, ByteWriter<?> w) {
		w.writeByte(EXT_FUNCTION_PREFIX);
		writeBaseFunctionTo(function, w);
		w.writeByte(function.data);
		w.writeByte(function.command);
	}

	public Entry readEntryFrom(ByteReader r) {
		int header = r.readUbyte();
		int b = r.readUbyte();
		if (header == ADDRESS_PREFIX) return Entry.of(Data.toAddress(b));
		House house = Data.toHouse(b >> 4);
		FunctionType type = Data.toFunctionType(b);
		if (DimFunction.isAllowed(type)) {
			int percent = toDim(header >> 3);
			return Entry.of(new DimFunction(house, type, percent));
		}
		if (ExtFunction.isAllowed(type)) return Entry.of(Data.decodeExtFunction(house, r));
		return Entry.of(Function.of(house, type));
	}

	public ByteProvider encode(Entry entry) {
		return ByteArray.Encoder.of().apply(w -> writeEntryTo(entry, w)).immutable();
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
