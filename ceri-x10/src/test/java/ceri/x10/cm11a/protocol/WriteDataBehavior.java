package ceri.x10.cm11a.protocol;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteReader;
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
	public void shouldReadExtFunctionEntryFromInput() {
		assertThat(write.readEntryFrom(in(0x07, 0x67, 0x7f, 0x80)),
			is(new Entry(new ExtFunction(House.A, (byte) 0x7f, (byte) 0x80))));
	}

	@Test
	public void shouldReadDimFunctionEntryFromInput() {
		assertThat(write.readEntryFrom(in(0x5e, 0x65)),
			is(new Entry(DimFunction.bright(House.A, 50))));
	}

	@Test
	public void shouldReadFunctionEntryFromInput() {
		assertThat(write.readEntryFrom(in(0x06, 0x66)),
			is(new Entry(new Function(House.A, FunctionType.ALL_LIGHTS_OFF))));
	}

	@Test
	public void shouldReadAddressEntryFromInput() {
		assertThat(write.readEntryFrom(in(0x04, 0x66)), is(new Entry(Address.fromString("A1"))));
	}

	@Test
	public void shouldWriteExtFunctionToOutput() {
		byte[] bytes = ByteArray.encoder().apply(w -> write.writeExtFunctionTo( //
			new ExtFunction(House.A, (byte) 0x7f, (byte) 0x80), w)).bytes();
		assertArray(bytes, 0x07, 0x67, 0x7f, 0x80);
	}

	@Test
	public void shouldReadExtFunctionFromInput() {
		assertThat(write.readExtFunctionFrom(in(0x07, 0x67, 0x7f, 0x80)),
			is(new ExtFunction(House.A, (byte) 0x7f, (byte) 0x80)));
	}

	@Test
	public void shouldWriteDimFunctionToOutput() {
		byte[] bytes = ByteArray.encoder()
			.apply(w -> write.writeDimFunctionTo(DimFunction.bright(House.A, 50), w)).bytes();
		assertArray(bytes, 0x5e, 0x65);
	}

	@Test
	public void shouldReadDimFunctionFromInput() {
		assertThat(write.readDimFunctionFrom(in(0x5e, 0x64)), is(DimFunction.dim(House.A, 50)));
	}

	@Test
	public void shouldReadFunctionFromInput() {
		assertThat(write.readFunctionFrom(in(0x06, 0x66)),
			is(new Function(House.A, FunctionType.ALL_LIGHTS_OFF)));
	}

	@Test
	public void shouldReadAddressFromInput() {
		assertThat(write.readAddressFrom(in(0x04, 0x66)), is(Address.fromString("A1")));
	}

	private ByteReader in(int... bytes) {
		return Immutable.wrap(bytes).reader(0);
	}

}
