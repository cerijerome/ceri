package ceri.common.function;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertOptional;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.fail;
import java.io.IOException;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
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
		assertAssertion(() -> FunctionUtil.getSilently(() -> fail(), "x"));
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
	public void testForEach() {
		var iterator = Arrays.asList("1", "2", null).iterator();
		var captor = Captor.of();
		FunctionUtil.forEach(iterator::next, captor::accept);
		captor.verify("1", "2");
	}

	@Test
	public void testOptional() {
		assertOptional(FunctionUtil.optional(false, "test"), null);
		assertOptional(FunctionUtil.optional(true, "test"), "test");
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
		Excepts.Consumer<IOException, String> consumer = s -> store[0] = s;
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
	public void testAny() {
		assertTrue(FunctionUtil.any(String::isBlank, "a", "b", " ", "c"));
		assertFalse(FunctionUtil.any(String::isBlank, "a", "b", "c"));
		assertFalse(FunctionUtil.any(String::isBlank));
	}

	@Test
	public void testAll() {
		assertTrue(FunctionUtil.all(String::isBlank, "", " ", "\t"));
		assertFalse(FunctionUtil.all(String::isBlank, "a", " ", "\t"));
		assertTrue(FunctionUtil.all(String::isBlank));
	}
}
