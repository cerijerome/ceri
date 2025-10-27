package ceri.common.collect;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.test.Assert;

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
		Assert.ordered(Lists.build(-1, 0).add(nullList).get(), -1, 0);
		Assert.ordered(Lists.build(-1, 0).add(list).get(), -1, 0, -1, null, 1);
		Assert.ordered(Lists.build(-1, 0).add(nullArray).get(), -1, 0);
		Assert.ordered(Lists.build(-1, 0).add(null, nullArray).get(), -1, 0, null);
		Assert.ordered(Lists.build(-1, 0).add(null, 1).get(), -1, 0, null, 1);
		Assert.ordered(Lists.build(-1, 0).apply(null).wrap(), -1, 0);
		Assert.ordered(Lists.build(-1, 0).apply(l -> Collectable.addAll(l, 1)).wrap(), -1, 0, 1);
		Assert.ordered(Lists.build(Lists::link, -1, null, 1).wrap(), -1, null, 1);
	}

	@Test
	public void testOfArray() {
		Assert.ordered(Lists.ofAll(nullArray));
		Assert.ordered(Lists.ofAll(emptyArray));
		Assert.ordered(Lists.ofAll(array()), -1, null, 1);
		Assert.ordered(Lists.of(nullArray, 1));
		Assert.ordered(Lists.of(emptyArray, 1));
		Assert.ordered(Lists.of(array(), 1), null, 1);
	}

	@Test
	public void testOfIterable() {
		Assert.ordered(Lists.of(nullList));
		Assert.ordered(Lists.of(emptyList));
		Assert.ordered(Lists.of(list), -1, null, 1);
		Assert.ordered(Lists.of(list.subList(1, 3)), null, 1);
	}

	@Test
	public void testAdaptArray() throws Exception {
		Assert.ordered(Lists.adaptAll(nullFn, array()));
		Assert.ordered(Lists.adaptAll(fn, nullArray));
		Assert.ordered(Lists.adaptAll(fn, emptyArray));
		Assert.ordered(Lists.adaptAll(fn, array()), "-1", "null", "1");
		Assert.ordered(Lists.adapt(nullFn, array(), 1));
		Assert.ordered(Lists.adapt(fn, nullArray, 1));
		Assert.ordered(Lists.adapt(fn, emptyArray, 1));
		Assert.ordered(Lists.adapt(fn, array(), 1), "null", "1");
	}

	@Test
	public void testAdaptIterable() throws Exception {
		Assert.ordered(Lists.adapt(nullFn, list));
		Assert.ordered(Lists.adapt(fn, nullArray, 1));
		Assert.ordered(Lists.adapt(fn, emptyArray, 1));
		Assert.ordered(Lists.adapt(fn, array(), 1), "null", "1");
	}

	@Test
	public void testConvert() throws Exception {
		Assert.ordered(Lists.convert(nullBiFn, map));
		Assert.ordered(Lists.convert(biFn, nullMap));
		Assert.ordered(Lists.convert(biFn, emptyMap));
		Assert.unordered(Lists.convert(biFn, map), -1, "B", 1);
	}

	@Test
	public void testAt() {
		Assert.equal(Lists.at(nullList, 0), null);
		Assert.equal(Lists.at(emptyList, 0), null);
		Assert.equal(Lists.at(list, -1), null);
		Assert.equal(Lists.at(list, 0), -1);
		Assert.equal(Lists.at(list, 1), null);
		Assert.equal(Lists.at(list, 2), 1);
		Assert.equal(Lists.at(list, 3), null);
	}

	@Test
	public void testLast() {
		Assert.equal(Lists.last(nullList), null);
		Assert.equal(Lists.last(emptyList), null);
		Assert.equal(Lists.last(list), 1);
	}

	@Test
	public void testSub() {
		Assert.ordered(Lists.sub(null, 0, 0));
		Assert.ordered(Lists.sub(list, 1, 4), null, 1);
	}

	@Test
	public void testSort() {
		Assert.equal(Lists.sort(nullList), null);
		Assert.ordered(Lists.sort(emptyList));
		Assert.ordered(Lists.sort(list(-1, null, 1)), null, -1, 1);
		Assert.equal(Lists.sort(null, comp), null);
		Assert.ordered(Lists.sort(list(-1, null, 1), null), -1, null, 1);
		Assert.ordered(Lists.sort(list(-1, null, 1), comp), -1, 1, null);
	}

	@Test
	public void testFill() {
		Assert.equal(Lists.fill(null, 1), null);
		Assert.ordered(Lists.fill(Lists.ofAll(0, 0, 0), null), null, null, null);
		Assert.ordered(Lists.fill(Lists.ofAll(0, 0, 0), 2, null), 0, 0, null);
	}

	@Test
	public void testInsertArray() {
		Assert.equal(Lists.insertAll(nullList, 0, array()), null);
		Assert.ordered(Lists.insertAll(list(-1, null, 1), 1, nullArray), -1, null, 1);
		Assert.ordered(Lists.insertAll(list(-1, null, 1), 1, emptyArray), -1, null, 1);
		Assert.ordered(Lists.insertAll(list(-1, null, 1), 1, array()), -1, -1, null, 1, null, 1);
		Assert.equal(Lists.insert(nullList, 0, array(), 1), null);
		Assert.ordered(Lists.insert(list(-1, null, 1), 1, nullArray, 1), -1, null, 1);
		Assert.ordered(Lists.insert(list(-1, null, 1), 1, emptyArray, 1), -1, null, 1);
		Assert.ordered(Lists.insert(list(-1, null, 1), 1, array(), 1), -1, null, 1, null, 1);
	}

	@Test
	public void testInsertIterable() {
		Assert.equal(Lists.insert(nullList, 0, list), null);
		Assert.ordered(Lists.insert(list(-1, null, 1), 1, nullList), -1, null, 1);
		Assert.ordered(Lists.insert(list(-1, null, 1), 1, emptyList), -1, null, 1);
		Assert.ordered(Lists.insert(list(-1, null, 1), 1, list), -1, -1, null, 1, null, 1);
	}
}
