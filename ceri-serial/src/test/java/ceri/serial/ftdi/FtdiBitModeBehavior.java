package ceri.serial.ftdi;

import org.junit.Test;
import ceri.common.io.Direction;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;
import ceri.serial.ftdi.jna.LibFtdi;

public class FtdiBitModeBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = FtdiBitMode.builder(LibFtdi.ftdi_mpsse_mode.BITMODE_FT1284).mask(0xa5).build();
		var eq0 = FtdiBitMode.builder(LibFtdi.ftdi_mpsse_mode.BITMODE_FT1284).mask(0xa5).build();
		var eq1 = FtdiBitMode.builder(LibFtdi.ftdi_mpsse_mode.BITMODE_FT1284).allLines(Direction.in)
			.line(Direction.out, 0, 2, 5, 7, 8).build();
		var eq2 = FtdiBitMode.builder(LibFtdi.ftdi_mpsse_mode.BITMODE_FT1284)
			.allLines(Direction.out).line(Direction.in, 1, 3, 4, 6).build();
		var ne0 = FtdiBitMode.builder(LibFtdi.ftdi_mpsse_mode.BITMODE_CBUS).mask(0xa5).build();
		var ne1 = FtdiBitMode.builder(LibFtdi.ftdi_mpsse_mode.BITMODE_FT1284).mask(0xa4).build();
		TestUtil.exerciseEquals(t, eq0, eq1, eq2);
		Assert.notEqualAll(t, ne0, ne1);
	}
}
