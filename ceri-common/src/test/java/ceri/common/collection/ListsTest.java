package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.util.List;
import org.junit.Test;

public class ListsTest {
	private final List<Integer> NULL = null;
	private final List<Integer> empty = List.of();
	private final List<Integer> ints = Immutable.listOf(-1, null, 1, null, 1);

	@Test
	public void testSupplier() {
		assertOrdered(Lists.Supplier.array(1));
		assertOrdered(Lists.Supplier.linked());
	}

	@Test
	public void testOf() {
		assertOrdered(Lists.of((Integer[]) null));
		assertOrdered(Lists.of(-1, null, 1), -1, null, 1);
	}

	@Test
	public void testListFromArray() {
		Integer[] array = { -1, null, 1, null, 1 };
		assertOrdered(Lists.of(null, 0), null, 0);
		assertOrdered(Lists.list(array, 3), null, 1);
	}

	@Test
	public void testListFromIterable() {
		assertOrdered(Lists.list(NULL));
		assertOrdered(Lists.list(empty));
		assertOrdered(Lists.list(ints), -1, null, 1, null, 1);
	}

	@Test
	public void testAdaptArray() throws Exception {
		Integer[] array = { -1, null, 1 };
		assertOrdered(Lists.adaptAll(null, array));
		assertOrdered(Lists.adaptAll(String::valueOf, (Integer[]) null));
		assertOrdered(Lists.adapt(String::valueOf, array, 1), "null", "1");
	}

	@Test
	public void testAdaptIterable() throws Exception {
		assertOrdered(Lists.adapt(null, ints));
		assertOrdered(Lists.adapt(String::valueOf, ints), "-1", "null", "1", "null", "1");
	}

	@Test
	public void testUnmap() throws Exception {
		var map = Maps.Builder.of(1, "A", null, "B", 3, null).map;
		assertOrdered(Lists.unmap(null, map));
		assertOrdered(Lists.unmap(ListsTest::either, null));
		assertUnordered(Lists.unmap(ListsTest::either, map), 1, "B", 3);
	}

	@Test
	public void testAt() {
		assertEquals(Lists.at(NULL, 0), null);
		assertEquals(Lists.at(empty, 0), null);
		assertEquals(Lists.at(ints, -1), null);
		assertEquals(Lists.at(ints, 5), null);
		assertEquals(Lists.at(ints, 2), 1);
	}

	@Test
	public void testLast() {
		assertEquals(Lists.last(NULL), null);
		assertEquals(Lists.last(empty), null);
		assertEquals(Lists.last(ints), 1);
	}

	@Test
	public void testInsertArray() {
		Integer[] array = { 1, null, 1 };
		assertEquals(Lists.insertAll(NULL, 0, -1, null), null);
		assertOrdered(Lists.insertAll(Lists.of(-1, null), 1, (Integer[]) null), -1, null);
		assertOrdered(Lists.insertAll(Lists.of(-1, null), 1, 1, null, 1), -1, 1, null, 1, null);
		assertOrdered(Lists.insert(Lists.of(-1, null), 1, array, 1), -1, null, 1, null);
	}

	@Test
	public void testInsertIterable() {
		assertEquals(Lists.insert(NULL, 1, Lists.of(1, null)), null);
		assertOrdered(Lists.insert(Lists.of(-1, null), 1, NULL), -1, null);
		assertEquals(Lists.insert(NULL, 1, ints), null);
		assertOrdered(Lists.insert(Lists.of(-1, null), 1, NULL), -1, null);
		assertOrdered(Lists.insert(Lists.of(-1, null), 1, ints), -1, -1, null, 1, null, 1, null);
	}

	private static Object either(Object l, Object r) {
		return l != null ? l : r;
	}
}
