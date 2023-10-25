package ceri.serial.comm.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.jna.clib.test.TestCLibNative;
import ceri.log.io.SelfHealingConfig;
import ceri.log.util.LogUtil;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.test.TestSerial;

public class SelfHealingSerialBehavior {
	private static final SelfHealingSerialConfig config =
		SelfHealingSerialConfig.builder("test").selfHealing(SelfHealingConfig.NULL).build();
	private Enclosed<RuntimeException, TestCLibNative> enc;
	private SelfHealingSerial serial;
	private TestSerial testSerial;

	@After
	public void after() {
		LogUtil.close(serial, enc);
		serial = null;
		testSerial = null;
	}

	@Test
	public void shouldProvidePort() throws IOException {
		initLib();
		serial = SelfHealingSerial.of(config);
		assertEquals(serial.port(), "test"); // from config
		serial.open();
		assertEquals(serial.port(), "test"); // from serial
	}

	@Test
	public void shouldConfigureSerialDevice() throws IOException {
		initLib();
		serial = SelfHealingSerial.of(config);
		serial.inBufferSize(111);
		serial.outBufferSize(222);
		serial.params(SerialParams.of(19200));
		serial.flowControls(FlowControl.xonXoffOut);
		serial.open();
		assertEquals(serial.inBufferSize(), 111);
		assertEquals(serial.outBufferSize(), 222);
		assertEquals(serial.params(), SerialParams.of(19200));
		assertEquals(serial.flowControl(), Set.of(FlowControl.xonXoffOut));
		serial.inBufferSize(333);
		serial.outBufferSize(444);
		serial.params(SerialParams.of(38400));
		serial.flowControl(FlowControl.NONE);
		assertEquals(serial.inBufferSize(), 333);
		assertEquals(serial.outBufferSize(), 444);
		assertEquals(serial.params(), SerialParams.of(38400));
		assertEquals(serial.flowControl(), FlowControl.NONE);
	}

	@Test
	public void shouldSetSerialStates() throws IOException {
		testSerial = TestSerial.of();
		serial = SelfHealingSerial
			.of(SelfHealingSerialConfig.builder(config).factory(testSerial.factory()).build());
		serial.open();
		serial.brk(true);
		serial.rts(false);
		serial.dtr(true);
		testSerial.brk.assertAuto(true);
		testSerial.rts.assertAuto(false);
		testSerial.dtr.assertAuto(true);
	}

	@Test
	public void shouldGetSerialStates() throws IOException {
		testSerial = TestSerial.of();
		serial = SelfHealingSerial
			.of(SelfHealingSerialConfig.builder(config).factory(testSerial.factory()).build());
		serial.open();
		testSerial.rts.value(false);
		testSerial.dtr.value(true);
		testSerial.cd.autoResponses(false);
		testSerial.cts.autoResponses(true);
		testSerial.dsr.autoResponses(false);
		testSerial.ri.autoResponses(true);
		assertEquals(serial.rts(), false);
		assertEquals(serial.dtr(), true);
		assertEquals(serial.cd(), false);
		assertEquals(serial.cts(), true);
		assertEquals(serial.dsr(), false);
		assertEquals(serial.ri(), true);
	}

	@Test
	public void shouldHandleOpenFailure() {
		testSerial = TestSerial.of();
		testSerial.open.error.setFrom(IOX, null);
		serial = SelfHealingSerial
			.of(SelfHealingSerialConfig.builder(config).factory(testSerial.factory()).build());
		assertThrown(serial::open);
	}

	private void initLib() {
		enc = TestCLibNative.register();
	}
}
