package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.fail;
import static ceri.common.test.AssertUtil.throwRuntime;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.util.function.ObjIntConsumer;
import org.junit.Test;
import ceri.common.data.IntArray;
import ceri.common.function.Funcs.ObjIntFunction;

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
	public void shouldNotBreachEqualsContractForTyped() {
		Indexer.Typed<String> t = Indexer.typed(String::length, "aa", "b", "ccc");
		Indexer.Typed<String> eq0 = Indexer.typed(String::length, "aa", "b", "ccc");
		Indexer.Typed<String> ne0 = Indexer.typed(s -> s.length() + 1, "aa", "b", "ccc");
		Indexer.Typed<String> ne1 = Indexer.typed(String::length, "aa", "b", "ddd");
		Indexer.Typed<String> ne2 = Indexer.typed(String::length, "aa", "b", "cccc");
		Indexer.Typed<String> ne3 = Indexer.Typed.ofNull();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldFindTypeAtPosition() {
		var typed = Indexer.typed(String::length, "aa", "b", "ccc");
		assertEquals(typed.length(), 6);
		assertEquals(typed.at(-1), null);
		assertEquals(typed.at(0), "aa");
		assertEquals(typed.at(1), "aa");
		assertEquals(typed.at(2), "b");
		assertEquals(typed.at(3), "ccc");
		assertEquals(typed.at(4), "ccc");
		assertEquals(typed.at(5), "ccc");
		assertEquals(typed.at(6), null);
	}

	@Test
	public void shouldAcceptTypeConsumer() {
		var typed = Indexer.typed(String::length, "aa", "b", "ccc");
		typed.accept(-1, (_, _) -> fail());
		typed.accept(0, assertTypeConsumer("aa", 0));
		typed.accept(1, assertTypeConsumer("aa", 1));
		typed.accept(2, assertTypeConsumer("b", 0));
		typed.accept(3, assertTypeConsumer("ccc", 0));
		typed.accept(4, assertTypeConsumer("ccc", 1));
		typed.accept(5, assertTypeConsumer("ccc", 2));
		typed.accept(6, (_, _) -> fail());
	}

	@Test
	public void shouldApplyTypeFunction() {
		var typed = Indexer.typed(String::length, "aa", "b", "ccc");
		assertEquals(typed.apply(-1, (_, _) -> throwRuntime()), null);
		assertEquals(typed.apply(0, assertTypeFunction("aa", 0, "test")), "test");
		assertEquals(typed.apply(1, assertTypeFunction("aa", 1, "test")), "test");
		assertEquals(typed.apply(2, assertTypeFunction("b", 0, "test")), "test");
		assertEquals(typed.apply(3, assertTypeFunction("ccc", 0, "test")), "test");
		assertEquals(typed.apply(4, assertTypeFunction("ccc", 1, "test")), "test");
		assertEquals(typed.apply(5, assertTypeFunction("ccc", 2, "test")), "test");
		assertEquals(typed.apply(6, (_, _) -> throwRuntime()), null);
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
	public void shouldFailToCreateFromDecreasingIndex() {
		assertThrown(() -> Indexer.of(1, 3, 2));
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
		t.accept(-1, (_, _, _) -> fail());
		t.accept(0, assertConsumer(0, 0, 1));
		t.accept(1, assertConsumer(1, 0, 2));
		t.accept(2, assertConsumer(1, 1, 2));
		t.accept(3, assertConsumer(2, 0, 3));
		t.accept(4, assertConsumer(2, 1, 3));
		t.accept(5, assertConsumer(2, 2, 3));
		t.accept(6, (_, _, _) -> fail());
	}

	@Test
	public void shouldApplyFunction() {
		Indexer t = Indexer.of(1, 3, 6);
		assertEquals(t.apply(-1, (_, _, _) -> throwRuntime()), null);
		assertEquals(t.apply(0, assertFunction(0, 0, 1, "test")), "test");
		assertEquals(t.apply(1, assertFunction(1, 0, 2, "test")), "test");
		assertEquals(t.apply(2, assertFunction(1, 1, 2, "test")), "test");
		assertEquals(t.apply(3, assertFunction(2, 0, 3, "test")), "test");
		assertEquals(t.apply(4, assertFunction(2, 1, 3, "test")), "test");
		assertEquals(t.apply(5, assertFunction(2, 2, 3, "test")), "test");
		assertEquals(t.apply(6, (_, _, _) -> throwRuntime()), null);
	}

	private static <T> Indexer.Function<T> assertFunction(int index, int offset, int length, T t) {
		var consumer = assertConsumer(index, offset, length);
		return (_, off, len) -> {
			consumer.accept(index, off, len);
			return t;
		};
	}

	private static Indexer.Consumer assertConsumer(int index, int offset, int length) {
		return (i, off, _) -> {
			assertEquals(i, index);
			assertEquals(off, offset);
			assertEquals(length, length);
		};
	}

	private static <T, R> ObjIntFunction<T, R> assertTypeFunction(T type, int offset, R r) {
		var consumer = assertTypeConsumer(type, offset);
		return (t, off) -> {
			consumer.accept(t, off);
			return r;
		};
	}

	private static <T> ObjIntConsumer<T> assertTypeConsumer(T type, int offset) {
		return (t, off) -> {
			assertEquals(t, type);
			assertEquals(off, offset);
		};
	}
}
