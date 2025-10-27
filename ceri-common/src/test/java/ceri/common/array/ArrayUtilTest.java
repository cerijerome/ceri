package ceri.common.array;

import java.util.Objects;
import org.junit.Test;
import ceri.common.function.Compares;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.common.text.Format;
import ceri.common.text.Joiner;

public class ArrayUtilTest {
	private static final Object[] NULL = null;
	private final Integer[] ints = { -1, null, 1 };

	private static Integer[] ints(int... ints) {
		return ArrayUtil.ints.boxed(ints);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(ArrayUtil.class, ArrayUtil.Empty.class, ArrayUtil.Filter.class);
	}

	@Test
	public void testFilterAny() throws Exception {
		Assert.equal(ArrayUtil.Filter.any(null).test(ints), false);
		var filter = ArrayUtil.Filter.any((_, i) -> i > 0);
		Assert.equal(filter.test(NULL), false);
		Assert.equal(filter.test(ints(1)), false);
		Assert.equal(filter.test(ints), true);
	}

	@Test
	public void testFilterAll() throws Exception {
		Assert.equal(ArrayUtil.Filter.all(null).test(ints), false);
		var filter = ArrayUtil.Filter.all((_, i) -> i < 1);
		Assert.equal(filter.test(NULL), true);
		Assert.equal(filter.test(ints(1)), true);
		Assert.equal(filter.test(ints), false);
	}

	@Test
	public void testOf() {
		Assert.equal(ArrayUtil.of(NULL), null);
		Assert.array(ArrayUtil.of(true, 1, null), true, 1, null);
	}

	@Test
	public void testOfType() {
		Assert.equal(ArrayUtil.ofType(null, 1), null);
		Assert.illegalArg(() -> ArrayUtil.ofType(boolean.class, 1));
		Assert.array(ArrayUtil.ofType(String.class, 0));
		var array = ArrayUtil.ofType(String.class, 3);
		array[1] = "test";
		Assert.array(array, null, "test", null);
	}

	@Test
	public void testLength() {
		Assert.equal(ArrayUtil.length(NULL), 0);
		Assert.equal(ArrayUtil.length(ArrayUtil.Empty.strings), 0);
		Assert.equal(ArrayUtil.length(ints), 3);
	}

	@Test
	public void testIsValidSlice() {
		Assert.equal(ArrayUtil.isValidSlice(0, 1, 0), false);
		Assert.equal(ArrayUtil.isValidSlice(0, 0, 1), false);
		Assert.equal(ArrayUtil.isValidSlice(1, 1, 1), false);
		Assert.equal(ArrayUtil.isValidSlice(1, 0, 2), false);
		Assert.equal(ArrayUtil.isValidSlice(1, -1, 1), false);
		Assert.equal(ArrayUtil.isValidSlice(1, 0, -1), false);
		Assert.equal(ArrayUtil.isValidSlice(0, 0, 0), true);
		Assert.equal(ArrayUtil.isValidSlice(1, 0, 1), true);
	}

	@Test
	public void testIsValidRange() {
		Assert.equal(ArrayUtil.isValidRange(0, 1, 1), false);
		Assert.equal(ArrayUtil.isValidRange(0, 0, 1), false);
		Assert.equal(ArrayUtil.isValidRange(1, -1, 1), false);
		Assert.equal(ArrayUtil.isValidRange(1, 1, 0), false);
		Assert.equal(ArrayUtil.isValidRange(0, 0, 0), true);
		Assert.equal(ArrayUtil.isValidRange(1, 0, 1), true);
		Assert.equal(ArrayUtil.isValidRange(1, 1, 1), true);
	}

	@Test
	public void testArrayType() {
		Assert.equal(ArrayUtil.arrayType(null), null);
		Assert.illegalArg(() -> ArrayUtil.arrayType(boolean.class));
		Assert.equal(ArrayUtil.arrayType(String.class), String[].class);
	}

	@Test
	public void testComponentType() {
		Assert.equal(ArrayUtil.componentType(null), null);
		Assert.equal(ArrayUtil.componentType(String[].class), String.class);
	}

	@Test
	public void testIn() {
		Assert.equal(ArrayUtil.in(0, 0), false);
		Assert.equal(ArrayUtil.in(1, -1), false);
		Assert.equal(ArrayUtil.in(1, 1), false);
		Assert.equal(ArrayUtil.in(1, 0), true);
		Assert.equal(ArrayUtil.in(ArrayUtil.Empty.ints, 0), false);
		Assert.equal(ArrayUtil.in(ints, 3), false);
		Assert.equal(ArrayUtil.in(ints, 2), true);
	}

