package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.common.data.IntArray;

public class IndexerBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Indexer t = Indexer.of(1, 3, 6);
		Indexer eq0 = Indexer.of(1, 3, 6);
		Indexer eq1 = Indexer.from(1, 2, 3);
		Indexer ne0 = Indexer.of(1, 3, 7);
		Indexer ne1 = Indexer.of(1, 3, 6, 6);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldFindFromManyIndexes() {
		var indexer = Indexer.from(IntArray.Encoder.fixed(300).fill(300, 10).ints());
		assertEquals(indexer.index(-1), -1);
		assertEquals(indexer.index(0), 0);
		assertEquals(indexer.index(1000), 100);
		assertEquals(indexer.index(2999), 299);
		assertEquals(indexer.index(3000), -1);
	}

	@Test
	public void shouldFindFromIndexes() {
		var indexer = Indexer.from(IntArray.Encoder.fixed(100).fill(100, 10).ints());
		assertEquals(indexer.index(-1), -1);
		assertEquals(indexer.index(0), 0);
		assertEquals(indexer.index(100), 10);
		assertEquals(indexer.index(999), 99);
		assertEquals(indexer.index(1000), -1);
	}

	@Test
	public void shouldProvideSectionLength() {
		var indexer = Indexer.of(1, 3, 6, 10);
		assertEquals(indexer.length(-1), 0);
		assertEquals(indexer.length(0), 1);
		assertEquals(indexer.length(1), 2);
		assertEquals(indexer.length(2), 3);
		assertEquals(indexer.length(3), 4);
		assertEquals(indexer.length(4), 0);
	}

}
