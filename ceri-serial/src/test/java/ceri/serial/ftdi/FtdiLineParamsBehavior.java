package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type.BREAK_ON;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type.BITS_7;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.MARK;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.NONE;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type.STOP_BIT_15;
import org.junit.Test;

public class FtdiLineParamsBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		FtdiLineParams t = FtdiLineParams.builder().parity(MARK).build();
		FtdiLineParams eq0 = FtdiLineParams.builder().parity(MARK).build();
		FtdiLineParams ne0 = FtdiLineParams.builder().parity(NONE).build();
		FtdiLineParams ne1 = FtdiLineParams.builder().dataBits(BITS_7).parity(MARK).build();
		FtdiLineParams ne2 = FtdiLineParams.builder().stopBits(STOP_BIT_15).parity(MARK).build();
		FtdiLineParams ne3 = FtdiLineParams.builder().parity(MARK).breakType(BREAK_ON).build();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

}
