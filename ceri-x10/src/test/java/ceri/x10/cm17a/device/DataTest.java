package ceri.x10.cm17a.device;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.x10.type.FunctionType.bright;
import static ceri.x10.type.FunctionType.dim;
import static ceri.x10.type.FunctionType.off;
import static ceri.x10.type.FunctionType.on;
import static ceri.x10.type.House.A;
import static ceri.x10.type.House.B;
import static ceri.x10.type.House.H;
import static ceri.x10.type.House.J;
import static ceri.x10.type.House.M;
import static ceri.x10.type.Unit._1;
import static ceri.x10.type.Unit._16;
import org.junit.Test;

public class DataTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Data.class);
	}

	@Test
	public void testTransmissionForUnitOnOff() {
		assertArray(Data.transmission(A, _1, off), 0xd5, 0xaa, 0x60, 0x20, 0xad);
		assertArray(Data.transmission(J, _16, on), 0xd5, 0xaa, 0xf4, 0x58, 0xad);
		assertArray(Data.transmission(M, _1, on), 0xd5, 0xaa, 0x00, 0x00, 0xad);
	}

	@Test
	public void testTransmissionSequenceForDim() {
		assertArray(Data.transmission(B, dim), 0xd5, 0xaa, 0x70, 0x98, 0xad);
		assertArray(Data.transmission(H, bright), 0xd5, 0xaa, 0xb0, 0x88, 0xad);
		assertArray(Data.transmission(M, dim), 0xd5, 0xaa, 0x00, 0x98, 0xad);
	}

}
