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

public class FunctionalTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Functional.class);
	}

	@Test
	public void testNullRunnable() {
		Functional.NULL_RUNNABLE.run();
	}

	@Test
	public void testNullConsumer() {
		Functional.nullConsumer().accept(null);
		Functional.nullConsumer().accept("test");
	}

	@Test
	public void testTruePredicate() {
		assertTrue(Functional.truePredicate().test(null));
		assertTrue(Functional.truePredicate().test("test"));
	}

	@Test
	public void testGetSilently() {
		assertEquals(Functional.getSilently(() -> "test"), "test");
		assertEquals(Functional.getSilently(() -> "test", "x"), "test");
		assertAssertion(() -> Functional.getSilently(() -> fail(), "x"));
		assertEquals(Functional.getSilently(() -> {
			throw new IOException();
		}, "test"), "test");
		assertNull(Functional.getSilently(() -> {
			throw new RuntimeException();
		}));
		assertFalse(Thread.interrupted());
		assertNull(Functional.getSilently(() -> {
			throw new InterruptedException();
		}));
		assertTrue(Thread.interrupted());
	}

	@Test
	public void testRunSilently() {
		assertTrue(Functional.runSilently(() -> {}));
		assertFalse(Functional.runSilently(() -> {
			throw new IOException();
		}));
		assertFalse(Functional.runSilently(() -> {
			throw new RuntimeException();
		}));
		assertFalse(Thread.interrupted());
		assertFalse(Functional.runSilently(() -> {
			throw new InterruptedException();
		}));
		assertTrue(Thread.interrupted());
	}

	@Test
	public void testSequentialSupplier() {
		assertNull(Functional.sequentialSupplier().get());
		var supplier0 = Functional.sequentialSupplier(1);
		assertEquals(supplier0.get(), 1);
		assertEquals(supplier0.get(), 1);
		var supplier1 = Functional.sequentialSupplier(1, 2, 3);
		assertEquals(supplier1.get(), 1);
		assertEquals(supplier1.get(), 2);
		assertEquals(supplier1.get(), 3);
		assertEquals(supplier1.get(), 3);
	}

	@Test
	public void testForEach() {
		var iterator = Arrays.asList("1", "2", null).iterator();
		var captor = Captor.of();
		Functional.forEach(iterator::next, captor::accept);
		captor.verify("1", "2");
	}

	@Test
	public void testOptional() {
		assertOptional(Functional.optional(false, "test"), null);
		assertOptional(Functional.optional(true, "test"), "test");
		assertEquals(Functional.optional((Integer) null), OptionalInt.empty());
		assertEquals(Functional.optional((Long) null), OptionalLong.empty());
		assertEquals(Functional.optional((Double) null), OptionalDouble.empty());
		assertEquals(Functional.optional(123), OptionalInt.of(123));
		assertEquals(Functional.optional(123L), OptionalLong.of(123L));
		assertEquals(Functional.optional(123.0), OptionalDouble.of(123.0));
	}

	@Test
	public void testOptionalValue() {
		assertEquals(Functional.value(OptionalInt.empty()), null);
		assertEquals(Functional.value(OptionalLong.empty()), null);
		assertEquals(Functional.value(OptionalDouble.empty()), null);
		assertEquals(Functional.value(OptionalInt.of(123)), 123);
		assertEquals(Functional.value(OptionalLong.of(123)), 123L);
		assertEquals(Functional.value(OptionalDouble.of(123)), 123.0);
	}

	@Test
	public void testSafeAccept() throws IOException {
		String[] store = { "" };
		Excepts.Consumer<IOException, String> consumer = s -> store[0] = s;
		Functional.safeAccept("test", consumer);
		assertArray(store, "test");
		Functional.safeAccept(null, consumer);
		assertArray(store, "test");
		Functional.safeAccept("abc", s -> s.length() <= 3, consumer);
		assertArray(store, "abc");
		Functional.safeAccept(null, s -> s.length() <= 3, consumer);
		assertArray(store, "abc");
		Functional.safeAccept("abcd", s -> s.length() <= 3, consumer);
		assertArray(store, "abc");
	}

	@Test
	public void testSafeApply() {
		assertNull(Functional.safeApply(null, String::length));
		assertEquals(Functional.safeApply("test", String::length), 4);
		assertEquals(Functional.safeApply(null, String::length, 3), 3);
		assertEquals(Functional.safeApply("test", String::length, 3), 4);
		assertEquals(Functional.safeApplyGet(null, String::length, () -> 2), 2);
		assertEquals(Functional.safeApplyGet("test", String::length, () -> 2), 4);
	}

	@Test
	public void testSafeApplyAsInt() {
		assertEquals(Functional.safeApplyAsInt(null, String::length, 3), 3);
		assertEquals(Functional.safeApplyAsInt("test", String::length, 3), 4);
		assertEquals(Functional.safeApplyGetAsInt(null, String::length, () -> 2), 2);
		assertEquals(Functional.safeApplyGetAsInt("test", String::length, () -> 2), 4);
	}

	@Test
	public void testRecurse() {
		assertEquals(Functional.recurse("test", s -> s.replaceFirst("[a-z]", "X")), "XXXX");
		assertEquals(Functional.recurse("hello", s -> s.substring(1), 3), "lo");
		assertThrown(() -> Functional.recurse("hello", s -> s.substring(1)));
	}

	@Test
	public void testAny() {
		assertTrue(Functional.any(String::isBlank, "a", "b", " ", "c"));
		assertFalse(Functional.any(String::isBlank, "a", "b", "c"));
		assertFalse(Functional.any(String::isBlank));
	}

	@Test
	public void testAll() {
		assertTrue(Functional.all(String::isBlank, "", " ", "\t"));
		assertFalse(Functional.all(String::isBlank, "a", " ", "\t"));
		assertTrue(Functional.all(String::isBlank));
	}
}
