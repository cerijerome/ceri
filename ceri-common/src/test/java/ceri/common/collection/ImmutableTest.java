package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertImmutable;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import ceri.common.collection.Immutable.Wrap;
import ceri.common.comparator.Comparators;
import ceri.common.function.Functions;
import ceri.common.util.BasicUtil;

public class ImmutableTest {
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
	private static final Map<Integer, String> map =
		Immutable.of(Wrap.seqMap(), -1, "A", null, "B", 1, null);
	private static final Map<String, Object> idMap =
		Immutable.of(Wrap.idMap(), s0, -1, null, 1, s1, null);
	private static final Integer[] nullArray = null;
	private static final Integer[] emptyArray = new Integer[0];
	private static final Functions.Function<Object, Object> nullFn = null;
	private static final Functions.Function<Object, Object> fn = String::valueOf;
	private static final Functions.BiOperator<Object> nullBiFn = null;
	private static final Functions.BiOperator<Object> biFn = (l, r) -> l != null ? l : r;
	private static final Functions.BiPredicate<Object, Object> nullBiPr = null;
	private static final Functions.BiPredicate<Object, Object> biPr = BasicUtil::noneNull;
	private static final Comparator<Integer> comp = Comparators.nullsLast();

	private static Integer[] array() {
		return list.toArray(Integer[]::new);
	}

	@Test
	public void testBiMapFromNull() {
		var biMap = Immutable.biMap(nullMap);
		assertMap(biMap.keys);
		assertMap(biMap.values);
		assertEquals(biMap.key(null), null);
		assertEquals(biMap.value(null), null);
	}

	@Test
	public void testBiMapFromMap() {
		var biMap = Immutable.biMap(map);
		assertMap(biMap.keys, -1, "A", null, "B", 1, null);
		assertMap(biMap.values, "A", -1, "B", null, null, 1);
		assertEquals(biMap.key(null), 1);
		assertEquals(biMap.value(null), "B");
	}

