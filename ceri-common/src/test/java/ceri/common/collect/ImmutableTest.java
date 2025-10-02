package ceri.common.collect;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertImmutable;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.junit.Test;
import ceri.common.collect.Immutable.Wrap;
import ceri.common.function.Compares;
import ceri.common.function.Functions;
import ceri.common.test.AssertUtil;

public class ImmutableTest {
	private static final String s0 = new String("s");
	private static final String s1 = new String("s");
	private static final List<Integer> nullList = null;
	private static final List<Integer> list = Immutable.listOf(-1, null, 1);
	private static final Set<Integer> nullSet = null;
	private static final SortedSet<Integer> nullSortSet = null;
	private static final NavigableSet<Integer> nullNavSet = null;
	private static final Set<Integer> set = Immutable.setOf(-1, null, 1);
	private static final Set<String> idSet = Immutable.ofAll(Wrap.idSet(), s0, null, s1);
	private static final Map<Integer, String> nullMap = null;
	private static final SortedMap<Integer, String> nullSortMap = null;
	private static final NavigableMap<Integer, String> nullNavMap = null;
	private static final Map<Integer, String> emptyMap = Map.of();
	private static final Map<Integer, String> map =
		Immutable.of(Wrap.seqMap(), -1, "A", null, "B", 1, null);
	private static final Map<String, Object> idMap =
		Immutable.of(Wrap.idMap(), s0, -1, null, 1, s1, null);
	private static final Integer[] nullArray = null;
	private static final Integer[] emptyArray = new Integer[0];
	private static final Functions.Function<Object, Object> fn = String::valueOf;
	private static final Functions.BiOperator<Object> biFn = (l, r) -> l != null ? l : r;
	private static final Comparator<Integer> comp = Compares.nullsLast();

	private static Integer[] array() {
		return list.toArray(Integer[]::new);
	}

	private static <T> Functions.Supplier<T> nullFn() {
		return null;
	}

	// wraps

	@Test
	public void testWrapTypeWrap() {
		assertUnordered(Wrap.collect().wrap(null));
		assertUnordered(Wrap.collect().wrap(c -> Collectable.add(c, list)), -1, null, 1);
		assertOrdered(Wrap.seqCollect().wrap(c -> Collectable.add(c, list)), -1, null, 1);
		assertOrdered(Wrap.linkList().wrap(c -> Collectable.add(c, list)), -1, null, 1);
		assertOrdered(Wrap.seqSet().wrap(c -> Collectable.add(c, list)), -1, null, 1);
		assertOrdered(Wrap.<Integer>sortSet().wrap(c -> Collectable.add(c, list)), null, -1, 1);
		assertOrdered(Wrap.sortSet(comp).wrap(c -> Collectable.add(c, list)), -1, 1, null);
		assertOrdered(Wrap.<Integer>navSet().wrap(c -> Collectable.add(c, list)), null, -1, 1);
		assertOrdered(Wrap.navSet(comp).wrap(c -> Collectable.add(c, list)), -1, 1, null);
		assertUnordered(Wrap.idSet().wrap(c -> Collectable.add(c, idSet)), s0, null, s1);
		assertMap(Wrap.map().wrap(c -> Maps.put(c, map)), -1, "A", null, "B", 1, null);
		assertOrdered(Wrap.seqMap().wrap(c -> Maps.put(c, map)), -1, "A", null, "B", 1, null);
		assertOrdered(Wrap.<Integer, Object>sortMap().wrap(c -> Maps.put(c, map)), null, "B", -1,
			"A", 1, null);
		assertOrdered(Wrap.sortMap(comp).wrap(c -> Maps.put(c, map)), -1, "A", 1, null, null, "B");
		assertOrdered(Wrap.navMap(comp).wrap(c -> Maps.put(c, map)), -1, "A", 1, null, null, "B");
		assertMap(Wrap.idMap().wrap(c -> Maps.put(c, idMap)), s0, -1, null, 1, s1, null);
	}

