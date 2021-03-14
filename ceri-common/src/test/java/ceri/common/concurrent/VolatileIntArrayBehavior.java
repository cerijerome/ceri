package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.IntArray;
import ceri.common.data.IntProvider;

public class VolatileIntArrayBehavior {

	@Test
	public void shouldCreateWithGivenSize() {
		VolatileIntArray a = VolatileIntArray.of(4);
		assertArray(a.copy(0), 0, 0, 0, 0);
	}

	@Test
	public void shouldCreateFromIntProvider() {
		IntProvider b = IntArray.Immutable.wrap(1, 2, 3);
		VolatileIntArray a = VolatileIntArray.wrap(b.copy(0));
		a.setInt(1, 0);
		assertArray(a.copy(0), 1, 0, 3);
		assertArray(b.copy(0), 1, 2, 3);
	}

	@Test
	public void shouldCreateACopyFromIntArray() {
		int[] b = ArrayUtil.ints(0xff, 0x80, 0x7f);
		VolatileIntArray a = VolatileIntArray.copyOf(ArrayUtil.ints(0xff, 0x80, 0x7f));
		a.setInt(2, 0);
		assertArray(b, 0xff, 0x80, 0x7f);
		assertArray(a.copy(0), 0xff, 0x80, 0);
	}

	@Test
	public void shouldCreateByWrappingInts() {
		int[] b = ArrayUtil.ints(0xff, 0x80, 0x7f);
		VolatileIntArray a = VolatileIntArray.wrap(b);
		a.setInt(2, 0);
		assertArray(b, 0xff, 0x80, 0);
		assertArray(a.copy(0), 0xff, 0x80, 0);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertTrue(VolatileIntArray.of(0).isEmpty());
		assertTrue(VolatileIntArray.wrap().isEmpty());
		assertFalse(VolatileIntArray.wrap(1).isEmpty());
	}

	@Test
	public void shouldCreateSlicesOfTheArray() {
		VolatileIntArray a0 = VolatileIntArray.wrap(1, 2, 3, 4, 5);
		VolatileIntArray a1 = a0.slice(2);
		VolatileIntArray a2 = a0.slice(0, 3);
		VolatileIntArray a3 = a2.slice(2);
		a0.setInt(2, 0xff);
		assertArray(a0.copy(0), 1, 2, 0xff, 4, 5);
		assertArray(a1.copy(0), 0xff, 4, 5);
		assertArray(a2.copy(0), 1, 2, 0xff);
		assertArray(a3.copy(0), 0xff);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(VolatileIntArray.wrap(-1, 0x80, 0x7f).toString(), "[0xff, 0x80, 0x7f]");
	}

}
