package ceri.common.collect;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.junit.Test;
import ceri.common.collect.Immutable.Wrap;
import ceri.common.collect.Maps.Put;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
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
		Assert.privateConstructor(Maps.class, Maps.Filter.class, Maps.Compare.class);
	}

	@Test
	public void testCompare() {
		var map = Maps.of(1, "B", -1, "C", 0, "A");
		Assert.ordered(Maps.sort(Maps.Compare.key(), map), -1, "C", 0, "A", 1, "B");
		Assert.ordered(Maps.sort(Maps.Compare.value(), map), 0, "A", 1, "B", -1, "C");
	}

	@Test
	public void testPutType() {
		Assert.equal(Put.put(Put.def, null, -1, "A"), null);
		Assert.map(apply(Maps.of(-1, "A"), //
			m -> Assert.equal(Put.put(null, m, -1, null), null)), -1, "A");
		Assert.map(apply(Maps.of(-1, "A"), //
			m -> Assert.equal(Put.first.put(m, -1, null), "A")), -1, "A");
		Assert.map(apply(Maps.of(-1, "A"), //
			m -> Assert.equal(Put.last.put(m, -1, null), "A")), -1, null);
		Assert.map(apply(Maps.of(-1, "A"), //
			m -> Assert.equal(Put.unique.put(m, 1, null), null)), -1, "A", 1, null);
		Assert.map(apply(Maps.of(-1, "A"), //
			m -> Assert.illegalState(() -> Put.unique.put(m, -1, null))), -1, "A");
	}

	@Test
	public void testBiMap() {
		Assert.map(Maps.Bi.of(null).keys);
		Assert.map(Maps.Bi.of(null).values);
		Assert.equal(Maps.Bi.of(null).key(null), null);
		Assert.equal(Maps.Bi.of(null).value(null), null);
		Assert.map(Maps.Bi.of(emptyMap).keys);
		Assert.map(Maps.Bi.of(emptyMap).values);
		Assert.equal(Maps.Bi.of(emptyMap).key(null), null);
		Assert.equal(Maps.Bi.of(emptyMap).value(null), null);
		Assert.map(Maps.Bi.of(map).keys, -1, "A", null, "B", 1, null);
		Assert.map(Maps.Bi.of(map).values, "A", -1, "B", null, null, 1);
		Assert.equal(Maps.Bi.of(map).key(null), 1);
		Assert.equal(Maps.Bi.of(map).value(null), "B");
	}

	@Test
	public void testBuilder() {
		Assert.map(Maps.Builder.of().put(nullMap).get());
		Assert.map(Maps.Builder.of().put(map).get(), -1, "A", null, "B", 1, null);
		Assert.map(Maps.build(-1, "").put(nullMap).get(), -1, "");
		Assert.map(Maps.build(-1, "").put(map).get(), -1, "A", null, "B", 1, null);
		Assert.map(Maps.build(-1, "").put(null, "B").get(), -1, "", null, "B");
		Assert.map(Maps.build(-1, "").put(-1, "A").get(), -1, "A");
		Assert.map(Maps.build(-1, "").put((Put) null).put(-1, "A").get(), -1, "A");
		Assert.map(Maps.build(-1, "").put(Put.first).put(-1, "A").get(), -1, "");
		Assert.map(Maps.build(-1, "").apply(null).wrap(), -1, "");
		Assert.map(Maps.build(-1, "").apply(m -> Maps.put(m, map)).wrap(), -1, "A", null, "B", 1,
			null);
		Assert.ordered(Maps.build(Maps::tree, -1, "").put(null, "B").wrap(), null, "B", -1, "");
		Assert.map(Maps.build(Maps::syncWeak, 1, "A").putKeys("B", 2, 3).get(), 1, "A", 2, "B", 3,
			"B");
	}

	@Test
	public void testCache() {
		Assert.map(apply(Maps.cache(0), m -> m.put(-1, "A")));
		Assert.map(apply(Maps.cache(1), m -> m.put(-1, "A")), -1, "A");
		Assert.map(apply(Maps.cache(1), m -> Maps.put(m, map)), 1, null);
		Assert.map(apply(Maps.cache(2), m -> Maps.put(m, map)), -1, "A", 1, null); // nav map order
		Assert.map(apply(Maps.cache(3), m -> Maps.put(m, map)), -1, "A", null, "B", 1, null);
	}

	@Test
	public void testOf() {
		Assert.map(Maps.of(-1, "A"), -1, "A");
		Assert.map(Maps.of(-1, "A", null, "B"), -1, "A", null, "B");
		Assert.map(Maps.of(-1, "A", null, "B", 1, null), -1, "A", null, "B", 1, null);
		Assert.map(Maps.of(-1, "A", null, "B", 1, null, null, null), -1, "A", null, null, 1, null);
		Assert.map(Maps.of(-1, "A", null, "B", 1, null, null, null, -1, ""), -1, "", null, null, 1,
			null);
	}

	@Test
	public void testCopy() {
		Assert.map(Maps.copy(nullMap));
		Assert.map(Maps.copy(emptyMap));
		Assert.map(Maps.copy(map), -1, "A", null, "B", 1, null);
		Assert.equal(Maps.copy(null, map), null);
		Assert.equal(Maps.copy(() -> null, map), null);
		Assert.map(Maps.copy(Maps::of, nullMap));
		Assert.map(Maps.copy(Maps::of, emptyMap));
		Assert.ordered(Maps.copy(() -> Maps.tree(), map), null, "B", -1, "A", 1, null);
	}

	@Test
	public void testConvert() {
		Assert.map(Maps.convert(nullFn, list));
		Assert.map(Maps.convert(fn, nullList));
		Assert.map(Maps.convert(fn, emptyList));
		Assert.map(Maps.convert(fn, list), "-1", -1, "null", null, "1", 1);
		Assert.map(Maps.convert(nullFn, fn, list));
		Assert.map(Maps.convert(fn, nullFn, list));
		Assert.map(Maps.convert(fn, fn, nullList));
		Assert.map(Maps.convert(fn, fn, emptyList));
		Assert.map(Maps.convert(fn, fn, list), "-1", "-1", "null", "null", "1", "1");
	}

	@Test
	public void testAdapt() {
		Assert.map(Maps.adapt(nullFn, map));
		Assert.map(Maps.adapt(fn, nullMap));
		Assert.map(Maps.adapt(fn, emptyMap));
		Assert.map(Maps.adapt(fn, map), "-1", "A", "null", "B", "1", null);
		Assert.map(Maps.adapt(nullFn, fn, map));
		Assert.map(Maps.adapt(fn, nullFn, map));
		Assert.map(Maps.adapt(fn, fn, nullMap));
		Assert.map(Maps.adapt(fn, fn, emptyMap));
		Assert.map(Maps.adapt(fn, fn, map), "-1", "A", "null", "B", "1", "null");
		Assert.equal(Maps.adapt(Maps.Put.first, null, fn, fn, emptyMap), null);
	}

	@Test
	public void testBiAdapt() {
		Assert.map(Maps.biAdapt(nullBiFn, map));
		Assert.map(Maps.biAdapt(biFn, nullMap));
		Assert.map(Maps.biAdapt(biFn, emptyMap));
		Assert.map(Maps.biAdapt(biFn, map), -1, "A", "B", "B", 1, null);
		Assert.map(Maps.biAdapt(nullBiFn, biFn, map));
		Assert.map(Maps.biAdapt(biFn, nullBiFn, map));
		Assert.map(Maps.biAdapt(biFn, biFn, nullMap));
		Assert.map(Maps.biAdapt(biFn, biFn, emptyMap));
		Assert.map(Maps.biAdapt(biFn, biFn, map), -1, -1, "B", "B", 1, 1);
		Assert.equal(Maps.biAdapt(Maps.Put.first, null, biFn, biFn, emptyMap), null);
	}

	@Test
	public void testInvert() {
		Assert.map(Maps.invert(nullMap));
		Assert.map(Maps.invert(emptyMap));
		Assert.map(Maps.invert(map), "A", -1, "B", null, null, 1);
		Assert.equal(Maps.invert(null, emptyMap), null);
	}

	@Test
	public void testSort() {
		Assert.map(Maps.sort(null, null));
		Assert.ordered(
			Maps.sort(Comparator.comparing(Map.Entry::getValue), Maps.of(1, "C", 2, "A", 3, "B")),
			2, "A", 3, "B", 1, "C");
	}

	@Test
	public void testIsEmpty() {
		Assert.equal(Maps.isEmpty(nullMap), true);
		Assert.equal(Maps.isEmpty(emptyMap), true);
		Assert.equal(Maps.isEmpty(map), false);
	}

	@Test
	public void testNonEmpty() {
		Assert.equal(Maps.nonEmpty(nullMap), false);
		Assert.equal(Maps.nonEmpty(emptyMap), false);
		Assert.equal(Maps.nonEmpty(map), true);
	}

	@Test
	public void testSize() {
		Assert.equal(Maps.size(nullMap), 0);
		Assert.equal(Maps.size(emptyMap), 0);
		Assert.equal(Maps.size(map), 3);
	}

	@Test
	public void testGet() {
		Assert.equal(Maps.get(nullMap, -1), null);
		Assert.equal(Maps.get(emptyMap, -1), null);
		Assert.equal(Maps.get(map, 0), null);
		Assert.equal(Maps.get(map, null), "B");
		Assert.equal(Maps.get(nullMap, -1, ""), "");
		Assert.equal(Maps.get(emptyMap, -1, ""), "");
		Assert.equal(Maps.get(map, 0, ""), "");
		Assert.equal(Maps.get(map, null, ""), "B");
	}

	@Test
	public void testGetOrThrow() {
		Assert.illegalArg(() -> Maps.getOrThrow(nullMap, -1));
		Assert.illegalArg(() -> Maps.getOrThrow(emptyMap, -1));
		Assert.illegalArg(() -> Maps.getOrThrow(map, 0));
		Assert.equal(Maps.getOrThrow(map, -1), "A");
		Assert.equal(Maps.getOrThrow(map, null), "B");
		Assert.equal(Maps.getOrThrow(map, 1), null);
	}
	
	@Test
	public void testFirstKey() {
		Assert.equal(Maps.firstKey(nullMap), null);
		Assert.equal(Maps.firstKey(emptyMap), null);
		Assert.equal(Maps.firstKey(map), null);
		Assert.equal(Maps.firstKey(nullMap, 0), 0);
		Assert.equal(Maps.firstKey(emptyMap, 0), 0);
		Assert.equal(Maps.firstKey(map, 0), null);
	}

	@Test
	public void testLastKey() {
		Assert.equal(Maps.lastKey(nullMap), null);
		Assert.equal(Maps.lastKey(emptyMap), null);
		Assert.equal(Maps.lastKey(map), 1);
		Assert.equal(Maps.lastKey(nullMap, 0), 0);
		Assert.equal(Maps.lastKey(emptyMap, 0), 0);
		Assert.equal(Maps.lastKey(map, 0), 1);
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
		Assert.equal(Maps.removeIf(null, (_, _) -> false), null);
		Assert.map(Maps.removeIf(Maps.of(1, "C", -1, "B", 0, "A"), null), 1, "C", -1, "B", 0, "A");
		Assert.map(Maps.removeIf(Maps.of(1, "C", -1, "B", 0, "A"), (k, _) -> k >= 0), -1, "B");
	}

	@Test
	public void testKeys() {
		Assert.stream(Maps.keys(nullBiPr, map));
		Assert.stream(Maps.keys(biPr, nullMap));
		Assert.stream(Maps.keys(biPr, emptyMap));
		Assert.stream(Maps.keys(biPr, map), -1);
	}

	@Test
	public void testValues() {
		Assert.stream(Maps.values(nullBiPr, map));
		Assert.stream(Maps.values(biPr, nullMap));
		Assert.stream(Maps.values(biPr, emptyMap));
		Assert.stream(Maps.values(biPr, map), "A");
	}

	@Test
	public void testPut() {
		Assert.equal(Maps.put(nullMap, 1, "A"), null);
		Assert.map(Maps.put(map(1, ""), null, null), 1, "", null, null);
		Assert.map(Maps.put(map(1, ""), 1, "A"), 1, "A");
		Assert.equal(Maps.put(nullMap, map), null);
		Assert.map(Maps.put(map(1, ""), nullMap), 1, "");
		Assert.map(Maps.put(map(1, ""), emptyMap), 1, "");
		Assert.map(Maps.put(map(1, ""), map), -1, "A", null, "B", 1, null);
	}

	@Test
	public void testPutMethod() {
		Assert.equal(Maps.put(Put.first, nullMap, 1, "A"), null);
		Assert.map(Maps.put(Put.first, map(1, ""), null, null), 1, "", null, null);
		Assert.map(Maps.put(Put.first, map(1, ""), 1, "A"), 1, "");
		Assert.equal(Maps.put(Put.first, nullMap, map), null);
		Assert.map(Maps.put(Put.first, map(1, ""), nullMap), 1, "");
		Assert.map(Maps.put(Put.first, map(1, ""), emptyMap), 1, "");
		Assert.map(Maps.put(Put.first, map(1, ""), map), -1, "A", null, "B", 1, "");
	}

	@Test
	public void testMapPut() {
		Assert.equal(Maps.convertPut(null, fn, fn, list), null);
		Assert.map(Maps.convertPut(map(1, ""), nullFn, fn, list), 1, "");
		Assert.map(Maps.convertPut(map(1, ""), fn, nullFn, list), 1, "");
		Assert.map(Maps.convertPut(map(1, ""), fn, fn, nullList), 1, "");
		Assert.map(Maps.convertPut(map(1, ""), fn, fn, emptyList), 1, "");
		Assert.map(Maps.convertPut(map("1", ""), fn, fn, list), "-1", "-1", "null", "null", "1",
			"1");
		Assert.map(Maps.convertPut(map(1, ""), fn, fn, list), "-1", "-1", "null", "null", "1", "1",
			1, "");
	}

	@Test
	public void testMapPutMethod() {
		Assert.equal(Maps.convertPut(Put.first, null, fn, fn, list), null);
		Assert.map(Maps.convertPut(Put.first, map(1, ""), nullFn, fn, list), 1, "");
		Assert.map(Maps.convertPut(Put.first, map(1, ""), fn, nullFn, list), 1, "");
		Assert.map(Maps.convertPut(Put.first, map(1, ""), fn, fn, nullList), 1, "");
		Assert.map(Maps.convertPut(Put.first, map(1, ""), fn, fn, emptyList), 1, "");
		Assert.map(Maps.convertPut(Put.first, map("1", ""), fn, fn, list), "-1", "-1", "null",
			"null", "1", "");
		Assert.map(Maps.convertPut(Put.first, map(1, ""), fn, fn, list), "-1", "-1", "null", "null",
			"1", "1", 1, "");
	}

	@Test
	public void testAdaptPut() {
		Assert.equal(Maps.adaptPut(null, fn, fn, map), null);
		Assert.map(Maps.adaptPut(map(1, ""), nullFn, fn, map), 1, "");
		Assert.map(Maps.adaptPut(map(1, ""), fn, nullFn, map), 1, "");
		Assert.map(Maps.adaptPut(map(1, ""), fn, fn, nullMap), 1, "");
		Assert.map(Maps.adaptPut(map(1, ""), fn, fn, emptyMap), 1, "");
		Assert.map(Maps.adaptPut(map("1", ""), fn, fn, map), "-1", "A", "null", "B", "1", "null");
		Assert.map(Maps.adaptPut(map(1, ""), fn, fn, map), "-1", "A", "null", "B", "1", "null", 1,
			"");
	}

	@Test
	public void testAdaptPutMethod() {
		Assert.equal(Maps.adaptPut(Put.first, null, fn, fn, map), null);
		Assert.map(Maps.adaptPut(Put.first, map(1, ""), nullFn, fn, map), 1, "");
		Assert.map(Maps.adaptPut(Put.first, map(1, ""), fn, nullFn, map), 1, "");
		Assert.map(Maps.adaptPut(Put.first, map(1, ""), fn, fn, nullMap), 1, "");
		Assert.map(Maps.adaptPut(Put.first, map(1, ""), fn, fn, emptyMap), 1, "");
		Assert.map(Maps.adaptPut(Put.first, map("1", ""), fn, fn, map), "-1", "A", "null", "B", "1",
			"");
		Assert.map(Maps.adaptPut(Put.first, map(1, ""), fn, fn, map), "-1", "A", "null", "B", "1",
			"null", 1, "");
	}

	@Test
	public void testBiAdaptPut() {
		Assert.equal(Maps.biAdaptPut(null, biFn, biFn, map), null);
		Assert.map(Maps.biAdaptPut(map(1, ""), nullBiFn, biFn, map), 1, "");
		Assert.map(Maps.biAdaptPut(map(1, ""), biFn, nullBiFn, map), 1, "");
		Assert.map(Maps.biAdaptPut(map(1, ""), biFn, biFn, nullMap), 1, "");
		Assert.map(Maps.biAdaptPut(map(1, ""), biFn, biFn, emptyMap), 1, "");
		Assert.map(Maps.biAdaptPut(map(1, ""), biFn, biFn, map), -1, -1, "B", "B", 1, 1);
		Assert.map(Maps.biAdaptPut(map("1", ""), biFn, biFn, map), -1, -1, "B", "B", 1, 1, "1", "");
	}

	@Test
	public void testBiAdaptPutMethod() {
		Assert.equal(Maps.biAdaptPut(Put.first, null, biFn, biFn, map), null);
		Assert.map(Maps.biAdaptPut(Put.first, map(1, ""), nullBiFn, biFn, map), 1, "");
		Assert.map(Maps.biAdaptPut(Put.first, map(1, ""), biFn, nullBiFn, map), 1, "");
		Assert.map(Maps.biAdaptPut(Put.first, map(1, ""), biFn, biFn, nullMap), 1, "");
		Assert.map(Maps.biAdaptPut(Put.first, map(1, ""), biFn, biFn, emptyMap), 1, "");
		Assert.map(Maps.biAdaptPut(Put.first, map(1, ""), biFn, biFn, map), -1, -1, "B", "B", 1,
			"");
		Assert.map(Maps.biAdaptPut(Put.first, map("1", ""), biFn, biFn, map), -1, -1, "B", "B", 1,
			1, "1", "");
	}

	@Test
	public void testConvertPut() {
		Assert.equal(Maps.convertPutAll(null, fn, fn, array()), null);
		Assert.map(Maps.convertPutAll(map("-1", ""), nullFn, fn, array()), "-1", "");
		Assert.map(Maps.convertPutAll(map("-1", ""), fn, nullFn, array()), "-1", "");
		Assert.map(Maps.convertPutAll(map("-1", ""), fn, fn, nullArray), "-1", "");
		Assert.map(Maps.convertPutAll(map("-1", ""), fn, fn, array()), "-1", "-1", "null", "null",
			"1", "1");
	}

	@Test
	public void testConvertPutMethod() {
		Assert.equal(Maps.convertPutAll(Put.first, null, fn, fn, array()), null);
		Assert.map(Maps.convertPutAll(null, map("-1", ""), nullFn, fn, array()), "-1", "");
		Assert.map(Maps.convertPutAll(Put.first, map("-1", ""), nullFn, fn, array()), "-1", "");
		Assert.map(Maps.convertPutAll(Put.first, map("-1", ""), fn, nullFn, array()), "-1", "");
		Assert.map(Maps.convertPutAll(Put.first, map("-1", ""), fn, fn, nullArray), "-1", "");
		Assert.map(Maps.convertPutAll(Put.first, map("-1", ""), fn, fn, array()), "-1", "", "null",
			"null", "1", "1");
	}

	private static <K, V, M extends Map<K, V>> M apply(M map,
		Functions.Consumer<Map<K, V>> consumer) {
		consumer.accept(map);
		return map;
	}
}
