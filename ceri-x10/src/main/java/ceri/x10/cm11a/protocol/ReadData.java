package ceri.x10.cm11a.protocol;

import ceri.common.data.ByteReader;
import ceri.common.data.ByteWriter;
import ceri.common.math.MathUtil;
import ceri.x10.cm11a.device.Entry;
import ceri.x10.type.Address;
import ceri.x10.type.BaseFunction;
import ceri.x10.type.DimFunction;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.Function;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;

/**
 * Methods for converting between types and byte arrays for reading from the CM11A controller.
 */
public class ReadData {
	private static final int DIM_MAX = 210;

	ReadData() {}

	public Address readAddressFrom(ByteReader r) {
		return Data.toAddress(r.readUbyte());
	}

	private void writeAddressTo(Address address, ByteWriter<?> w) {
		w.writeByte(Data.fromAddress(address));
	}

	public Function readFunctionFrom(ByteReader r) {
		int b = r.readUbyte();
		House house = Data.toHouse((b >> 4) & 0xf);
		FunctionType type = Data.toFunctionType(b & 0xf);
		return Function.of(house, type);
	}

	private void writeBaseFunctionTo(BaseFunction function, ByteWriter<?> w) {
		int house = Data.fromHouse(function.house);
		int type = Data.fromFunctionType(function.type);
		w.writeByte(house << 4 | type);
	}

	public void writeFunctionTo(Function function, ByteWriter<?> w) {
		writeBaseFunctionTo(function, w);
	}

	public DimFunction readDimFunctionFrom(ByteReader r) {
		int b = r.readUbyte();
		House house = Data.toHouse((b >> 4) & 0xf);
		FunctionType type = Data.toFunctionType(b & 0xf);
		int dim = r.readUbyte();
		return new DimFunction(house, type, toDim(dim));
	}

	public void writeDimFunctionTo(DimFunction function, ByteWriter<?> w) {
		writeBaseFunctionTo(function, w);
		w.writeByte(fromDim(function.percent));
	}

	public int toDim(int data) {
		double percent = (double) MathUtil.ubyte(data) * 100 / DIM_MAX;
		return (int) percent;
	}

	public int fromDim(int percent) {
		double data = (double) percent * DIM_MAX / 100;
		return (int) data;
	}

	public ExtFunction readExtFunctionFrom(ByteReader r) {
		int b = r.readUbyte();
		House house = Data.toHouse((b >> 4) & 0xf);
		FunctionType type = Data.toFunctionType(b & 0xf);
		if (type != FunctionType.extended)
			throw new IllegalArgumentException("Function type is not extended: " + type);
		return Data.decodeExtFunction(house, r);
	}

	public void writeExtFunctionTo(ExtFunction function, ByteWriter<?> w) {
		writeBaseFunctionTo(function, w);
		w.writeByte(function.data);
		w.writeByte(function.command);
	}

	public int sizeInBytes(Entry entry) {
		switch (entry.type) {
		case address:
		case function:
			return 1;
		case dim:
			return 2;
		case ext:
			return 3;
		default:
			throw new IllegalArgumentException("Unknown type: " + entry.type);
		}
	}

	public Entry readEntryFrom(boolean isFunction, ByteReader r) {
		int b = r.readUbyte();
		if (!isFunction) return Entry.of(Data.toAddress(b));
		House house = Data.toHouse(b >> 4);
		FunctionType type = Data.toFunctionType(b);
		if (DimFunction.isAllowed(type)) {
			int percent = Data.read.toDim(r.readUbyte());
			return Entry.of(new DimFunction(house, type, percent));
		}
		if (ExtFunction.isAllowed(type))
			return Entry.of(Data.decodeExtFunction(house, r));
		return Entry.of(Function.of(house, type));
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

}
