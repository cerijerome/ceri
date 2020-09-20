package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.biConsumer;
import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.function.FunctionTestUtil.intConsumer;
import static ceri.common.function.FunctionTestUtil.intSupplier;
import static ceri.common.function.FunctionTestUtil.runnable;
import static ceri.common.function.FunctionTestUtil.supplier;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Test;
import ceri.common.test.Capturer;
import ceri.common.test.TestUtil;

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
		assertThat(FunctionUtil.getQuietly(() -> "test"), is("test"));
		assertNull(FunctionUtil.getQuietly(() -> {
			throw new IOException();
		}));
		assertThrown(() -> FunctionUtil.getQuietly(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void testExecQuietly() {
		assertThat(FunctionUtil.execQuietly(() -> {}), is(true));
		assertThat(FunctionUtil.execQuietly(() -> {
			throw new IOException();
		}), is(false));
		assertThrown(() -> FunctionUtil.execQuietly(() -> {
			throw new RuntimeException();
		}));
	}

	@Test
	public void testExecSilently() {
		assertThat(FunctionUtil.execSilently(() -> {}), is(true));
		assertThat(FunctionUtil.execSilently(() -> {
			throw new IOException();
		}), is(false));
	}

	@Test
	public void testCallSilently() {
		assertThat(FunctionUtil.callSilently(() -> "test"), is("test"));
		assertNull(FunctionUtil.callSilently(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testFirst() {
		assertThat(FunctionUtil.first(null, () -> null, () -> "a", () -> "b"), is("a"));
		assertThat(FunctionUtil.first("test", () -> null, () -> "a", () -> "b"), is("test"));
	}

	@Test
	public void testCastApply() {
		Object obj = new int[] { -1 };
		assertThat(FunctionUtil.castApply(int[].class, obj, x -> x[0] = 1), is(1));
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
		assertThat(fn.apply(" "), is(""));
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
		assertThat(FunctionUtil.safeApply("test", String::length), is(4));
		assertThat(FunctionUtil.safeApply(null, String::length, 3), is(3));
		assertThat(FunctionUtil.safeApply("test", String::length, 3), is(4));
		assertThat(FunctionUtil.safeApplyGet(null, String::length, () -> 2), is(2));
		assertThat(FunctionUtil.safeApplyGet("test", String::length, () -> 2), is(4));
	}

	@Test
	public void testRecurse() {
		assertThat(FunctionUtil.recurse("test", s -> s.replaceFirst("[a-z]", "X")), is("XXXX"));
		assertThat(FunctionUtil.recurse("hello", s -> s.substring(1), 3), is("lo"));
		TestUtil.assertThrown(() -> FunctionUtil.recurse("hello", s -> s.substring(1)));
	}

	@Test
	public void testAsFunction() throws IOException {
		assertThat(FunctionUtil.asFunction(consumer()).apply(2), is(Boolean.TRUE));
		assertThat(FunctionUtil.asFunction(supplier(2)).apply(null), is(2));
		assertThat(FunctionUtil.asFunction(runnable(2)).apply(null), is(Boolean.TRUE));
		assertThat(FunctionUtil.asFunction(runnable(2)).apply("x"), is(Boolean.TRUE));
		assertThat(FunctionUtil.asToIntFunction(intSupplier(2)).applyAsInt(null), is(2));
		assertThat(FunctionUtil.asToIntFunction(intSupplier(2)).applyAsInt("x"), is(2));
		assertThat(FunctionUtil.asBiFunction(biConsumer()).apply(2, 3), is(Boolean.TRUE));
	}

	@Test
	public void testAsSupplier() throws IOException {
		assertThat(FunctionUtil.asSupplier(runnable(2), 5).get(), is(5));
	}

	@Test
	public void testForEachIterable() {
		Capturer.Int capturer = Capturer.ofInt();
		assertThrown(IOException.class,
			() -> FunctionUtil.forEach(Arrays.asList(1, 2, 3), consumer()));
		assertThrown(RuntimeException.class,
			() -> FunctionUtil.forEach(Arrays.asList(0, 1, 2), consumer()));
		FunctionUtil.forEach(Arrays.asList(1, 2, 3), capturer.reset()::accept);
		capturer.verify(1, 2, 3);
	}

	@Test
	public void testForEachStream() {
		Capturer.Int capturer = Capturer.ofInt();
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
		Capturer.Bi<Integer, Integer> capturer = Capturer.ofBi();
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
		assertThat(p.test(""), is(false));
		assertThat(p.test("abc"), is(true));
		assertThat(p.toString(), is("test"));
	}

	@Test
	public void testNamedIntPredicate() {
		IntPredicate p = FunctionUtil.namedInt(i -> i > 0, "test");
		assertThat(p.test(0), is(false));
		assertThat(p.test(1), is(true));
		assertThat(p.toString(), is("test"));
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
		assertThat(FunctionUtil.and(p0, null).test(-1), is(false));
		assertThat(FunctionUtil.and(p0, null).test(1), is(true));
		assertThat(FunctionUtil.and(null, p1).test(-1), is(true));
		assertThat(FunctionUtil.and(null, p1).test(1), is(false));
		assertThat(FunctionUtil.and(p0, p1).test(-1), is(false));
		assertThat(FunctionUtil.and(p0, p1).test(0), is(true));
		assertThat(FunctionUtil.and(p0, p1).test(1), is(false));
	}

	@Test
	public void testPredicateOr() {
		Predicate<Integer> n = null;
		Predicate<Integer> p0 = i -> i > -1;
		Predicate<Integer> p1 = i -> i < 1;
		assertNull(FunctionUtil.or(n, n));
		assertThat(FunctionUtil.or(p0, null).test(-1), is(false));
		assertThat(FunctionUtil.or(p0, null).test(1), is(true));
		assertThat(FunctionUtil.or(null, p1).test(-1), is(true));
		assertThat(FunctionUtil.or(null, p1).test(1), is(false));
		assertThat(FunctionUtil.or(p0, p1).test(-1), is(true));
		assertThat(FunctionUtil.or(p0, p1).test(0), is(true));
		assertThat(FunctionUtil.or(p0, p1).test(1), is(true));
	}

	@Test
	public void testTesting() {
		Predicate<Integer> p0 = i -> i > 0;
		Predicate<String> p = FunctionUtil.testing(String::length, p0);
		assertThat(p.test(""), is(false));
		assertThat(p.test("x"), is(true));
	}

	@Test
	public void testTestingInt() {
		IntPredicate p0 = i -> i > 0;
		Predicate<String> p = FunctionUtil.testingInt(String::length, p0);
		assertThat(p.test(""), is(false));
		assertThat(p.test("x"), is(true));
	}

}
