package ceri.common.array;

import java.util.Objects;
import org.junit.Test;
import ceri.common.function.Compares;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.common.text.Format;
import ceri.common.text.Joiner;

public class ArrayTest {
	private static final Object[] NULL = null;
	private final Integer[] ints = { -1, null, 1 };

	private static Integer[] ints(int... ints) {
		return Array.INT.boxed(ints);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Array.class, Array.Filter.class);
	}

	@Test
	public void testFilterAny() throws Exception {
		Assert.equal(Array.Filter.any(null).test(ints), false);
		var filter = Array.Filter.any((_, i) -> i > 0);
		Assert.equal(filter.test(NULL), false);
		Assert.equal(filter.test(ints(1)), false);
		Assert.equal(filter.test(ints), true);
	}

	@Test
	public void testFilterAll() throws Exception {
		Assert.equal(Array.Filter.all(null).test(ints), false);
		var filter = Array.Filter.all((_, i) -> i < 1);
		Assert.equal(filter.test(NULL), true);
		Assert.equal(filter.test(ints(1)), true);
		Assert.equal(filter.test(ints), false);
	}

	@Test
	public void testOf() {
		Assert.equal(Array.of(NULL), null);
		Assert.array(Array.of(true, 1, null), true, 1, null);
	}

	@Test
	public void testOfType() {
		Assert.equal(Array.ofType(null, 1), null);
		Assert.illegalArg(() -> Array.ofType(boolean.class, 1));
		Assert.array(Array.ofType(String.class, 0));
		var array = Array.ofType(String.class, 3);
		array[1] = "test";
		Assert.array(array, null, "test", null);
	}

	@Test
	public void testLength() {
		Assert.equal(Array.length(NULL), 0);
		Assert.equal(Array.length(Array.STRING.empty), 0);
		Assert.equal(Array.length(ints), 3);
	}

	@Test
	public void testIsValidSlice() {
		Assert.equal(Array.isValidSlice(0, 1, 0), false);
		Assert.equal(Array.isValidSlice(0, 0, 1), false);
		Assert.equal(Array.isValidSlice(1, 1, 1), false);
		Assert.equal(Array.isValidSlice(1, 0, 2), false);
		Assert.equal(Array.isValidSlice(1, -1, 1), false);
		Assert.equal(Array.isValidSlice(1, 0, -1), false);
		Assert.equal(Array.isValidSlice(0, 0, 0), true);
		Assert.equal(Array.isValidSlice(1, 0, 1), true);
	}

	@Test
	public void testIsValidRange() {
		Assert.equal(Array.isValidRange(0, 1, 1), false);
		Assert.equal(Array.isValidRange(0, 0, 1), false);
		Assert.equal(Array.isValidRange(1, -1, 1), false);
		Assert.equal(Array.isValidRange(1, 1, 0), false);
		Assert.equal(Array.isValidRange(0, 0, 0), true);
		Assert.equal(Array.isValidRange(1, 0, 1), true);
		Assert.equal(Array.isValidRange(1, 1, 1), true);
	}

	@Test
	public void testArrayType() {
		Assert.equal(Array.arrayType(null), null);
		Assert.illegalArg(() -> Array.arrayType(boolean.class));
		Assert.equal(Array.arrayType(String.class), String[].class);
	}

	@Test
	public void testComponentType() {
		Assert.equal(Array.componentType(null), null);
		Assert.equal(Array.componentType(String[].class), String.class);
	}

	@Test
	public void testIn() {
		Assert.equal(Array.in(0, 0), false);
		Assert.equal(Array.in(1, -1), false);
		Assert.equal(Array.in(1, 1), false);
		Assert.equal(Array.in(1, 0), true);
		Assert.equal(Array.in(Array.INT.box().empty, 0), false);
		Assert.equal(Array.in(ints, 3), false);
		Assert.equal(Array.in(ints, 2), true);
	}

	@Test
	public void testAt() {
		Assert.equal(Array.at(null, 0), null);
		Assert.equal(Array.at(ints, -1), null);
		Assert.equal(Array.at(ints, 3), null);
		Assert.equal(Array.at(ints, 2), 1);
	}

	@Test
	public void testLast() {
		Assert.equal(Array.last(null), null);
		Assert.equal(Array.last(Array.INT.box().empty), null);
		Assert.equal(Array.last(ints), 1);
	}

	@Test
	public void testResize() {
		Assert.equal(Array.resize(null, ints, 2), null);
		Assert.same(Array.resize(null, ints, 3), ints);
		Assert.array(Array.resize(Integer[]::new, null, 2), null, null);
		Assert.array(Array.resize(Integer[]::new, ints, -1));
		Assert.array(Array.resize(Integer[]::new, ints, 0));
		Assert.same(Array.resize(Integer[]::new, ints, 3), ints);
		Assert.array(Array.resize(Integer[]::new, ints, 2), -1, null);
		Assert.array(Array.resize(Integer[]::new, ints, 4), -1, null, 1, null);
	}

	@Test
	public void testCopyOf() {
		Assert.notSame(Array.copyOf(Integer[]::new, ints), ints);
		Assert.array(Array.copyOf(Integer[]::new, ints), ints);
		Assert.array(Array.copyOf(Integer[]::new, ints, -1));
		Assert.array(Array.copyOf(Integer[]::new, ints, 0));
		Assert.array(Array.copyOf(Integer[]::new, ints, 2), -1, null);
		Assert.array(Array.copyOf(Integer[]::new, ints, 4), -1, null, 1, null);
	}

	@Test
	public void testCopy() {
		Assert.equal(Array.copy(ints, null), null);
		Assert.array(Array.copy(null, new Integer[2]), null, null);
		Assert.array(Array.copy(ints, new Integer[2]), -1, null);
	}

	@Test
	public void testAppend() {
		Assert.same(Array.append(null, ints), ints);
		Assert.same(Array.append(null, ints, 1), ints);
		Assert.same(Array.append(Integer[]::new, ints), ints);
		Assert.equal(Array.append(Integer[]::new, null), null);
		Assert.array(Array.append(Integer[]::new, ints, 0), -1, null, 1, 0);
	}

	@Test
	public void testInsert() {
		Assert.same(Array.insert(null, ints, 0), ints);
		Assert.same(Array.insert(null, ints, 1, 1), ints);
		Assert.same(Array.insert(Integer[]::new, ints, 1), ints);
		Assert.equal(Array.insert(Integer[]::new, null, 1), null);
		Assert.array(Array.insert(Integer[]::new, ints, 1, 0), -1, 0, null, 1);
	}

	@Test
	public void testHas() {
		Assert.equal(Array.has(null, 1), false);
		Assert.equal(Array.has(ints, null), true);
		Assert.equal(Array.has(ints, 0), false);
	}

	@Test
	public void testContains() {
		Assert.equal(Array.contains(null), false);
		Assert.equal(Array.contains(null, 1), false);
		Assert.equal(Array.contains(ints, (Integer) null), true);
		Assert.equal(Array.contains(ints, null, 1), true);
		Assert.equal(Array.contains(ints, -1, 1), false);
	}

	@Test
	public void testIndexOf() {
		Integer[] ints = { -1, null, 1, null, -1 };
		Assert.equal(Array.indexOf(null), -1);
		Assert.equal(Array.indexOf(null, 1), -1);
		Assert.equal(Array.indexOf(ints, (Integer) null), 1);
		Assert.equal(Array.indexOf(ints, null, 1), 1);
		Assert.equal(Array.indexOf(ints, -1, 1), -1);
	}

	@Test
	public void testLastIndexOf() {
		Integer[] ints = { -1, null, 1, null, 1 };
		Assert.equal(Array.lastIndexOf(null), -1);
		Assert.equal(Array.lastIndexOf(null, 1), -1);
		Assert.equal(Array.lastIndexOf(ints, (Integer) null), 3);
		Assert.equal(Array.lastIndexOf(ints, null, 1), 3);
		Assert.equal(Array.lastIndexOf(ints, -1, 1), -1);
	}

	@Test
	public void testFill() {
		Assert.equal(Array.fill(null, null), null);
		Assert.equal(Array.fill(null, 1), null);
		Assert.array(Array.fill(new Integer[0], 0));
		Assert.array(Array.fill(new Integer[3], 1), 1, 1, 1);
	}

	@Test
	public void testReverse() {
		Assert.equal(Array.reverse(NULL), null);
		Assert.array(Array.reverse(ints.clone()), 1, null, -1);
	}

	@Test
	public void testForEach() throws Exception {
		Assert.equal(Array.forEach(null, _ -> {}), null);
		Assert.same(Array.forEach(ints, null), ints);
		Captor.of().apply(c -> Array.forEach(ints, c::accept)).verify(-1, null, 1);
	}

	@Test
	public void testForEachIndexed() throws Exception {
		Assert.equal(Array.forEachIndexed(null, (_, _) -> {}), null);
		Assert.same(Array.forEachIndexed(ints, null), ints);
		Captor.ofBi().apply(c -> Array.forEachIndexed(ints, c::accept)).verify(-1, 0, null, 1,
			1, 2);
	}

	@Test
	public void testApplySlice() throws Exception {
		Assert.equal(Array.applySlice(3, 1, 3, null), null);
		Assert.equal(Array.applySlice(3, 1, 3, (_, _) -> 1), 1);
		Captor.ofBi().apply(c -> Array.applySlice(0, 1, 3, (o, l) -> c.accept(o, l, null)))
			.verify(0, 0);
		Captor.ofBi().apply(c -> Array.applySlice(3, 1, 3, (o, l) -> c.accept(o, l, null)))
			.verify(1, 2);
		Captor.ofBi().apply(c -> Array.applySlice(3, -1, 2, (o, l) -> c.accept(o, l, null)))
			.verify(0, 2);
	}

	@Test
	public void testAcceptSlice() throws Exception {
		Array.acceptSlice(3, 1, 3, null);
		Captor.ofBi().apply(c -> Array.acceptSlice(0, 1, 3, c::accept)).verify(0, 0);
		Captor.ofBi().apply(c -> Array.acceptSlice(3, 1, 3, c::accept)).verify(1, 2);
		Captor.ofBi().apply(c -> Array.acceptSlice(3, -1, 2, c::accept)).verify(0, 2);
	}

	@Test
	public void testAcceptIndexes() {
		Array.acceptIndexes(3, 1, 3, null);
		Captor.of().apply(c -> Array.acceptIndexes(0, 0, 1, c::accept)).verify();
		Captor.of().apply(c -> Array.acceptIndexes(3, -1, 2, c::accept)).verify(0, 1);
	}

	@Test
	public void testApplyBiSlice() throws Exception {
		Assert.equal(Array.applyBiSlice(1, 0, 1, 1, 0, 1, null), null);
	}

	@Test
	public void testAcceptBiSlice() {
		Array.acceptBiSlice(1, 0, 1, 1, 0, 1, null);
	}

	@Test
	public void testSort() {
		Assert.equal(Array.sort(null), null);
		Assert.array(Array.sort(ints.clone()), null, -1, 1);
		Assert.array(Array.sort(ints.clone(), Compares.of(Compares.Nulls.last)), -1, 1,
			null);
	}

	@Test
	public void testEquals() {
		Assert.equal(Array.equals(NULL, NULL), true);
		Assert.equal(Array.equals(NULL, new Object[0]), false);
		Assert.equal(Array.equals(ints, -1, (Integer) null), false);
		Assert.equal(Array.equals(ints, -1, (Integer) null, 1), true);
		Assert.equal(Array.equals(ints, ints), true);
	}

	@Test
	public void testHash() {
		Assert.equal(Array.hash(NULL), 0);
		Assert.equal(Array.hash(ints), Objects.hash((Object[]) ints));
	}

	@Test
	public void testToString() {
		Assert.equal(Array.toString(NULL), "null");
		Assert.equal(Array.toString(new Object[0]), "[]");
		Assert.equal(Array.toString(ints), "[-1, null, 1]");
	}

	@Test
	public void testToStringWithJoiner() {
		Assert.equal(Array.toString(null, NULL), "null");
		Assert.equal(Array.toString(null, new Object[0]), "null");
		Assert.equal(Array.toString(Joiner.OR, new Object[0]), "");
		Assert.equal(Array.toString(Joiner.OR, ints), "-1|null|1");
	}

	@Test
	public void testToCustomStringWithJoiner() {
		Assert.equal(Array.toString(null, Joiner.OR, NULL), "null");
		Assert.equal(Array.toString(null, Joiner.OR, new Object[0]), "null");
		Assert.equal(Array.toString(_ -> "!", Joiner.OR, new Object[0]), "");
		Assert.equal(Array.toString(Format.HEX::uint, Joiner.OR, ints), "0xffffffff|null|0x1");
	}
}