	@Test
	public void testWrapTypeTo() {
		assertSame(Wrap.list().to(null), Wrap.list());
	}

	@Test
	public void testWrap() {
		assertEquals(Immutable.wrap(null, map), null);
		assertOrdered(assertImmutable(Immutable.wrap(nullList)));
		assertOrdered(assertImmutable(Immutable.wrap(nullSet)));
		assertOrdered(assertImmutable(Immutable.wrapSort(nullSortSet)));
		assertOrdered(assertImmutable(Immutable.wrapNav(nullNavSet)));
		assertMap(assertImmutable(Immutable.wrap(nullMap)));
		assertMap(assertImmutable(Immutable.wrapSort(nullSortMap)));
		assertMap(assertImmutable(Immutable.wrapNav(nullNavMap)));
	}

	@Test
	public void testWrapList() {
		assertOrdered(assertImmutable(Immutable.wrapListOf(nullArray)));
		assertOrdered(assertImmutable(Immutable.wrapListOf(emptyArray)));
		assertOrdered(assertImmutable(Immutable.wrapList(nullArray, 0)));
		assertOrdered(assertImmutable(Immutable.wrapList(emptyArray, 0)));
	}

	@Test
	public void testListIsWrapped() {
		var array = array();
		var listOf = assertImmutable(Immutable.wrapListOf(array));
		var list = assertImmutable(Immutable.wrapList(array, 1));
		assertOrdered(listOf, -1, null, 1);
		assertOrdered(list, null, 1);
		array[2] = 0;
		assertOrdered(listOf, -1, null, 0);
		assertOrdered(list, null, 0);
	}

	@Test
	public void testWrapMapOfLists() {
		var lA = Lists.ofAll("A", null);
		var lB = Lists.ofAll(null, "B");
		var m = Maps.of(-1, lA, null, lB, 1, null);
		assertMap(Immutable.wrapMapOfLists(null));
		var wm = assertImmutable(Immutable.wrapMapOfLists(m));
		assertMap(assertImmutable(wm), -1, lA, null, lB, 1, List.of());
		wm.values().forEach(AssertUtil::assertImmutable);
		lB.clear();
		assertMap(wm, -1, lA, null, List.of(), 1, List.of());
	}

	@Test
	public void testWrapMapOfSets() {
		var sA = Sets.ofAll("A", null);
		var sB = Sets.ofAll(null, "B");
		var m = Maps.of(-1, sA, null, sB, 1, null);
		assertMap(Immutable.wrapMapOfSets(null));
		var wm = assertImmutable(Immutable.wrapMapOfSets(m));
		assertMap(assertImmutable(wm), -1, sA, null, sB, 1, Set.of());
		wm.values().forEach(AssertUtil::assertImmutable);
		sB.clear();
		assertMap(wm, -1, sA, null, Set.of(), 1, Set.of());
	}

	@Test
	public void testWrapMapOfMaps() {
		var mA = Maps.<Integer, String>of(1, null);
		var mB = Maps.<Integer, String>of(null, "B");
		var m = Maps.of(-1, mA, null, mB, 1, null);
		assertMap(Immutable.wrapMapOfMaps(null));
		var wm = assertImmutable(Immutable.wrapMapOfMaps(m));
		assertMap(assertImmutable(wm), -1, mA, null, mB, 1, emptyMap);
		wm.values().forEach(AssertUtil::assertImmutable);
		mB.clear();
		assertMap(wm, -1, mA, null, emptyMap, 1, emptyMap);
	}

