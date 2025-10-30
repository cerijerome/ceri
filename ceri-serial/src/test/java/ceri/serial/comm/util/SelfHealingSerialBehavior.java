package ceri.serial.comm.util;

import java.io.IOException;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;
import ceri.common.test.Testing;
import ceri.jna.clib.test.TestCLibNative;
import ceri.jna.util.JnaLibrary;
import ceri.log.io.SelfHealing;
import ceri.serial.comm.DataBits;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Parity;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.StopBits;
import ceri.serial.comm.test.TestSerial;

public class SelfHealingSerialBehavior {
	private static final SelfHealingSerial.Config config =
		SelfHealingSerial.Config.builder("test").selfHealing(SelfHealing.Config.NULL).build();
	private final JnaLibrary.Ref<? extends TestCLibNative> ref = TestCLibNative.ref();
	private SelfHealingSerial serial;
	private TestSerial testSerial;

	@After
	public void after() {
		Closeables.close(serial, ref);
		serial = null;
		testSerial = null; // sometimes log error if closed
	}

	@Test
	public void shouldDetermineIfEnabled() {
		Assert.yes(SelfHealingSerial.Config.of("test").enabled());
		Assert.no(SelfHealingSerial.Config.builder((PortSupplier) null).build().enabled());
	}

	@Test
	public void shouldReplaceSerialParams() {
		Assert.equal(SelfHealingSerial.Config.NULL.replace(null).serial.params,
			SerialParams.DEFAULT);
		Assert.equal(SelfHealingSerial.Config.NULL.replace(SerialParams.DEFAULT).serial.params,
			SerialParams.DEFAULT);
		Assert.equal(SelfHealingSerial.Config.NULL.replace(SerialParams.NULL).serial.params,
			SerialParams.NULL);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.find(SelfHealingSerial.Config.of("port"), "port.*lambda");
		try (var serial = SelfHealingSerial.of(config)) {
			Assert.find(serial, "test");
		}
	}

	@Test
	public void shouldCreateFromProperties() throws IOException {
		var p = Testing.properties("serial");
		var conf = new SelfHealingSerial.Properties(p, "serial").config();
		Assert.equal(conf.portSupplier.get(), "port0");
		Assert.equal(conf.serial.params.baud, 250000);
		Assert.equal(conf.serial.params.dataBits, DataBits._6);
		Assert.equal(conf.serial.params.stopBits, StopBits._2);
		Assert.equal(conf.serial.params.parity, Parity.mark);
		Assert.equal(conf.serial.flowControl, Set.of(FlowControl.rtsCtsIn, FlowControl.xonXoffOut));
		Assert.equal(conf.serial.inBufferSize, 111);
		Assert.equal(conf.serial.outBufferSize, 222);
		Assert.equal(conf.selfHealing.fixRetryDelayMs, 333);
		Assert.equal(conf.selfHealing.recoveryDelayMs, 444);
	}

	@Test
	public void shouldProvidePort() throws IOException {
		ref.init();
		serial = SelfHealingSerial.of(config);
		Assert.equal(serial.port(), "test"); // from config
		serial.open();
		Assert.equal(serial.port(), "test"); // from serial
	}

	@Test
	public void shouldConfigureSerialDevice() throws IOException {
		ref.init();
		serial = SelfHealingSerial.of(config);
		serial.inBufferSize(111);
		serial.outBufferSize(222);
		serial.params(SerialParams.of(19200));
		serial.flowControls(FlowControl.xonXoffOut);
		serial.open();
		Assert.equal(serial.inBufferSize(), 111);
		Assert.equal(serial.outBufferSize(), 222);
		Assert.equal(serial.params(), SerialParams.of(19200));
		Assert.equal(serial.flowControl(), Set.of(FlowControl.xonXoffOut));
		serial.inBufferSize(333);
		serial.outBufferSize(444);
		serial.params(SerialParams.of(38400));
		serial.flowControl(FlowControl.NONE);
		Assert.equal(serial.inBufferSize(), 333);
		Assert.equal(serial.outBufferSize(), 444);
		Assert.equal(serial.params(), SerialParams.of(38400));
		Assert.equal(serial.flowControl(), FlowControl.NONE);
	}

	@Test
	public void shouldSetSerialStates() throws IOException {
		testSerial = TestSerial.of();
		serial = SelfHealingSerial.of(testSerial.selfHealingConfig());
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
		serial = SelfHealingSerial.of(testSerial.selfHealingConfig());
		serial.open();
		testSerial.rts.value(false);
		testSerial.dtr.value(true);
		testSerial.cd.autoResponses(false);
		testSerial.cts.autoResponses(true);
		testSerial.dsr.autoResponses(false);
		testSerial.ri.autoResponses(true);
		Assert.equal(serial.rts(), false);
		Assert.equal(serial.dtr(), true);
		Assert.equal(serial.cd(), false);
		Assert.equal(serial.cts(), true);
		Assert.equal(serial.dsr(), false);
		Assert.equal(serial.ri(), true);
	}

	@Test
	public void shouldHandleOpenFailure() {
		testSerial = TestSerial.of();
		testSerial.open.error.setFrom(ErrorGen.IOX, null);
		serial = SelfHealingSerial.of(testSerial.selfHealingConfig());
		Assert.thrown(serial::open);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOverrideConstruction() {
		testSerial = TestSerial.of();
		Assert.same(testSerial.config().serial(), testSerial);
	}
}
