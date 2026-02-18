package ceri.common.collect;

import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class SplitRangeBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = SplitRange.of(1, 3, 6);
		var eq0 = SplitRange.of(1, 3, 6);
		var eq1 = SplitRange.from(1, 2, 3);
		var eq2 = SplitRange.from(String::length, "a", "bb", "ccc");
		var ne0 = SplitRange.of(1, 3, 7);
		var ne1 = SplitRange.of(1, 3, 6, 6);
		Testing.exerciseEquals(t, eq0, eq1, eq2);
		Assert.notEqualAll(t, ne0, ne1);
	}

	@Test
	public void shouldNotBreachEqualsContractForTyped() {
		var t = SplitRange.typed(String::length, "aa", "b", "ccc");
		var eq0 = SplitRange.typed(String::length, "aa", "b", "ccc");
		var ne0 = SplitRange.typed(s -> s.length() + 1, "aa", "b", "ccc");
		var ne1 = SplitRange.typed(String::length, "aa", "b", "ddd");
		var ne2 = SplitRange.typed(String::length, "aa", "b", "cccc");
		var ne3 = SplitRange.Typed.ofNull();
		Testing.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldFindTypeAtPosition() {
		var typed = SplitRange.typed(String::length, "aa", "b", "ccc");
		Assert.equal(typed.length(), 6);
		Assert.equal(typed.at(-1), null);
		Assert.equal(typed.at(0), "aa");
		Assert.equal(typed.at(1), "aa");
		Assert.equal(typed.at(2), "b");
		Assert.equal(typed.at(3), "ccc");
		Assert.equal(typed.at(4), "ccc");
		Assert.equal(typed.at(5), "ccc");
		Assert.equal(typed.at(6), null);
	}

	@Test
	public void shouldAcceptTypeConsumer() {
		var typed = SplitRange.typed(String::length, "aa", "b", "ccc");
		typed.accept(-1, (_, _) -> Assert.fail());
		typed.accept(0, assertTypeConsumer("aa", 0));
		typed.accept(1, assertTypeConsumer("aa", 1));
		typed.accept(2, assertTypeConsumer("b", 0));
		typed.accept(3, assertTypeConsumer("ccc", 0));
		typed.accept(4, assertTypeConsumer("ccc", 1));
		typed.accept(5, assertTypeConsumer("ccc", 2));
		typed.accept(6, (_, _) -> Assert.fail());
	}

	@Test
	public void shouldApplyTypeFunction() {
		var typed = SplitRange.typed(String::length, "aa", "b", "ccc");
		Assert.equal(typed.apply(-1, (_, _) -> Assert.throwRuntime()), null);
		Assert.equal(typed.apply(0, assertTypeFunction("aa", 0, "test")), "test");
		Assert.equal(typed.apply(1, assertTypeFunction("aa", 1, "test")), "test");
		Assert.equal(typed.apply(2, assertTypeFunction("b", 0, "test")), "test");
		Assert.equal(typed.apply(3, assertTypeFunction("ccc", 0, "test")), "test");
		Assert.equal(typed.apply(4, assertTypeFunction("ccc", 1, "test")), "test");
		Assert.equal(typed.apply(5, assertTypeFunction("ccc", 2, "test")), "test");
		Assert.equal(typed.apply(6, (_, _) -> Assert.throwRuntime()), null);
	}

	@Test
	public void shouldFindFromManyIndexes() {
		var range = SplitRange.from(Array.INT.fill(new int[300], 10));
		Assert.equal(range.index(-1), -1);
		Assert.equal(range.index(0), 0);
		Assert.equal(range.index(1000), 100);
		Assert.equal(range.index(2999), 299);
		Assert.equal(range.index(3000), -1);
	}

	@Test
	public void shouldFindFromIndexes() {
		var range = SplitRange.from(Array.INT.fill(new int[100], 10));
		Assert.equal(range.length, 1000);
		Assert.equal(range.index(-1), -1);
		Assert.equal(range.index(0), 0);
		Assert.equal(range.index(100), 10);
		Assert.equal(range.index(999), 99);
		Assert.equal(range.index(1000), -1);
	}

	@Test
	public void shouldFailToCreateFromDecreasingIndex() {
		Assert.thrown(() -> SplitRange.of(1, 3, 2));
	}

	@Test
	public void shouldProvideSectionStarts() {
		var range = SplitRange.of(1, 3, 6, 10);
		Assert.equal(range.start(-1), 0);
		Assert.equal(range.start(0), 0);
		Assert.equal(range.start(1), 1);
		Assert.equal(range.start(2), 3);
		Assert.equal(range.start(3), 6);
		Assert.equal(range.start(4), 0);
	}

	@Test
	public void shouldProvideSectionLength() {
		var range = SplitRange.of(1, 3, 6, 10);
		Assert.equal(range.length(-1), 0);
		Assert.equal(range.length(0), 1);
		Assert.equal(range.length(1), 2);
		Assert.equal(range.length(2), 3);
		Assert.equal(range.length(3), 4);
		Assert.equal(range.length(4), 0);
	}

	@Test
	public void shouldAcceptConsumer() {
		SplitRange t = SplitRange.of(1, 3, 6);
		t.accept(-1, (_, _, _) -> Assert.fail());
		t.accept(0, assertConsumer(0, 0, 1));
		t.accept(1, assertConsumer(1, 0, 2));
		t.accept(2, assertConsumer(1, 1, 2));
		t.accept(3, assertConsumer(2, 0, 3));
		t.accept(4, assertConsumer(2, 1, 3));
		t.accept(5, assertConsumer(2, 2, 3));
		t.accept(6, (_, _, _) -> Assert.fail());
	}

	@Test
	public void shouldApplyFunction() {
		SplitRange t = SplitRange.of(1, 3, 6);
		Assert.equal(t.apply(-1, (_, _, _) -> Assert.throwRuntime()), null);
		Assert.equal(t.apply(0, assertFunction(0, 0, 1, "test")), "test");
		Assert.equal(t.apply(1, assertFunction(1, 0, 2, "test")), "test");
		Assert.equal(t.apply(2, assertFunction(1, 1, 2, "test")), "test");
		Assert.equal(t.apply(3, assertFunction(2, 0, 3, "test")), "test");
		Assert.equal(t.apply(4, assertFunction(2, 1, 3, "test")), "test");
		Assert.equal(t.apply(5, assertFunction(2, 2, 3, "test")), "test");
		Assert.equal(t.apply(6, (_, _, _) -> Assert.throwRuntime()), null);
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
			Assert.equal(i, index);
			Assert.equal(off, offset);
			Assert.equal(length, length);
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
			Assert.equal(t, type);
			Assert.equal(off, offset);
		};
	}
}
