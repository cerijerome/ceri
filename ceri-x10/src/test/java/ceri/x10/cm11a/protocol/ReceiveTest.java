package ceri.x10.cm11a.protocol;

import static ceri.common.test.TestUtil.assertArray;
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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ReceiveTest {

	@Test
	public void testEntrySize() {
		assertThat(Receive.size(Entry.address(C, _3)), is(1));
		assertThat(Receive.size(Entry.function(C, off)), is(1));
		assertThat(Receive.size(Entry.dim(C, bright, 33)), is(2));
		assertThat(Receive.size(Entry.ext(C, 33, 55)), is(3));
	}

	@Test
	public void testDecodeEntry() {
		assertThat(Receive.decode(false, reader(0x66)), is(Entry.address(A, _1)));
		assertThat(Receive.decode(true, reader(0x66)), is(Entry.function(A, allLightsOff)));
		assertThat(Receive.decode(true, reader(0x23)), is(Entry.function(C, off)));
		assertThat(Receive.decode(true, reader(0x15, 0xd0)), is(Entry.dim(E, bright, 99)));
		assertThat(Receive.decode(true, reader(0x57, 66, 22)), is(Entry.ext(G, 66, 22)));
		// assertThat(Receive.decode(false, reader()), is(Entry.address(House.A, null)));
	}

	@Test
	public void testEncodeEntry() {
		assertArray(Receive.encode(Entry.address(A, _1)), 0x66);
		assertArray(Receive.encode(Entry.function(A, allLightsOff)), 0x66);
		assertArray(Receive.encode(Entry.function(C, off)), 0x23);
		assertArray(Receive.encode(Entry.dim(E, bright, 99)), 0x15, 0xd0);
		assertArray(Receive.encode(Entry.ext(G, 66, 22)), 0x57, 66, 22);
		// assertThat(Receive.(), is());
	}

}