	@Test
	public void testOf() {
		assertEquals(Immutable.ofAll(null), null);
		assertUnordered(Immutable.ofAll(Wrap.collect()));
		assertOrdered(Immutable.ofAll(Wrap.seqCollect(), array()), -1, null, 1);
		assertEquals(Immutable.of(null, list), null);
		assertUnordered(Immutable.of(Wrap.idSet(), list), -1, null, 1);
		assertEquals(Immutable.of(null, map), null);
		assertEquals(Immutable.of(null, -1, "A"), null);
		assertEquals(Immutable.of(null, -1, "A", null, "B"), null);
		assertEquals(Immutable.of(null, -1, "A", null, "B", 1, null), null);
		assertEquals(Immutable.of(null, -1, "A", null, "B", 1, null, 0, "D"), null);
		assertEquals(Immutable.of(null, -1, "A", null, "B", 1, null, 0, "D", null, null), null);
		assertOrdered(Immutable.of(Wrap.seqMap(), -1, "A"), -1, "A");
		assertOrdered(Immutable.of(Wrap.sortMap(), -1, "A", null, "B"), null, "B", -1, "A");
		assertOrdered(Immutable.of(Wrap.navMap(), -1, "A", null, "B", 1, null), //
			null, "B", -1, "A", 1, null);
		assertMap(Immutable.of(Wrap.idMap(), -1, "A", null, "B", 1, null, 0, "D"), //
			-1, "A", null, "B", 1, null, 0, "D");
		assertMap(Immutable.of(Wrap.map(), -1, "A", null, "B", 1, null, 0, "D", null, null), //
			-1, "A", 1, null, 0, "D", null, null);
	}

	@Test
	public void testAdapt() {
		assertEquals(Immutable.adaptAll(null, fn, array()), null);
		assertEquals(Immutable.adapt(null, fn, fn, map), null);
	}

	@Test
	public void testBiAdapt() {
		assertEquals(Immutable.biAdapt(null, biFn, biFn, map), null);
	}

	@Test
	public void testConvert() {
		assertEquals(Immutable.convert(null, biFn, map), null);
		assertEquals(Immutable.convertAll(null, fn, fn, array()), null);
		assertEquals(Immutable.convert(null, fn, fn, list), null);
	}

	@Test
	public void testInvert() {
		assertEquals(Immutable.invert(null, map), null);
	}

	// lists

	@Test
	public void testList() {
		assertOrdered(Immutable.list());
		assertOrdered(Immutable.listOf(array()), -1, null, 1);
		assertOrdered(Immutable.list(array(), 2), 1);
		assertOrdered(Immutable.list(list), -1, null, 1);
		assertOrdered(Immutable.listOfAll(null, array()), -1, null, 1);
		assertOrdered(Immutable.list(null, array(), 2), 1);
		assertOrdered(Immutable.list(null, list), -1, null, 1);
		assertOrdered(Immutable.listOfAll(Lists::of, array()), -1, null, 1);
		assertOrdered(Immutable.list(Lists::of, array(), 2), 1);
		assertOrdered(Immutable.list(Lists::of, list), -1, null, 1);
	}

	@Test
	public void testAdaptList() {
		assertOrdered(Immutable.adaptListOf(fn, array()), "-1", "null", "1");
		assertOrdered(Immutable.adaptList(fn, array(), 1), "null", "1");
		assertOrdered(Immutable.adaptList(fn, list), "-1", "null", "1");
		assertOrdered(Immutable.adaptListOfAll(null, fn, array()), "-1", "null", "1");
		assertOrdered(Immutable.adaptList(null, fn, list), "-1", "null", "1");
		assertOrdered(Immutable.adaptListOfAll(Lists::of, fn, array()), "-1", "null", "1");
		assertOrdered(Immutable.adaptList(Lists::of, fn, list), "-1", "null", "1");
	}

	@Test
	public void testConvertList() {
		assertUnordered(Immutable.convertList(biFn, map), -1, "B", 1);
		assertUnordered(Immutable.convertList(null, biFn, map), -1, "B", 1);
		assertUnordered(Immutable.convertList(Lists::of, biFn, map), -1, "B", 1);
	}

	// sets

