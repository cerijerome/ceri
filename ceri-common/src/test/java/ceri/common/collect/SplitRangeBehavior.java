package ceri.common.collect;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.fail;
import static ceri.common.test.AssertUtil.throwRuntime;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Functions;
import ceri.common.test.TestUtil;

public class SplitRangeBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = SplitRange.of(1, 3, 6);
		var eq0 = SplitRange.of(1, 3, 6);
		var eq1 = SplitRange.from(1, 2, 3);
		var eq2 = SplitRange.from(String::length, "a", "bb", "ccc");
		var ne0 = SplitRange.of(1, 3, 7);
		var ne1 = SplitRange.of(1, 3, 6, 6);
		TestUtil.exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldNotBreachEqualsContractForTyped() {
		var t = SplitRange.typed(String::length, "aa", "b", "ccc");
		var eq0 = SplitRange.typed(String::length, "aa", "b", "ccc");
		var ne0 = SplitRange.typed(s -> s.length() + 1, "aa", "b", "ccc");
		var ne1 = SplitRange.typed(String::length, "aa", "b", "ddd");
		var ne2 = SplitRange.typed(String::length, "aa", "b", "cccc");
		var ne3 = SplitRange.Typed.ofNull();
		TestUtil.exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldFindTypeAtPosition() {
		var typed = SplitRange.typed(String::length, "aa", "b", "ccc");
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
		var typed = SplitRange.typed(String::length, "aa", "b", "ccc");
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
		var typed = SplitRange.typed(String::length, "aa", "b", "ccc");
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
		var range = SplitRange.from(ArrayUtil.ints.fill(new int[300], 10));
		assertEquals(range.index(-1), -1);
		assertEquals(range.index(0), 0);
		assertEquals(range.index(1000), 100);
		assertEquals(range.index(2999), 299);
		assertEquals(range.index(3000), -1);
	}

	@Test
	public void shouldFindFromIndexes() {
		var range = SplitRange.from(ArrayUtil.ints.fill(new int[100], 10));
		assertEquals(range.length, 1000);
		assertEquals(range.index(-1), -1);
		assertEquals(range.index(0), 0);
		assertEquals(range.index(100), 10);
		assertEquals(range.index(999), 99);
		assertEquals(range.index(1000), -1);
	}

	@Test
	public void shouldFailToCreateFromDecreasingIndex() {
		assertThrown(() -> SplitRange.of(1, 3, 2));
	}

	@Test
	public void shouldProvideSectionStarts() {
		var range = SplitRange.of(1, 3, 6, 10);
		assertEquals(range.start(-1), 0);
		assertEquals(range.start(0), 0);
		assertEquals(range.start(1), 1);
		assertEquals(range.start(2), 3);
		assertEquals(range.start(3), 6);
		assertEquals(range.start(4), 0);
	}

	@Test
	public void shouldProvideSectionLength() {
		var range = SplitRange.of(1, 3, 6, 10);
		assertEquals(range.length(-1), 0);
		assertEquals(range.length(0), 1);
		assertEquals(range.length(1), 2);
		assertEquals(range.length(2), 3);
		assertEquals(range.length(3), 4);
		assertEquals(range.length(4), 0);
	}

	@Test
	public void shouldAcceptConsumer() {
		SplitRange t = SplitRange.of(1, 3, 6);
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
		SplitRange t = SplitRange.of(1, 3, 6);
		assertEquals(t.apply(-1, (_, _, _) -> throwRuntime()), null);
		assertEquals(t.apply(0, assertFunction(0, 0, 1, "test")), "test");
		assertEquals(t.apply(1, assertFunction(1, 0, 2, "test")), "test");
		assertEquals(t.apply(2, assertFunction(1, 1, 2, "test")), "test");
		assertEquals(t.apply(3, assertFunction(2, 0, 3, "test")), "test");
		assertEquals(t.apply(4, assertFunction(2, 1, 3, "test")), "test");
		assertEquals(t.apply(5, assertFunction(2, 2, 3, "test")), "test");
		assertEquals(t.apply(6, (_, _, _) -> throwRuntime()), null);
	}

	private static <T> SplitRange.Function<T> assertFunction(int index, int offset, int length,
		T t) {
		var consumer = assertConsumer(index, offset, length);
		return (_, off, len) -> {
			consumer.accept(index, off, len);
			return t;
		};
	}

	private static SplitRange.Consumer assertConsumer(int index, int offset, int length) {
		return (i, off, _) -> {
			assertEquals(i, index);
			assertEquals(off, offset);
			assertEquals(length, length);
		};
	}

	private static <T, R> Functions.ObjIntFunction<T, R> assertTypeFunction(T type, int offset,
		R r) {
		var consumer = assertTypeConsumer(type, offset);
		return (t, off) -> {
			consumer.accept(t, off);
			return r;
		};
	}

	private static <T> Functions.ObjIntConsumer<T> assertTypeConsumer(T type, int offset) {
		return (t, off) -> {
			assertEquals(t, type);
			assertEquals(off, offset);
		};
	}
}
