package ceri.x10.cm11a.protocol;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.x10.command.FunctionType.dim;
import static ceri.x10.command.FunctionType.off;
import static ceri.x10.command.FunctionType.on;
import static ceri.x10.command.House.B;
import static ceri.x10.command.House.C;
import static ceri.x10.command.House.E;
import static ceri.x10.command.Unit._1;
import static ceri.x10.command.Unit._2;
import static ceri.x10.command.Unit._3;
import static ceri.x10.command.Unit._4;
import static ceri.x10.command.Unit._5;
import static ceri.x10.command.Unit._6;
import static ceri.x10.command.Unit._7;
import static ceri.x10.command.Unit._8;
import org.junit.Test;
import ceri.x10.command.Command;

public class EntryBufferBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		EntryBuffer t = EntryBuffer.of(Entry.address(E, _8), Entry.function(E, off));
		EntryBuffer eq0 = EntryBuffer.of(Entry.address(E, _8), Entry.function(E, off));
		EntryBuffer ne0 = EntryBuffer.of(Entry.address(E, _7), Entry.function(E, off));
		EntryBuffer ne1 = EntryBuffer.of(Entry.address(E, _8), Entry.function(E, on));
		EntryBuffer ne2 = EntryBuffer.of(Entry.address(E, _8));
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateBuffersFromCommand() {
		var buffers = EntryBuffer.allFrom(Command.dim(B, 99, _1, _2, _3));
		assertIterable(buffers, EntryBuffer.of(Entry.address(B, _1), Entry.address(B, _2),
			Entry.address(B, _3), Entry.dim(B, dim, 99)));
	}

	@Test
	public void shouldStartNewBufferIfSizeExceeded() {
		var buffers = EntryBuffer.allFrom(Command.dim(B, 99, _1, _2, _3, _4, _5, _6, _7));
		assertIterable(buffers,
			EntryBuffer.of(Entry.address(B, _1), Entry.address(B, _2), Entry.address(B, _3),
				Entry.address(B, _4), Entry.address(B, _5), Entry.address(B, _6),
				Entry.address(B, _7)),
			EntryBuffer.of(Entry.dim(B, dim, 99)));
	}

	@Test
	public void shouldNotCreateEmptyBuffer() {
		var buffers = EntryBuffer.allFrom(Command.on(B));
		assertIterable(buffers);
	}

	@Test
	public void shouldCombineBuffers() {
		EntryBuffer buffer0 = EntryBuffer.of(Entry.address(B, _1), Entry.dim(B, dim, 99));
		EntryBuffer buffer1 =
			EntryBuffer.of(Entry.address(C, _2), Entry.address(C, _2), Entry.function(C, off));
		var entries = EntryBuffer.combine(buffer0, buffer1);
		assertIterable(entries, Entry.address(B, _1), Entry.dim(B, dim, 99), Entry.address(C, _2),
			Entry.address(C, _2), Entry.function(C, off));
	}

	@Test
	public void shouldEncodeBuffer() {
		EntryBuffer buffer =
			EntryBuffer.of(Entry.address(B, _1), Entry.address(B, _5), Entry.dim(B, dim, 99));
		assertArray(buffer.encode(), 5, 0x4, 0xe6, 0xe1, 0xe4, 0xd0);
	}

}
