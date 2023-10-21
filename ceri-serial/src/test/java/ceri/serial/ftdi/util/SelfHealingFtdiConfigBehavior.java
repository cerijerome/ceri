package ceri.serial.ftdi.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.baseProperties;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_interface.INTERFACE_ANY;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_interface.INTERFACE_D;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_FT1284;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.MARK;
import java.io.IOException;
import java.util.function.Predicate;
import org.junit.Test;
import ceri.log.io.SelfHealingConfig;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.libusb.jna.LibUsbFinder;

public class SelfHealingFtdiConfigBehavior {

	@Test
	public void shouldSetFields() {
		var params = FtdiLineParams.builder().parity(MARK).build();
		var bitMode = FtdiBitMode.of(BITMODE_FT1284);
		var ftdi = FtdiConfig.builder().params(params).bitMode(bitMode).build();
		Predicate<Exception> predicate = e -> "test".equals(e.getMessage());
		var selfHealing = SelfHealingConfig.builder().brokenPredicate(predicate).build();
		var config = SelfHealingFtdiConfig.builder().ftdi(ftdi).selfHealing(selfHealing).build();
		assertEquals(config.ftdi.params, params);
		assertEquals(config.ftdi.bitMode, FtdiBitMode.of(BITMODE_FT1284));
		assertEquals(config.selfHealing.brokenPredicate, predicate);
		assertTrue(config.selfHealing.brokenPredicate.test(new IOException("test")));
		assertFalse(config.selfHealing.brokenPredicate.test(new IOException()));
		assertFind(config.toString(), "\\bMARK\\b");
	}

	@Test
	public void shouldCreateFromDescriptor() {
		assertEquals(SelfHealingFtdiConfig.of("").finder, LibUsbFinder.of(0, 0));
		assertEquals(SelfHealingFtdiConfig.of("0x401:0x66").finder, LibUsbFinder.of(0x401, 0x66));
	}

	@Test
	public void shouldCopyFromConfig() {
		var c0 = SelfHealingFtdiConfig.of("0x401:0x66");
		var config = SelfHealingFtdiConfig.builder(SelfHealingFtdiConfig.of("0x401:0x66")).build();
		assertEquals(config.finder, c0.finder);
	}

	@Test
	public void shouldCreateFromProperties() {
		var properties = baseProperties("ftdi");
		var config1 = new SelfHealingFtdiProperties(properties, "ftdi.1").config();
		var config2 = new SelfHealingFtdiProperties(properties, "ftdi.2").config();
		assertEquals(config1.finder, LibUsbFinder.builder().vendor(0x401).build());
		assertEquals(config1.iface, INTERFACE_D);
		assertEquals(config1.ftdi.baud, 19200);
		assertEquals(config1.ftdi.bitMode, FtdiBitMode.builder(BITMODE_FT1284).mask(0x3f).build());
		assertEquals(config1.ftdi.params.dataBits, ftdi_data_bits_type.BITS_7);
		assertEquals(config1.ftdi.params.breakType, ftdi_break_type.BREAK_ON);
		assertEquals(config1.selfHealing.fixRetryDelayMs, 33);
		assertEquals(config1.selfHealing.recoveryDelayMs, 777);
		assertEquals(config2.finder, LibFtdiUtil.FINDER);
		assertEquals(config2.iface, INTERFACE_ANY);
		assertEquals(config2.ftdi.baud, 38400);
		assertEquals(config2.ftdi.bitMode, null);
		assertEquals(config2.selfHealing.fixRetryDelayMs, 444);
		assertEquals(config2.selfHealing.recoveryDelayMs, 88);
	}

}
