package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalState;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertStream;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.junit.Test;
import ceri.common.collection.Immutable.Wrap;
import ceri.common.collection.Maps.Put;
import ceri.common.function.Functions;
import ceri.common.util.BasicUtil;

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
	private static final Functions.BiPredicate<Object, Object> biPr = BasicUtil::noneNull;

	private static Integer[] array() {
		return list.toArray(Integer[]::new);
	}

	private static <K, V> Map<K, V> map(K k, V v) {
		return Maps.of(k, v);
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
	}

	@Test
	public void testInvert() {
		assertMap(Maps.invert(nullMap));
		assertMap(Maps.invert(emptyMap));
		assertMap(Maps.invert(map), "A", -1, "B", null, null, 1);
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
	public void testKeys() {
		assertStream(Maps.keys(nullBiPr, map), null, -1, 1);
		assertStream(Maps.keys(biPr, nullMap));
		assertStream(Maps.keys(biPr, emptyMap));
		assertStream(Maps.keys(biPr, map), -1);
	}

	@Test
	public void testValues() {
		assertStream(Maps.values(nullBiPr, map), "B", "A", null);
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
