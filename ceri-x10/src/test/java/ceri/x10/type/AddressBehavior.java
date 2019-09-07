package ceri.x10.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class AddressBehavior {

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToCreateFromInvalidString() {
		Address.fromString("A");
	}

	@Test
	public void shouldObeyEqualsContract() {
		Address addr1 = Address.fromString("A10");
		Address addr2 = Address.fromString("A10");
		Address addr3 = Address.fromString("A11");
		Address addr4 = Address.fromString("B10");
		assertThat(addr1, is(addr1));
		assertThat(addr1, is(addr2));
		assertNotEquals(null, addr1);
		assertNotEquals(addr1, new Object());
		assertThat(addr1, not(addr3));
		assertThat(addr1, not(addr4));
		assertThat(addr1.toString(), is(addr2.toString()));
	}

}
