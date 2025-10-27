package ceri.serial.ftdi.util;

import java.util.Properties;
import org.junit.Test;
import ceri.common.property.TypedProperties;
import ceri.common.test.Assert;
import ceri.serial.ftdi.jna.LibFtdi;

public class FtdiPropertiesBehavior {

	@Test
	public void shouldReadStopBits() {
		var p = new Properties();
		var props = new FtdiProperties(TypedProperties.from(p));
		p.put("stop.bits", "1");
		Assert.equal(props.config().params.stopBits(), LibFtdi.ftdi_stop_bits_type.STOP_BIT_1);
		p.put("stop.bits", "1.5");
		Assert.equal(props.config().params.stopBits(), LibFtdi.ftdi_stop_bits_type.STOP_BIT_15);
		p.put("stop.bits", "2");
		Assert.equal(props.config().params.stopBits(), LibFtdi.ftdi_stop_bits_type.STOP_BIT_2);
		p.put("stop.bits", "1.1");
		Assert.thrown(() -> props.config());
	}

	@Test
	public void shouldReadParity() {
		var p = new Properties();
		var props = new FtdiProperties(TypedProperties.from(p));
		p.put("parity", "odd");
		Assert.equal(props.config().params.parity(), LibFtdi.ftdi_parity_type.ODD);
		p.put("parity", "none");
		Assert.equal(props.config().params.parity(), LibFtdi.ftdi_parity_type.NONE);
		p.put("parity", "null");
		Assert.thrown(() -> props.config());
	}
}
