package ceri.x10.cm17a.device;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertPrivateConstructor;
import org.junit.Test;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

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
		assertEquals(Data.code(House.A, Unit._1, FunctionType.off), 0x6020);
		assertEquals(Data.code(House.J, Unit._16, FunctionType.on), 0xf458);
		assertEquals(Data.code(House.M, Unit._1, FunctionType.on), 0x0000);
	}

	@Test
	public void testCodeDim() {
		assertEquals(Data.code(House.B, FunctionType.dim), 0x7098);
		assertEquals(Data.code(House.H, FunctionType.bright), 0xb088);
		assertEquals(Data.code(House.M, FunctionType.dim), 0x0098);
	}
}
