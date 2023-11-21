package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.fail;
import static ceri.common.test.AssertUtil.throwRuntime;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.common.data.IntArray;

public class IndexerBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Indexer t = Indexer.of(1, 3, 6);
		Indexer eq0 = Indexer.of(1, 3, 6);
		Indexer eq1 = Indexer.from(1, 2, 3);
		Indexer eq2 = Indexer.from(String::length, "a", "bb", "ccc");
		Indexer ne0 = Indexer.of(1, 3, 7);
		Indexer ne1 = Indexer.of(1, 3, 6, 6);
		exerciseEquals(t, eq0, eq1, eq2);
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
	public void shouldProvideSectionStarts() {
		var indexer = Indexer.of(1, 3, 6, 10);
		assertEquals(indexer.start(-1), 0);
		assertEquals(indexer.start(0), 0);
		assertEquals(indexer.start(1), 1);
		assertEquals(indexer.start(2), 3);
		assertEquals(indexer.start(3), 6);
		assertEquals(indexer.start(4), 0);
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

	@Test
	public void shouldAcceptConsumer() {
		Indexer t = Indexer.of(1, 3, 6);
		t.accept(-1, (i, off, len) -> fail());
		t.accept(0, assertConsumer(0, 0, 1));
		t.accept(1, assertConsumer(1, 0, 2));
		t.accept(2, assertConsumer(1, 1, 2));
		t.accept(3, assertConsumer(2, 0, 3));
		t.accept(4, assertConsumer(2, 1, 3));
		t.accept(5, assertConsumer(2, 2, 3));
		t.accept(6, (i, off, len) -> fail());
	}

	@Test
	public void shouldAcceptFunction() {
		Indexer t = Indexer.of(1, 3, 6);
		assertEquals(t.apply(-1, (i, off, len) -> throwRuntime()), null);
		assertEquals(t.apply(0, assertFunction(0, 0, 1, "test")), "test");
		assertEquals(t.apply(1, assertFunction(1, 0, 2, "test")), "test");
		assertEquals(t.apply(2, assertFunction(1, 1, 2, "test")), "test");
		assertEquals(t.apply(3, assertFunction(2, 0, 3, "test")), "test");
		assertEquals(t.apply(4, assertFunction(2, 1, 3, "test")), "test");
		assertEquals(t.apply(5, assertFunction(2, 2, 3, "test")), "test");
		assertEquals(t.apply(6, (i, off, len) -> throwRuntime()), null);
	}

	private static <T> Indexer.Function<T> assertFunction(int index, int offset, int length, T t) {
		var consumer = assertConsumer(index, offset, length);
		return (i, off, len) -> {
			consumer.accept(index, off, len);
			return t;
		};
	}

	private static Indexer.Consumer assertConsumer(int index, int offset, int length) {
		return (i, off, len) -> {
			assertEquals(i, index);
			assertEquals(off, offset);
			assertEquals(length, length);
		};
	}
}
