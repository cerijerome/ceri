package ceri.x10.command;

import static ceri.common.test.Assert.assertAllNotEqual;
import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class AddressBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Address.of(House.E, Unit._13);
		var eq0 = Address.of(House.E, Unit._13);
		var eq1 = Address.from("E13");
		var ne0 = Address.of(House.D, Unit._13);
		var ne1 = Address.of(House.E, Unit._14);
		var ne2 = Address.from("F13");
		var ne3 = Address.from("E12");
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromString() {
		assertEquals(Address.from("P16"), Address.of(House.P, Unit._16));
		assertEquals(Address.from("O8"), Address.of(House.O, Unit._8));
		Assert.thrown(() -> Address.from(null));
		Assert.thrown(() -> Address.from(""));
		Assert.thrown(() -> Address.from("A"));
		Assert.thrown(() -> Address.from("A0"));
		Assert.thrown(() -> Address.from("A17"));
		Assert.thrown(() -> Address.from("17"));
		Assert.thrown(() -> Address.from("Q1"));
	}

	@Test
	public void shouldCompareAddresses() {
		assertEquals(Address.from("I10").compareTo(Address.from("I10")), 0);
		assertEquals(Address.from("I10").compareTo(Address.from("I11")), -1);
		assertEquals(Address.from("I10").compareTo(Address.from("J11")), -1);
		assertEquals(Address.from("I10").compareTo(Address.from("I9")), 1);
		assertEquals(Address.from("I10").compareTo(Address.from("H10")), 1);
	}
}