	@Test
	public void testSet() {
		assertUnordered(Immutable.set());
		assertUnordered(Immutable.setOf(array()), -1, null, 1);
		assertUnordered(Immutable.set(array(), 2), 1);
		assertUnordered(Immutable.set(set), -1, null, 1);
		assertUnordered(Immutable.setOfAll(null, array()), -1, null, 1);
		assertUnordered(Immutable.set(null, array(), 2), 1);
		assertUnordered(Immutable.set(null, set), -1, null, 1);
		assertUnordered(Immutable.setOfAll(Sets::of, array()), -1, null, 1);
		assertUnordered(Immutable.set(Sets::of, array(), 2), 1);
		assertUnordered(Immutable.set(Sets::of, set), -1, null, 1);
	}

	@Test
	public void testAdaptSet() {
		assertUnordered(Immutable.adaptSetOf(fn, array()), "-1", "null", "1");
		assertUnordered(Immutable.adaptSet(fn, array(), 1), "null", "1");
		assertUnordered(Immutable.adaptSet(fn, list), "-1", "null", "1");
		assertUnordered(Immutable.adaptSetOfAll(null, fn, array()), "-1", "null", "1");
		assertUnordered(Immutable.adaptSet(null, fn, list), "-1", "null", "1");
		assertUnordered(Immutable.adaptSetOfAll(Sets::of, fn, array()), "-1", "null", "1");
		assertUnordered(Immutable.adaptSet(Sets::of, fn, list), "-1", "null", "1");
	}

	@Test
	public void testConvertSet() {
		assertUnordered(Immutable.convertSet(biFn, map), -1, "B", 1);
		assertUnordered(Immutable.convertSet(null, biFn, map), -1, "B", 1);
		assertUnordered(Immutable.convertSet(Sets::of, biFn, map), -1, "B", 1);
	}

	// maps

	@Test
	public void testMapOf() {
		assertMap(Immutable.mapOf(1, "A"), 1, "A");
		assertMap(Immutable.mapOf(1, "A", null, "B"), 1, "A", null, "B");
		assertMap(Immutable.mapOf(1, "A", null, "B", 3, null), 1, "A", null, "B", 3, null);
		assertMap(Immutable.mapOf(1, "A", null, "B", 3, null, null, null), 1, "A", null, null, 3,
			null);
		assertMap(Immutable.mapOf(1, "A", null, "B", 3, null, null, null, 5, "E"), 1, "A", null,
			null, 3, null, 5, "E");
		assertMap(Immutable.mapOf(nullFn(), 1, "A"), 1, "A");
		assertMap(Immutable.mapOf(nullFn(), 1, "A", null, "B"), 1, "A", null, "B");
		assertMap(Immutable.mapOf(nullFn(), 1, "A", null, "B", 3, null), 1, "A", null, "B", 3,
			null);
		assertMap(Immutable.mapOf(nullFn(), 1, "A", null, "B", 3, null, null, null), 1, "A", null,
			null, 3, null);
		assertMap(Immutable.mapOf(nullFn(), 1, "A", null, "B", 3, null, null, null, 5, "E"), 1, "A",
			null, null, 3, null, 5, "E");
		assertMap(Immutable.mapOf(Maps::of, 1, "A"), 1, "A");
		assertMap(Immutable.mapOf(Maps::of, 1, "A", null, "B"), 1, "A", null, "B");
		assertMap(Immutable.mapOf(Maps::of, 1, "A", null, "B", 3, null), 1, "A", null, "B", 3,
			null);
		assertMap(Immutable.mapOf(Maps::of, 1, "A", null, "B", 3, null, null, null), 1, "A", null,
			null, 3, null);
		assertMap(Immutable.mapOf(Maps::of, 1, "A", null, "B", 3, null, null, null, 5, "E"), 1, "A",
			null, null, 3, null, 5, "E");
	}

	@Test
	public void testMap() {
		assertMap(Immutable.map(map), -1, "A", null, "B", 1, null);
		assertMap(Immutable.map(nullFn(), map), -1, "A", null, "B", 1, null);
		assertMap(Immutable.map(Maps::of, map), -1, "A", null, "B", 1, null);
	}

