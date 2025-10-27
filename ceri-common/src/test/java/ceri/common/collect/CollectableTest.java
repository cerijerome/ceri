package ceri.common.collect;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.test.Assert;

public class CollectableTest {
	private static final List<Integer> nullList = null;
	private static final List<Integer> emptyList = List.of();
	private static final List<Integer> list = Immutable.listOf(-1, null, 1);
	private static final Set<Integer> set = Immutable.setOf(-1, null, 1);
	private static final Map<Integer, String> nullMap = null;
	private static final Map<Integer, String> emptyMap = Map.of();
	private static final Map<Integer, String> map = Immutable.mapOf(-1, "A", null, "B", 1, null);
	private static final Integer[] nullArray = null;
	private static final Integer[] emptyArray = new Integer[0];
	private static final Functions.Function<Object, Object> nullFn = null;
	private static final Functions.Function<Object, Object> fn = String::valueOf;
	private static final Functions.BiOperator<Object> nullBiFn = null;
	private static final Functions.BiOperator<Object> biFn = (l, r) -> l != null ? l : r;
	private static final Functions.Supplier<Collection<Integer>> supplier = Stack::new;

	private static Integer[] array() {
		return set.toArray(Integer[]::new);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Collectable.class, Collectable.Filter.class);
	}

	@Test
	public void testFilterHas() throws Exception {
		var filter = Collectable.Filter.has(null);
		Assert.equal(filter.test(null), false);
		Assert.equal(filter.test(Lists.ofAll(1, -1)), false);
		Assert.equal(filter.test(Lists.ofAll(1, -1, null)), true);
	}

	@Test
	public void testFilterHasAll() throws Exception {
		Assert.equal(Collectable.Filter.hasAll(nullArray).test(null), true);
		Assert.equal(Collectable.Filter.hasAll(nullList).test(null), true);
		var filter = Collectable.Filter.hasAll(null, 1);
		Assert.equal(filter.test(null), false);
		Assert.equal(filter.test(Lists.ofAll(1, -1)), false);
		Assert.equal(filter.test(Lists.ofAll(1, -1, null)), true);
	}

	@Test
	public void testBuilder() {
		Assert.ordered(Collectable.build(supplier, -1, 0).add(nullList).get(), -1, 0);
		Assert.ordered(Collectable.build(supplier, -1, 0).add(list).get(), -1, 0, -1, null, 1);
		Assert.ordered(Collectable.build(supplier, -1, 0).add(nullArray).get(), -1, 0);
		Assert.ordered(Collectable.build(supplier, -1, 0).add(null, nullArray).get(), -1, 0, null);
		Assert.ordered(Collectable.build(supplier, -1, 0).add(null, 1).get(), -1, 0, null, 1);
		Assert.ordered(Collectable.build(supplier, -1, 0).apply(null).wrap(), -1, 0);
		Assert.ordered(Collectable.build(supplier, -1, 0).apply( //
			l -> Collectable.addAll(l, 1)).wrap(), -1, 0, 1);
	}

	@Test
	public void testIsEmpty() {
		Assert.equal(Collectable.isEmpty(null), true);
		Assert.equal(Collectable.isEmpty(emptyList), true);
		Assert.equal(Collectable.isEmpty(list), false);
	}

	@Test
	public void testNonEmpty() {
		Assert.equal(Collectable.nonEmpty(null), false);
		Assert.equal(Collectable.nonEmpty(emptyList), false);
		Assert.equal(Collectable.nonEmpty(list), true);
	}

	@Test
	public void testSize() {
		Assert.equal(Collectable.size(null), 0);
		Assert.equal(Collectable.size(emptyList), 0);
		Assert.equal(Collectable.size(list), 3);
	}

	@Test
	public void testAddTo() {
		Assert.equal(Collectable.addTo(null, 1), null);
		Assert.ordered(Collectable.addTo(Lists.ofAll(-1), 1), -1, 1);
	}

	@Test
	public void testAddArray() {
		Assert.equal(Collectable.addAll(null, array()), null);
		Assert.ordered(Collectable.addAll(Lists.of(), nullArray));
		Assert.ordered(Collectable.addAll(Lists.of(), emptyArray));
		Assert.ordered(Collectable.add(Lists.of(), array(), 1), null, 1);
	}

	@Test
	public void testAddIterable() {
		Assert.equal(Collectable.add(null, list), null);
		Assert.ordered(Collectable.add(Lists.of(), nullList));
		Assert.ordered(Collectable.add(Lists.of(), list), -1, null, 1);
	}

	@Test
	public void testAdaptAddArray() {
		Assert.equal(Collectable.adaptAddAll(null, fn, array()), null);
		Assert.ordered(Collectable.adaptAddAll(Lists.of(), nullFn, array()));
		Assert.ordered(Collectable.adaptAddAll(Lists.of(), fn, nullArray));
		Assert.ordered(Collectable.adaptAddAll(Lists.of(), fn, emptyArray));
		Assert.ordered(Collectable.adaptAddAll(Lists.of(), fn, array()), "-1", "null", "1");
		Assert.equal(Collectable.adaptAdd(null, fn, array(), 1), null);
		Assert.ordered(Collectable.adaptAdd(Lists.of(), nullFn, array(), 1));
		Assert.ordered(Collectable.adaptAdd(Lists.of(), fn, nullArray, 1));
		Assert.ordered(Collectable.adaptAdd(Lists.of(), fn, emptyArray, 1));
		Assert.ordered(Collectable.adaptAdd(Lists.of(), fn, array(), 1), "null", "1");
	}

	@Test
	public void testAdaptAddIterable() {
		Assert.equal(Collectable.adaptAdd(null, fn, list), null);
		Assert.ordered(Collectable.adaptAdd(Lists.of(), nullFn, list));
		Assert.ordered(Collectable.adaptAdd(Lists.of(), fn, nullList));
		Assert.ordered(Collectable.adaptAdd(Lists.of(), fn, list), "-1", "null", "1");
	}

	@Test
	public void testConvertAdd() throws Exception {
		Assert.equal(Collectable.convertAdd(null, biFn, map), null);
		Assert.ordered(Collectable.convertAdd(Lists.of(), nullBiFn, map));
		Assert.ordered(Collectable.convertAdd(Lists.of(), biFn, nullMap));
		Assert.ordered(Collectable.convertAdd(Lists.of(), biFn, emptyMap));
		Assert.unordered(Collectable.convertAdd(Lists.of(), biFn, map), -1, "B", 1);
	}

	@Test
	public void testRemoveAll() {
		Assert.equal(Collectable.removeAll(null, nullArray), null);
		Assert.ordered(Collectable.removeAll(Lists.ofAll(1, -1), nullArray), 1, -1);
		Assert.ordered(Collectable.removeAll(Lists.ofAll(1, null), -1, null), 1);
	}
}
