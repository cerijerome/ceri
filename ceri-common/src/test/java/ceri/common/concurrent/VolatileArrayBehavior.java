package ceri.common.concurrent;

import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.test.Assert;

public class VolatileArrayBehavior {

	@Test
	public void shouldCreateWithGivenSize() {
		Assert.array(VolatileArray.BYTES.of(4).copy(0), 0, 0, 0, 0);
		Assert.array(VolatileArray.INTS.of(4).copy(0), 0, 0, 0, 0);
		Assert.array(VolatileArray.LONGS.of(4).copy(0), 0, 0, 0, 0);
	}

	@Test
	public void shouldCopyArray() {
		byte[] b = Array.BYTE.of(0xff, 0x80, 0x7f);
		var a = VolatileArray.BYTES.copyOf(b);
		a.setByte(2, 0);
		Assert.array(b, 0xff, 0x80, 0x7f);
		Assert.array(a.copy(0), 0xff, 0x80, 0);
	}

	@Test
	public void shouldWrapArray() {
		long[] l = Array.LONG.of(-1, 0x80, 0x7f);
		var a = VolatileArray.LONGS.wrap(l);
		a.setLong(2, 0);
		Assert.array(l, -1, 0x80, 0);
		Assert.array(a.copy(0), -1, 0x80, 0);
	}

	@Test
	public void shouldSliceArray() {
		var a0 = ints(1, 2, 3, 4, 5);
		var a1 = a0.slice(2);
		var a2 = a0.slice(0, 3);
		var a3 = a2.slice(2);
		a0.setInt(2, -1);
		Assert.array(a0.copy(0), 1, 2, -1, 4, 5);
		Assert.array(a1.copy(0), -1, 4, 5);
		Assert.array(a2.copy(0), 1, 2, -1);
		Assert.array(a3.copy(0), -1);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.find(bytes(-1, 0x80, 0x7f).toString(), "[0xff, 0x80, 0x7f]");
		Assert.find(ints(-1, 0x80, 0x7f).toString(), "[-1, 128, 127]");
		Assert.find(longs(-1, 0x80, 0x7f).toString(), "[-1, 128, 127]");
	}

	private static VolatileArray.Bytes bytes(int... values) {
		return VolatileArray.BYTES.wrap(Array.BYTE.of(values));
	}

	private static VolatileArray.Ints ints(int... values) {
		return VolatileArray.INTS.wrap(values);
	}

	private static VolatileArray.Longs longs(long... values) {
		return VolatileArray.LONGS.wrap(values);
	}
}
