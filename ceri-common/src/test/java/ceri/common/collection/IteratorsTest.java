package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.util.Iterator;
import org.junit.Test;

public class IteratorsTest {

	@Test
	public void testIndexed() {
		Iterator<String> iter = Iterators.indexed(3, i -> String.valueOf(i));
		assertEquals(iter.next(), "0");
		assertEquals(iter.next(), "1");
		assertEquals(iter.next(), "2");
		assertThrown(() -> iter.next());
	}

	@Test
	public void testIntIndexed() {
		var iter = Iterators.intIndexed(3, i -> -i);
		assertEquals(iter.next(), 0);
		assertEquals(iter.next(), -1);
		assertEquals(iter.next(), -2);
		assertThrown(() -> iter.next());
	}

	@Test
	public void testLongIndexed() {
		var iter = Iterators.longIndexed(3, l -> -l);
		assertEquals(iter.next(), 0L);
		assertEquals(iter.next(), -1L);
		assertEquals(iter.next(), -2L);
		assertThrown(() -> iter.next());
	}

}