	@Test
	public void testAt() {
		Assert.equal(ArrayUtil.at(null, 0), null);
		Assert.equal(ArrayUtil.at(ints, -1), null);
		Assert.equal(ArrayUtil.at(ints, 3), null);
		Assert.equal(ArrayUtil.at(ints, 2), 1);
	}

	@Test
	public void testLast() {
		Assert.equal(ArrayUtil.last(null), null);
		Assert.equal(ArrayUtil.last(ArrayUtil.Empty.ints), null);
		Assert.equal(ArrayUtil.last(ints), 1);
	}

	@Test
	public void testResize() {
		Assert.equal(ArrayUtil.resize(null, ints, 2), null);
		Assert.same(ArrayUtil.resize(null, ints, 3), ints);
		Assert.array(ArrayUtil.resize(Integer[]::new, null, 2), null, null);
		Assert.array(ArrayUtil.resize(Integer[]::new, ints, -1));
		Assert.array(ArrayUtil.resize(Integer[]::new, ints, 0));
		Assert.same(ArrayUtil.resize(Integer[]::new, ints, 3), ints);
		Assert.array(ArrayUtil.resize(Integer[]::new, ints, 2), -1, null);
		Assert.array(ArrayUtil.resize(Integer[]::new, ints, 4), -1, null, 1, null);
	}

	@Test
	public void testCopyOf() {
		Assert.notSame(ArrayUtil.copyOf(Integer[]::new, ints), ints);
		Assert.array(ArrayUtil.copyOf(Integer[]::new, ints), ints);
		Assert.array(ArrayUtil.copyOf(Integer[]::new, ints, -1));
		Assert.array(ArrayUtil.copyOf(Integer[]::new, ints, 0));
		Assert.array(ArrayUtil.copyOf(Integer[]::new, ints, 2), -1, null);
		Assert.array(ArrayUtil.copyOf(Integer[]::new, ints, 4), -1, null, 1, null);
	}

	@Test
	public void testCopy() {
		Assert.equal(ArrayUtil.copy(ints, null), null);
		Assert.array(ArrayUtil.copy(null, new Integer[2]), null, null);
		Assert.array(ArrayUtil.copy(ints, new Integer[2]), -1, null);
	}

	@Test
	public void testAppend() {
		Assert.same(ArrayUtil.append(null, ints), ints);
		Assert.same(ArrayUtil.append(null, ints, 1), ints);
		Assert.same(ArrayUtil.append(Integer[]::new, ints), ints);
		Assert.equal(ArrayUtil.append(Integer[]::new, null), null);
		Assert.array(ArrayUtil.append(Integer[]::new, ints, 0), -1, null, 1, 0);
	}

	@Test
	public void testInsert() {
		Assert.same(ArrayUtil.insert(null, ints, 0), ints);
		Assert.same(ArrayUtil.insert(null, ints, 1, 1), ints);
		Assert.same(ArrayUtil.insert(Integer[]::new, ints, 1), ints);
		Assert.equal(ArrayUtil.insert(Integer[]::new, null, 1), null);
		Assert.array(ArrayUtil.insert(Integer[]::new, ints, 1, 0), -1, 0, null, 1);
	}

	@Test
	public void testHas() {
		Assert.equal(ArrayUtil.has(null, 1), false);
		Assert.equal(ArrayUtil.has(ints, null), true);
		Assert.equal(ArrayUtil.has(ints, 0), false);
	}

	@Test
	public void testContains() {
		Assert.equal(ArrayUtil.contains(null), false);
		Assert.equal(ArrayUtil.contains(null, 1), false);
		Assert.equal(ArrayUtil.contains(ints, (Integer) null), true);
		Assert.equal(ArrayUtil.contains(ints, null, 1), true);
		Assert.equal(ArrayUtil.contains(ints, -1, 1), false);
	}

	@Test
	public void testIndexOf() {
		Integer[] ints = { -1, null, 1, null, -1 };
		Assert.equal(ArrayUtil.indexOf(null), -1);
		Assert.equal(ArrayUtil.indexOf(null, 1), -1);
		Assert.equal(ArrayUtil.indexOf(ints, (Integer) null), 1);
		Assert.equal(ArrayUtil.indexOf(ints, null, 1), 1);
		Assert.equal(ArrayUtil.indexOf(ints, -1, 1), -1);
	}

