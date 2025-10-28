package ceri.x10.cm11a.protocol;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.x10.command.Command;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class EntryBufferBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = EntryBuffer.of(Entry.address(House.E, Unit._8),
			Entry.function(House.E, FunctionType.off));
		var eq0 = EntryBuffer.of(Entry.address(House.E, Unit._8),
			Entry.function(House.E, FunctionType.off));
		var ne0 = EntryBuffer.of(Entry.address(House.E, Unit._7),
			Entry.function(House.E, FunctionType.off));
		var ne1 = EntryBuffer.of(Entry.address(House.E, Unit._8),
			Entry.function(House.E, FunctionType.on));
		var ne2 = EntryBuffer.of(Entry.address(House.E, Unit._8));
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateBuffersFromCommand() {
		var buffers = EntryBuffer.allFrom(Command.dim(House.B, 99, Unit._1, Unit._2, Unit._3));
		Assert.ordered(buffers,
			EntryBuffer.of(Entry.address(House.B, Unit._1), Entry.address(House.B, Unit._2),
				Entry.address(House.B, Unit._3), Entry.dim(House.B, FunctionType.dim, 99)));
	}

	@Test
	public void shouldStartNewBufferIfSizeExceeded() {
		var buffers = EntryBuffer.allFrom(Command.dim(House.B, 99, Unit._1, Unit._2, Unit._3,
			Unit._4, Unit._5, Unit._6, Unit._7));
		Assert.ordered(buffers,
			EntryBuffer.of(Entry.address(House.B, Unit._1), Entry.address(House.B, Unit._2),
				Entry.address(House.B, Unit._3), Entry.address(House.B, Unit._4),
				Entry.address(House.B, Unit._5), Entry.address(House.B, Unit._6),
				Entry.address(House.B, Unit._7)),
			EntryBuffer.of(Entry.dim(House.B, FunctionType.dim, 99)));
	}

	@Test
	public void shouldNotCreateEmptyBuffer() {
		var buffers = EntryBuffer.allFrom(Command.on(House.B));
		Assert.ordered(buffers);
	}

	@Test
	public void shouldCombineBuffers() {
		var buffer0 = EntryBuffer.of(Entry.address(House.B, Unit._1),
			Entry.dim(House.B, FunctionType.dim, 99));
		var buffer1 = EntryBuffer.of(Entry.address(House.C, Unit._2),
			Entry.address(House.C, Unit._2), Entry.function(House.C, FunctionType.off));
		var entries = EntryBuffer.combine(buffer0, buffer1);
		Assert.ordered(entries, Entry.address(House.B, Unit._1),
			Entry.dim(House.B, FunctionType.dim, 99), Entry.address(House.C, Unit._2),
			Entry.address(House.C, Unit._2), Entry.function(House.C, FunctionType.off));
	}

	@Test
	public void shouldEncodeBuffer() {
		var buffer = EntryBuffer.of(Entry.address(House.B, Unit._1),
			Entry.address(House.B, Unit._5), Entry.dim(House.B, FunctionType.dim, 99));
		Assert.array(buffer.encode(), 5, 0x4, 0xe6, 0xe1, 0xe4, 0xd0);
	}
}
