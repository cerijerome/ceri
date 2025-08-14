package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import ceri.common.collection.Immutable.Wrap;
import ceri.common.function.Functions;
import ceri.common.util.BasicUtil;

public class MutableTest {
	private static final String s0 = new String("s");
	private static final String s1 = new String("s");
	private static final List<Integer> nullList = null;
	private static final List<Integer> emptyList = List.of();
	private static final List<Integer> list = Immutable.listOf(-1, null, 1);
	private static final Set<Integer> nullSet = null;
	private static final Set<Integer> emptySet = Set.of();
	private static final Set<Integer> set = Immutable.setOf(-1, null, 1);
	private static final Set<String> idSet = Immutable.ofAll(Wrap.idSet(), s0, null, s1);
	private static final Map<Integer, String> nullMap = null;
	private static final Map<Integer, String> emptyMap = Map.of();
	private static final Map<Integer, String> map = Immutable.mapOf(-1, "A", null, "B", 1, null);
	private static final Map<String, Object> idMap = Immutable.of(Wrap.idMap(), s0, -1, null, 1, s1, null);
	private static final Integer[] nullArray = null;
	private static final Integer[] emptyArray = new Integer[0];
	private static final Functions.Function<Object, Object> nullFn = null;
	private static final Functions.Function<Object, Object> fn = String::valueOf;
	private static final Functions.BiOperator<Object> nullBiFn = null;
	private static final Functions.BiOperator<Object> biFn = (l, r) -> l != null ? l : r;
	private static final Functions.BiPredicate<Object, Object> nullBiPr = null;
	private static final Functions.BiPredicate<Object, Object> biPr = BasicUtil::noneNull;

	private static Integer[] array() {
		return list.toArray(Integer[]::new);
	}

	@SafeVarargs
	private static <T> List<T> list(T... ts) {
		return Mutable.listOf(ts);
	}

	private static <K, V> Map<K, V> map(K k, V v) {
		return Mutable.mapOf(k, v);
	}

	@Test
	public void testBuilderPopulation() {
		var map =
			Mutable.builder(-1, "A").apply(null).apply(m -> Mutable.put(m, null, "B", 1, null)).map;
		assertMap(map, -1, "A", null, "B", 1, null);
	}

	@Test
	public void testListFromArray() {
		assertOrdered(Mutable.listOf(nullArray));
		assertOrdered(Mutable.listOf(emptyArray));
		assertOrdered(Mutable.listOf(array()), -1, null, 1);
		assertOrdered(Mutable.list(nullArray, 1));
		assertOrdered(Mutable.list(emptyArray, 1));
		assertOrdered(Mutable.list(array(), 1), null, 1);
	}

	@Test
	public void testListFromIterable() {
		assertOrdered(Mutable.list(nullList));
		assertOrdered(Mutable.list(emptyList));
		assertOrdered(Mutable.list(list), -1, null, 1);
		assertOrdered(Mutable.list(list.subList(1, 3)), null, 1);
	}

	@Test
	public void testAdaptListFromArray() throws Exception {
		assertOrdered(Mutable.adaptListOf(nullFn, array()));
		assertOrdered(Mutable.adaptListOf(fn, nullArray));
		assertOrdered(Mutable.adaptListOf(fn, emptyArray));
		assertOrdered(Mutable.adaptListOf(fn, array()), "-1", "null", "1");
		assertOrdered(Mutable.adaptList(nullFn, array(), 1));
		assertOrdered(Mutable.adaptList(fn, nullArray, 1));
		assertOrdered(Mutable.adaptList(fn, emptyArray, 1));
		assertOrdered(Mutable.adaptList(fn, array(), 1), "null", "1");
	}

	@Test
	public void testAdaptListFromIterable() throws Exception {
		assertOrdered(Mutable.adaptList(nullFn, list));
		assertOrdered(Mutable.adaptList(fn, nullArray, 1));
		assertOrdered(Mutable.adaptList(fn, emptyArray, 1));
		assertOrdered(Mutable.adaptList(fn, array(), 1), "null", "1");
	}

	@Test
	public void testUnmapList() throws Exception {
		assertOrdered(Mutable.convertList(nullBiFn, map));
		assertOrdered(Mutable.convertList(biFn, nullMap));
		assertOrdered(Mutable.convertList(biFn, emptyMap));
		assertUnordered(Mutable.convertList(biFn, map), -1, "B", 1);
	}

