package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import org.junit.Test;
import ceri.common.test.TestUtil;
import ceri.serial.ftdi.jna.LibFtdi;

public class FtdiLineParamsBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = FtdiLineParams.builder().parity(LibFtdi.ftdi_parity_type.MARK).build();
		var eq0 = FtdiLineParams.builder().parity(LibFtdi.ftdi_parity_type.MARK).build();
		var ne0 = FtdiLineParams.builder().parity(LibFtdi.ftdi_parity_type.NONE).build();
		var ne1 = FtdiLineParams.builder().dataBits(LibFtdi.ftdi_data_bits_type.BITS_7)
			.parity(LibFtdi.ftdi_parity_type.MARK).build();
		var ne2 = FtdiLineParams.builder().stopBits(LibFtdi.ftdi_stop_bits_type.STOP_BIT_15)
			.parity(LibFtdi.ftdi_parity_type.MARK).build();
		var ne3 = FtdiLineParams.builder().parity(LibFtdi.ftdi_parity_type.MARK)
			.breakType(LibFtdi.ftdi_break_type.BREAK_ON).build();
		TestUtil.exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}
}
