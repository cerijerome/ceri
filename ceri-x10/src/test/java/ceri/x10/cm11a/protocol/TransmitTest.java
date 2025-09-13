package ceri.x10.cm11a.protocol;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.reader;
import org.junit.Test;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

public class TransmitTest {

	@Test
	public void testDecode() {
		assertEquals(Transmit.decode(reader(0x04, 0x9b)), Entry.address(House.F, Unit._12));
		assertEquals(Transmit.decode(reader(0x06, 0x90)),
			Entry.function(House.F, FunctionType.allUnitsOff));
		assertEquals(Transmit.decode(reader(0x06, 0x92)), Entry.function(House.F, FunctionType.on));
		assertEquals(Transmit.decode(reader(0x26, 0x94)), Entry.dim(House.F, FunctionType.dim, 18));
		assertEquals(Transmit.decode(reader(0x07, 0x97, 0x14, 0x1e)), Entry.ext(House.F, 20, 30));
	}
}
