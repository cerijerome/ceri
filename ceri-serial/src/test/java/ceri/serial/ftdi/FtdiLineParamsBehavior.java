package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type.BREAK_ON;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type.BITS_7;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.EVEN;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.MARK;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.NONE;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.ODD;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type.STOP_BIT_1;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type.STOP_BIT_15;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type.STOP_BIT_2;
import org.junit.Test;
import ceri.common.test.TestUtil;

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

	@Test
	public void shouldCreateFromProperties() {
		var base = TestUtil.baseProperties("line");
		assertEquals(new FtdiLineProperties(base, "line.1").params(), FtdiLineParams.builder()
			.dataBits(BITS_7).stopBits(STOP_BIT_1).parity(ODD).breakType(BREAK_ON).build());
		assertEquals(new FtdiLineProperties(base, "line.2").params(),
			FtdiLineParams.builder().stopBits(STOP_BIT_15).parity(EVEN).build());
		assertEquals(new FtdiLineProperties(base, "line.3").params(),
			FtdiLineParams.builder().stopBits(STOP_BIT_2).build());
		assertThrown(() -> new FtdiLineProperties(base, "line.4").params());
	}

}
