package ceri.common.util;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class HolderBehavior {

	@Test
	public void testEquals() {
		Assert.yes(Holder.equals(null, null));
		Assert.no(Holder.equals(null, Holder.of(null)));
		Assert.no(Holder.equals(Holder.of(null), null));
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		Holder<?> t = Holder.of("test");
		Holder<?> eq0 = Holder.of("test");
		Holder<?> eq1 = Holder.mutable("test");
		Holder<?> ne0 = Holder.of("Test");
		Holder<?> ne1 = Holder.mutable("Test");
		Holder<?> ne2 = Holder.of();
		Holder<?> ne3 = Holder.mutable();
		Testing.exerciseEquals(t, eq0, eq1);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.equal(Holder.of("test").toString(), "[test]");
		Assert.equal(Holder.of(null).toString(), "[null]");
		Assert.equal(Holder.mutable("test").toString(), "[test]");
		Assert.equal(Holder.mutable(null).toString(), "[null]");
		Assert.equal(Holder.of().toString(), "empty");
	}

	@Test
	public void shouldModifyMutableHolder() {
		var holder = Holder.mutable();
		Assert.no(holder.nullValue());
		Assert.yes(holder.isEmpty());
		holder.set(null);
		Assert.yes(holder.nullValue());
		Assert.no(holder.isEmpty());
		holder.clear();
		Assert.no(holder.nullValue());
		Assert.yes(holder.isEmpty());
	}

	@Test
	public void shouldProvideVolatileHolder() {
		var holder = Holder.ofVolatile(null);
		Assert.no(holder.isEmpty());
		Assert.equal(holder.set(123).value(), 123);
		Assert.no(holder.isEmpty());
		Assert.equal(holder.set(null).value(), null);
	}

	@Test
	public void shouldProvideDefaultValue() {
		Assert.equal(Holder.of().value(null), null);
		Assert.equal(Holder.of().value("test"), "test");
		Assert.equal(Holder.of("test").value("test0"), "test");
		Assert.equal(Holder.mutable().value(null), null);
		Assert.equal(Holder.mutable().value("test"), "test");
		Assert.equal(Holder.mutable("test").value("test0"), "test");
	}

	@Test
	public void shouldDetermineIfHoldsValue() {
		Assert.yes(Holder.of("test").holds("test"));
		Assert.no(Holder.of("test").holds("test0"));
		Assert.no(Holder.of("test").holds(null));
		Assert.yes(Holder.of(null).holds(null));
	}

	@Test
	public void shouldVerifyNotEmpty() {
		Assert.equal(Holder.of("test").verify(), "test");
		Assert.equal(Holder.of(null).verify(), null);
		Assert.thrown(Holder.of()::verify);
	}
}