	@Test
	public void testAt() {
		assertEquals(Mutable.at(nullList, 0), null);
		assertEquals(Mutable.at(emptyList, 0), null);
		assertEquals(Mutable.at(list, -1), null);
		assertEquals(Mutable.at(list, 5), null);
		assertEquals(Mutable.at(list, 2), 1);
		assertEquals(Mutable.at(list, 3, 0), 0);
	}

	@Test
	public void testLast() {
		assertEquals(Mutable.last(nullList), null);
		assertEquals(Mutable.last(emptyList), null);
		assertEquals(Mutable.last(list), 1);
	}

	@Test
	public void testInsertArray() {
		assertEquals(Mutable.insertAll(null, 0, array()), null);
		assertOrdered(Mutable.insertAll(list(-1, null, 1), 1, nullArray), -1, null, 1);
		assertOrdered(Mutable.insertAll(list(-1, null, 1), 1, emptyArray), -1, null, 1);
		assertOrdered(Mutable.insertAll(list(-1, null, 1), 1, array()), -1, -1, null, 1, null, 1);
		assertEquals(Mutable.insert(null, 0, array(), 1), null);
		assertOrdered(Mutable.insert(list(-1, null, 1), 1, nullArray, 1), -1, null, 1);
		assertOrdered(Mutable.insert(list(-1, null, 1), 1, emptyArray, 1), -1, null, 1);
		assertOrdered(Mutable.insert(list(-1, null, 1), 1, array(), 1), -1, null, 1, null, 1);
	}

	@Test
	public void testInsertIterable() {
		assertEquals(Mutable.insert(null, 0, list), null);
		assertOrdered(Mutable.insert(list(-1, null, 1), 1, nullList), -1, null, 1);
		assertOrdered(Mutable.insert(list(-1, null, 1), 1, emptyList), -1, null, 1);
		assertOrdered(Mutable.insert(list(-1, null, 1), 1, list), -1, -1, null, 1, null, 1);
	}

	@Test
	public void testIdSet() {
		var s0 = new String("test");
		var s1 = new String("test");
		var set = Mutable.addAll(Mutable.idSet(), s0, s1);
		assertUnordered(set, s0, s1);
	}

	@Test
	public void testSetFromArray() {
		assertUnordered(Mutable.setOf(nullArray));
		assertUnordered(Mutable.setOf(emptyArray));
		assertUnordered(Mutable.setOf(array()), -1, null, 1);
		assertUnordered(Mutable.set(nullArray, 1));
		assertUnordered(Mutable.set(emptyArray, 1));
		assertUnordered(Mutable.set(array(), 1), null, 1);
	}

	@Test
	public void testSetFromIterable() {
		assertUnordered(Mutable.set(nullSet));
		assertUnordered(Mutable.set(emptySet));
		assertUnordered(Mutable.set(set), -1, null, 1);
		assertUnordered(Mutable.set(list.subList(1, 3)), null, 1);
	}

	@Test
	public void testAdaptSetFromArray() throws Exception {
		assertUnordered(Mutable.adaptSetOf(nullFn, array()));
		assertUnordered(Mutable.adaptSetOf(fn, nullArray));
		assertUnordered(Mutable.adaptSetOf(fn, emptyArray));
		assertUnordered(Mutable.adaptSetOf(fn, array()), "-1", "null", "1");
		assertUnordered(Mutable.adaptSet(nullFn, array(), 1));
		assertUnordered(Mutable.adaptSet(fn, nullArray, 1));
		assertUnordered(Mutable.adaptSet(fn, emptyArray, 1));
		assertUnordered(Mutable.adaptSet(fn, array(), 1), "null", "1");
	}

	@Test
	public void testAdaptSetFromIterable() throws Exception {
		assertUnordered(Mutable.adaptSet(nullFn, set));
		assertUnordered(Mutable.adaptSet(fn, nullArray, 1));
		assertUnordered(Mutable.adaptSet(fn, emptyArray, 1));
		assertUnordered(Mutable.adaptSet(fn, array(), 1), "null", "1");
	}

	@Test
	public void testUnmapSet() throws Exception {
		assertUnordered(Mutable.convertSet(nullBiFn, map));
		assertUnordered(Mutable.convertSet(biFn, nullMap));
		assertUnordered(Mutable.convertSet(biFn, emptyMap));
		assertUnordered(Mutable.convertSet(biFn, map), -1, "B", 1);
	}

