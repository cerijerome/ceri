package ceri.common.function;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertOptional;
import static ceri.common.test.Assert.assertPrivateConstructor;
import static ceri.common.test.Assert.assertion;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.junit.Test;
import ceri.common.function.Excepts.Function;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.Captor;

public class FunctionalTest {
	private static final Functions.Function<String, Integer> fn = String::length;
	private static final Functions.Supplier<Integer> sp = () -> 1;
	private static final Throws.Supplier<Integer> esp = () -> Assert.throwIo();
	private static final Throws.Supplier<Integer> isp = () -> Assert.throwInterrupted();
	private static final Throws.Supplier<Integer> fsp = () -> Assert.fail();

	public static class A implements Functional.Access<Integer> {
		private final CallSync.Supplier<Integer> supplier = CallSync.supplier(0, 1, 2, 3, 4);

		@Override
		public <E extends Exception, R> R apply(Function<E, ? super Integer, R> function) throws E {
			return function.apply(supplier.get());
		}
	}
	
	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Functional.class);
	}

	@Test
	public void testAdaptRunSupplier() throws Exception {
		assertEquals(Functional.Adapt.runSupplier(null, 1).get(), 1);
	}
	
	@Test
	public void testAccess() throws Exception {
		var a = new A();
		assertEquals(a.apply(i -> i - 1), -1);
		a.accept(null);
		a.accept(i -> assertEquals(i, 2));
	}
	
	@Test
	public void testAccessOf() throws Exception {
		var a = Functional.Access.of(1);
		assertEquals(a.apply(i -> i - 1), 0);
		a.accept(null);
		a.accept(i -> assertEquals(i, 1));
	}
	
	@Test
	public void testApply() throws Exception {
		assertEquals(Functional.apply(null, ""), null);
		assertEquals(Functional.apply(fn, null), null);
		assertEquals(Functional.apply(fn, "abc"), 3);
		assertEquals(Functional.apply(null, "", 0), 0);
		assertEquals(Functional.apply(fn, null, 0), 0);
		assertEquals(Functional.apply(fn, "abc", 0), 3);
	}

	@Test
	public void testApplyGet() throws Exception {
		assertEquals(Functional.applyGet(null, "", null), null);
		assertEquals(Functional.applyGet(fn, null, null), null);
		assertEquals(Functional.applyGet(fn, "abc", null), 3);
		assertEquals(Functional.applyGet(null, "", () -> 0), 0);
		assertEquals(Functional.applyGet(fn, null, () -> 0), 0);
		assertEquals(Functional.applyGet(fn, "abc", () -> 0), 3);
	}

	@Test
	public void testApplyAsInt() {
		assertEquals(Functional.applyAsInt(null, "", 0), 0);
		assertEquals(Functional.applyAsInt(fn::apply, null, 0), 0);
		assertEquals(Functional.applyAsInt(fn::apply, "abc", 0), 3);
	}

	@Test
	public void testApplyAsLong() {
		assertEquals(Functional.applyAsLong(null, "", 0), 0L);
		assertEquals(Functional.applyAsLong(fn::apply, null, 0), 0L);
		assertEquals(Functional.applyAsLong(fn::apply, "abc", 0), 3L);
	}

	@Test
	public void testApplyAsDouble() {
		assertEquals(Functional.applyAsDouble(null, "", 0), 0.0);
		assertEquals(Functional.applyAsDouble(fn::apply, null, 0), 0.0);
		assertEquals(Functional.applyAsDouble(fn::apply, "abc", 0), 3.0);
	}

	@Test
	public void testAccept() {
		assertEquals(Functional.accept(null, ""), false);
		assertCaptor(c -> Functional.accept(c, null), false);
		assertCaptor(c -> Functional.accept(c, "abc"), true, "abc");
	}

	@Test
	public void testAcceptInt() {
		assertEquals(Functional.acceptInt(null, 1), false);
		assertCaptor(c -> Functional.acceptInt(c::accept, 1), true, 1);
	}

	@Test
	public void testAcceptLong() {
		assertEquals(Functional.acceptLong(null, 1), false);
		assertCaptor(c -> Functional.acceptLong(c::accept, 1), true, 1L);
	}

	@Test
	public void testAcceptDouble() {
		assertEquals(Functional.acceptDouble(null, 1), false);
		assertCaptor(c -> Functional.acceptDouble(c::accept, 1), true, 1.0);
	}

	@Test
	public void testGet() throws Exception {
		assertEquals(Functional.get(null), null);
		assertEquals(Functional.get(sp), 1);
		assertEquals(Functional.get(null, 0), 0);
		assertEquals(Functional.get(sp, 0), 1);
	}

	@Test
	public void testGetAsInt() throws Exception {
		assertEquals(Functional.getAsInt(null, 0), 0);
		assertEquals(Functional.getAsInt(sp::get, 0), 1);
	}

	@Test
	public void testGetAsLong() throws Exception {
		assertEquals(Functional.getAsLong(null, 0), 0L);
		assertEquals(Functional.getAsLong(sp::get, 0), 1L);
	}

	@Test
	public void testGetAsDouble() throws Exception {
		assertEquals(Functional.getAsDouble(null, 0), 0.0);
		assertEquals(Functional.getAsDouble(sp::get, 0), 1.0);
	}

	@Test
	public void testRun() throws Exception {
		assertEquals(Functional.run(null), false);
		assertCaptor(c -> Functional.run(() -> c.accept(1)), true, 1);
	}

	@Test
	public void testMuteGet() {
		assertEquals(Functional.muteGet(null), null);
		assertEquals(Functional.muteGet(esp), null);
		assertInterrupted(Functional.muteGet(isp), null);
		assertEquals(Functional.muteGet(sp), 1);
		assertion(() -> Functional.muteGet(fsp));
		assertEquals(Functional.muteGet(null, 0), 0);
		assertEquals(Functional.muteGet(esp, 0), 0);
		assertInterrupted(Functional.muteGet(isp, 0), 0);
		assertEquals(Functional.muteGet(sp, 0), 1);
		assertion(() -> Functional.muteGet(fsp, 0));
	}

	@Test
	public void testMuteGetInt() {
		assertEquals(Functional.muteGetInt(null, 0), 0);
		assertEquals(Functional.muteGetInt(esp::get, 0), 0);
		assertInterrupted(Functional.muteGetInt(isp::get, 0), 0);
		assertEquals(Functional.muteGetInt(sp::get, 0), 1);
		assertion(() -> Functional.muteGetInt(fsp::get, 0));
	}

	@Test
	public void testMuteGetLong() {
		assertEquals(Functional.muteGetLong(null, 0), 0L);
		assertEquals(Functional.muteGetLong(esp::get, 0), 0L);
		assertInterrupted(Functional.muteGetLong(isp::get, 0), 0L);
		assertEquals(Functional.muteGetLong(sp::get, 0), 1L);
		assertion(() -> Functional.muteGetLong(fsp::get, 0));
	}

	@Test
	public void testMuteGetDouble() {
		assertEquals(Functional.muteGetDouble(null, 0), 0.0);
		assertEquals(Functional.muteGetDouble(esp::get, 0), 0.0);
		assertInterrupted(Functional.muteGetDouble(isp::get, 0), 0.0);
		assertEquals(Functional.muteGetDouble(sp::get, 0), 1.0);
		assertion(() -> Functional.muteGetDouble(fsp::get, 0));
	}

	@Test
	public void testMuteRun() {
		assertEquals(Functional.muteRun(null), false);
		assertEquals(Functional.muteRun(esp::get), false);
		assertInterrupted(Functional.muteRun(isp::get), false);
		assertCaptor(c -> Functional.muteRun(() -> c.accept(1)), true, 1);
		assertion(() -> Functional.muteRun(fsp::get));
	}

	@Test
	public void testSequentialSupplier() {
		Assert.isNull(Functional.sequentialSupplier().get());
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
	public void testRecurse() {
		assertEquals(Functional.recurse(s -> s.replaceFirst("[a-z]", "X"), "test"), "XXXX");
		assertEquals(Functional.recurse(s -> s.substring(1), "hello", 3), "lo");
		Assert.thrown(() -> Functional.recurse(s -> s.substring(1), "hello"));
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

	@SafeVarargs
	private static <T, R> void assertCaptor(Functions.Function<Captor<T>, R> action, R expected,
		T... values) {
		var captor = Captor.<T>of();
		assertEquals(action.apply(captor), expected);
		captor.verify(values);
	}

	private static <R> void assertInterrupted(R result, R expected) {
		assertEquals(result, expected);
		assertEquals(Thread.interrupted(), true);
	}
}
