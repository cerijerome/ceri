package ceri.common.function;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.junit.Test;
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
		public <E extends Exception, R> R apply(Excepts.Function<E, ? super Integer, R> function)
			throws E {
			return function.apply(supplier.get());
		}
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Functional.class);
	}

	@Test
	public void testAdaptRunSupplier() throws Exception {
		Assert.equal(Functional.Adapt.runSupplier(null, 1).get(), 1);
	}

	@Test
	public void testAccess() throws Exception {
		var a = new A();
		Assert.equal(a.apply(i -> i - 1), -1);
		a.accept(null);
		a.accept(i -> Assert.equal(i, 2));
	}

	@Test
	public void testAccessOf() throws Exception {
		var a = Functional.Access.of(1);
		Assert.equal(a.apply(i -> i - 1), 0);
		a.accept(null);
		a.accept(i -> Assert.equal(i, 1));
	}

	@Test
	public void testApply() throws Exception {
		Assert.equal(Functional.apply(null, ""), null);
		Assert.equal(Functional.apply(fn, null), null);
		Assert.equal(Functional.apply(fn, "abc"), 3);
		Assert.equal(Functional.apply(null, "", 0), 0);
		Assert.equal(Functional.apply(fn, null, 0), 0);
		Assert.equal(Functional.apply(fn, "abc", 0), 3);
	}

	@Test
	public void testApplyGet() throws Exception {
		Assert.equal(Functional.applyGet(null, "", null), null);
		Assert.equal(Functional.applyGet(fn, null, null), null);
		Assert.equal(Functional.applyGet(fn, "abc", null), 3);
		Assert.equal(Functional.applyGet(null, "", () -> 0), 0);
		Assert.equal(Functional.applyGet(fn, null, () -> 0), 0);
		Assert.equal(Functional.applyGet(fn, "abc", () -> 0), 3);
	}

	@Test
	public void testApplyAsInt() {
		Assert.equal(Functional.applyAsInt(null, "", 0), 0);
		Assert.equal(Functional.applyAsInt(fn::apply, null, 0), 0);
		Assert.equal(Functional.applyAsInt(fn::apply, "abc", 0), 3);
	}

	@Test
	public void testApplyAsLong() {
		Assert.equal(Functional.applyAsLong(null, "", 0), 0L);
		Assert.equal(Functional.applyAsLong(fn::apply, null, 0), 0L);
		Assert.equal(Functional.applyAsLong(fn::apply, "abc", 0), 3L);
	}

	@Test
	public void testApplyAsDouble() {
		Assert.equal(Functional.applyAsDouble(null, "", 0), 0.0);
		Assert.equal(Functional.applyAsDouble(fn::apply, null, 0), 0.0);
		Assert.equal(Functional.applyAsDouble(fn::apply, "abc", 0), 3.0);
	}

	@Test
	public void testAccept() {
		Assert.equal(Functional.accept(null, ""), false);
		assertCaptor(c -> Functional.accept(c, null), false);
		assertCaptor(c -> Functional.accept(c, "abc"), true, "abc");
	}

	@Test
	public void testAcceptInt() {
		Assert.equal(Functional.acceptInt(null, 1), false);
		assertCaptor(c -> Functional.acceptInt(i -> c.accept(i), 1), true, 1);
	}

	@Test
	public void testAcceptLong() {
		Assert.equal(Functional.acceptLong(null, 1), false);
		assertCaptor(c -> Functional.acceptLong(l -> c.accept(l), 1), true, 1L);
	}

	@Test
	public void testAcceptDouble() {
		Assert.equal(Functional.acceptDouble(null, 1), false);
		assertCaptor(c -> Functional.acceptDouble(d -> c.accept(d), 1), true, 1.0);
	}

	@Test
	public void testGet() throws Exception {
		Assert.equal(Functional.get(null), null);
		Assert.equal(Functional.get(sp), 1);
		Assert.equal(Functional.get(null, 0), 0);
		Assert.equal(Functional.get(sp, 0), 1);
	}

	@Test
	public void testGetAsInt() throws Exception {
		Assert.equal(Functional.getAsInt(null, 0), 0);
		Assert.equal(Functional.getAsInt(sp::get, 0), 1);
	}

	@Test
	public void testGetAsLong() throws Exception {
		Assert.equal(Functional.getAsLong(null, 0), 0L);
		Assert.equal(Functional.getAsLong(sp::get, 0), 1L);
	}

	@Test
	public void testGetAsDouble() throws Exception {
		Assert.equal(Functional.getAsDouble(null, 0), 0.0);
		Assert.equal(Functional.getAsDouble(sp::get, 0), 1.0);
	}

	@Test
	public void testRun() throws Exception {
		Assert.equal(Functional.run(null), false);
		assertCaptor(c -> Functional.run(() -> c.accept(1)), true, 1);
	}

	@Test
	public void testMuteGet() {
		Assert.equal(Functional.muteGet(null), null);
		Assert.equal(Functional.muteGet(esp), null);
		assertInterrupted(Functional.muteGet(isp), null);
		Assert.equal(Functional.muteGet(sp), 1);
		Assert.assertion(() -> Functional.muteGet(fsp));
		Assert.equal(Functional.muteGet(null, 0), 0);
		Assert.equal(Functional.muteGet(esp, 0), 0);
		assertInterrupted(Functional.muteGet(isp, 0), 0);
		Assert.equal(Functional.muteGet(sp, 0), 1);
		Assert.assertion(() -> Functional.muteGet(fsp, 0));
	}

	@Test
	public void testMuteGetInt() {
		Assert.equal(Functional.muteGetInt(null, 0), 0);
		Assert.equal(Functional.muteGetInt(esp::get, 0), 0);
		assertInterrupted(Functional.muteGetInt(isp::get, 0), 0);
		Assert.equal(Functional.muteGetInt(sp::get, 0), 1);
		Assert.assertion(() -> Functional.muteGetInt(fsp::get, 0));
	}

	@Test
	public void testMuteGetLong() {
		Assert.equal(Functional.muteGetLong(null, 0), 0L);
		Assert.equal(Functional.muteGetLong(esp::get, 0), 0L);
		assertInterrupted(Functional.muteGetLong(isp::get, 0), 0L);
		Assert.equal(Functional.muteGetLong(sp::get, 0), 1L);
		Assert.assertion(() -> Functional.muteGetLong(fsp::get, 0));
	}

	@Test
	public void testMuteGetDouble() {
		Assert.equal(Functional.muteGetDouble(null, 0), 0.0);
		Assert.equal(Functional.muteGetDouble(esp::get, 0), 0.0);
		assertInterrupted(Functional.muteGetDouble(isp::get, 0), 0.0);
		Assert.equal(Functional.muteGetDouble(sp::get, 0), 1.0);
		Assert.assertion(() -> Functional.muteGetDouble(fsp::get, 0));
	}

	@Test
	public void testMuteRun() {
		Assert.equal(Functional.muteRun(null), false);
		Assert.equal(Functional.muteRun(esp::get), false);
		assertInterrupted(Functional.muteRun(isp::get), false);
		assertCaptor(c -> Functional.muteRun(() -> c.accept(1)), true, 1);
		Assert.assertion(() -> Functional.muteRun(fsp::get));
	}

	@Test
	public void testSequentialSupplier() {
		Assert.isNull(Functional.sequentialSupplier().get());
		var supplier0 = Functional.sequentialSupplier(1);
		Assert.equal(supplier0.get(), 1);
		Assert.equal(supplier0.get(), 1);
		var supplier1 = Functional.sequentialSupplier(1, 2, 3);
		Assert.equal(supplier1.get(), 1);
		Assert.equal(supplier1.get(), 2);
		Assert.equal(supplier1.get(), 3);
		Assert.equal(supplier1.get(), 3);
	}

	@Test
	public void testRecurse() {
		Assert.equal(Functional.recurse(s -> s.replaceFirst("[a-z]", "X"), "test"), "XXXX");
		Assert.equal(Functional.recurse(s -> s.substring(1), "hello", 3), "lo");
		Assert.thrown(() -> Functional.recurse(s -> s.substring(1), "hello"));
	}

	@Test
	public void testOptional() {
		Assert.optional(Functional.optional(false, "test"), null);
		Assert.optional(Functional.optional(true, "test"), "test");
		Assert.equal(Functional.optional((Integer) null), OptionalInt.empty());
		Assert.equal(Functional.optional((Long) null), OptionalLong.empty());
		Assert.equal(Functional.optional((Double) null), OptionalDouble.empty());
		Assert.equal(Functional.optional(123), OptionalInt.of(123));
		Assert.equal(Functional.optional(123L), OptionalLong.of(123L));
		Assert.equal(Functional.optional(123.0), OptionalDouble.of(123.0));
	}

	@Test
	public void testOptionalValue() {
		Assert.equal(Functional.value(OptionalInt.empty()), null);
		Assert.equal(Functional.value(OptionalLong.empty()), null);
		Assert.equal(Functional.value(OptionalDouble.empty()), null);
		Assert.equal(Functional.value(OptionalInt.of(123)), 123);
		Assert.equal(Functional.value(OptionalLong.of(123)), 123L);
		Assert.equal(Functional.value(OptionalDouble.of(123)), 123.0);
	}

	@SafeVarargs
	private static <T, R> void assertCaptor(Functions.Function<Captor<T>, R> action, R expected,
		T... values) {
		var captor = Captor.<T>of();
		Assert.equal(action.apply(captor), expected);
		captor.verify(values);
	}

	private static <R> void assertInterrupted(R result, R expected) {
		Assert.equal(result, expected);
		Assert.equal(Thread.interrupted(), true);
	}
}
