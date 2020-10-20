package ceri.x10.cm11a.protocol;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.reader;
import static ceri.x10.command.FunctionType.allLightsOff;
import static ceri.x10.command.FunctionType.bright;
import static ceri.x10.command.FunctionType.off;
import static ceri.x10.command.House.A;
import static ceri.x10.command.House.C;
import static ceri.x10.command.House.E;
import static ceri.x10.command.House.G;
import static ceri.x10.command.Unit._1;
import static ceri.x10.command.Unit._3;
import org.junit.Test;

public class ReceiveTest {

	@Test
	public void testEntrySize() {
		assertEquals(Receive.size(Entry.address(C, _3)), 1);
		assertEquals(Receive.size(Entry.function(C, off)), 1);
		assertEquals(Receive.size(Entry.dim(C, bright, 33)), 2);
		assertEquals(Receive.size(Entry.ext(C, 33, 55)), 3);
	}

	@Test
	public void testDecodeEntry() {
		assertEquals(Receive.decode(false, reader(0x66)), Entry.address(A, _1));
		assertEquals(Receive.decode(true, reader(0x66)), Entry.function(A, allLightsOff));
		assertEquals(Receive.decode(true, reader(0x23)), Entry.function(C, off));
		assertEquals(Receive.decode(true, reader(0x15, 0xd0)), Entry.dim(E, bright, 99));
		assertEquals(Receive.decode(true, reader(0x57, 66, 22)), Entry.ext(G, 66, 22));
		// assertEquals(Receive.decode(false, reader()), Entry.address(House.A, null));
	}

	@Test
	public void testEncodeEntry() {
		assertArray(Receive.encode(Entry.address(A, _1)), 0x66);
		assertArray(Receive.encode(Entry.function(A, allLightsOff)), 0x66);
		assertArray(Receive.encode(Entry.function(C, off)), 0x23);
		assertArray(Receive.encode(Entry.dim(E, bright, 99)), 0x15, 0xd0);
		assertArray(Receive.encode(Entry.ext(G, 66, 22)), 0x57, 66, 22);
		// assertEquals(Receive.(), );
	}

}
