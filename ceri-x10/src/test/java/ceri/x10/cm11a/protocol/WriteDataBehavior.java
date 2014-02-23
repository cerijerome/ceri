package ceri.x10.cm11a.protocol;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.Test;
import ceri.x10.cm11a.Entry;
import ceri.x10.type.Address;
import ceri.x10.type.DimFunction;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.Function;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;

public class WriteDataBehavior {
	private final WriteData write = new WriteData();

	@Test
	public void shouldReadExtFunctionEntryFromInput() throws IOException {
		assertThat(write.readEntryFrom(in(0x07, 0x67, 0x7f, 0x80)), is(new Entry(new ExtFunction(
			House.A, (byte) 0x7f, (byte) 0x80))));
	}

	@Test
	public void shouldReadDimFunctionEntryFromInput() throws IOException {
		assertThat(write.readEntryFrom(in(0x5e, 0x65)), is(new Entry(DimFunction
			.bright(House.A, 50))));
	}

	@Test
	public void shouldReadFunctionEntryFromInput() throws IOException {
		assertThat(write.readEntryFrom(in(0x06, 0x66)), is(new Entry(new Function(House.A,
			FunctionType.ALL_LIGHTS_OFF))));
	}

	@Test
	public void shouldReadAddressEntryFromInput() throws IOException {
		assertThat(write.readEntryFrom(in(0x04, 0x66)), is(new Entry(Address.fromString("A1"))));
	}

	@Test
	public void shouldWriteExtFunctionToOutput() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(out);
		write.writeExtFunctionTo(new ExtFunction(House.A, (byte) 0x7f, (byte) 0x80), data);
		assertArrayEquals(out.toByteArray(), new byte[] { 0x07, 0x67, 0x7f, (byte) 0x80 });
	}

	@Test
	public void shouldReadExtFunctionFromInput() throws IOException {
		assertThat(write.readExtFunctionFrom(in(0x07, 0x67, 0x7f, 0x80)), is(new ExtFunction(
			House.A, (byte) 0x7f, (byte) 0x80)));
	}

	@Test
	public void shouldWriteDimFunctionToOutput() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(out);
		write.writeDimFunctionTo(DimFunction.bright(House.A, 50), data);
		assertArrayEquals(out.toByteArray(), new byte[] { 0x5e, 0x65 });
	}

	@Test
	public void shouldReadDimFunctionFromInput() throws IOException {
		assertThat(write.readDimFunctionFrom(in(0x5e, 0x64)), is(DimFunction.dim(House.A, 50)));
	}

	@Test
	public void shouldReadFunctionFromInput() throws IOException {
		assertThat(write.readFunctionFrom(in(0x06, 0x66)), is(new Function(House.A,
			FunctionType.ALL_LIGHTS_OFF)));
	}

	@Test
	public void shouldReadAddressFromInput() throws IOException {
		assertThat(write.readAddressFrom(in(0x04, 0x66)), is(Address.fromString("A1")));
	}

	private DataInput in(int... bytes) {
		byte[] b = new byte[bytes.length];
		for (int i = 0; i < b.length; i++)
			b[i] = (byte) bytes[i];
		return new DataInputStream(new ByteArrayInputStream(b));
	}

}
