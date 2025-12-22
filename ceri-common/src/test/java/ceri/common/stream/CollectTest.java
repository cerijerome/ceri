package ceri.common.stream;

import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import org.junit.Test;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Sets;
import ceri.common.test.Assert;

public class CollectTest {
	private static final Immutable.Wrap<NavigableSet<Integer>> wset = Immutable.Wrap.navSet();
	private static final Immutable.Wrap<NavigableMap<String, Integer>> wmap =
		Immutable.Wrap.navMap();

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Collect.class, Collect.Ints.class, Collect.Longs.class,
			Collect.Doubles.class);
	}

	@Test
	public void testNoCombiner() {
		Assert.array(Lists.ofAll(1, -1, 0).stream().collect(Collect.array()), 1, -1, 0);
		Assert.unsupportedOp(
			() -> Lists.ofAll(1, -1, 0).stream().parallel().collect(Collect.array()));
	}

	@Test
	public void testOf() {
		Assert.ordered(testStream().collect(Collect.of(Lists::of, List::add)), -1, null, 1);
	}

	@Test
	public void testSortedArray() {
		Assert.array(stream(1, -1, 0).collect(Collect.sortedArray(Integer.class)), -1, 0, 1);
	}

	@Test
	public void testCollection() {
		Assert.ordered(testStream().collect(Collect.collection(wset)), null, -1, 1);
	}

	@Test
	public void testMap() {
		Assert.map(testStream().collect(Collect.map(String::valueOf)), "-1", -1, "null", null, "1",
			1);
		Assert.map(testStream().collect(Collect.map(wmap, String::valueOf)), "-1", -1, "null", null,
			"1", 1);
	}

	@Test
	public void testMapSet() {
		Assert.map(testStream().collect(Collect.mapSet(String::valueOf)), "-1", Sets.ofAll(-1),
			"null", Sets.ofAll((Integer) null), "1", Sets.ofAll(1));
	}

	private static Stream<RuntimeException, Integer> stream(Integer... values) {
		return Streams.of(values);
	}

	private static Stream<RuntimeException, Integer> testStream() {
		return stream(-1, null, 1);
	}
}
