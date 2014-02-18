package ceri.x10.cm11a.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
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

	public Address readAddressFrom(DataInput in) throws IOException {
		byte b = in.readByte();
		if (b != ADDRESS_PREFIX) throw new IllegalArgumentException("Header does not match 0x" +
			Integer.toHexString(ADDRESS_PREFIX) + ": 0x" + Integer.toHexString(b));
		return Data.toAddress(in.readByte());
	}

	public void writeAddressTo(Address address, DataOutput out) throws IOException {
		out.writeByte(ADDRESS_PREFIX);
		out.writeByte(Data.fromAddress(address));
	}

	public Function readFunctionFrom(DataInput in) throws IOException {
		byte b = in.readByte();
		if (b != FUNCTION_PREFIX) throw new IllegalArgumentException("Header does not match 0x" +
			Integer.toHexString(FUNCTION_PREFIX) + ": 0x" + Integer.toHexString(b));
		b = in.readByte();
		House house = Data.toHouse(b >> 4 & 0xf);
		FunctionType type = Data.toFunctionType(b & 0xf);
		return new Function(house, type);
	}

	private void writeBaseFunctionTo(BaseFunction function, DataOutput out) throws IOException {
		int house = Data.fromHouse(function.house);
		int type = Data.fromFunctionType(function.type);
		out.writeByte(house << 4 | type);
	}

	public void writeFunctionTo(Function function, DataOutput out) throws IOException {
		out.writeByte(FUNCTION_PREFIX);
		writeBaseFunctionTo(function, out);
	}

	public DimFunction readDimFunctionFrom(DataInput in) throws IOException {
		byte b = in.readByte();
		int header = b & FUNCTION_PREFIX_MASK;
		if (header != FUNCTION_PREFIX) throw new IllegalArgumentException(
			"Header does not match 0x" + Integer.toHexString(FUNCTION_PREFIX) + ": 0x" +
				Integer.toHexString(header));
		int percent = toDim(b >> 3);
		b = in.readByte();
		House house = Data.toHouse(b >> 4 & 0xf);
		FunctionType type = Data.toFunctionType(b & 0xf);
		return new DimFunction(house, type, percent);
	}

	public void writeDimFunctionTo(DimFunction function, DataOutput out) throws IOException {
		int header = fromDim(function.percent) << 3 | FUNCTION_PREFIX;
		out.writeByte(header);
		writeBaseFunctionTo(function, out);
	}

	public int toDim(int data) {
		double percent = (double) (data) * 100 / DIM_MAX;
		return (int) percent;
	}

	public int fromDim(int percent) {
		double data = (double) percent * DIM_MAX / 100;
		return (int) data;
	}

	public ExtFunction readExtFunctionFrom(DataInput in) throws IOException {
		byte b = in.readByte();
		if (b != EXT_FUNCTION_PREFIX) throw new IllegalArgumentException(
			"Header does not match 0x" + Integer.toHexString(EXT_FUNCTION_PREFIX) + ": 0x" +
				Integer.toHexString(b));
		b = in.readByte();
		House house = Data.toHouse(b >> 4 & 0xf);
		FunctionType type = Data.toFunctionType(b & 0xf);
		if (!ExtFunction.isAllowed(type)) throw new IllegalArgumentException(
			"Type is not extended: " + type);
		byte data = in.readByte();
		byte command = in.readByte();
		return new ExtFunction(house, data, command);
	}

	public void writeExtFunctionTo(ExtFunction function, DataOutput out) throws IOException {
		out.writeByte(EXT_FUNCTION_PREFIX);
		writeBaseFunctionTo(function, out);
		out.writeByte(function.data);
		out.writeByte(function.command);
	}

	public Entry readEntryFrom(DataInput in) throws IOException {
		byte header = in.readByte();
		if (header == ADDRESS_PREFIX) return new Entry(Data.toAddress(in.readByte()));
		byte b = in.readByte();
		House house = Data.toHouse(b >> 4);
		FunctionType type = Data.toFunctionType(b);
		if (DimFunction.isAllowed(type)) {
			int percent = Data.write.toDim(header >> 3);
			return new Entry(new DimFunction(house, type, percent));
		}
		if (ExtFunction.isAllowed(type)) {
			byte data = in.readByte();
			byte command = in.readByte();
			return new Entry(new ExtFunction(house, data, command));
		}
		return new Entry(new Function(house, type));
	}

	public byte[] fromEntry(Entry entry) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dOut = new DataOutputStream(out);
			writeEntryTo(entry, dOut);
			dOut.flush();
			return out.toByteArray();
		} catch (IOException e) {
			throw new AssertionError("Not possible", e);
		}
	}

	public void writeEntryTo(Entry entry, DataOutput out) throws IOException {
		switch (entry.type) {
		case address:
			writeAddressTo(entry.asAddress(), out);
			break;
		case function:
			writeFunctionTo(entry.asFunction(), out);
			break;
		case dim:
			writeDimFunctionTo(entry.asDimFunction(), out);
			break;
		case ext:
			writeExtFunctionTo(entry.asExtFunction(), out);
			break;
		}
	}

}
