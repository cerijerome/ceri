package ceri.x10.cm11a;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.x10.type.Address;
import ceri.x10.type.DimFunction;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.House;

public class EntryBehavior {

	@Test
	public void shouldReturnNullForNonMatchingWrappedType() {
		Entry addr = new Entry(Address.fromString("G15"));
		assertThat(addr.asAddress(), is(Address.fromString("G15")));
		assertNull(addr.asBaseFunction());
		assertNull(addr.asDimFunction());
		assertNull(addr.asExtFunction());
		assertNull(addr.asFunction());
	}

	@Test
	public void shouldObeyEqualsContract() {
		Entry dim1 = new Entry(DimFunction.dim(House.G, 10));
		Entry dim2 = new Entry(DimFunction.dim(House.G, 10));
		Entry dim3 = new Entry(DimFunction.dim(House.H, 10));
		Entry dim4 = new Entry(DimFunction.dim(House.G, 11));
		Entry bright = new Entry(DimFunction.bright(House.G, 10));
		Entry ext = new Entry(new ExtFunction(House.G, (byte) 10, (byte) 10));
		assertThat(dim1, is(dim1));
		assertThat(dim1, is(dim2));
		assertNotEquals(null, dim1);
		assertThat(dim1, not(new Object()));
		assertThat(dim1, not(dim3));
		assertThat(dim1, not(dim4));
		assertThat(dim1, not(bright));
		assertThat(dim1, not(ext));
		assertThat(dim1.hashCode(), is(dim2.hashCode()));
		assertThat(dim1.hashCode(), not(dim3.hashCode()));
		assertThat(dim1.toString(), is(dim2.toString()));
		assertThat(dim1.toString(), not(dim3.toString()));
	}

}
