package ceri.common.array;

import java.util.List;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.common.text.Joiner;

public class RawArrayTest {
	private final Object obj = new Object();
	private final Object[] objs = new Object[1];
	private final int[] ints = { -1, 1, 0 };

	@Test
	public void testSubTo() {
		var sub = new RawArray.Sub<>(ints, 1, 2);
		Assert.equal(sub.to(), 3);
		Assert.same(sub.array(), ints);
	}

	@Test
	public void testIsArray() {
		Assert.equal(RawArray.isArray(null), false);
		Assert.equal(RawArray.isArray(obj), false);
		Assert.equal(RawArray.isArray(objs), true);
		Assert.equal(RawArray.isArray(ints), true);
	}

	@Test
	public void testOfType() {
		Assert.equal(RawArray.ofType(null, 0), null);
		Assert.array((int[]) RawArray.ofType(int.class, 1), new int[] { 0 });
		Assert.array(RawArray.ofType(Object.class, 1), objs);
	}

	@Test
	public void testIterable() {
		Assert.illegalArg(() -> RawArray.iterable(obj, 0, 0)); // not an array
		Assert.ordered(RawArray.iterable(ints, 0, 2), -1, 1);
		var i = RawArray.iterable(ints, 1, 1).iterator();
		Assert.equal(i.next(), 1);
		Assert.noSuchElement(() -> i.next());
	}

	@Test
	public void testCopy() {
		Assert.equal(RawArray.copy(ints, null), null);
		Assert.array(RawArray.copy(null, new int[2]), 0, 0);
		Assert.array(RawArray.copy(ints, new int[2]), -1, 1);
	}

	@Test
	public void testArrayCopy() {
		Assert.array((int[]) RawArray.arrayCopy(null, 0, new int[1], 0, 1), 0);
		Assert.equal((int[]) RawArray.arrayCopy(ints, 0, null, 0, 1), null);
		Assert.array((Integer[]) RawArray.arrayCopy(ints, 1, new Integer[3], 0, 2), 1, 0, null);
		Assert.array((int[]) RawArray.arrayCopy(new Integer[] { -1, 1, 2 }, 0, new int[3], 0, 2), -1,
			1, 0);
	}

	@Test
	public void testApplySlice() throws Exception {
		Assert.equal(RawArray.applySlice(null, 0, 1, (_, _) -> Assert.fail()), null);
		Assert.equal(RawArray.applySlice(ints, 0, 1, null), null);
		Captor.ofBi()
			.apply(
				c -> Assert.equal(RawArray.applySlice(ints, -1, 4, (o, l) -> c.accept(o, l, 1)), 1))
			.verify(0, 3);
	}

	@Test
	public void testAcceptIndexes() throws Exception {
		Assert.equal(RawArray.acceptIndexes(null, 0, 1, _ -> Assert.fail()), null);
		Assert.same(RawArray.acceptIndexes(ints, 0, 1, null), ints);
		Captor.of().apply(c -> RawArray.acceptIndexes(ints, -1, 2, c::accept)).verify(0, 1);
	}

	@Test
	public void testApplyBiSlice() throws Exception {
		Assert.equal(RawArray.applyBiSlice(ints, 0, 1, ints, 1, 1, null), null);
	}

	@Test
	public void testAcceptBiSlice() throws Exception {
		RawArray.acceptBiSlice(ints, 0, 1, ints, 1, 1, null);
	}

	@Test
	public void testInsert() {
		Assert.same(RawArray.insert(null, ints, 1, 1), ints);
		Assert.equal(RawArray.insert(int[]::new, null, 1, 1), null);
		Assert.same(RawArray.insert(int[]::new, ints, 1, 0), ints);
		Assert.array(RawArray.insert(int[]::new, ints, 0, 2), 0, 0, -1, 1, 0);
		Assert.array(RawArray.insert(int[]::new, ints, 1, 2), -1, 0, 0, 1, 0);
		Assert.same(RawArray.insert(int[]::new, ints, 1, ints, 1, 0), ints);
	}

	@Test
	public void testAdaptValues() {
		Assert.equal(RawArray.adaptValues(null, ints, (_, _, _) -> {}), null);
		Assert.equal(RawArray.adaptValues(byte[]::new, null, (_, _, _) -> {}), null);
		Assert.equal(RawArray.adaptValues(byte[]::new, ints, null), null);
		Assert.array(RawArray.adaptValues(byte[]::new, ints, (a, v, i) -> a[i] = (byte) v[i]),
			new byte[] { -1, 1, 0 });
	}

	@Test
	public void testBoxed() {
		Assert.equal(RawArray.boxed(null, ints, 1, 2), null);
		Assert.equal(RawArray.boxed(Object[]::new, null, 1, 2), null);
		Assert.array(RawArray.boxed(ints, 1, 2), new Object[] { 1, 0 });
	}

	@Test
	public void testUnboxed() {
		Assert.equal(RawArray.unboxed(null, new Object[] { -1, 1, 0 }, 1, 2), null);
		Assert.equal(RawArray.unboxed(null, List.of(-1, 1, 0)), null);
		Assert.equal(RawArray.unboxed(null, List.of(-1, 1, 0), 1, 2), null);
		Assert.equal(RawArray.unboxed(int[]::new, (Object[]) null, 1, 2), null);
		Assert.equal(RawArray.unboxed(int[]::new, (List<?>) null), null);
		Assert.equal(RawArray.unboxed(int[]::new, (List<?>) null, 1, 2), null);
		Assert.array(RawArray.unboxed(int[]::new, new Object[] { -1, 1, 0 }, 1, 2), 1, 0);
		Assert.array(RawArray.unboxed(int[]::new, List.of(-1, 1, 0)), -1, 1, 0);
		Assert.array(RawArray.unboxed(int[]::new, List.of(-1, 1, 0), 1, 2), 1, 0);
	}

	@Test
	public void testReverse() {
		Assert.same(RawArray.reverse(null, ints, 1, 2), ints);
	}

	@Test
	public void testEquals() {
		Assert.equal(RawArray.equals(null, ints, 0, ints, 0, 3), false);
		Assert.equal(RawArray.equals(null, null, 0, ints, 0, 3), false);
		Assert.equal(RawArray.equals(null, ints, 0, null, 0, 3), false);
		Assert.equal(RawArray.equals(java.util.Arrays::equals, ints, 0, ints, 0, 3), true);
	}

	@Test
	public void testToString() {
		Assert.equal(RawArray.toString(null, Joiner.OR, ints, 0, 2), "null");
		Assert.equal(RawArray.toString((a, i) -> "" + a[i], null, ints, 0, 2), "null");
		Assert.equal(RawArray.toString((a, i) -> "" + a[i], Joiner.OR, (int[]) null, 0, 2), "null");
		Assert.equal(RawArray.toString((a, i) -> "" + a[i], Joiner.OR, ints, 0, 2), "-1|1");
	}

}
