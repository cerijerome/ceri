package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.biConsumer;
import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.function.FunctionTestUtil.intConsumer;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Test;
import ceri.common.test.Captor;

public class FunctionUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(FunctionUtil.class);
	}

	@Test
	public void testNullRunnable() {
		FunctionUtil.NULL_RUNNABLE.run();
	}

	@Test
	public void testNullConsumer() {
		FunctionUtil.nullConsumer().accept(null);
		FunctionUtil.nullConsumer().accept("test");
	}

	@Test
	public void testTruePredicate() {
		assertTrue(FunctionUtil.truePredicate().test(null));
		assertTrue(FunctionUtil.truePredicate().test("test"));
	}

	@Test
	public void testGetSilently() {
		assertEquals(FunctionUtil.getSilently(() -> "test"), "test");
		assertEquals(FunctionUtil.getSilently(() -> "test", "x"), "test");
		assertEquals(FunctionUtil.getSilently(() -> {
			throw new IOException();
		}, "test"), "test");
		assertNull(FunctionUtil.getSilently(() -> {
			throw new RuntimeException();
		}));
		assertFalse(Thread.interrupted());
		assertNull(FunctionUtil.getSilently(() -> {
			throw new InterruptedException();
		}));
		assertTrue(Thread.interrupted());
	}

	@Test
	public void testRunSilently() {
		assertTrue(FunctionUtil.runSilently(() -> {}));
		assertFalse(FunctionUtil.runSilently(() -> {
			throw new IOException();
		}));
		assertFalse(FunctionUtil.runSilently(() -> {
			throw new RuntimeException();
		}));
		assertFalse(Thread.interrupted());
		assertFalse(FunctionUtil.runSilently(() -> {
			throw new InterruptedException();
		}));
		assertTrue(Thread.interrupted());
	}

	@Test
	public void testSequentialSupplier() {
		assertNull(FunctionUtil.sequentialSupplier().get());
		var supplier0 = FunctionUtil.sequentialSupplier(1);
		assertEquals(supplier0.get(), 1);
		assertEquals(supplier0.get(), 1);
		var supplier1 = FunctionUtil.sequentialSupplier(1, 2, 3);
		assertEquals(supplier1.get(), 1);
		assertEquals(supplier1.get(), 2);
		assertEquals(supplier1.get(), 3);
		assertEquals(supplier1.get(), 3);
	}

	@Test
	public void testOptional() {
		assertEquals(FunctionUtil.optional((Integer) null), OptionalInt.empty());
		assertEquals(FunctionUtil.optional((Long) null), OptionalLong.empty());
		assertEquals(FunctionUtil.optional((Double) null), OptionalDouble.empty());
		assertEquals(FunctionUtil.optional(123), OptionalInt.of(123));
		assertEquals(FunctionUtil.optional(123L), OptionalLong.of(123L));
		assertEquals(FunctionUtil.optional(123.0), OptionalDouble.of(123.0));
	}

	@Test
	public void testOptionalValue() {
		assertEquals(FunctionUtil.value(OptionalInt.empty()), null);
		assertEquals(FunctionUtil.value(OptionalLong.empty()), null);
		assertEquals(FunctionUtil.value(OptionalDouble.empty()), null);
		assertEquals(FunctionUtil.value(OptionalInt.of(123)), 123);
		assertEquals(FunctionUtil.value(OptionalLong.of(123)), 123L);
		assertEquals(FunctionUtil.value(OptionalDouble.of(123)), 123.0);
	}

	@Test
	public void testSafeAccept() throws IOException {
		String[] store = { "" };
		ExceptionConsumer<IOException, String> consumer = s -> store[0] = s;
		FunctionUtil.safeAccept("test", consumer);
		assertArray(store, "test");
		FunctionUtil.safeAccept(null, consumer);
		assertArray(store, "test");
		FunctionUtil.safeAccept("abc", s -> s.length() <= 3, consumer);
		assertArray(store, "abc");
		FunctionUtil.safeAccept(null, s -> s.length() <= 3, consumer);
		assertArray(store, "abc");
		FunctionUtil.safeAccept("abcd", s -> s.length() <= 3, consumer);
		assertArray(store, "abc");
	}

	@Test
	public void testSafeApply() {
		assertNull(FunctionUtil.safeApply(null, String::length));
		assertEquals(FunctionUtil.safeApply("test", String::length), 4);
		assertEquals(FunctionUtil.safeApply(null, String::length, 3), 3);
		assertEquals(FunctionUtil.safeApply("test", String::length, 3), 4);
		assertEquals(FunctionUtil.safeApplyGet(null, String::length, () -> 2), 2);
		assertEquals(FunctionUtil.safeApplyGet("test", String::length, () -> 2), 4);
	}

	@Test
	public void testSafeApplyAsInt() {
		assertEquals(FunctionUtil.safeApplyAsInt(null, String::length, 3), 3);
		assertEquals(FunctionUtil.safeApplyAsInt("test", String::length, 3), 4);
		assertEquals(FunctionUtil.safeApplyGetAsInt(null, String::length, () -> 2), 2);
		assertEquals(FunctionUtil.safeApplyGetAsInt("test", String::length, () -> 2), 4);
	}

	@Test
	public void testRecurse() {
		assertEquals(FunctionUtil.recurse("test", s -> s.replaceFirst("[a-z]", "X")), "XXXX");
		assertEquals(FunctionUtil.recurse("hello", s -> s.substring(1), 3), "lo");
		assertThrown(() -> FunctionUtil.recurse("hello", s -> s.substring(1)));
	}

	@Test
	public void testForEachIterable() {
		Captor.OfInt capturer = Captor.ofInt();
		assertThrown(IOException.class,
			() -> FunctionUtil.forEach(Arrays.asList(1, 2, 3), consumer()));
		assertThrown(RuntimeException.class,
			() -> FunctionUtil.forEach(Arrays.asList(0, 1, 2), consumer()));
		FunctionUtil.forEach(Arrays.asList(1, 2, 3), capturer.reset()::accept);
		capturer.verify(1, 2, 3);
	}

	@Test
	public void testForEachStream() {
		Captor.OfInt capturer = Captor.ofInt();
		FunctionUtil.forEach(Stream.of(1, 2, 3), capturer.reset()::accept);
		capturer.verify(1, 2, 3);
		FunctionUtil.forEach(IntStream.of(1, 2, 3), capturer.reset()::accept);
		capturer.verify(1, 2, 3);
		assertThrown(IOException.class, () -> FunctionUtil.forEach(Stream.of(1, 2, 3), consumer()));
		assertThrown(IOException.class,
			() -> FunctionUtil.forEach(IntStream.of(1, 2, 3), intConsumer()));
		assertThrown(RuntimeException.class,
			() -> FunctionUtil.forEach(Stream.of(2, 0, 3), consumer()));
		assertThrown(RuntimeException.class,
			() -> FunctionUtil.forEach(IntStream.of(2, 0, 3), intConsumer()));
	}

	@Test
	public void testForEachMap() {
		Captor.Bi<Integer, Integer> capturer = Captor.ofBi();
		FunctionUtil.forEach(Map.of(1, 2, 3, 4), capturer.reset()::accept);
		assertCollection(capturer.first.values, 1, 3);
		assertCollection(capturer.second.values, 2, 4);
		assertThrown(IOException.class,
			() -> FunctionUtil.forEach(Map.of(1, 2, 3, 4), biConsumer()));
		assertThrown(RuntimeException.class,
			() -> FunctionUtil.forEach(Map.of(3, 2, 0, 4), biConsumer()));
	}

	@Test
	public void testPredicateAnd() {
		Predicate<Integer> n = null;
		Predicate<Integer> p0 = i -> i > -1;
		Predicate<Integer> p1 = i -> i < 1;
		assertNull(FunctionUtil.and(n, n));
		assertFalse(FunctionUtil.and(p0, null).test(-1));
		assertTrue(FunctionUtil.and(p0, null).test(1));
		assertTrue(FunctionUtil.and(null, p1).test(-1));
		assertFalse(FunctionUtil.and(null, p1).test(1));
		assertFalse(FunctionUtil.and(p0, p1).test(-1));
		assertTrue(FunctionUtil.and(p0, p1).test(0));
		assertFalse(FunctionUtil.and(p0, p1).test(1));
	}

	@Test
	public void testPredicateOr() {
		Predicate<Integer> n = null;
		Predicate<Integer> p0 = i -> i > -1;
		Predicate<Integer> p1 = i -> i < 1;
		assertNull(FunctionUtil.or(n, n));
		assertFalse(FunctionUtil.or(p0, null).test(-1));
		assertTrue(FunctionUtil.or(p0, null).test(1));
		assertTrue(FunctionUtil.or(null, p1).test(-1));
		assertFalse(FunctionUtil.or(null, p1).test(1));
		assertTrue(FunctionUtil.or(p0, p1).test(-1));
		assertTrue(FunctionUtil.or(p0, p1).test(0));
		assertTrue(FunctionUtil.or(p0, p1).test(1));
	}

	@Test
	public void testTesting() {
		Predicate<Integer> p0 = i -> i > 0;
		Predicate<String> p = FunctionUtil.testing(String::length, p0);
		assertFalse(p.test(""));
		assertTrue(p.test("x"));
	}

	@Test
	public void testTestingInt() {
		IntPredicate p0 = i -> i > 0;
		Predicate<String> p = FunctionUtil.testingInt(String::length, p0);
		assertFalse(p.test(""));
		assertTrue(p.test("x"));
	}
}
