package ceri.x10.cm17a.device;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class DataTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Data.class);
	}

	@Test
	public void testDimCount() {
		Assert.equal(Data.toDimCount(0), 0);
		Assert.equal(Data.toDimCount(1), 1);
		Assert.equal(Data.toDimCount(3), 1);
		Assert.equal(Data.toDimCount(7), 1);
		Assert.equal(Data.toDimCount(8), 2);
		Assert.equal(Data.toDimCount(12), 2);
		Assert.equal(Data.toDimCount(200), 20);
	}

	@Test
	public void testFromDimCount() {
		Assert.equal(Data.fromDimCount(0), 0);
		Assert.equal(Data.fromDimCount(1), 5);
		Assert.equal(Data.fromDimCount(25), 100);
	}

	@Test
	public void testCodeOnOff() {
		Assert.equal(Data.code(null, null, null), 0);
		Assert.equal(Data.code(House.A, Unit._1, FunctionType.off), 0x6020);
		Assert.equal(Data.code(House.J, Unit._16, FunctionType.on), 0xf458);
		Assert.equal(Data.code(House.M, Unit._1, FunctionType.on), 0x0000);
	}

	@Test
	public void testCodeDim() {
		Assert.equal(Data.code(House.B, FunctionType.dim), 0x7098);
		Assert.equal(Data.code(House.H, FunctionType.bright), 0xb088);
		Assert.equal(Data.code(House.M, FunctionType.dim), 0x0098);
	}
}