	@Test
	public void testAddArray() {
		assertEquals(Mutable.addAll(null, array()), null);
		assertOrdered(Mutable.addAll(list(-1, null, 1), nullArray), -1, null, 1);
		assertOrdered(Mutable.addAll(list(-1, null, 1), emptyArray), -1, null, 1);
		assertEquals(Mutable.add(null, array(), 1), null);
		assertOrdered(Mutable.add(list(-1, null, 1), nullArray, 1), -1, null, 1);
		assertOrdered(Mutable.add(list(-1, null, 1), emptyArray, 1), -1, null, 1);
	}

	@Test
	public void testAddIterable() {
		assertEquals(Mutable.add(null, list), null);
		assertOrdered(Mutable.add(list(-1, null, 1), nullList), -1, null, 1);
		assertOrdered(Mutable.add(list(-1, null, 1), emptyList), -1, null, 1);
	}

	@Test
	public void testAdaptAddArray() {
		assertEquals(Mutable.adaptAddAll(null, fn, array()), null);
		assertOrdered(Mutable.adaptAddAll(list("1", null), nullFn, array()), "1", null);
		assertOrdered(Mutable.adaptAddAll(list("1", null), fn, nullArray), "1", null);
		assertOrdered(Mutable.adaptAddAll(list("1", null), fn, emptyArray), "1", null);
		assertEquals(Mutable.adaptAdd(null, fn, array(), 1), null);
		assertOrdered(Mutable.adaptAdd(list("1", null), nullFn, array(), 1), "1", null);
		assertOrdered(Mutable.adaptAdd(list("1", null), fn, nullArray, 1), "1", null);
		assertOrdered(Mutable.adaptAdd(list("1", null), fn, emptyArray, 1), "1", null);
	}

	@Test
	public void testAdaptAddIterable() {
		assertEquals(Mutable.adaptAdd(null, fn, list), null);
		assertOrdered(Mutable.adaptAdd(list("1", null), nullFn, list), "1", null);
		assertOrdered(Mutable.adaptAdd(list("1", null), fn, nullList), "1", null);
		assertOrdered(Mutable.adaptAdd(list("1", null), fn, emptyList), "1", null);
	}

	@Test
	public void testUnmapAdd() throws Exception {
		assertEquals(Mutable.convertAdd(null, biFn, map), null);
		assertOrdered(Mutable.convertAdd(list("1", null), nullBiFn, map), "1", null);
		assertOrdered(Mutable.convertAdd(list("1", null), biFn, nullMap), "1", null);
		assertOrdered(Mutable.convertAdd(list("1", null), biFn, emptyMap), "1", null);
	}

	@Test
	public void testMapFromKeysAndValues() {
		var map = Mutable.mapOf(1, "A", 2, null, null, "C", 4, "D", null, null);
		assertMap(map, 1, "A", 2, null, null, null, 4, "D");
		assertMap(Mutable.map(map), 1, "A", 2, null, null, null, 4, "D");
	}

	@Test
	public void testConvertMap() {
		assertMap(Mutable.convertMap(nullFn, list));
		assertMap(Mutable.convertMap(fn, nullList));
		assertMap(Mutable.convertMap(fn, emptyList));
		assertMap(Mutable.convertMap(fn, list), "-1", -1, "null", null, "1", 1);
		assertMap(Mutable.convertMap(nullFn, fn, list));
		assertMap(Mutable.convertMap(fn, nullFn, list));
		assertMap(Mutable.convertMap(fn, fn, nullList));
		assertMap(Mutable.convertMap(fn, fn, emptyList));
		assertMap(Mutable.convertMap(fn, fn, list), "-1", "-1", "null", "null", "1", "1");
	}

	@Test
	public void testAdaptMap() {
		assertMap(Mutable.adaptMap(nullFn, map));
		assertMap(Mutable.adaptMap(fn, nullMap));
		assertMap(Mutable.adaptMap(fn, emptyMap));
		assertMap(Mutable.adaptMap(fn, map), "-1", "A", "null", "B", "1", null);
		assertMap(Mutable.adaptMap(nullFn, fn, map));
		assertMap(Mutable.adaptMap(fn, nullFn, map));
		assertMap(Mutable.adaptMap(fn, fn, nullMap));
		assertMap(Mutable.adaptMap(fn, fn, emptyMap));
		assertMap(Mutable.adaptMap(fn, fn, map), "-1", "A", "null", "B", "1", "null");
	}

