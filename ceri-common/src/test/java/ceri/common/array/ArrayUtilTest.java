package ceri.common.array;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertNotSame;
import static ceri.common.test.AssertUtil.assertSame;
import java.util.Objects;
import org.junit.Test;
import ceri.common.function.Compares;
import ceri.common.test.Captor;
import ceri.common.text.Format;
import ceri.common.text.Joiner;

public class ArrayUtilTest {
	private static final Object[] NULL = null;
	private final Integer[] ints = { -1, null, 1 };

	@Test
	public void testOf() {
		assertEquals(ArrayUtil.of(NULL), null);
		assertArray(ArrayUtil.of(true, 1, null), true, 1, null);
	}

	@Test
	public void testOfType() {
		assertEquals(ArrayUtil.ofType(null, 1), null);
		assertIllegalArg(() -> ArrayUtil.ofType(boolean.class, 1));
		assertArray(ArrayUtil.ofType(String.class, 0));
		var array = ArrayUtil.ofType(String.class, 3);
		array[1] = "test";
		assertArray(array, null, "test", null);
	}

	@Test
	public void testLength() {
		assertEquals(ArrayUtil.length(NULL), 0);
		assertEquals(ArrayUtil.length(ArrayUtil.Empty.strings), 0);
		assertEquals(ArrayUtil.length(ints), 3);
	}

	@Test
	public void testIsValidSlice() {
		assertEquals(ArrayUtil.isValidSlice(0, 1, 0), false);
		assertEquals(ArrayUtil.isValidSlice(0, 0, 1), false);
		assertEquals(ArrayUtil.isValidSlice(1, 1, 1), false);
		assertEquals(ArrayUtil.isValidSlice(1, 0, 2), false);
		assertEquals(ArrayUtil.isValidSlice(1, -1, 1), false);
		assertEquals(ArrayUtil.isValidSlice(1, 0, -1), false);
		assertEquals(ArrayUtil.isValidSlice(0, 0, 0), true);
		assertEquals(ArrayUtil.isValidSlice(1, 0, 1), true);
	}

	@Test
	public void testIsValidRange() {
		assertEquals(ArrayUtil.isValidRange(0, 1, 1), false);
		assertEquals(ArrayUtil.isValidRange(0, 0, 1), false);
		assertEquals(ArrayUtil.isValidRange(1, -1, 1), false);
		assertEquals(ArrayUtil.isValidRange(1, 1, 0), false);
		assertEquals(ArrayUtil.isValidRange(0, 0, 0), true);
		assertEquals(ArrayUtil.isValidRange(1, 0, 1), true);
		assertEquals(ArrayUtil.isValidRange(1, 1, 1), true);
	}

	@Test
	public void testArrayType() {
		assertEquals(ArrayUtil.arrayType(null), null);
		assertIllegalArg(() -> ArrayUtil.arrayType(boolean.class));
		assertEquals(ArrayUtil.arrayType(String.class), String[].class);
	}

	@Test
	public void testComponentType() {
		assertEquals(ArrayUtil.componentType(null), null);
		assertEquals(ArrayUtil.componentType(String[].class), String.class);
	}

	@Test
	public void testIn() {
		assertEquals(ArrayUtil.in(0, 0), false);
		assertEquals(ArrayUtil.in(1, -1), false);
		assertEquals(ArrayUtil.in(1, 1), false);
		assertEquals(ArrayUtil.in(1, 0), true);
		assertEquals(ArrayUtil.in(ArrayUtil.Empty.ints, 0), false);
		assertEquals(ArrayUtil.in(ints, 3), false);
		assertEquals(ArrayUtil.in(ints, 2), true);
	}

	@Test
	public void testAt() {
		assertEquals(ArrayUtil.at(null, 0), null);
		assertEquals(ArrayUtil.at(ints, -1), null);
		assertEquals(ArrayUtil.at(ints, 3), null);
		assertEquals(ArrayUtil.at(ints, 2), 1);
	}

	@Test
	public void testLast() {
		assertEquals(ArrayUtil.last(null), null);
		assertEquals(ArrayUtil.last(ArrayUtil.Empty.ints), null);
		assertEquals(ArrayUtil.last(ints), 1);
	}

