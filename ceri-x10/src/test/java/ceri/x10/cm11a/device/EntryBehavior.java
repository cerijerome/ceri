package ceri.x10.cm11a.device;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.x10.command.Address;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;
import ceri.x10.type.DimFunction;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.Function;

public class EntryBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Entry t = Entry.of(DimFunction.dim(House.H, 50));
		Entry eq0 = Entry.of(DimFunction.dim(House.H, 50));
		Entry ne0 = Entry.of(DimFunction.dim(House.I, 50));
		Entry ne1 = Entry.of(Address.of(House.H, Unit._10));
		Entry ne2 = Entry.of(Function.of(House.H, FunctionType.on));
		Entry ne3 = Entry.of(ExtFunction.of(House.H, 50, 0));
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldValidateArguments() {
		assertThrown(() -> Entry.of((Address) null));
		assertThrown(() -> Entry.of((Function) null));
		assertThrown(() -> Entry.of((DimFunction) null));
		assertThrown(() -> Entry.of((ExtFunction) null));
	}

	@Test
	public void shouldReturnNullForNonMatchingWrappedType() {
		Entry addr = Entry.of(Address.from("G15"));
		assertThat(addr.asAddress(), is(Address.from("G15")));
		assertNull(addr.asBaseFunction());
		assertNull(addr.asDimFunction());
		assertNull(addr.asExtFunction());
		assertNull(addr.asFunction());
	}

}
