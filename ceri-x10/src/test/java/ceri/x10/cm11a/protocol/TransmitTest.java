package ceri.x10.cm11a.protocol;

import static ceri.common.test.TestUtil.reader;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class TransmitTest {

	@Test
	public void testDecode() {
		assertThat(Transmit.decode(reader(0x04, 0x9b)), is(Entry.address(House.F, Unit._12)));
		assertThat(Transmit.decode(reader(0x06, 0x90)),
			is(Entry.function(House.F, FunctionType.allUnitsOff)));
		assertThat(Transmit.decode(reader(0x06, 0x92)),
			is(Entry.function(House.F, FunctionType.on)));
		assertThat(Transmit.decode(reader(0x26, 0x94)),
			is(Entry.dim(House.F, FunctionType.dim, 18)));
		assertThat(Transmit.decode(reader(0x07, 0x97, 0x14, 0x1e)), is(Entry.ext(House.F, 20, 30)));
	}

}
