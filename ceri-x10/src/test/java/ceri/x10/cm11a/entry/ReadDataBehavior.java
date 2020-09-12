package ceri.x10.cm11a.entry;

import static ceri.common.test.TestUtil.assertArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteReader;
import ceri.x10.command.Address;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.type.DimFunction;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.Function;

public class ReadDataBehavior {
	private final ReadData read = new ReadData();

	@Test
	public void shouldReadExtFunctionFromInput() {
		assertThat(read.readExtFunctionFrom(in(0x67, 0x7f, 0x80)),
			is(ExtFunction.of(House.A, 0x7f, 0x80)));
	}

	@Test
	public void shouldWriteDimFunctionToOutput() {
		byte[] bytes = ByteArray.Encoder.of()
			.apply(w -> read.writeDimFunctionTo(DimFunction.bright(House.A, 100), w)).bytes();
		assertArray(bytes, 0x65, 0xd2);
	}

	@Test
	public void shouldReadDimFunctionFromInput() {
		assertThat(read.readDimFunctionFrom(in(0x64, 0x69)), is(DimFunction.dim(House.A, 50)));
	}

	@Test
	public void shouldReadFunctionFromInput() {
		assertThat(read.readFunctionFrom(in(0x66)),
			is(Function.of(House.A, FunctionType.allLightsOff)));
	}

	@Test
	public void shouldReadAddressFromInput() {
		assertThat(read.readAddressFrom(in(0x66)), is(Address.from("A1")));
	}

	private ByteReader in(int... bytes) {
		return Immutable.wrap(bytes).reader(0);
	}

}