	@Test
	public void testLastIndexOf() {
		Integer[] ints = { -1, null, 1, null, 1 };
		Assert.equal(ArrayUtil.lastIndexOf(null), -1);
		Assert.equal(ArrayUtil.lastIndexOf(null, 1), -1);
		Assert.equal(ArrayUtil.lastIndexOf(ints, (Integer) null), 3);
		Assert.equal(ArrayUtil.lastIndexOf(ints, null, 1), 3);
		Assert.equal(ArrayUtil.lastIndexOf(ints, -1, 1), -1);
	}

	@Test
	public void testFill() {
		Assert.equal(ArrayUtil.fill(null, null), null);
		Assert.equal(ArrayUtil.fill(null, 1), null);
		Assert.array(ArrayUtil.fill(new Integer[0], 0));
		Assert.array(ArrayUtil.fill(new Integer[3], 1), 1, 1, 1);
	}

	@Test
	public void testReverse() {
		Assert.equal(ArrayUtil.reverse(NULL), null);
		Assert.array(ArrayUtil.reverse(ints.clone()), 1, null, -1);
	}

	@Test
	public void testForEach() throws Exception {
		Assert.equal(ArrayUtil.forEach(null, _ -> {}), null);
		Assert.same(ArrayUtil.forEach(ints, null), ints);
		Captor.of().apply(c -> ArrayUtil.forEach(ints, c::accept)).verify(-1, null, 1);
	}

	@Test
	public void testForEachIndexed() throws Exception {
		Assert.equal(ArrayUtil.forEachIndexed(null, (_, _) -> {}), null);
		Assert.same(ArrayUtil.forEachIndexed(ints, null), ints);
		Captor.ofBi().apply(c -> ArrayUtil.forEachIndexed(ints, c::accept)).verify(-1, 0, null, 1,
			1, 2);
	}

	@Test
	public void testApplySlice() throws Exception {
		Assert.equal(ArrayUtil.applySlice(3, 1, 3, null), null);
		Assert.equal(ArrayUtil.applySlice(3, 1, 3, (_, _) -> 1), 1);
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
	public void testApplyBiSlice() throws Exception {
		Assert.equal(ArrayUtil.applyBiSlice(1, 0, 1, 1, 0, 1, null), null);
	}

	@Test
	public void testAcceptBiSlice() {
		ArrayUtil.acceptBiSlice(1, 0, 1, 1, 0, 1, null);
	}

	@Test
	public void testSort() {
		Assert.equal(ArrayUtil.sort(null), null);
		Assert.array(ArrayUtil.sort(ints.clone()), null, -1, 1);
		Assert.array(ArrayUtil.sort(ints.clone(), Compares.of(Compares.Nulls.last)), -1, 1,
			null);
	}

	@Test
	public void testEquals() {
		Assert.equal(ArrayUtil.equals(NULL, NULL), true);
		Assert.equal(ArrayUtil.equals(NULL, new Object[0]), false);
		Assert.equal(ArrayUtil.equals(ints, -1, (Integer) null), false);
		Assert.equal(ArrayUtil.equals(ints, -1, (Integer) null, 1), true);
		Assert.equal(ArrayUtil.equals(ints, ints), true);
	}

	@Test
	public void testHash() {
		Assert.equal(ArrayUtil.hash(NULL), 0);
		Assert.equal(ArrayUtil.hash(ints), Objects.hash((Object[]) ints));
	}

	@Test
	public void testToString() {
		Assert.equal(ArrayUtil.toString(NULL), "null");
		Assert.equal(ArrayUtil.toString(new Object[0]), "[]");
		Assert.equal(ArrayUtil.toString(ints), "[-1, null, 1]");
	}

	@Test
	public void testToStringWithJoiner() {
		Assert.equal(ArrayUtil.toString(null, NULL), "null");
		Assert.equal(ArrayUtil.toString(null, new Object[0]), "null");
		Assert.equal(ArrayUtil.toString(Joiner.OR, new Object[0]), "");
		Assert.equal(ArrayUtil.toString(Joiner.OR, ints), "-1|null|1");
	}

	@Test
	public void testToCustomStringWithJoiner() {
		Assert.equal(ArrayUtil.toString(null, Joiner.OR, NULL), "null");
		Assert.equal(ArrayUtil.toString(null, Joiner.OR, new Object[0]), "null");
		Assert.equal(ArrayUtil.toString(_ -> "!", Joiner.OR, new Object[0]), "");
		Assert.equal(ArrayUtil.toString(Format.HEX::uint, Joiner.OR, ints), "0xffffffff|null|0x1");
	}
}
