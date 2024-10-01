package ceri.serial.ftdi.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.util.Properties;
import org.junit.Test;
import ceri.common.property.TypedProperties;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type;

public class FtdiPropertiesBehavior {

	@Test
	public void shouldReadStopBits() {
		var p = new Properties();
		var props = new FtdiProperties(TypedProperties.from(p));
		p.put("stop.bits", "1");
		assertEquals(props.config().params.stopBits(), ftdi_stop_bits_type.STOP_BIT_1);
		p.put("stop.bits", "1.5");
		assertEquals(props.config().params.stopBits(), ftdi_stop_bits_type.STOP_BIT_15);
		p.put("stop.bits", "2");
		assertEquals(props.config().params.stopBits(), ftdi_stop_bits_type.STOP_BIT_2);
		p.put("stop.bits", "1.1");
		assertThrown(() -> props.config());
	}

	@Test
	public void shouldReadParity() {
		var p = new Properties();
		var props = new FtdiProperties(TypedProperties.from(p));
		p.put("parity", "odd");
		assertEquals(props.config().params.parity(), ftdi_parity_type.ODD);
		p.put("parity", "none");
		assertEquals(props.config().params.parity(), ftdi_parity_type.NONE);
		p.put("parity", "null");
		assertThrown(() -> props.config());
	}

}
