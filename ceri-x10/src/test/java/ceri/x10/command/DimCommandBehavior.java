package ceri.x10.command;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import ceri.x10.type.FunctionType;
import ceri.x10.type.House;
import ceri.x10.type.Unit;

public class DimCommandBehavior {

	@Test
	public void shouldOnlyAllowZeroToOneHundredPercentDim() {
		assertNotNull(new DimCommand(House.P, Unit._8, FunctionType.DIM, 0));
		assertNotNull(new DimCommand(House.P, Unit._8, FunctionType.BRIGHT, 100));
		try {
			assertNotNull(new DimCommand(House.P, Unit._8, FunctionType.BRIGHT, -1));
			fail();
		} catch (IllegalArgumentException e) {}
		try {
			assertNotNull(new DimCommand(House.P, Unit._8, FunctionType.DIM, 101));
			fail();
		} catch (IllegalArgumentException e) {}
	}

	
	@Test
	public void shouldObeyEqualsContract() {
		DimCommand dim1 = new DimCommand(House.P, Unit._8, FunctionType.DIM, 100);
		DimCommand dim2 = new DimCommand(House.P, Unit._8, FunctionType.DIM, 100);
		DimCommand dim3 = new DimCommand(House.P, Unit._8, FunctionType.DIM, 99);
		DimCommand dim4 = new DimCommand(House.P, Unit._8, FunctionType.BRIGHT, 100);
		DimCommand dim5 = new DimCommand(House.P, Unit._7, FunctionType.DIM, 100);
		DimCommand dim6 = new DimCommand(House.O, Unit._7, FunctionType.DIM, 100);
		assertThat(dim1, is(dim1));
		assertThat(dim1, is(dim2));
		assertFalse(dim1.equals(null));
		assertFalse(dim1.equals(new Object()));
		assertThat(dim1, not(dim3));
		assertThat(dim1, not(dim4));
		assertThat(dim1, not(dim5));
		assertThat(dim1, not(dim6));
		assertThat(dim1.hashCode(), is(dim2.hashCode()));
	}


}
