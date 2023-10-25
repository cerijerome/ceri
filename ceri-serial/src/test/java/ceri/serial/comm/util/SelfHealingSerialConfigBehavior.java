package ceri.serial.comm.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.util.Set;
import org.junit.Test;
import ceri.common.test.TestUtil;
import ceri.serial.comm.DataBits;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Parity;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.StopBits;

public class SelfHealingSerialConfigBehavior {

	@Test
	public void shouldDetermineIfEnabled() {
		assertTrue(SelfHealingSerialConfig.of("test").enabled());
		assertFalse(SelfHealingSerialConfig.builder((PortSupplier) null).build().enabled());
	}

	@Test
	public void shouldReplaceSerialParams() {
		assertEquals(SelfHealingSerialConfig.NULL.replace(null).serial.params,
			SerialParams.DEFAULT);
		assertEquals(SelfHealingSerialConfig.NULL.replace(SerialParams.DEFAULT).serial.params,
			SerialParams.DEFAULT);
		assertEquals(SelfHealingSerialConfig.NULL.replace(SerialParams.NULL).serial.params,
			SerialParams.NULL);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(SelfHealingSerialConfig.of("port"), "port.*lambda");
	}

	@Test
	public void shouldCreateFromProperties() throws IOException {
		var p = TestUtil.baseProperties("serial");
		var conf = new SelfHealingSerialProperties(p, "serial").config();
		assertEquals(conf.portSupplier.get(), "port0");
		assertEquals(conf.serial.params.baud, 250000);
		assertEquals(conf.serial.params.dataBits, DataBits._6);
		assertEquals(conf.serial.params.stopBits, StopBits._2);
		assertEquals(conf.serial.params.parity, Parity.mark);
		assertEquals(conf.serial.flowControl, Set.of(FlowControl.rtsCtsIn, FlowControl.xonXoffOut));
		assertEquals(conf.serial.inBufferSize, 111);
		assertEquals(conf.serial.outBufferSize, 222);
		assertEquals(conf.selfHealing.fixRetryDelayMs, 333);
		assertEquals(conf.selfHealing.recoveryDelayMs, 444);
	}

}