	@Test
	public void testResize() {
		assertEquals(ArrayUtil.resize(null, ints, 2), null);
		assertSame(ArrayUtil.resize(null, ints, 3), ints);
		assertArray(ArrayUtil.resize(Integer[]::new, null, 2), null, null);
		assertArray(ArrayUtil.resize(Integer[]::new, ints, -1));
		assertArray(ArrayUtil.resize(Integer[]::new, ints, 0));
		assertSame(ArrayUtil.resize(Integer[]::new, ints, 3), ints);
		assertArray(ArrayUtil.resize(Integer[]::new, ints, 2), -1, null);
		assertArray(ArrayUtil.resize(Integer[]::new, ints, 4), -1, null, 1, null);
	}

	@Test
	public void testCopyOf() {
		assertNotSame(ArrayUtil.copyOf(Integer[]::new, ints), ints);
		assertArray(ArrayUtil.copyOf(Integer[]::new, ints), ints);
		assertArray(ArrayUtil.copyOf(Integer[]::new, ints, -1));
		assertArray(ArrayUtil.copyOf(Integer[]::new, ints, 0));
		assertArray(ArrayUtil.copyOf(Integer[]::new, ints, 2), -1, null);
		assertArray(ArrayUtil.copyOf(Integer[]::new, ints, 4), -1, null, 1, null);
	}

	@Test
	public void testCopy() {
		assertEquals(ArrayUtil.copy(ints, null), null);
		assertArray(ArrayUtil.copy(null, new Integer[2]), null, null);
		assertArray(ArrayUtil.copy(ints, new Integer[2]), -1, null);
	}

	@Test
	public void testAppend() {
		assertSame(ArrayUtil.append(null, ints), ints);
		assertSame(ArrayUtil.append(null, ints, 1), ints);
		assertSame(ArrayUtil.append(Integer[]::new, ints), ints);
		assertEquals(ArrayUtil.append(Integer[]::new, null), null);
		assertArray(ArrayUtil.append(Integer[]::new, ints, 0), -1, null, 1, 0);
	}

	@Test
	public void testInsert() {
		assertSame(ArrayUtil.insert(null, ints, 0), ints);
		assertSame(ArrayUtil.insert(null, ints, 1, 1), ints);
		assertSame(ArrayUtil.insert(Integer[]::new, ints, 1), ints);
		assertEquals(ArrayUtil.insert(Integer[]::new, null, 1), null);
		assertArray(ArrayUtil.insert(Integer[]::new, ints, 1, 0), -1, 0, null, 1);
	}

	@Test
	public void testHas() {
		assertEquals(ArrayUtil.has(null, 1), false);
		assertEquals(ArrayUtil.has(ints, null), true);
		assertEquals(ArrayUtil.has(ints, 0), false);
	}

	@Test
	public void testContains() {
		assertEquals(ArrayUtil.contains(null), false);
		assertEquals(ArrayUtil.contains(null, 1), false);
		assertEquals(ArrayUtil.contains(ints, (Integer) null), true);
		assertEquals(ArrayUtil.contains(ints, null, 1), true);
		assertEquals(ArrayUtil.contains(ints, -1, 1), false);
	}

	@Test
	public void testIndexOf() {
		Integer[] ints = { -1, null, 1, null, -1 };
		assertEquals(ArrayUtil.indexOf(null), -1);
		assertEquals(ArrayUtil.indexOf(null, 1), -1);
		assertEquals(ArrayUtil.indexOf(ints, (Integer) null), 1);
		assertEquals(ArrayUtil.indexOf(ints, null, 1), 1);
		assertEquals(ArrayUtil.indexOf(ints, -1, 1), -1);
	}

	@Test
	public void testLastIndexOf() {
		Integer[] ints = { -1, null, 1, null, 1 };
		assertEquals(ArrayUtil.lastIndexOf(null), -1);
		assertEquals(ArrayUtil.lastIndexOf(null, 1), -1);
		assertEquals(ArrayUtil.lastIndexOf(ints, (Integer) null), 3);
		assertEquals(ArrayUtil.lastIndexOf(ints, null, 1), 3);
		assertEquals(ArrayUtil.lastIndexOf(ints, -1, 1), -1);
	}

	@Test
	public void testFill() {
		assertEquals(ArrayUtil.fill(null, null), null);
		assertEquals(ArrayUtil.fill(null, 1), null);
		assertArray(ArrayUtil.fill(new Integer[0], 0));
		assertArray(ArrayUtil.fill(new Integer[3], 1), 1, 1, 1);
	}

	@Test
	public void testReverse() {
		assertEquals(ArrayUtil.reverse(NULL), null);
		assertArray(ArrayUtil.reverse(ints.clone()), 1, null, -1);
	}

