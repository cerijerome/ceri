package ceri.x10.cm17a.device;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
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
import org.junit.Test;

public class DataTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Data.class);
	}

	@Test
	public void testDimCount() {
		assertEquals(Data.toDimCount(0), 0);
		assertEquals(Data.toDimCount(1), 1);
		assertEquals(Data.toDimCount(3), 1);
		assertEquals(Data.toDimCount(7), 1);
		assertEquals(Data.toDimCount(8), 2);
		assertEquals(Data.toDimCount(12), 2);
		assertEquals(Data.toDimCount(200), 20);
	}

	@Test
	public void testFromDimCount() {
		assertEquals(Data.fromDimCount(0), 0);
		assertEquals(Data.fromDimCount(1), 5);
		assertEquals(Data.fromDimCount(25), 100);
	}

	@Test
	public void testCodeOnOff() {
		assertEquals(Data.code(null, null, null), 0);
		assertEquals(Data.code(A, _1, off), 0x6020);
		assertEquals(Data.code(J, _16, on), 0xf458);
		assertEquals(Data.code(M, _1, on), 0x0000);
	}

	@Test
	public void testCodeDim() {
		assertEquals(Data.code(B, dim), 0x7098);
		assertEquals(Data.code(H, bright), 0xb088);
		assertEquals(Data.code(M, dim), 0x0098);
	}

}
