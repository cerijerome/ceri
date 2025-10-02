package ceri.common.collect;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import ceri.common.function.Functions;

public class ListsTest {
	private static final List<Integer> nullList = null;
	private static final List<Integer> emptyList = List.of();
	private static final List<Integer> list = Immutable.listOf(-1, null, 1);
	private static final Map<Integer, String> nullMap = null;
	private static final Map<Integer, String> emptyMap = Map.of();
	private static final Map<Integer, String> map = Immutable.mapOf(-1, "A", null, "B", 1, null);
	private static final Integer[] nullArray = null;
	private static final Integer[] emptyArray = new Integer[0];
	private static final Functions.Function<Object, Object> nullFn = null;
	private static final Functions.Function<Object, Object> fn = String::valueOf;
	private static final Functions.BiOperator<Object> nullBiFn = null;
	private static final Functions.BiOperator<Object> biFn = (l, r) -> l != null ? l : r;
	private static final Comparator<Object> comp = Comparator.comparing(String::valueOf);

	private static Integer[] array() {
		return list.toArray(Integer[]::new);
	}

	@SafeVarargs
	private static <T> List<T> list(T... ts) {
		return Lists.ofAll(ts);
	}

	@Test
	public void testBuilder() {
		assertOrdered(Lists.build(-1, 0).add(nullList).get(), -1, 0);
		assertOrdered(Lists.build(-1, 0).add(list).get(), -1, 0, -1, null, 1);
		assertOrdered(Lists.build(-1, 0).add(nullArray).get(), -1, 0);
		assertOrdered(Lists.build(-1, 0).add(null, nullArray).get(), -1, 0, null);
		assertOrdered(Lists.build(-1, 0).add(null, 1).get(), -1, 0, null, 1);
		assertOrdered(Lists.build(-1, 0).apply(null).wrap(), -1, 0);
		assertOrdered(Lists.build(-1, 0).apply(l -> Collectable.addAll(l, 1)).wrap(), -1, 0, 1);
		assertOrdered(Lists.build(Lists::link, -1, null, 1).wrap(), -1, null, 1);
	}

	@Test
	public void testOfArray() {
		assertOrdered(Lists.ofAll(nullArray));
		assertOrdered(Lists.ofAll(emptyArray));
		assertOrdered(Lists.ofAll(array()), -1, null, 1);
		assertOrdered(Lists.of(nullArray, 1));
		assertOrdered(Lists.of(emptyArray, 1));
		assertOrdered(Lists.of(array(), 1), null, 1);
	}

	@Test
	public void testOfIterable() {
		assertOrdered(Lists.of(nullList));
		assertOrdered(Lists.of(emptyList));
		assertOrdered(Lists.of(list), -1, null, 1);
		assertOrdered(Lists.of(list.subList(1, 3)), null, 1);
	}

	@Test
	public void testAdaptArray() throws Exception {
		assertOrdered(Lists.adaptAll(nullFn, array()));
		assertOrdered(Lists.adaptAll(fn, nullArray));
		assertOrdered(Lists.adaptAll(fn, emptyArray));
		assertOrdered(Lists.adaptAll(fn, array()), "-1", "null", "1");
		assertOrdered(Lists.adapt(nullFn, array(), 1));
		assertOrdered(Lists.adapt(fn, nullArray, 1));
		assertOrdered(Lists.adapt(fn, emptyArray, 1));
		assertOrdered(Lists.adapt(fn, array(), 1), "null", "1");
	}

	@Test
	public void testAdaptIterable() throws Exception {
		assertOrdered(Lists.adapt(nullFn, list));
		assertOrdered(Lists.adapt(fn, nullArray, 1));
		assertOrdered(Lists.adapt(fn, emptyArray, 1));
		assertOrdered(Lists.adapt(fn, array(), 1), "null", "1");
	}

	@Test
	public void testConvert() throws Exception {
		assertOrdered(Lists.convert(nullBiFn, map));
		assertOrdered(Lists.convert(biFn, nullMap));
		assertOrdered(Lists.convert(biFn, emptyMap));
		assertUnordered(Lists.convert(biFn, map), -1, "B", 1);
	}

	@Test
	public void testAt() {
		assertEquals(Lists.at(nullList, 0), null);
		assertEquals(Lists.at(emptyList, 0), null);
		assertEquals(Lists.at(list, -1), null);
		assertEquals(Lists.at(list, 0), -1);
		assertEquals(Lists.at(list, 1), null);
		assertEquals(Lists.at(list, 2), 1);
		assertEquals(Lists.at(list, 3), null);
	}

	@Test
	public void testLast() {
		assertEquals(Lists.last(nullList), null);
		assertEquals(Lists.last(emptyList), null);
		assertEquals(Lists.last(list), 1);
	}

	@Test
	public void testSort() {
		assertEquals(Lists.sort(nullList), null);
		assertOrdered(Lists.sort(emptyList));
		assertOrdered(Lists.sort(list(-1, null, 1)), null, -1, 1);
		assertEquals(Lists.sort(null, comp), null);
		assertOrdered(Lists.sort(list(-1, null, 1), null), -1, null, 1);
		assertOrdered(Lists.sort(list(-1, null, 1), comp), -1, 1, null);
	}

	@Test
	public void testInsertArray() {
		assertEquals(Lists.insertAll(nullList, 0, array()), null);
		assertOrdered(Lists.insertAll(list(-1, null, 1), 1, nullArray), -1, null, 1);
		assertOrdered(Lists.insertAll(list(-1, null, 1), 1, emptyArray), -1, null, 1);
		assertOrdered(Lists.insertAll(list(-1, null, 1), 1, array()), -1, -1, null, 1, null, 1);
		assertEquals(Lists.insert(nullList, 0, array(), 1), null);
		assertOrdered(Lists.insert(list(-1, null, 1), 1, nullArray, 1), -1, null, 1);
		assertOrdered(Lists.insert(list(-1, null, 1), 1, emptyArray, 1), -1, null, 1);
		assertOrdered(Lists.insert(list(-1, null, 1), 1, array(), 1), -1, null, 1, null, 1);
	}

	@Test
	public void testInsertIterable() {
		assertEquals(Lists.insert(nullList, 0, list), null);
		assertOrdered(Lists.insert(list(-1, null, 1), 1, nullList), -1, null, 1);
		assertOrdered(Lists.insert(list(-1, null, 1), 1, emptyList), -1, null, 1);
		assertOrdered(Lists.insert(list(-1, null, 1), 1, list), -1, -1, null, 1, null, 1);
	}
}
