package ceri.x10.command;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class AddressBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Address t = Address.of(House.E, Unit._13);
		Address eq0 = Address.of(House.E, Unit._13);
		Address eq1 = Address.from("E13");
		Address ne0 = Address.of(House.D, Unit._13);
		Address ne1 = Address.of(House.E, Unit._14);
		Address ne2 = Address.from("F13");
		Address ne3 = Address.from("E12");
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromString() {
		assertThat(Address.from("P16"), is(Address.of(House.P, Unit._16)));
		assertThat(Address.from("O8"), is(Address.of(House.O, Unit._8)));
		assertThrown(() -> Address.from(null));
		assertThrown(() -> Address.from(""));
		assertThrown(() -> Address.from("A"));
		assertThrown(() -> Address.from("A0"));
		assertThrown(() -> Address.from("A17"));
		assertThrown(() -> Address.from("17"));
		assertThrown(() -> Address.from("Q1"));
	}

	@Test
	public void shouldCompareAddresses() {
		assertThat(Address.from("I10").compareTo(Address.from("I10")), is(0));
		assertThat(Address.from("I10").compareTo(Address.from("I11")), is(-1));
		assertThat(Address.from("I10").compareTo(Address.from("J11")), is(-1));
		assertThat(Address.from("I10").compareTo(Address.from("I9")), is(1));
		assertThat(Address.from("I10").compareTo(Address.from("H10")), is(1));
	}

}
