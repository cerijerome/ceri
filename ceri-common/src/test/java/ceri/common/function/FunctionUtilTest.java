package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.biConsumer;
import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.function.FunctionTestUtil.intConsumer;
import static ceri.common.function.FunctionTestUtil.intSupplier;
import static ceri.common.function.FunctionTestUtil.runnable;
import static ceri.common.function.FunctionTestUtil.supplier;
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
	public void testGetQuietly() {
		assertEquals(FunctionUtil.getQuietly(() -> "test"), "test");
		assertNull(FunctionUtil.getQuietly(() -> {
			throw new IOException();
		}));
		assertThrown(() -> FunctionUtil.getQuietly(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void testExecQuietly() {
		assertTrue(FunctionUtil.execQuietly(() -> {}));
		assertFalse(FunctionUtil.execQuietly(() -> {
			throw new IOException();
		}));
		assertThrown(() -> FunctionUtil.execQuietly(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void testExecSilently() {
		assertTrue(FunctionUtil.execSilently(() -> {}));
		assertFalse(FunctionUtil.execSilently(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testCallSilently() {
		assertEquals(FunctionUtil.callSilently(() -> "test"), "test");
		assertNull(FunctionUtil.callSilently(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testFirst() {
		assertEquals(FunctionUtil.first(null, () -> null, () -> "a", () -> "b"), "a");
		assertEquals(FunctionUtil.first("test", () -> null, () -> "a", () -> "b"), "test");
	}

	@Test
	public void testCastApply() throws Exception {
		Object obj = new int[] { -1 };
		assertEquals(FunctionUtil.castApply(int[].class, obj, x -> x[0] = 1), 1);
		assertArray((int[]) obj, 1);
		assertNull(FunctionUtil.castApply((Class<int[]>) null, obj, x -> x[0] = 2));
		assertArray((int[]) obj, 1);
		assertNull(FunctionUtil.castApply(int[].class, null, x -> x[0] = 2));
		assertArray((int[]) obj, 1);
		assertNull(FunctionUtil.castApply(int[].class, obj, null));
		assertArray((int[]) obj, 1);
		assertNull(FunctionUtil.castApply(long[].class, obj, x -> x[0] = 2));
		assertArray((int[]) obj, 1);
	}

	@Test
	public void testCastAccept() {
		Object obj = new int[] { -1 };
		FunctionUtil.castAccept(int[].class, obj, x -> x[0] = 1);
		assertArray((int[]) obj, 1);
		// noinspection RedundantCast
		FunctionUtil.castAccept((Class<int[]>) null, obj, x -> x[0] = 2);
		assertArray((int[]) obj, 1);
		FunctionUtil.castAccept(int[].class, null, x -> x[0] = 2);
		assertArray((int[]) obj, 1);
		FunctionUtil.castAccept(int[].class, obj, null);
		assertArray((int[]) obj, 1);
		FunctionUtil.castAccept(long[].class, obj, x -> x[0] = 2);
		assertArray((int[]) obj, 1);
	}

	@Test
	public void testSafe() throws IOException {
		ExceptionFunction<IOException, String, String> fn = FunctionUtil.safe(String::trim);
		assertEquals(fn.apply(" "), "");
		assertNull(fn.apply(null));
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
	public void testRecurse() {
		assertEquals(FunctionUtil.recurse("test", s -> s.replaceFirst("[a-z]", "X")), "XXXX");
		assertEquals(FunctionUtil.recurse("hello", s -> s.substring(1), 3), "lo");
		assertThrown(() -> FunctionUtil.recurse("hello", s -> s.substring(1)));
	}

	@Test
	public void testAsFunction() throws IOException {
		assertEquals(FunctionUtil.asFunction(consumer()).apply(2), Boolean.TRUE);
		assertEquals(FunctionUtil.asFunction(supplier(2)).apply(null), 2);
		assertEquals(FunctionUtil.asFunction(runnable(2)).apply(null), Boolean.TRUE);
		assertEquals(FunctionUtil.asFunction(runnable(2)).apply("x"), Boolean.TRUE);
		assertEquals(FunctionUtil.asToIntFunction(intSupplier(2)).applyAsInt(null), 2);
		assertEquals(FunctionUtil.asToIntFunction(intSupplier(2)).applyAsInt("x"), 2);
		assertEquals(FunctionUtil.asBiFunction(biConsumer()).apply(2, 3), Boolean.TRUE);
	}

	@Test
	public void testAsSupplier() throws IOException {
		assertEquals(FunctionUtil.asSupplier(runnable(2), 5).get(), 5);
	}

	@Test
	public void testForEachIterable() {
		Captor.Int capturer = Captor.ofInt();
		assertThrown(IOException.class,
			() -> FunctionUtil.forEach(Arrays.asList(1, 2, 3), consumer()));
		assertThrown(RuntimeException.class,
			() -> FunctionUtil.forEach(Arrays.asList(0, 1, 2), consumer()));
		FunctionUtil.forEach(Arrays.asList(1, 2, 3), capturer.reset()::accept);
		capturer.verify(1, 2, 3);
	}

	@Test
	public void testForEachStream() {
		Captor.Int capturer = Captor.ofInt();
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
	public void testNamedPredicate() {
		Predicate<String> p = FunctionUtil.named(s -> !s.isEmpty(), "test");
		assertFalse(p.test(""));
		assertTrue(p.test("abc"));
		assertEquals(p.toString(), "test");
	}

	@Test
	public void testNamedIntPredicate() {
		IntPredicate p = FunctionUtil.namedInt(i -> i > 0, "test");
		assertFalse(p.test(0));
		assertTrue(p.test(1));
		assertEquals(p.toString(), "test");
	}

	@Test
	public void testAnonymousLambda() {
		assertFalse(FunctionUtil.isAnonymousLambda(null));
		assertFalse(FunctionUtil.isAnonymousLambda(new Object() {
			@Override
			public String toString() {
				return null;
			}
		}));
		assertFalse(FunctionUtil.isAnonymousLambda(new Object()));
		IntPredicate p = i -> true;
		assertFalse(FunctionUtil.isAnonymousLambda(FunctionUtil.namedInt(p, "test")));
		assertTrue(FunctionUtil.isAnonymousLambda(p));
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
