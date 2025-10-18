package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertFind;
import org.junit.Test;
import ceri.common.array.ArrayUtil;

public class VolatileArrayBehavior {

	@Test
	public void shouldCreateWithGivenSize() {
		assertArray(VolatileArray.BYTES.of(4).copy(0), 0, 0, 0, 0);
		assertArray(VolatileArray.INTS.of(4).copy(0), 0, 0, 0, 0);
		assertArray(VolatileArray.LONGS.of(4).copy(0), 0, 0, 0, 0);
	}

	@Test
	public void shouldCopyArray() {
		byte[] b = ArrayUtil.bytes.of(0xff, 0x80, 0x7f);
		var a = VolatileArray.BYTES.copyOf(b);
		a.setByte(2, 0);
		assertArray(b, 0xff, 0x80, 0x7f);
		assertArray(a.copy(0), 0xff, 0x80, 0);
	}

	@Test
	public void shouldWrapArray() {
		long[] l = ArrayUtil.longs.of(-1, 0x80, 0x7f);
		var a = VolatileArray.LONGS.wrap(l);
		a.setLong(2, 0);
		assertArray(l, -1, 0x80, 0);
		assertArray(a.copy(0), -1, 0x80, 0);
	}

	@Test
	public void shouldSliceArray() {
		var a0 = ints(1, 2, 3, 4, 5);
		var a1 = a0.slice(2);
		var a2 = a0.slice(0, 3);
		var a3 = a2.slice(2);
		a0.setInt(2, -1);
		assertArray(a0.copy(0), 1, 2, -1, 4, 5);
		assertArray(a1.copy(0), -1, 4, 5);
		assertArray(a2.copy(0), 1, 2, -1);
		assertArray(a3.copy(0), -1);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(bytes(-1, 0x80, 0x7f).toString(), "[0xff, 0x80, 0x7f]");
		assertFind(ints(-1, 0x80, 0x7f).toString(), "[-1, 128, 127]");
		assertFind(longs(-1, 0x80, 0x7f).toString(), "[-1, 128, 127]");
	}

	private static VolatileArray.Bytes bytes(int... values) {
		return VolatileArray.BYTES.wrap(ArrayUtil.bytes.of(values));
	}

	private static VolatileArray.Ints ints(int... values) {
		return VolatileArray.INTS.wrap(values);
	}

	private static VolatileArray.Longs longs(long... values) {
		return VolatileArray.LONGS.wrap(values);
	}
}
