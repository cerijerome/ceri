package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.util.Iterator;
import org.junit.Test;
import ceri.common.test.Captor;

public class IteratorUtilTest {

	@Test
	public void testIndexed() {
		Iterator<String> iter = IteratorUtil.indexed(3, i -> String.valueOf(i));
		assertEquals(iter.next(), "0");
		assertEquals(iter.next(), "1");
		assertEquals(iter.next(), "2");
		assertThrown(() -> iter.next());
	}

	@Test
	public void testIntIndexed() {
		var iter = IteratorUtil.intIndexed(3, i -> -i);
		assertEquals(iter.next(), 0);
		assertEquals(iter.next(), -1);
		assertEquals(iter.next(), -2);
		assertThrown(() -> iter.next());
	}

	@Test
	public void testLongIndexed() {
		var iter = IteratorUtil.longIndexed(3, l -> -l);
		assertEquals(iter.next(), 0L);
		assertEquals(iter.next(), -1L);
		assertEquals(iter.next(), -2L);
		assertThrown(() -> iter.next());
	}

	@Test
	public void testSpliteratorTryAdvance() {
		Captor<Object> captor = Captor.of();
		IteratorUtil.spliterator(null, 1, 0).tryAdvance(captor);
		captor.verify();
	}

	@Test
	public void testSpliteratorNext() {
		Captor<Object> captor = Captor.of();
		IteratorUtil.spliterator(null, null, 1, 0).tryAdvance(captor);
		captor.verify();
		IteratorUtil.spliterator(() -> false, null, 1, 0).tryAdvance(captor);
		captor.verify();
		IteratorUtil.spliterator(() -> true, null, 1, 0).tryAdvance(captor);
		captor.verify();
		IteratorUtil.spliterator(() -> true, () -> "test", 1, 0).tryAdvance(captor);
		captor.verify("test");
	}

	@Test
	public void testIntSpliteratorTryAdvance() {
		var captor = Captor.of();
		IteratorUtil.intSpliterator(null, 1, 0).tryAdvance(captor);
		captor.verify();
	}

	@Test
	public void testIntSpliteratorNext() {
		var captor = Captor.of();
		IteratorUtil.intSpliterator(null, null, 1, 0).tryAdvance(captor);
		captor.verify();
		IteratorUtil.intSpliterator(() -> false, null, 1, 0).tryAdvance(captor);
		captor.verify();
		IteratorUtil.intSpliterator(() -> true, null, 1, 0).tryAdvance(captor);
		captor.verify();
		IteratorUtil.intSpliterator(() -> true, () -> 333, 1, 0).tryAdvance(captor);
		captor.verify(333);
	}

	@Test
	public void testLongSpliteratorTryAdvance() {
		var captor = Captor.of();
		IteratorUtil.longSpliterator(null, 1, 0).tryAdvance(captor);
		captor.verify();
	}

	@Test
	public void testLongSpliteratorNext() {
		var captor = Captor.<Long>of();
		IteratorUtil.longSpliterator(null, null, 1, 0).tryAdvance(captor);
		captor.verify();
		IteratorUtil.longSpliterator(() -> false, null, 1, 0).tryAdvance(captor);
		captor.verify();
		IteratorUtil.longSpliterator(() -> true, null, 1, 0).tryAdvance(captor);
		captor.verify();
		IteratorUtil.longSpliterator(() -> true, () -> 333L, 1, 0).tryAdvance(captor);
		captor.verify(333L);
	}
}
