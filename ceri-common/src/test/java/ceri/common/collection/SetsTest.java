package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class SetsTest {
	private final Set<Integer> NULL = null;
	private final Set<Integer> empty = Set.of();
	private final List<Integer> ints = Immutable.listOf(-1, null, 1, null, 1);

	@Test
	public void testSupplier() {
		assertUnordered(Sets.Supplier.hash(1));
		assertUnordered(Sets.Supplier.linked());
		assertUnordered(Sets.Supplier.linked(1));
		assertUnordered(Sets.Supplier.tree());
		assertUnordered(Sets.Supplier.identity());
	}

	@Test
	public void testOf() {
		assertUnordered(Sets.of((Integer[]) null));
		assertUnordered(Sets.of(-1, null, 1), -1, null, 1);
	}

	@Test
	public void testSetFromArray() {
		Integer[] array = { -1, null, 1, null, 1 };
		assertUnordered(Sets.set(null, 0));
		assertUnordered(Sets.set(array, 3), null, 1);
	}

	@Test
	public void testSetFromIterable() {
		assertUnordered(Sets.set(NULL));
		assertUnordered(Sets.set(empty));
		assertUnordered(Sets.set(ints), -1, null, 1);
	}

	@Test
	public void testAdaptArray() throws Exception {
		Integer[] array = { -1, null, 1 };
		assertUnordered(Sets.adaptAll(null, array));
		assertUnordered(Sets.adaptAll(String::valueOf, (Integer[]) null));
		assertUnordered(Sets.adapt(String::valueOf, array, 1), "null", "1");
	}

	@Test
	public void testAdaptIterable() throws Exception {
		assertUnordered(Sets.adapt(null, ints));
		assertUnordered(Sets.adapt(String::valueOf, ints), "-1", "null", "1");
	}

	@Test
	public void testUnmap() throws Exception {
		var map = Maps.Builder.of(1, "A", null, "B", 3, null).map;
		assertUnordered(Sets.unmap(null, map));
		assertUnordered(Sets.unmap(SetsTest::either, null));
		assertUnordered(Sets.unmap(SetsTest::either, map), 1, "B", 3);
	}

	private static Object either(Object l, Object r) {
		return l != null ? l : r;
	}
}
