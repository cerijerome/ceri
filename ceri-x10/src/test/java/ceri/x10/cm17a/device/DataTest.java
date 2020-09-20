package ceri.x10.cm17a.device;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.x10.command.FunctionType.bright;
import static ceri.x10.command.FunctionType.dim;
import static ceri.x10.command.FunctionType.off;
import static ceri.x10.command.FunctionType.on;
import static ceri.x10.command.House.A;
import static ceri.x10.command.House.B;
import static ceri.x10.command.House.H;
import static ceri.x10.command.House.J;
import static ceri.x10.command.House.M;
import static ceri.x10.command.Unit._1;
import static ceri.x10.command.Unit._16;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class DataTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Data.class);
	}

	@Test
	public void testDimCount() {
		assertThat(Data.toDimCount(0), is(0));
		assertThat(Data.toDimCount(1), is(1));
		assertThat(Data.toDimCount(3), is(1));
		assertThat(Data.toDimCount(7), is(1));
		assertThat(Data.toDimCount(8), is(2));
		assertThat(Data.toDimCount(12), is(2));
		assertThat(Data.toDimCount(200), is(20));
	}

	@Test
	public void testFromDimCount() {
		assertThat(Data.fromDimCount(0), is(0));
		assertThat(Data.fromDimCount(1), is(5));
		assertThat(Data.fromDimCount(25), is(100));
	}

	@Test
	public void testCodeOnOff() {
		assertThat(Data.code(null, null, null), is(0));
		assertThat(Data.code(A, _1, off), is(0x6020));
		assertThat(Data.code(J, _16, on), is(0xf458));
		assertThat(Data.code(M, _1, on), is(0x0000));
	}

	@Test
	public void testCodeDim() {
		assertThat(Data.code(B, dim), is(0x7098));
		assertThat(Data.code(H, bright), is(0xb088));
		assertThat(Data.code(M, dim), is(0x0098));
	}

}
