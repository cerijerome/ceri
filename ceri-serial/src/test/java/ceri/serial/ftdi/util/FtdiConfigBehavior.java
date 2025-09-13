package ceri.serial.ftdi.util;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.TestUtil;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.jna.LibFtdi;
import ceri.serial.ftdi.test.TestFtdi;

public class FtdiConfigBehavior {
	private TestFtdi ftdi;

	@After
	public void after() {
		ftdi = TestUtil.close(ftdi);
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = FtdiConfig.builder().baud(19200).bitMode(FtdiBitMode.BITBANG)
			.flowControl(FtdiFlowControl.rtsCts).latencyTimer(123).readChunkSize(333)
			.writeChunkSize(444).params(FtdiLineParams.DEFAULT).build();
		var eq0 = FtdiConfig.builder(t).build();
		var ne0 = FtdiConfig.NULL;
		var ne1 = FtdiConfig.builder(t).baud(250000).build();
		var ne2 = FtdiConfig.builder(t).bitMode(FtdiBitMode.OFF).build();
		var ne3 = FtdiConfig.builder(t).flowControl(FtdiFlowControl.disabled).build();
		var ne4 = FtdiConfig.builder(t).latencyTimer(122).build();
		var ne5 = FtdiConfig.builder(t).readChunkSize(334).build();
		var ne6 = FtdiConfig.builder(t).writeChunkSize(445).build();
		var ne7 = FtdiConfig.builder(t)
			.params(FtdiLineParams.builder().parity(LibFtdi.ftdi_parity_type.SPACE).build())
			.build();
		TestUtil.exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7);
	}

	@Test
	public void shouldApplyToFtdiDevice() throws IOException {
		ftdi = TestFtdi.of();
		var params = FtdiLineParams.builder().breakType(LibFtdi.ftdi_break_type.BREAK_ON)
			.dataBits(LibFtdi.ftdi_data_bits_type.BITS_7).parity(LibFtdi.ftdi_parity_type.MARK)
			.stopBits(LibFtdi.ftdi_stop_bits_type.STOP_BIT_2).build();
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
