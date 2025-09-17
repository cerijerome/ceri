package ceri.common.array;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertNoSuchElement;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.fail;
import java.util.List;
import org.junit.Test;
import ceri.common.test.Captor;
import ceri.common.text.Joiner;

public class RawArrayTest {
	private final Object obj = new Object();
	private final Object[] objs = new Object[1];
	private final int[] ints = { -1, 1, 0 };

	@Test
	public void testSubTo() {
		var sub = new RawArray.Sub<>(ints, 1, 2);
		assertEquals(sub.to(), 3);
		assertSame(sub.array(), ints);
	}

	@Test
	public void testIsArray() {
		assertEquals(RawArray.isArray(null), false);
		assertEquals(RawArray.isArray(obj), false);
		assertEquals(RawArray.isArray(objs), true);
		assertEquals(RawArray.isArray(ints), true);
	}

	@Test
	public void testOfType() {
		assertEquals(RawArray.ofType(null, 0), null);
		assertArray((int[]) RawArray.ofType(int.class, 1), new int[] { 0 });
		assertArray(RawArray.ofType(Object.class, 1), objs);
	}

	@Test
	public void testIterable() {
		assertIllegalArg(() -> RawArray.iterable(obj, 0, 0)); // not an array
		assertOrdered(RawArray.iterable(ints, 0, 2), -1, 1);
		var i = RawArray.iterable(ints, 1, 1).iterator();
		assertEquals(i.next(), 1);
		assertNoSuchElement(() -> i.next());
	}

	@Test
	public void testCopy() {
		assertEquals(RawArray.copy(ints, null), null);
		assertArray(RawArray.copy(null, new int[2]), 0, 0);
		assertArray(RawArray.copy(ints, new int[2]), -1, 1);
	}

	@Test
	public void testArrayCopy() {
		assertArray((int[]) RawArray.arrayCopy(null, 0, new int[1], 0, 1), 0);
		assertEquals((int[]) RawArray.arrayCopy(ints, 0, null, 0, 1), null);
		assertArray((Integer[]) RawArray.arrayCopy(ints, 1, new Integer[3], 0, 2), 1, 0, null);
		assertArray((int[]) RawArray.arrayCopy(new Integer[] { -1, 1, 2 }, 0, new int[3], 0, 2),
			-1, 1, 0);
	}

	@Test
	public void testApplySlice() throws Exception {
		assertEquals(RawArray.applySlice(null, 0, 1, (_, _) -> fail()), null);
		assertEquals(RawArray.applySlice(ints, 0, 1, null), null);
		Captor.ofBi().apply(
			c -> assertEquals(RawArray.applySlice(ints, -1, 4, (o, l) -> c.accept(o, l, 1)), 1))
			.verify(0, 3);
	}

	@Test
	public void testAcceptIndexes() throws Exception {
		assertEquals(RawArray.acceptIndexes(null, 0, 1, _ -> fail()), null);
		assertSame(RawArray.acceptIndexes(ints, 0, 1, null), ints);
		Captor.of().apply(c -> RawArray.acceptIndexes(ints, -1, 2, c::accept)).verify(0, 1);
	}

	@Test
	public void testInsert() {
		assertSame(RawArray.insert(null, ints, 1, 1), ints);
		assertEquals(RawArray.insert(int[]::new, null, 1, 1), null);
		assertSame(RawArray.insert(int[]::new, ints, 1, 0), ints);
		assertArray(RawArray.insert(int[]::new, ints, 0, 2), 0, 0, -1, 1, 0);
		assertArray(RawArray.insert(int[]::new, ints, 1, 2), -1, 0, 0, 1, 0);
	}

	@Test
	public void testAdaptValues() {
		assertEquals(RawArray.adaptValues(null, ints, (_, _, _) -> {}), null);
		assertEquals(RawArray.adaptValues(byte[]::new, null, (_, _, _) -> {}), null);
		assertEquals(RawArray.adaptValues(byte[]::new, ints, null), null);
		assertArray(RawArray.adaptValues(byte[]::new, ints, (a, v, i) -> a[i] = (byte) v[i]),
			new byte[] { -1, 1, 0 });
	}

	@Test
	public void testBoxed() {
		assertEquals(RawArray.boxed(null, ints, 1, 2), null);
		assertEquals(RawArray.boxed(Object[]::new, null, 1, 2), null);
		assertArray(RawArray.boxed(ints, 1, 2), new Object[] { 1, 0 });
	}

	@Test
	public void testUnboxed() {
		assertEquals(RawArray.unboxed(null, new Object[] { -1, 1, 0 }, 1, 2), null);
		assertEquals(RawArray.unboxed(null, List.of(-1, 1, 0)), null);
		assertEquals(RawArray.unboxed(null, List.of(-1, 1, 0), 1, 2), null);
		assertEquals(RawArray.unboxed(int[]::new, (Object[]) null, 1, 2), null);
		assertEquals(RawArray.unboxed(int[]::new, (List<?>) null), null);
		assertEquals(RawArray.unboxed(int[]::new, (List<?>) null, 1, 2), null);
		assertArray(RawArray.unboxed(int[]::new, new Object[] { -1, 1, 0 }, 1, 2), 1, 0);
		assertArray(RawArray.unboxed(int[]::new, List.of(-1, 1, 0)), -1, 1, 0);
		assertArray(RawArray.unboxed(int[]::new, List.of(-1, 1, 0), 1, 2), 1, 0);
	}

	@Test
	public void testReverse() {
		assertSame(RawArray.reverse(null, ints, 1, 2), ints);
	}

	@Test
	public void testEquals() {
		assertEquals(RawArray.equals(null, ints, 0, ints, 0, 3), false);
		assertEquals(RawArray.equals(null, null, 0, ints, 0, 3), false);
		assertEquals(RawArray.equals(null, ints, 0, null, 0, 3), false);
		assertEquals(RawArray.equals(java.util.Arrays::equals, ints, 0, ints, 0, 3), true);
	}

	@Test
	public void testToString() {
		assertEquals(RawArray.toString(null, Joiner.OR, ints, 0, 2), "null");
		assertEquals(RawArray.toString((a, i) -> "" + a[i], null, ints, 0, 2), "null");
		assertEquals(RawArray.toString((a, i) -> "" + a[i], Joiner.OR, (int[]) null, 0, 2),
			"null");
		assertEquals(RawArray.toString((a, i) -> "" + a[i], Joiner.OR, ints, 0, 2), "-1|1");
	}

}
