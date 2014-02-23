package ceri.x10.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class FunctionBehavior {

	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToCreateWithUnsupportedFunctionType() {
		assertNotNull(new Function(House.A, FunctionType.DIM));
	}


	@Test
	public void shouldObeyEqualsContract() {
		Function fn1 = new Function(House.L, FunctionType.ALL_UNITS_OFF);
		Function fn2 = new Function(House.L, FunctionType.ALL_UNITS_OFF);
		Function fn3 = new Function(House.N, FunctionType.ALL_UNITS_OFF);
		Function fn4 = new Function(House.L, FunctionType.ALL_LIGHTS_OFF);
		assertThat(fn1, is(fn1));
		assertThat(fn1, is(fn2));
		assertFalse(fn1.equals(null));
		assertFalse(fn1.equals(new Object()));
		assertThat(fn1, not(fn3));
		assertThat(fn1, not(fn4));
		assertThat(fn1.toString(), is(fn2.toString()));
	}

}