	@Test
	public void testMapOfLists() {
		var lA = Lists.ofAll("A", null);
		var lB = Lists.ofAll(null, "B");
		var m = Maps.of(-1, lA, null, lB, 1, null);
		assertMap(Immutable.mapOfLists(null));
		var wm = assertImmutable(Immutable.mapOfLists(m));
		assertMap(assertImmutable(wm), -1, lA, null, lB, 1, List.of());
		wm.values().forEach(AssertUtil::assertImmutable);
		lB.clear();
		assertMap(wm, -1, lA, null, Immutable.listOf(null, "B"), 1, List.of());
	}

	@Test
	public void testMapOfSets() {
		var sA = Sets.ofAll("A", null);
		var sB = Sets.ofAll(null, "B");
		var m = Maps.of(-1, sA, null, sB, 1, null);
		assertMap(Immutable.mapOfSets(null));
		var wm = assertImmutable(Immutable.mapOfSets(m));
		assertMap(assertImmutable(wm), -1, sA, null, sB, 1, Set.of());
		wm.values().forEach(AssertUtil::assertImmutable);
		sB.clear();
		assertMap(wm, -1, sA, null, Immutable.setOf(null, "B"), 1, Set.of());
	}

	@Test
	public void testMapOfMaps() {
		var mA = Maps.<Integer, String>of(1, null);
		var mB = Maps.<Integer, String>of(null, "B");
		var m = Maps.of(-1, mA, null, mB, 1, null);
		assertMap(Immutable.mapOfMaps(null));
		var wm = assertImmutable(Immutable.mapOfMaps(m));
		assertMap(assertImmutable(wm), -1, mA, null, mB, 1, emptyMap);
		wm.values().forEach(AssertUtil::assertImmutable);
		mB.clear();
		assertMap(wm, -1, mA, null, Immutable.mapOf(null, "B"), 1, emptyMap);
	}

	@Test
	public void testAdaptMap() {
		assertMap(Immutable.adaptMap(fn, map), "-1", "A", "null", "B", "1", null);
		assertMap(Immutable.adaptMap(fn, fn, map), "-1", "A", "null", "B", "1", "null");
		assertMap(Immutable.adaptMap(null, fn, fn, map), "-1", "A", "null", "B", "1", "null");
		assertMap(Immutable.adaptMap(Maps::of, fn, fn, map), "-1", "A", "null", "B", "1", "null");
	}

	@Test
	public void testBiAdaptMap() {
		assertMap(Immutable.biAdaptMap(biFn, map), -1, "A", "B", "B", 1, null);
		assertMap(Immutable.biAdaptMap(biFn, biFn, map), -1, -1, "B", "B", 1, 1);
		assertMap(Immutable.biAdaptMap(nullFn(), biFn, biFn, map), -1, -1, "B", "B", 1, 1);
		assertMap(Immutable.biAdaptMap(Maps::of, biFn, biFn, map), -1, -1, "B", "B", 1, 1);
	}

	@Test
	public void testInvertMap() {
		assertMap(Immutable.invertMap(map), "A", -1, "B", null, null, 1);
		assertMap(Immutable.invertMap(nullFn(), map), "A", -1, "B", null, null, 1);
		assertMap(Immutable.invertMap(Maps::of, map), "A", -1, "B", null, null, 1);
	}

	@Test
	public void testConvertMap() {
		assertMap(Immutable.convertMapOf(fn, fn, array()), "-1", "-1", "null", "null", "1", "1");
		assertMap(Immutable.convertMap(nullFn(), fn, fn, array(), 1, 2), "null", "null", "1", "1");
		assertMap(Immutable.convertMap(Maps::of, fn, fn, array(), 1, 2), "null", "null", "1", "1");
		assertMap(Immutable.convertMap(fn, list), "-1", -1, "null", null, "1", 1);
		assertMap(Immutable.convertMap(fn, fn, list), "-1", "-1", "null", "null", "1", "1");
		assertMap(Immutable.convertMap(nullFn(), fn, fn, list), "-1", "-1", "null", "null", "1",
			"1");
		assertMap(Immutable.convertMap(Maps::of, fn, fn, list), "-1", "-1", "null", "null", "1",
			"1");
	}
}
