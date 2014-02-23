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
import ceri.x10.type.Address;
import ceri.x10.type.DimFunction;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.Function;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;

public class ReadDataBehavior {
	private final ReadData read = new ReadData();

	@Test
	public void shouldReadExtFunctionFromInput() throws IOException {
		assertThat(read.readExtFunctionFrom(in(0x67, 0x7f, 0x80)), is(new ExtFunction(House.A,
			(byte)0x7f, (byte)0x80)));
	}

	@Test
	public void shouldWriteDimFunctionToOutput() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(out);
		read.writeDimFunctionTo(DimFunction.bright(House.A, 100), data);
		assertArrayEquals(out.toByteArray(), new byte[] { 0x65, (byte)0xd2 }); 
	}

	@Test
	public void shouldReadDimFunctionFromInput() throws IOException {
		assertThat(read.readDimFunctionFrom(in(0x64, 0x69)), is(DimFunction.dim(House.A, 50)));
	}

	@Test
	public void shouldReadFunctionFromInput() throws IOException {
		assertThat(read.readFunctionFrom(in(0x66)), is(new Function(House.A,
			FunctionType.ALL_LIGHTS_OFF)));
	}

	@Test
	public void shouldReadAddressFromInput() throws IOException {
		assertThat(read.readAddressFrom(in(0x66)), is(Address.fromString("A1")));
	}

	private DataInput in(int... bytes) {
		byte[] b = new byte[bytes.length];
		for (int i = 0; i < b.length; i++)
			b[i] = (byte) bytes[i];
		return new DataInputStream(new ByteArrayInputStream(b));
	}

}
