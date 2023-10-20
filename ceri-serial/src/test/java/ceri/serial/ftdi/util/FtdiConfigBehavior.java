package ceri.serial.ftdi.util;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type;
import ceri.serial.ftdi.test.TestFtdi;

public class FtdiConfigBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		FtdiConfig t = FtdiConfig.builder().baud(19200).bitMode(FtdiBitMode.BITBANG)
			.flowControl(FtdiFlowControl.rtsCts).latencyTimer(123).readChunkSize(333)
			.writeChunkSize(444).params(FtdiLineParams.DEFAULT).build();
		FtdiConfig eq0 = FtdiConfig.builder(t).build();
		FtdiConfig ne0 = FtdiConfig.NULL;
		FtdiConfig ne1 = FtdiConfig.builder(t).baud(250000).build();
		FtdiConfig ne2 = FtdiConfig.builder(t).bitMode(FtdiBitMode.OFF).build();
		FtdiConfig ne3 = FtdiConfig.builder(t).flowControl(FtdiFlowControl.disabled).build();
		FtdiConfig ne4 = FtdiConfig.builder(t).latencyTimer(122).build();
		FtdiConfig ne5 = FtdiConfig.builder(t).readChunkSize(334).build();
		FtdiConfig ne6 = FtdiConfig.builder(t).writeChunkSize(445).build();
		FtdiConfig ne7 = FtdiConfig.builder(t)
			.params(FtdiLineParams.builder().parity(ftdi_parity_type.SPACE).build()).build();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7);
	}

	@Test
	public void shouldApplyToFtdiDevice() throws IOException {
		try (var ftdi = TestFtdi.of()) {
			var params = FtdiLineParams.builder().breakType(ftdi_break_type.BREAK_ON)
				.dataBits(ftdi_data_bits_type.BITS_7).parity(ftdi_parity_type.MARK)
				.stopBits(ftdi_stop_bits_type.STOP_BIT_2).build();
			var config = FtdiConfig.builder().baud(19200).bitMode(FtdiBitMode.BITBANG)
				.flowControl(FtdiFlowControl.rtsCts).latencyTimer(123).readChunkSize(333)
				.writeChunkSize(444).params(params).build();
			ftdi.open();
			config.apply(ftdi);
			ftdi.baud.assertAuto(19200);
			ftdi.bitMode.assertAuto(FtdiBitMode.BITBANG);
			ftdi.flowControl.assertAuto(FtdiFlowControl.rtsCts);
			ftdi.latency.assertAuto(123);
			ftdi.params.assertAuto(params);
		}
	}

}
