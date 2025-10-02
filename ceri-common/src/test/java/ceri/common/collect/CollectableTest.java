package ceri.common.collect;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.junit.Test;
import ceri.common.function.Functions;

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
	public void testBuilder() {
		assertOrdered(Collectable.build(supplier, -1, 0).add(nullList).get(), -1, 0);
		assertOrdered(Collectable.build(supplier, -1, 0).add(list).get(), -1, 0, -1, null, 1);
		assertOrdered(Collectable.build(supplier, -1, 0).add(nullArray).get(), -1, 0);
		assertOrdered(Collectable.build(supplier, -1, 0).add(null, nullArray).get(), -1, 0, null);
		assertOrdered(Collectable.build(supplier, -1, 0).add(null, 1).get(), -1, 0, null, 1);
		assertOrdered(Collectable.build(supplier, -1, 0).apply(null).wrap(), -1, 0);
		assertOrdered(Collectable.build(supplier, -1, 0).apply( //
			l -> Collectable.addAll(l, 1)).wrap(), -1, 0, 1);
	}

	@Test
	public void testIsEmpty() {
		assertEquals(Collectable.isEmpty(null), true);
		assertEquals(Collectable.isEmpty(emptyList), true);
		assertEquals(Collectable.isEmpty(list), false);
	}

	@Test
	public void testNonEmpty() {
		assertEquals(Collectable.nonEmpty(null), false);
		assertEquals(Collectable.nonEmpty(emptyList), false);
		assertEquals(Collectable.nonEmpty(list), true);
	}

	@Test
	public void testSize() {
		assertEquals(Collectable.size(null), 0);
		assertEquals(Collectable.size(emptyList), 0);
		assertEquals(Collectable.size(list), 3);
	}

	@Test
	public void testAddArray() {
		assertEquals(Collectable.addAll(null, array()), null);
		assertOrdered(Collectable.addAll(Lists.of(), nullArray));
		assertOrdered(Collectable.addAll(Lists.of(), emptyArray));
		assertOrdered(Collectable.add(Lists.of(), array(), 1), null, 1);
	}

	@Test
	public void testAddIterable() {
		assertEquals(Collectable.add(null, list), null);
		assertOrdered(Collectable.add(Lists.of(), nullList));
		assertOrdered(Collectable.add(Lists.of(), list), -1, null, 1);
	}

	@Test
	public void testAdaptAddArray() {
		assertEquals(Collectable.adaptAddAll(null, fn, array()), null);
		assertOrdered(Collectable.adaptAddAll(Lists.of(), nullFn, array()));
		assertOrdered(Collectable.adaptAddAll(Lists.of(), fn, nullArray));
		assertOrdered(Collectable.adaptAddAll(Lists.of(), fn, emptyArray));
		assertOrdered(Collectable.adaptAddAll(Lists.of(), fn, array()), "-1", "null", "1");
		assertEquals(Collectable.adaptAdd(null, fn, array(), 1), null);
		assertOrdered(Collectable.adaptAdd(Lists.of(), nullFn, array(), 1));
		assertOrdered(Collectable.adaptAdd(Lists.of(), fn, nullArray, 1));
		assertOrdered(Collectable.adaptAdd(Lists.of(), fn, emptyArray, 1));
		assertOrdered(Collectable.adaptAdd(Lists.of(), fn, array(), 1), "null", "1");
	}

	@Test
	public void testAdaptAddIterable() {
		assertEquals(Collectable.adaptAdd(null, fn, list), null);
		assertOrdered(Collectable.adaptAdd(Lists.of(), nullFn, list));
		assertOrdered(Collectable.adaptAdd(Lists.of(), fn, nullList));
		assertOrdered(Collectable.adaptAdd(Lists.of(), fn, list), "-1", "null", "1");
	}

	@Test
	public void testConvertAdd() throws Exception {
		assertEquals(Collectable.convertAdd(null, biFn, map), null);
		assertOrdered(Collectable.convertAdd(Lists.of(), nullBiFn, map));
		assertOrdered(Collectable.convertAdd(Lists.of(), biFn, nullMap));
		assertOrdered(Collectable.convertAdd(Lists.of(), biFn, emptyMap));
		assertUnordered(Collectable.convertAdd(Lists.of(), biFn, map), -1, "B", 1);
	}
}