	@Test
	public void testForEach() throws Exception {
		assertEquals(ArrayUtil.forEach(null, _ -> {}), null);
		assertSame(ArrayUtil.forEach(ints, null), ints);
		Captor.of().apply(c -> ArrayUtil.forEach(ints, c::accept)).verify(-1, null, 1);
	}

	@Test
	public void testForEachIndexed() throws Exception {
		assertEquals(ArrayUtil.forEachIndexed(null, (_, _) -> {}), null);
		assertSame(ArrayUtil.forEachIndexed(ints, null), ints);
		Captor.ofBi().apply(c -> ArrayUtil.forEachIndexed(ints, c::accept)).verify(-1, 0, null, 1,
			1, 2);
	}

	@Test
	public void testApplySlice() throws Exception {
		assertEquals(ArrayUtil.applySlice(3, 1, 3, null), null);
		assertEquals(ArrayUtil.applySlice(3, 1, 3, (_, _) -> 1), 1);
		Captor.ofBi().apply(c -> ArrayUtil.applySlice(0, 1, 3, (o, l) -> c.accept(o, l, null)))
			.verify(0, 0);
		Captor.ofBi().apply(c -> ArrayUtil.applySlice(3, 1, 3, (o, l) -> c.accept(o, l, null)))
			.verify(1, 2);
		Captor.ofBi().apply(c -> ArrayUtil.applySlice(3, -1, 2, (o, l) -> c.accept(o, l, null)))
			.verify(0, 2);
	}

	@Test
	public void testAcceptSlice() throws Exception {
		ArrayUtil.acceptSlice(3, 1, 3, null);
		Captor.ofBi().apply(c -> ArrayUtil.acceptSlice(0, 1, 3, c::accept)).verify(0, 0);
		Captor.ofBi().apply(c -> ArrayUtil.acceptSlice(3, 1, 3, c::accept)).verify(1, 2);
		Captor.ofBi().apply(c -> ArrayUtil.acceptSlice(3, -1, 2, c::accept)).verify(0, 2);
	}

	@Test
	public void testAcceptIndexes() {
		ArrayUtil.acceptIndexes(3, 1, 3, null);
		Captor.of().apply(c -> ArrayUtil.acceptIndexes(0, 0, 1, c::accept)).verify();
		Captor.of().apply(c -> ArrayUtil.acceptIndexes(3, -1, 2, c::accept)).verify(0, 1);
	}

	@Test
	public void testSort() {
		assertEquals(ArrayUtil.sort(null), null);
		assertArray(ArrayUtil.sort(ints.clone()), null, -1, 1);
		assertArray(ArrayUtil.sort(ints.clone(), Compares.nullsLast()), -1, 1, null);
	}

	@Test
	public void testEquals() {
		assertEquals(ArrayUtil.equals(NULL, NULL), true);
		assertEquals(ArrayUtil.equals(NULL, new Object[0]), false);
		assertEquals(ArrayUtil.equals(ints, -1, (Integer) null), false);
		assertEquals(ArrayUtil.equals(ints, -1, (Integer) null, 1), true);
		assertEquals(ArrayUtil.equals(ints, ints), true);
	}

	@Test
	public void testHash() {
		assertEquals(ArrayUtil.hash(NULL), 0);
		assertEquals(ArrayUtil.hash(ints), Objects.hash((Object[]) ints));
	}

	@Test
	public void testToString() {
		assertEquals(ArrayUtil.toString(NULL), "null");
		assertEquals(ArrayUtil.toString(new Object[0]), "[]");
		assertEquals(ArrayUtil.toString(ints), "[-1, null, 1]");
	}

	@Test
	public void testToStringWithJoiner() {
		assertEquals(ArrayUtil.toString(null, NULL), "null");
		assertEquals(ArrayUtil.toString(null, new Object[0]), "null");
		assertEquals(ArrayUtil.toString(Joiner.OR, new Object[0]), "");
		assertEquals(ArrayUtil.toString(Joiner.OR, ints), "-1|null|1");
	}

	@Test
	public void testToCustomStringWithJoiner() {
		assertEquals(ArrayUtil.toString(null, Joiner.OR, NULL), "null");
		assertEquals(ArrayUtil.toString(null, Joiner.OR, new Object[0]), "null");
		assertEquals(ArrayUtil.toString(_ -> "!", Joiner.OR, new Object[0]), "");
		assertEquals(ArrayUtil.toString(Format.HEX::uint, Joiner.OR, ints), "0xffffffff|null|0x1");
	}
}