	@Test
	public void testBiAdaptMap() {
		assertMap(Mutable.biAdaptMap(nullBiFn, map));
		assertMap(Mutable.biAdaptMap(biFn, nullMap));
		assertMap(Mutable.biAdaptMap(biFn, emptyMap));
		assertMap(Mutable.biAdaptMap(biFn, map), -1, "A", "B", "B", 1, null);
		assertMap(Mutable.biAdaptMap(nullBiFn, biFn, map));
		assertMap(Mutable.biAdaptMap(biFn, nullBiFn, map));
		assertMap(Mutable.biAdaptMap(biFn, biFn, nullMap));
		assertMap(Mutable.biAdaptMap(biFn, biFn, emptyMap));
		assertMap(Mutable.biAdaptMap(biFn, biFn, map), -1, -1, "B", "B", 1, 1);
	}

	@Test
	public void testInvertMap() {
		assertMap(Mutable.invertMap(nullMap));
		assertMap(Mutable.invertMap(emptyMap));
		assertMap(Mutable.invertMap(map), "A", -1, "B", null, null, 1);
	}

	@Test
	public void testKeys() {
		assertStream(Mutable.keys(nullBiPr, map), -1, null, 1);
		assertStream(Mutable.keys(biPr, nullMap));
		assertStream(Mutable.keys(biPr, emptyMap));
		assertStream(Mutable.keys(biPr, map), -1);
	}

	@Test
	public void testValues() {
		assertStream(Mutable.values(nullBiPr, map), "A", "B", null);
		assertStream(Mutable.values(biPr, nullMap));
		assertStream(Mutable.values(biPr, emptyMap));
		assertStream(Mutable.values(biPr, map), "A");
	}

	@Test
	public void testPut() {
		assertEquals(Mutable.put(nullMap, 1, "A"), null);
		assertMap(Mutable.put(map(1, ""), null, null), 1, "", null, null);
		assertMap(Mutable.put(map(1, ""), 1, "A"), 1, "A");
		assertEquals(Mutable.put(nullMap, map), null);
		assertMap(Mutable.put(map(1, ""), nullMap), 1, "");
		assertMap(Mutable.put(map(1, ""), emptyMap), 1, "");
		assertMap(Mutable.put(map(1, ""), map), -1, "A", null, "B", 1, null);
	}

	@Test
	public void testPutIfAbsent() {
		assertEquals(Mutable.putIfAbsent(nullMap, 1, "A"), null);
		assertMap(Mutable.putIfAbsent(map(1, ""), null, null), 1, "", null, null);
		assertMap(Mutable.putIfAbsent(map(1, ""), 1, "A"), 1, "");
		assertEquals(Mutable.putIfAbsent(nullMap, map), null);
		assertMap(Mutable.putIfAbsent(map(1, ""), nullMap), 1, "");
		assertMap(Mutable.putIfAbsent(map(1, ""), emptyMap), 1, "");
		assertMap(Mutable.putIfAbsent(map(1, ""), map), -1, "A", null, "B", 1, "");
	}

	@Test
	public void testMapPut() {
		assertEquals(Mutable.convertPut(null, fn, fn, list), null);
		assertMap(Mutable.convertPut(map(1, ""), nullFn, fn, list), 1, "");
		assertMap(Mutable.convertPut(map(1, ""), fn, nullFn, list), 1, "");
		assertMap(Mutable.convertPut(map(1, ""), fn, fn, nullList), 1, "");
		assertMap(Mutable.convertPut(map(1, ""), fn, fn, emptyList), 1, "");
		assertMap(Mutable.convertPut(map("1", ""), fn, fn, list), "-1", "-1", "null", "null", "1", "1");
		assertMap(Mutable.convertPut(map(1, ""), fn, fn, list), "-1", "-1", "null", "null", "1", "1", 1,
			"");
	}

