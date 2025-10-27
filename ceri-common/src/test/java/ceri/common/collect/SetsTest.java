package ceri.common.collect;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.test.Assert;

public class SetsTest {
	private static final Set<Integer> nullSet = null;
	private static final Set<Integer> emptySet = Set.of();
	private static final List<Integer> list = Lists.ofAll(-1, null, 1);
	private static final Set<Integer> set = Immutable.set(Sets::link, list);
	private static final Map<Integer, String> nullMap = null;
	private static final Map<Integer, String> emptyMap = Map.of();
	private static final Map<Integer, String> map = Immutable.mapOf(-1, "A", null, "B", 1, null);
	private static final Integer[] nullArray = null;
	private static final Integer[] emptyArray = new Integer[0];
	private static final Functions.Function<Object, Object> nullFn = null;
	private static final Functions.Function<Object, Object> fn = String::valueOf;
	private static final Functions.BiOperator<Object> nullBiFn = null;
	private static final Functions.BiOperator<Object> biFn = (l, r) -> l != null ? l : r;

	private static Integer[] array() {
		return set.toArray(Integer[]::new);
	}

	@Test
	public void testBuilder() {
		Assert.unordered(Sets.build(-1, 0).add(nullSet).get(), -1, 0);
		Assert.unordered(Sets.build(-1, 0).add(set).get(), -1, 0, null, 1);
		Assert.unordered(Sets.build(-1, 0).add(nullArray).get(), -1, 0);
		Assert.unordered(Sets.build(-1, 0).add(null, nullArray).get(), -1, 0, null);
		Assert.unordered(Sets.build(-1, 0).add(null, 1).get(), -1, 0, null, 1);
		Assert.unordered(Sets.build(-1, 0).apply(null).wrap(), -1, 0);
		Assert.unordered(Sets.build(-1, 0).apply(l -> Collectable.addAll(l, 1)).wrap(), -1, 0, 1);
		Assert.unordered(Sets.build(Sets::link, -1, null, 1).wrap(), -1, null, 1);
	}

	@Test
	public void testCreate() {
		Assert.ordered(Sets.link(nullSet));
		Assert.ordered(Sets.link(set), -1, null, 1);
		Assert.ordered(Sets.tree(nullSet));
		Assert.ordered(Sets.tree(set), null, -1, 1);
	}

	@Test
	public void testOfArray() {
		Assert.unordered(Sets.ofAll(nullArray));
		Assert.unordered(Sets.ofAll(emptyArray));
		Assert.unordered(Sets.ofAll(array()), -1, null, 1);
		Assert.unordered(Sets.of(nullArray, 1));
		Assert.unordered(Sets.of(emptyArray, 1));
		Assert.unordered(Sets.of(array(), 1), null, 1);
	}

	@Test
	public void testOfIterable() {
		Assert.unordered(Sets.of(nullSet));
		Assert.unordered(Sets.of(emptySet));
		Assert.unordered(Sets.of(set), -1, null, 1);
		Assert.unordered(Sets.of(list.subList(1, 3)), null, 1);
	}

	@Test
	public void testAdaptArray() throws Exception {
		Assert.unordered(Sets.adaptAll(nullFn, array()));
		Assert.unordered(Sets.adaptAll(fn, nullArray));
		Assert.unordered(Sets.adaptAll(fn, emptyArray));
		Assert.unordered(Sets.adaptAll(fn, array()), "-1", "null", "1");
		Assert.unordered(Sets.adapt(nullFn, array(), 1));
		Assert.unordered(Sets.adapt(fn, nullArray, 1));
		Assert.unordered(Sets.adapt(fn, emptyArray, 1));
		Assert.unordered(Sets.adapt(fn, array(), 1), "null", "1");
	}

	@Test
	public void testAdaptIterable() throws Exception {
		Assert.unordered(Sets.adapt(nullFn, set));
		Assert.unordered(Sets.adapt(fn, nullArray, 1));
		Assert.unordered(Sets.adapt(fn, emptyArray, 1));
		Assert.unordered(Sets.adapt(fn, array(), 1), "null", "1");
	}

	@Test
	public void testConvert() throws Exception {
		Assert.unordered(Sets.convert(nullBiFn, map));
		Assert.unordered(Sets.convert(biFn, nullMap));
		Assert.unordered(Sets.convert(biFn, emptyMap));
		Assert.unordered(Sets.convert(biFn, map), -1, "B", 1);
	}
}
