package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.LongArray;
import ceri.common.data.LongProvider;

public class VolatileLongArrayBehavior {

	@Test
	public void shouldCreateWithGivenSize() {
		VolatileLongArray a = VolatileLongArray.of(4);
		assertArray(a.copy(0), 0, 0, 0, 0);
	}

	@Test
	public void shouldCreateFromLongProvider() {
		LongProvider b = LongArray.Immutable.wrap(1, 2, 3);
		VolatileLongArray a = VolatileLongArray.wrap(b.copy(0));
		a.setLong(1, 0);
		assertArray(a.copy(0), 1, 0, 3);
		assertArray(b.copy(0), 1, 2, 3);
	}

	@Test
	public void shouldCreateACopyFromLongArray() {
		long[] b = ArrayUtil.longs(0xff, 0x80, 0x7f);
		VolatileLongArray a = VolatileLongArray.copyOf(ArrayUtil.longs(0xff, 0x80, 0x7f));
		a.setLong(2, 0);
		assertArray(b, 0xff, 0x80, 0x7f);
		assertArray(a.copy(0), 0xff, 0x80, 0);
	}

	@Test
	public void shouldCreateByWrappingLongs() {
		long[] b = ArrayUtil.longs(0xff, 0x80, 0x7f);
		VolatileLongArray a = VolatileLongArray.wrap(b);
		a.setLong(2, 0);
		assertArray(b, 0xff, 0x80, 0);
		assertArray(a.copy(0), 0xff, 0x80, 0);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertTrue(VolatileLongArray.of(0).isEmpty());
		assertTrue(VolatileLongArray.wrap().isEmpty());
		assertFalse(VolatileLongArray.wrap(1).isEmpty());
	}

	@Test
	public void shouldCreateSlicesOfTheArray() {
		VolatileLongArray a0 = VolatileLongArray.wrap(1, 2, 3, 4, 5);
		VolatileLongArray a1 = a0.slice(2);
		VolatileLongArray a2 = a0.slice(0, 3);
		VolatileLongArray a3 = a2.slice(2);
		a0.setLong(2, 0xff);
		assertArray(a0.copy(0), 1, 2, 0xff, 4, 5);
		assertArray(a1.copy(0), 0xff, 4, 5);
		assertArray(a2.copy(0), 1, 2, 0xff);
		assertArray(a3.copy(0), 0xff);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(VolatileLongArray.wrap(-1, 0x80, 0x7f).toString(), "[0xff, 0x80, 0x7f]");
	}

}
