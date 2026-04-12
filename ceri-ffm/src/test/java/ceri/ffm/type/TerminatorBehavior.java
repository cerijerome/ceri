package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.ffm.core.Memory;

public class TerminatorBehavior {
	private final Terminator TERM = Terminator.of(4);
	private final Terminator LARGE = Terminator.of(72);

	@Test
	public void shouldFindTerminator() {
		Assert.equal(TERM.find(null), -1L);
		var m = MemorySegment.ofArray(new byte[11]);
		Assert.equal(TERM.find(m), 0L);
		Memory.fill(m, 1);
		Assert.equal(TERM.find(m), -1L);
		Memory.fill(m, 8, 0);
		Assert.equal(TERM.find(m), -1L);
		Memory.fill(m, 4, 0);
		Assert.equal(TERM.find(m), 4L);
	}

	@Test
	public void shouldFindLargeTerminator() {
		Assert.equal(LARGE.find(null), -1L);
		var m = MemorySegment.ofArray(new byte[160]);
		Assert.equal(LARGE.find(m), 0L);
		Memory.fill(m, 1);
		Assert.equal(LARGE.find(m), -1L);
		Memory.fill(m, 144, 0);
		Assert.equal(LARGE.find(m), -1L);
		Memory.fill(m, 72, 72, 0);
		Assert.equal(LARGE.find(m), 72L);
	}
}
