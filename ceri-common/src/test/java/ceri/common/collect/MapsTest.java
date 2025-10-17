package ceri.common.collect;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalState;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.junit.Test;
import ceri.common.collect.Immutable.Wrap;
import ceri.common.collect.Maps.Put;
import ceri.common.function.Functions;
import ceri.common.test.Captor;
import ceri.common.util.Basics;

public class MapsTest {
	private static final List<Integer> nullList = null;
	private static final List<Integer> emptyList = List.of();
	private static final List<Integer> list = Immutable.listOf(-1, null, 1);
	private static final TreeMap<Integer, String> nullMap = null;
	private static final NavigableMap<Integer, String> emptyMap =
		Immutable.wrapNav(Maps.<Integer, String>tree());
	private static final NavigableMap<Integer, String> map =
		Immutable.of(Wrap.navMap(), -1, "A", null, "B", 1, null);
	private static final Integer[] nullArray = null;
	private static final Functions.Function<Object, Object> nullFn = null;
	private static final Functions.Function<Object, Object> fn = String::valueOf;
	private static final Functions.BiOperator<Object> nullBiFn = null;
	private static final Functions.BiOperator<Object> biFn = (l, r) -> l != null ? l : r;
	private static final Functions.BiPredicate<Object, Object> nullBiPr = null;
	private static final Functions.BiPredicate<Object, Object> biPr = Basics::noneNull;

	private static Integer[] array() {
		return list.toArray(Integer[]::new);
	}

