package ceri.common.function;

import static ceri.common.function.FunctionTestUtil.assertIoEx;
import static ceri.common.function.FunctionTestUtil.assertRtEx;
import static ceri.common.function.FunctionTestUtil.consumer;
import static ceri.common.function.FunctionTestUtil.runnable;
import static ceri.common.function.FunctionTestUtil.supplier;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.Test;

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
	public void testExecSilently() {
		assertThat(FunctionUtil.execSilently(() -> {}), is(true));
		assertThat(FunctionUtil.execSilently(() -> {
			throw new IOException();
		}), is(false));
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
	public void testConsumerAsRunnable() throws Exception {
		String[] store = { "" };
		ExceptionConsumer<?, String> consumer = s -> store[0] = s;
		FunctionUtil.asRunnable("test", consumer).run();
		assertArray(store, "test");
	}

	@Test
	public void testFunctionAsRunnable() throws Exception {
		String[] store = { "" };
		ExceptionFunction<?, String, Integer> function = s -> {
			store[0] = "" + s.length();
			return s.length();
		};
		FunctionUtil.asRunnable("abc", function).run();
		assertArray(store, "3");
	}

	@Test
	public void testFunctionAsConsumer() throws Exception {
		String[] store = { "" };
		ExceptionFunction<?, String, Integer> function = s -> {
			store[0] = "" + s.length();
			return s.length();
		};
		FunctionUtil.asConsumer(function).accept("abc");
		assertArray(store, "3");
	}

	@Test
	public void testSafe() throws IOException {
		ExceptionFunction<IOException, String, String> fn = FunctionUtil.safe(s -> s.trim());
		assertThat(fn.apply(" "), is(""));
		assertNull(fn.apply(null));
	}

	@Test
	public void testSafeAccept() throws IOException {
		String[] store = { "" };
		ExceptionConsumer<IOException, String> consumer = s -> store[0] = s;
		FunctionUtil.safeAccept("test", consumer);
		assertArray(store, "test");
		FunctionUtil.safeAccept((String) null, consumer);
		assertArray(store, "test");
		FunctionUtil.safeAccept("abc", s -> s.length() <= 3, consumer);
		assertArray(store, "abc");
		FunctionUtil.safeAccept((String) null, s -> s.length() <= 3, consumer);
		assertArray(store, "abc");
		FunctionUtil.safeAccept("abcd", s -> s.length() <= 3, consumer);
		assertArray(store, "abc");
	}

	@Test
	public void testRecurse() {
		assertThat(FunctionUtil.recurse("test", s -> s.replaceFirst("[a-z]", "X")), is("XXXX"));
		assertThat(FunctionUtil.recurse("hello", s -> s.substring(1), 3), is("lo"));
		assertException(() -> FunctionUtil.recurse("hello", s -> s.substring(1)));
	}

	@Test
	public void testAsFunction() throws IOException {
		assertThat(FunctionUtil.asFunction(consumer(2)).apply(1), is(Boolean.TRUE));
		assertThat(FunctionUtil.asFunction(supplier(1, 2)).apply(1), is(1));
		assertThat(FunctionUtil.asFunction(runnable(1, 2)).apply(1), is(Boolean.TRUE));
	}

	@Test
	public void testForEach() throws IOException {
		assertIoEx("2", () -> FunctionUtil.forEach(Stream.of(1, 2, 3), consumer(2)));
		assertRtEx("0", () -> FunctionUtil.forEach(Stream.of(1, 0, 3), consumer(2)));
		FunctionUtil.forEach(Stream.of(1, 2, 3), consumer(4));
		assertIoEx("2", () -> FunctionUtil.forEach(Arrays.asList(1, 2, 3), consumer(2)));
		assertRtEx("0", () -> FunctionUtil.forEach(Arrays.asList(1, 0, 3), consumer(2)));
		FunctionUtil.forEach(Arrays.asList(1, 2, 3), consumer(4));
	}

	@Test
	public void testForEachMapEntry() throws IOException {
		Map<Integer, String> map = Map.of(1, "1", 2, "2", 3, "3");
		FunctionUtil.forEach(map, (k, v) -> {
			if (k < 1) throw new IOException();
		});
	}

	@Test
	public void testNamedPredicate() {
		Predicate<String> p = FunctionUtil.namedPredicate(s -> !s.isEmpty(), "test");
		assertThat(p.test(""), is(false));
		assertThat(p.test("abc"), is(true));
		assertThat(p.toString(), is("test"));
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

}
