package ceri.common.util;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class HolderBehavior {

	@Test
	public void testEquals() {
		assertTrue(Holder.equals(null, null));
		assertFalse(Holder.equals(null, Holder.of(null)));
		assertFalse(Holder.equals(Holder.of(null), null));
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
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertEquals(Holder.of("test").toString(), "[test]");
		assertEquals(Holder.of(null).toString(), "[null]");
		assertEquals(Holder.mutable("test").toString(), "[test]");
		assertEquals(Holder.mutable(null).toString(), "[null]");
		assertEquals(Holder.of().toString(), "empty");
	}

	@Test
	public void shouldModifyMutableHolder() {
		var holder = Holder.mutable();
		assertFalse(holder.nullValue());
		assertTrue(holder.isEmpty());
		holder.set(null);
		assertTrue(holder.nullValue());
		assertFalse(holder.isEmpty());
		holder.clear();
		assertFalse(holder.nullValue());
		assertTrue(holder.isEmpty());
	}

	@Test
	public void shouldProvideVolatileHolder() {
		var holder = Holder.ofVolatile(null);
		assertFalse(holder.isEmpty());
		assertEquals(holder.set(123).value(), 123);
		assertFalse(holder.isEmpty());
		assertEquals(holder.set(null).value(), null);
	}

	@Test
	public void shouldProvideDefaultValue() {
		assertEquals(Holder.of().value(null), null);
		assertEquals(Holder.of().value("test"), "test");
		assertEquals(Holder.of("test").value("test0"), "test");
		assertEquals(Holder.mutable().value(null), null);
		assertEquals(Holder.mutable().value("test"), "test");
		assertEquals(Holder.mutable("test").value("test0"), "test");
	}

	@Test
	public void shouldDetermineIfHoldsValue() {
		assertTrue(Holder.of("test").holds("test"));
		assertFalse(Holder.of("test").holds("test0"));
		assertFalse(Holder.of("test").holds(null));
		assertTrue(Holder.of(null).holds(null));
	}

	@Test
	public void shouldVerifyNotEmpty() {
		assertEquals(Holder.of("test").verify(), "test");
		assertEquals(Holder.of(null).verify(), null);
		assertThrown(Holder.of()::verify);
	}

}