	@Test
	public void testWrapTypeWrap() {
		assertUnordered(Wrap.collect().wrap(null));
		assertUnordered(Wrap.collect().wrap(c -> Mutable.add(c, list)), -1, null, 1);
		assertOrdered(Wrap.seqCollect().wrap(c -> Mutable.add(c, list)), -1, null, 1);
		assertOrdered(Wrap.linkList().wrap(c -> Mutable.add(c, list)), -1, null, 1);
		assertOrdered(Wrap.seqSet().wrap(c -> Mutable.add(c, list)), -1, null, 1);
		assertOrdered(Wrap.<Integer>sortSet().wrap(c -> Mutable.add(c, list)), null, -1, 1);
		assertOrdered(Wrap.sortSet(comp).wrap(c -> Mutable.add(c, list)), -1, 1, null);
		assertOrdered(Wrap.<Integer>navSet().wrap(c -> Mutable.add(c, list)), null, -1, 1);
		assertOrdered(Wrap.navSet(comp).wrap(c -> Mutable.add(c, list)), -1, 1, null);
		assertUnordered(Wrap.idSet().wrap(c -> Mutable.add(c, idSet)), s0, null, s1);
		assertMap(Wrap.map().wrap(c -> Mutable.put(c, map)), -1, "A", null, "B", 1, null);
		assertOrdered(Wrap.seqMap().wrap(c -> Mutable.put(c, map)), -1, "A", null, "B", 1, null);
		assertOrdered(Wrap.<Integer, Object>sortMap().wrap(c -> Mutable.put(c, map)), //
			null, "B", -1, "A", 1, null);
		assertOrdered(Wrap.sortMap(comp).wrap(c -> Mutable.put(c, map)), //
			-1, "A", 1, null, null, "B");
		assertOrdered(Wrap.navMap(comp).wrap(c -> Mutable.put(c, map)), //
			-1, "A", 1, null, null, "B");
		assertMap(Wrap.idMap().wrap(c -> Mutable.put(c, idMap)), s0, -1, null, 1, s1, null);
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
		assertMap(assertImmutable(Immutable.wrap(nullMap)));
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
	public void testOfCollection() {
		assertEquals(Immutable.ofAll(null), null);
		assertUnordered(Immutable.ofAll(Wrap.collect()));
		assertOrdered(Immutable.ofAll(Wrap.seqCollect(), array()), -1, null, 1);
		assertEquals(Immutable.of(null, list), null);
		assertUnordered(Immutable.of(Wrap.idSet(), list), -1, null, 1);
	}

	@Test
	public void testOfMap() {
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
	public void testAdaptCollection() {
		assertEquals(Immutable.adaptAll(null, fn, array()), null);
	}

	@Test
	public void testAdaptList() {
		assertOrdered(assertImmutable(Immutable.adaptListOf(fn, array())), "-1", "null", "1");
		assertOrdered(assertImmutable(Immutable.adaptList(fn, array(), 1)), "null", "1");
		assertOrdered(assertImmutable(Immutable.adaptList(fn, list)), "-1", "null", "1");
	}

	@Test
	public void testConvertList() {
		assertUnordered(assertImmutable(Immutable.convertList(biFn, map)), -1, "B", 1);
	}

	@Test
	public void testWrapSet() {
		assertOrdered(assertImmutable(Immutable.wrap(nullSet)));
	}

	@Test
	public void testAdaptSet() {
		assertUnordered(assertImmutable(Immutable.adaptSetOf(fn, array())), "-1", "null", "1");
		assertUnordered(assertImmutable(Immutable.adaptSet(fn, array(), 1)), "null", "1");
		assertUnordered(assertImmutable(Immutable.adaptSet(fn, list)), "-1", "null", "1");
	}

	@Test
	public void testConvertSet() {
		assertUnordered(assertImmutable(Immutable.convertSet(biFn, map)), -1, "B", 1);
	}

	@Test
	public void testConvertMap() {
		assertMap(assertImmutable(Immutable.wrap(nullMap)));
	}

	@Test
	public void testMapOfKeysAndValues() {
		assertMap(assertImmutable(Immutable.mapOf(1, "A")), 1, "A");
		assertMap(assertImmutable(Immutable.mapOf(1, "A", null, "B")), 1, "A", null, "B");
		assertMap(assertImmutable(Immutable.mapOf(1, "A", null, "B", 3, null)), 1, "A", null, "B",
			3, null);
		assertMap(assertImmutable(Immutable.mapOf(1, "A", null, "B", 3, null, null, null)), 1, "A",
			null, null, 3, null);
		assertMap(assertImmutable(Immutable.mapOf(1, "A", null, "B", 3, null, null, null, 5, "E")),
			1, "A", null, null, 3, null, 5, "E");
	}

	@Test
	public void testAdaptMap() {
		assertMap(assertImmutable(Immutable.adaptMap(fn, map)), "-1", "A", "null", "B", "1", null);
		assertMap(assertImmutable(Immutable.adaptMap(fn, fn, map)), "-1", "A", "null", "B", "1",
			"null");
	}

	@Test
	public void testBiAdaptMap() {
		assertMap(assertImmutable(Immutable.biAdaptMap(biFn, map)), -1, "A", "B", "B", 1, null);
		assertMap(assertImmutable(Immutable.biAdaptMap(biFn, biFn, map)), -1, -1, "B", "B", 1, 1);
	}

	@Test
	public void testAssertImmutables() {
		// Make sure immutable assertions are correct
		assertImmutable(Map.of());
		assertImmutable(Collections.unmodifiableMap(new HashMap<>()));
		assertImmutable(Map.of(1, "A"));
		assertImmutable(Collections.unmodifiableMap(new HashMap<>(Map.of(1, "A"))));
		assertImmutable(List.of());
		assertImmutable(Collections.unmodifiableList(new ArrayList<>()));
		assertImmutable(List.of(1));
		assertImmutable(Collections.unmodifiableList(new ArrayList<>(Set.of(1))));
		assertImmutable(Set.of());
		assertImmutable(Collections.unmodifiableSet(new HashSet<>()));
		assertImmutable(Set.of(1));
		assertImmutable(Collections.unmodifiableSet(new HashSet<>(Set.of(1))));
	}

}
