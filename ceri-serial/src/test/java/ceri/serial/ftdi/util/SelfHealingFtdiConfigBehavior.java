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
import org.junit.Test;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.libusb.jna.LibUsbFinder;

public class SelfHealingFtdiConfigBehavior {

	@Test
	public void shouldSetFields() {
		var line = FtdiLineParams.builder().parity(MARK).build();
		var config = SelfHealingFtdiConfig.builder().line(line).bitmode(BITMODE_FT1284)
			.brokenPredicate(e -> "test".equals(e.getMessage())).build();
		assertEquals(config.line, line);
		assertEquals(config.bitMode, FtdiBitMode.of(BITMODE_FT1284));
		assertTrue(config.brokenPredicate.test(new IOException("test")));
		assertFalse(config.brokenPredicate.test(new IOException()));
		assertFind(config.toString(), "\\bMARK\\b");
	}

	@Test
	public void shouldCreateFromDescriptor() {
		assertEquals(SelfHealingFtdiConfig.of("").finder, LibUsbFinder.of(0, 0));
		assertEquals(SelfHealingFtdiConfig.of("0x401:0x66").finder, LibUsbFinder.of(0x401, 0x66));
	}
	
	@Test
	public void shouldCreateFromProperties() {
		var properties = baseProperties("ftdi");
		var config1 = new SelfHealingFtdiProperties(properties, "ftdi.1").config();
		var config2 = new SelfHealingFtdiProperties(properties, "ftdi.2").config();
		assertEquals(config1.finder, LibUsbFinder.builder().vendor(0x401).build());
		assertEquals(config1.iface, INTERFACE_D);
		assertEquals(config1.baud, 19200);
		assertEquals(config1.bitMode, FtdiBitMode.builder(BITMODE_FT1284).mask(0x3f).build());
		assertEquals(config1.fixRetryDelayMs, 33);
		assertEquals(config1.recoveryDelayMs, 777);
		assertEquals(config2.finder, LibFtdiUtil.FINDER);
		assertEquals(config2.iface, INTERFACE_ANY);
		assertEquals(config2.baud, 38400);
		assertEquals(config2.bitMode, FtdiBitMode.BITBANG);
		assertEquals(config2.fixRetryDelayMs, 444);
		assertEquals(config2.recoveryDelayMs, 88);
	}

}
