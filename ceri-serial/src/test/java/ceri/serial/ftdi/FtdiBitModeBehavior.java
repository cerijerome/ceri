package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_CBUS;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_FT1284;
import org.junit.Test;
import ceri.common.io.Direction;

public class FtdiBitModeBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		FtdiBitMode t = FtdiBitMode.builder(BITMODE_FT1284).mask(0xa5).build();
		FtdiBitMode eq0 = FtdiBitMode.builder(BITMODE_FT1284).mask(0xa5).build();
		FtdiBitMode eq1 = FtdiBitMode.builder(BITMODE_FT1284).allLines(Direction.in)
			.line(Direction.out, 0, 2, 5, 7, 8).build();
		FtdiBitMode eq2 = FtdiBitMode.builder(BITMODE_FT1284).allLines(Direction.out)
			.line(Direction.in, 1, 3, 4, 6).build();
		FtdiBitMode ne0 = FtdiBitMode.builder(BITMODE_CBUS).mask(0xa5).build();
		FtdiBitMode ne1 = FtdiBitMode.builder(BITMODE_FT1284).mask(0xa4).build();
		exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1);
	}

}
