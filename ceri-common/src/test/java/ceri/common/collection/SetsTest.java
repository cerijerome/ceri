package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import ceri.common.function.Functions;

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
		assertUnordered(Sets.build(-1, 0).add(nullSet).get(), -1, 0);
		assertUnordered(Sets.build(-1, 0).add(set).get(), -1, 0, null, 1);
		assertUnordered(Sets.build(-1, 0).add(nullArray).get(), -1, 0);
		assertUnordered(Sets.build(-1, 0).add(null, nullArray).get(), -1, 0, null);
		assertUnordered(Sets.build(-1, 0).add(null, 1).get(), -1, 0, null, 1);
		assertUnordered(Sets.build(-1, 0).apply(null).wrap(), -1, 0);
		assertUnordered(Sets.build(-1, 0).apply(l -> Collectable.addAll(l, 1)).wrap(), -1, 0, 1);
		assertUnordered(Sets.build(Sets::link, -1, null, 1).wrap(), -1, null, 1);
	}

	@Test
	public void testOfArray() {
		assertUnordered(Sets.ofAll(nullArray));
		assertUnordered(Sets.ofAll(emptyArray));
		assertUnordered(Sets.ofAll(array()), -1, null, 1);
		assertUnordered(Sets.of(nullArray, 1));
		assertUnordered(Sets.of(emptyArray, 1));
		assertUnordered(Sets.of(array(), 1), null, 1);
	}

	@Test
	public void testOfIterable() {
		assertUnordered(Sets.of(nullSet));
		assertUnordered(Sets.of(emptySet));
		assertUnordered(Sets.of(set), -1, null, 1);
		assertUnordered(Sets.of(list.subList(1, 3)), null, 1);
	}

	@Test
	public void testAdaptArray() throws Exception {
		assertUnordered(Sets.adaptAll(nullFn, array()));
		assertUnordered(Sets.adaptAll(fn, nullArray));
		assertUnordered(Sets.adaptAll(fn, emptyArray));
		assertUnordered(Sets.adaptAll(fn, array()), "-1", "null", "1");
		assertUnordered(Sets.adapt(nullFn, array(), 1));
		assertUnordered(Sets.adapt(fn, nullArray, 1));
		assertUnordered(Sets.adapt(fn, emptyArray, 1));
		assertUnordered(Sets.adapt(fn, array(), 1), "null", "1");
	}

	@Test
	public void testAdaptIterable() throws Exception {
		assertUnordered(Sets.adapt(nullFn, set));
		assertUnordered(Sets.adapt(fn, nullArray, 1));
		assertUnordered(Sets.adapt(fn, emptyArray, 1));
		assertUnordered(Sets.adapt(fn, array(), 1), "null", "1");
	}

	@Test
	public void testConvert() throws Exception {
		assertUnordered(Sets.convert(nullBiFn, map));
		assertUnordered(Sets.convert(biFn, nullMap));
		assertUnordered(Sets.convert(biFn, emptyMap));
		assertUnordered(Sets.convert(biFn, map), -1, "B", 1);
	}
}
