package ceri.x10.cm11a.protocol;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class ReceiveTest {

	@Test
	public void testEntrySize() {
		Assert.equal(Receive.size(Entry.address(House.C, Unit._3)), 1);
		Assert.equal(Receive.size(Entry.function(House.C, FunctionType.off)), 1);
		Assert.equal(Receive.size(Entry.dim(House.C, FunctionType.bright, 33)), 2);
		Assert.equal(Receive.size(Entry.ext(House.C, 33, 55)), 3);
	}

	@Test
	public void testDecodeEntry() {
		Assert.equal(Receive.decode(false, TestUtil.reader(0x66)), Entry.address(House.A, Unit._1));
		Assert.equal(Receive.decode(true, TestUtil.reader(0x66)),
			Entry.function(House.A, FunctionType.allLightsOff));
		Assert.equal(Receive.decode(true, TestUtil.reader(0x23)),
			Entry.function(House.C, FunctionType.off));
		Assert.equal(Receive.decode(true, TestUtil.reader(0x15, 0xd0)),
			Entry.dim(House.E, FunctionType.bright, 99));
		Assert.equal(Receive.decode(true, TestUtil.reader(0x57, 66, 22)),
			Entry.ext(House.G, 66, 22));
		// assertEquals(Receive.decode(false, reader()), Entry.address(House.A, null));
	}

	@Test
	public void testEncodeEntry() {
		Assert.array(Receive.encode(Entry.address(House.A, Unit._1)), 0x66);
		Assert.array(Receive.encode(Entry.function(House.A, FunctionType.allLightsOff)), 0x66);
		Assert.array(Receive.encode(Entry.function(House.C, FunctionType.off)), 0x23);
		Assert.array(Receive.encode(Entry.dim(House.E, FunctionType.bright, 99)), 0x15, 0xd0);
		Assert.array(Receive.encode(Entry.ext(House.G, 66, 22)), 0x57, 66, 22);
		// assertEquals(Receive.(), );
	}
}