	@Test
	public void testMapPutIfAbsent() {
		assertEquals(Mutable.convertPutIfAbsent(null, fn, fn, list), null);
		assertMap(Mutable.convertPutIfAbsent(map(1, ""), nullFn, fn, list), 1, "");
		assertMap(Mutable.convertPutIfAbsent(map(1, ""), fn, nullFn, list), 1, "");
		assertMap(Mutable.convertPutIfAbsent(map(1, ""), fn, fn, nullList), 1, "");
		assertMap(Mutable.convertPutIfAbsent(map(1, ""), fn, fn, emptyList), 1, "");
		assertMap(Mutable.convertPutIfAbsent(map("1", ""), fn, fn, list), "-1", "-1", "null", "null",
			"1", "");
		assertMap(Mutable
			.convertPutIfAbsent(map(1, ""), fn, fn, list), "-1", "-1", "null", "null", "1",
			"1", 1, "");
	}

	@Test
	public void testAdaptPut() {
		assertEquals(Mutable.adaptPut(null, fn, fn, map), null);
		assertMap(Mutable.adaptPut(map(1, ""), nullFn, fn, map), 1, "");
		assertMap(Mutable.adaptPut(map(1, ""), fn, nullFn, map), 1, "");
		assertMap(Mutable.adaptPut(map(1, ""), fn, fn, nullMap), 1, "");
		assertMap(Mutable.adaptPut(map(1, ""), fn, fn, emptyMap), 1, "");
		assertMap(Mutable.adaptPut(map("1", ""), fn, fn, map), "-1", "A", "null", "B", "1", "null");
		assertMap(Mutable.adaptPut(map(1, ""), fn, fn, map), "-1", "A", "null", "B", "1", "null", 1,
			"");
	}

	@Test
	public void testAdaptPutIfAbsent() {
		assertEquals(Mutable.adaptPutIfAbsent(null, fn, fn, map), null);
		assertMap(Mutable.adaptPutIfAbsent(map(1, ""), nullFn, fn, map), 1, "");
		assertMap(Mutable.adaptPutIfAbsent(map(1, ""), fn, nullFn, map), 1, "");
		assertMap(Mutable.adaptPutIfAbsent(map(1, ""), fn, fn, nullMap), 1, "");
		assertMap(Mutable.adaptPutIfAbsent(map(1, ""), fn, fn, emptyMap), 1, "");
		assertMap(Mutable.adaptPutIfAbsent(map("1", ""), fn, fn, map), "-1", "A", "null", "B", "1",
			"");
		assertMap(Mutable.adaptPutIfAbsent(map(1, ""), fn, fn, map), "-1", "A", "null", "B", "1",
			"null", 1, "");
	}

	@Test
	public void testBiAdaptPut() {
		assertEquals(Mutable.biAdaptPut(null, biFn, biFn, map), null);
		assertMap(Mutable.biAdaptPut(map(1, ""), nullBiFn, biFn, map), 1, "");
		assertMap(Mutable.biAdaptPut(map(1, ""), biFn, nullBiFn, map), 1, "");
		assertMap(Mutable.biAdaptPut(map(1, ""), biFn, biFn, nullMap), 1, "");
		assertMap(Mutable.biAdaptPut(map(1, ""), biFn, biFn, emptyMap), 1, "");
		assertMap(Mutable.biAdaptPut(map(1, ""), biFn, biFn, map), -1, -1, "B", "B", 1, 1);
		assertMap(Mutable.biAdaptPut(map("1", ""), biFn, biFn, map), -1, -1, "B", "B", 1, 1, "1",
			"");
	}

	@Test
	public void testBiAdaptPutIfAbsent() {
		assertEquals(Mutable.biAdaptPutIfAbsent(null, biFn, biFn, map), null);
		assertMap(Mutable.biAdaptPutIfAbsent(map(1, ""), nullBiFn, biFn, map), 1, "");
		assertMap(Mutable.biAdaptPutIfAbsent(map(1, ""), biFn, nullBiFn, map), 1, "");
		assertMap(Mutable.biAdaptPutIfAbsent(map(1, ""), biFn, biFn, nullMap), 1, "");
		assertMap(Mutable.biAdaptPutIfAbsent(map(1, ""), biFn, biFn, emptyMap), 1, "");
		assertMap(Mutable.biAdaptPutIfAbsent(map(1, ""), biFn, biFn, map), -1, -1, "B", "B", 1, "");
		assertMap(Mutable.biAdaptPutIfAbsent(map("1", ""), biFn, biFn, map), -1, -1, "B", "B", 1, 1,
			"1", "");
	}

}