	private static <K, V> Map<K, V> map(K k, V v) {
		return Maps.of(k, v);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Maps.class, Maps.Filter.class, Maps.Compare.class);
	}

	@Test
	public void testCompare() {
		var map = Maps.of(1, "B", -1, "C", 0, "A");
		assertOrdered(Maps.sort(Maps.Compare.key(), map), -1, "C", 0, "A", 1, "B");
		assertOrdered(Maps.sort(Maps.Compare.value(), map), 0, "A", 1, "B", -1, "C");
	}

	@Test
	public void testPutType() {
		assertEquals(Put.put(Put.def, null, -1, "A"), null);
		assertMap(apply(Maps.of(-1, "A"), //
			m -> assertEquals(Put.put(null, m, -1, null), null)), -1, "A");
		assertMap(apply(Maps.of(-1, "A"), //
			m -> assertEquals(Put.first.put(m, -1, null), "A")), -1, "A");
		assertMap(apply(Maps.of(-1, "A"), //
			m -> assertEquals(Put.last.put(m, -1, null), "A")), -1, null);
		assertMap(apply(Maps.of(-1, "A"), //
			m -> assertEquals(Put.unique.put(m, 1, null), null)), -1, "A", 1, null);
		assertMap(apply(Maps.of(-1, "A"), //
			m -> assertIllegalState(() -> Put.unique.put(m, -1, null))), -1, "A");
	}

	@Test
	public void testBiMap() {
		assertMap(Maps.Bi.of(null).keys);
		assertMap(Maps.Bi.of(null).values);
		assertEquals(Maps.Bi.of(null).key(null), null);
		assertEquals(Maps.Bi.of(null).value(null), null);
		assertMap(Maps.Bi.of(emptyMap).keys);
		assertMap(Maps.Bi.of(emptyMap).values);
		assertEquals(Maps.Bi.of(emptyMap).key(null), null);
		assertEquals(Maps.Bi.of(emptyMap).value(null), null);
		assertMap(Maps.Bi.of(map).keys, -1, "A", null, "B", 1, null);
		assertMap(Maps.Bi.of(map).values, "A", -1, "B", null, null, 1);
		assertEquals(Maps.Bi.of(map).key(null), 1);
		assertEquals(Maps.Bi.of(map).value(null), "B");
	}

	@Test
	public void testBuilder() {
		assertMap(Maps.build(-1, "").put(nullMap).get(), -1, "");
		assertMap(Maps.build(-1, "").put(map).get(), -1, "A", null, "B", 1, null);
		assertMap(Maps.build(-1, "").put(null, "B").get(), -1, "", null, "B");
		assertMap(Maps.build(-1, "").put(-1, "A").get(), -1, "A");
		assertMap(Maps.build(-1, "").put((Put) null).put(-1, "A").get(), -1, "A");
		assertMap(Maps.build(-1, "").put(Put.first).put(-1, "A").get(), -1, "");
		assertMap(Maps.build(-1, "").apply(null).wrap(), -1, "");
		assertMap(Maps.build(-1, "").apply(m -> Maps.put(m, map)).wrap(), -1, "A", null, "B", 1,
			null);
		assertOrdered(Maps.build(Maps::tree, -1, "").put(null, "B").wrap(), null, "B", -1, "");
		assertMap(Maps.build(Maps::syncWeak, 1, "A").putKeys("B", 2, 3).get(), 1, "A", 2, "B", 3,
			"B");
	}

	@Test
	public void testCache() {
		assertMap(apply(Maps.cache(0), m -> m.put(-1, "A")));
		assertMap(apply(Maps.cache(1), m -> m.put(-1, "A")), -1, "A");
		assertMap(apply(Maps.cache(1), m -> Maps.put(m, map)), 1, null);
		assertMap(apply(Maps.cache(2), m -> Maps.put(m, map)), -1, "A", 1, null); // nav map order
		assertMap(apply(Maps.cache(3), m -> Maps.put(m, map)), -1, "A", null, "B", 1, null);
	}

	@Test
	public void testOf() {
		assertMap(Maps.of(-1, "A"), -1, "A");
		assertMap(Maps.of(-1, "A", null, "B"), -1, "A", null, "B");
		assertMap(Maps.of(-1, "A", null, "B", 1, null), -1, "A", null, "B", 1, null);
		assertMap(Maps.of(-1, "A", null, "B", 1, null, null, null), -1, "A", null, null, 1, null);
		assertMap(Maps.of(-1, "A", null, "B", 1, null, null, null, -1, ""), -1, "", null, null, 1,
			null);
	}

	@Test
	public void testCopy() {
		assertMap(Maps.copy(nullMap));
		assertMap(Maps.copy(emptyMap));
		assertMap(Maps.copy(map), -1, "A", null, "B", 1, null);
		assertEquals(Maps.copy(null, map), null);
		assertEquals(Maps.copy(() -> null, map), null);
		assertMap(Maps.copy(Maps::of, nullMap));
		assertMap(Maps.copy(Maps::of, emptyMap));
		assertOrdered(Maps.copy(() -> Maps.tree(), map), null, "B", -1, "A", 1, null);
	}

	@Test
	public void testConvert() {
		assertMap(Maps.convert(nullFn, list));
		assertMap(Maps.convert(fn, nullList));
		assertMap(Maps.convert(fn, emptyList));
		assertMap(Maps.convert(fn, list), "-1", -1, "null", null, "1", 1);
		assertMap(Maps.convert(nullFn, fn, list));
		assertMap(Maps.convert(fn, nullFn, list));
		assertMap(Maps.convert(fn, fn, nullList));
		assertMap(Maps.convert(fn, fn, emptyList));
		assertMap(Maps.convert(fn, fn, list), "-1", "-1", "null", "null", "1", "1");
	}

	@Test
	public void testAdapt() {
		assertMap(Maps.adapt(nullFn, map));
		assertMap(Maps.adapt(fn, nullMap));
		assertMap(Maps.adapt(fn, emptyMap));
		assertMap(Maps.adapt(fn, map), "-1", "A", "null", "B", "1", null);
		assertMap(Maps.adapt(nullFn, fn, map));
		assertMap(Maps.adapt(fn, nullFn, map));
		assertMap(Maps.adapt(fn, fn, nullMap));
		assertMap(Maps.adapt(fn, fn, emptyMap));
		assertMap(Maps.adapt(fn, fn, map), "-1", "A", "null", "B", "1", "null");
		assertEquals(Maps.adapt(Maps.Put.first, null, fn, fn, emptyMap), null);
	}

	@Test
	public void testBiAdapt() {
		assertMap(Maps.biAdapt(nullBiFn, map));
		assertMap(Maps.biAdapt(biFn, nullMap));
		assertMap(Maps.biAdapt(biFn, emptyMap));
		assertMap(Maps.biAdapt(biFn, map), -1, "A", "B", "B", 1, null);
		assertMap(Maps.biAdapt(nullBiFn, biFn, map));
		assertMap(Maps.biAdapt(biFn, nullBiFn, map));
		assertMap(Maps.biAdapt(biFn, biFn, nullMap));
		assertMap(Maps.biAdapt(biFn, biFn, emptyMap));
		assertMap(Maps.biAdapt(biFn, biFn, map), -1, -1, "B", "B", 1, 1);
		assertEquals(Maps.biAdapt(Maps.Put.first, null, biFn, biFn, emptyMap), null);
	}

	@Test
	public void testInvert() {
		assertMap(Maps.invert(nullMap));
		assertMap(Maps.invert(emptyMap));
		assertMap(Maps.invert(map), "A", -1, "B", null, null, 1);
		assertEquals(Maps.invert(null, emptyMap), null);
	}

	@Test
	public void testSort() {
		assertMap(Maps.sort(null, null));
		assertOrdered(
			Maps.sort(Comparator.comparing(Map.Entry::getValue), Maps.of(1, "C", 2, "A", 3, "B")),
			2, "A", 3, "B", 1, "C");
	}

	@Test
	public void testIsEmpty() {
		assertEquals(Maps.isEmpty(nullMap), true);
		assertEquals(Maps.isEmpty(emptyMap), true);
		assertEquals(Maps.isEmpty(map), false);
	}

	@Test
	public void testNonEmpty() {
		assertEquals(Maps.nonEmpty(nullMap), false);
		assertEquals(Maps.nonEmpty(emptyMap), false);
		assertEquals(Maps.nonEmpty(map), true);
	}

	@Test
	public void testSize() {
		assertEquals(Maps.size(nullMap), 0);
		assertEquals(Maps.size(emptyMap), 0);
		assertEquals(Maps.size(map), 3);
	}

	@Test
	public void testGet() {
		assertEquals(Maps.get(nullMap, -1), null);
		assertEquals(Maps.get(emptyMap, -1), null);
		assertEquals(Maps.get(map, 0), null);
		assertEquals(Maps.get(map, null), "B");
		assertEquals(Maps.get(nullMap, -1, ""), "");
		assertEquals(Maps.get(emptyMap, -1, ""), "");
		assertEquals(Maps.get(map, 0, ""), "");
		assertEquals(Maps.get(map, null, ""), "B");
	}

	@Test
	public void testFirstKey() {
		assertEquals(Maps.firstKey(nullMap), null);
		assertEquals(Maps.firstKey(emptyMap), null);
		assertEquals(Maps.firstKey(map), null);
		assertEquals(Maps.firstKey(nullMap, 0), 0);
		assertEquals(Maps.firstKey(emptyMap, 0), 0);
		assertEquals(Maps.firstKey(map, 0), null);
	}

	@Test
	public void testLastKey() {
		assertEquals(Maps.lastKey(nullMap), null);
		assertEquals(Maps.lastKey(emptyMap), null);
		assertEquals(Maps.lastKey(map), 1);
		assertEquals(Maps.lastKey(nullMap, 0), 0);
		assertEquals(Maps.lastKey(emptyMap, 0), 0);
		assertEquals(Maps.lastKey(map, 0), 1);
	}

	@Test
	public void testForEach() {
		Maps.forEach(null, null);
		Maps.forEach(emptyMap, null);
		Maps.forEach(map, null);
		Captor.ofBi().apply(c -> Maps.forEach(null, c)).verify();
		Captor.ofBi().apply(c -> Maps.forEach(emptyMap, c)).verify();
		Captor.ofBi().apply(c -> Maps.forEach(map, c)).verify(null, "B", -1, "A", 1, null);
		Captor.ofBi().apply(c -> Maps.forEach(null, c)).verify();
	}

	@Test
	public void testRemoveIf() throws Exception {
		assertEquals(Maps.removeIf(null, (_, _) -> false), null);
		assertMap(Maps.removeIf(Maps.of(1, "C", -1, "B", 0, "A"), null), 1, "C", -1, "B", 0, "A");
		assertMap(Maps.removeIf(Maps.of(1, "C", -1, "B", 0, "A"), (k, _) -> k >= 0), -1, "B");
	}

	@Test
	public void testKeys() {
		assertStream(Maps.keys(nullBiPr, map));
		assertStream(Maps.keys(biPr, nullMap));
		assertStream(Maps.keys(biPr, emptyMap));
		assertStream(Maps.keys(biPr, map), -1);
	}

	@Test
	public void testValues() {
		assertStream(Maps.values(nullBiPr, map));
		assertStream(Maps.values(biPr, nullMap));
		assertStream(Maps.values(biPr, emptyMap));
		assertStream(Maps.values(biPr, map), "A");
	}

	@Test
	public void testPut() {
		assertEquals(Maps.put(nullMap, 1, "A"), null);
		assertMap(Maps.put(map(1, ""), null, null), 1, "", null, null);
		assertMap(Maps.put(map(1, ""), 1, "A"), 1, "A");
		assertEquals(Maps.put(nullMap, map), null);
		assertMap(Maps.put(map(1, ""), nullMap), 1, "");
		assertMap(Maps.put(map(1, ""), emptyMap), 1, "");
		assertMap(Maps.put(map(1, ""), map), -1, "A", null, "B", 1, null);
	}

	@Test
	public void testPutMethod() {
		assertEquals(Maps.put(Put.first, nullMap, 1, "A"), null);
		assertMap(Maps.put(Put.first, map(1, ""), null, null), 1, "", null, null);
		assertMap(Maps.put(Put.first, map(1, ""), 1, "A"), 1, "");
		assertEquals(Maps.put(Put.first, nullMap, map), null);
		assertMap(Maps.put(Put.first, map(1, ""), nullMap), 1, "");
		assertMap(Maps.put(Put.first, map(1, ""), emptyMap), 1, "");
		assertMap(Maps.put(Put.first, map(1, ""), map), -1, "A", null, "B", 1, "");
	}

	@Test
	public void testMapPut() {
		assertEquals(Maps.convertPut(null, fn, fn, list), null);
		assertMap(Maps.convertPut(map(1, ""), nullFn, fn, list), 1, "");
		assertMap(Maps.convertPut(map(1, ""), fn, nullFn, list), 1, "");
		assertMap(Maps.convertPut(map(1, ""), fn, fn, nullList), 1, "");
		assertMap(Maps.convertPut(map(1, ""), fn, fn, emptyList), 1, "");
		assertMap(Maps.convertPut(map("1", ""), fn, fn, list), "-1", "-1", "null", "null", "1",
			"1");
		assertMap(Maps.convertPut(map(1, ""), fn, fn, list), "-1", "-1", "null", "null", "1", "1",
			1, "");
	}

	@Test
	public void testMapPutMethod() {
		assertEquals(Maps.convertPut(Put.first, null, fn, fn, list), null);
		assertMap(Maps.convertPut(Put.first, map(1, ""), nullFn, fn, list), 1, "");
		assertMap(Maps.convertPut(Put.first, map(1, ""), fn, nullFn, list), 1, "");
		assertMap(Maps.convertPut(Put.first, map(1, ""), fn, fn, nullList), 1, "");
		assertMap(Maps.convertPut(Put.first, map(1, ""), fn, fn, emptyList), 1, "");
		assertMap(Maps.convertPut(Put.first, map("1", ""), fn, fn, list), "-1", "-1", "null",
			"null", "1", "");
		assertMap(Maps.convertPut(Put.first, map(1, ""), fn, fn, list), "-1", "-1", "null", "null",
			"1", "1", 1, "");
	}

	@Test
	public void testAdaptPut() {
		assertEquals(Maps.adaptPut(null, fn, fn, map), null);
		assertMap(Maps.adaptPut(map(1, ""), nullFn, fn, map), 1, "");
		assertMap(Maps.adaptPut(map(1, ""), fn, nullFn, map), 1, "");
		assertMap(Maps.adaptPut(map(1, ""), fn, fn, nullMap), 1, "");
		assertMap(Maps.adaptPut(map(1, ""), fn, fn, emptyMap), 1, "");
		assertMap(Maps.adaptPut(map("1", ""), fn, fn, map), "-1", "A", "null", "B", "1", "null");
		assertMap(Maps.adaptPut(map(1, ""), fn, fn, map), "-1", "A", "null", "B", "1", "null", 1,
			"");
	}

	@Test
	public void testAdaptPutMethod() {
		assertEquals(Maps.adaptPut(Put.first, null, fn, fn, map), null);
		assertMap(Maps.adaptPut(Put.first, map(1, ""), nullFn, fn, map), 1, "");
		assertMap(Maps.adaptPut(Put.first, map(1, ""), fn, nullFn, map), 1, "");
		assertMap(Maps.adaptPut(Put.first, map(1, ""), fn, fn, nullMap), 1, "");
		assertMap(Maps.adaptPut(Put.first, map(1, ""), fn, fn, emptyMap), 1, "");
		assertMap(Maps.adaptPut(Put.first, map("1", ""), fn, fn, map), "-1", "A", "null", "B", "1",
			"");
		assertMap(Maps.adaptPut(Put.first, map(1, ""), fn, fn, map), "-1", "A", "null", "B", "1",
			"null", 1, "");
	}

	@Test
	public void testBiAdaptPut() {
		assertEquals(Maps.biAdaptPut(null, biFn, biFn, map), null);
		assertMap(Maps.biAdaptPut(map(1, ""), nullBiFn, biFn, map), 1, "");
		assertMap(Maps.biAdaptPut(map(1, ""), biFn, nullBiFn, map), 1, "");
		assertMap(Maps.biAdaptPut(map(1, ""), biFn, biFn, nullMap), 1, "");
		assertMap(Maps.biAdaptPut(map(1, ""), biFn, biFn, emptyMap), 1, "");
		assertMap(Maps.biAdaptPut(map(1, ""), biFn, biFn, map), -1, -1, "B", "B", 1, 1);
		assertMap(Maps.biAdaptPut(map("1", ""), biFn, biFn, map), -1, -1, "B", "B", 1, 1, "1", "");
	}

	@Test
	public void testBiAdaptPutMethod() {
		assertEquals(Maps.biAdaptPut(Put.first, null, biFn, biFn, map), null);
		assertMap(Maps.biAdaptPut(Put.first, map(1, ""), nullBiFn, biFn, map), 1, "");
		assertMap(Maps.biAdaptPut(Put.first, map(1, ""), biFn, nullBiFn, map), 1, "");
		assertMap(Maps.biAdaptPut(Put.first, map(1, ""), biFn, biFn, nullMap), 1, "");
		assertMap(Maps.biAdaptPut(Put.first, map(1, ""), biFn, biFn, emptyMap), 1, "");
		assertMap(Maps.biAdaptPut(Put.first, map(1, ""), biFn, biFn, map), -1, -1, "B", "B", 1, "");
		assertMap(Maps.biAdaptPut(Put.first, map("1", ""), biFn, biFn, map), -1, -1, "B", "B", 1, 1,
			"1", "");
	}

	@Test
	public void testConvertPut() {
		assertEquals(Maps.convertPutAll(null, fn, fn, array()), null);
		assertMap(Maps.convertPutAll(map("-1", ""), nullFn, fn, array()), "-1", "");
		assertMap(Maps.convertPutAll(map("-1", ""), fn, nullFn, array()), "-1", "");
		assertMap(Maps.convertPutAll(map("-1", ""), fn, fn, nullArray), "-1", "");
		assertMap(Maps.convertPutAll(map("-1", ""), fn, fn, array()), "-1", "-1", "null", "null",
			"1", "1");
	}

	@Test
	public void testConvertPutMethod() {
		assertEquals(Maps.convertPutAll(Put.first, null, fn, fn, array()), null);
		assertMap(Maps.convertPutAll(null, map("-1", ""), nullFn, fn, array()), "-1", "");
		assertMap(Maps.convertPutAll(Put.first, map("-1", ""), nullFn, fn, array()), "-1", "");
		assertMap(Maps.convertPutAll(Put.first, map("-1", ""), fn, nullFn, array()), "-1", "");
		assertMap(Maps.convertPutAll(Put.first, map("-1", ""), fn, fn, nullArray), "-1", "");
		assertMap(Maps.convertPutAll(Put.first, map("-1", ""), fn, fn, array()), "-1", "", "null",
			"null", "1", "1");
	}

	private static <K, V, M extends Map<K, V>> M apply(M map,
		Functions.Consumer<Map<K, V>> consumer) {
		consumer.accept(map);
		return map;
	}
}
