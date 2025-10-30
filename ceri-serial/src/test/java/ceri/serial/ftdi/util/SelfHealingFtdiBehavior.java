package ceri.serial.ftdi.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.array.Array;
import ceri.common.concurrent.ValueCondition;
import ceri.common.data.ByteProvider;
import ceri.common.function.Closeables;
import ceri.common.function.Enclosure;
import ceri.common.function.Functional;
import ceri.common.function.Functions;
import ceri.common.io.StateChange;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.common.test.Testing;
import ceri.jna.test.JnaTesting;
import ceri.log.io.SelfHealing;
import ceri.log.test.LogModifier;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.FtdiProgressInfo;
import ceri.serial.ftdi.jna.LibFtdi;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.ftdi.test.TestFtdi;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.LibUsbTestData.DeviceConfig;
import ceri.serial.libusb.test.TestLibUsbNative;

public class SelfHealingFtdiBehavior {
	private static final SelfHealingFtdi.Config config =
		SelfHealingFtdi.Config.builder().selfHealing(SelfHealing.Config.NULL).build();
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;
	private DeviceConfig sampleConfig;
	private SelfHealingFtdi con;

	@After
	public void after() {
		Closeables.close(con, enc);
		con = null;
		enc = null;
		sampleConfig = null;
	}

	@Test
	public void shouldSetFields() {
		var params = FtdiLineParams.builder().parity(LibFtdi.ftdi_parity_type.MARK).build();
		var bitMode = FtdiBitMode.of(LibFtdi.ftdi_mpsse_mode.BITMODE_FT1284);
		var ftdi = FtdiConfig.builder().params(params).bitMode(bitMode).build();
		Functions.Predicate<Exception> predicate = e -> "test".equals(e.getMessage());
		var selfHealing = SelfHealing.Config.builder().brokenPredicate(predicate).build();
		var config = SelfHealingFtdi.Config.builder().ftdi(ftdi).selfHealing(selfHealing).build();
		Assert.equal(config.ftdi.params, params);
		Assert.equal(config.ftdi.bitMode, FtdiBitMode.of(LibFtdi.ftdi_mpsse_mode.BITMODE_FT1284));
		Assert.equal(config.selfHealing.brokenPredicate, predicate);
		Assert.yes(config.selfHealing.brokenPredicate.test(new IOException("test")));
		Assert.no(config.selfHealing.brokenPredicate.test(new IOException()));
		Assert.find(config.toString(), "\\bMARK\\b");
	}

	@Test
	public void shouldCreateFromDescriptor() {
		Assert.equal(SelfHealingFtdi.Config.of("").finder, LibUsbFinder.of(0, 0));
		Assert.equal(SelfHealingFtdi.Config.of("0x401:0x66").finder, LibUsbFinder.of(0x401, 0x66));
	}

	@Test
	public void shouldCopyFromConfig() {
		var c0 = SelfHealingFtdi.Config.of("0x401:0x66");
		var config =
			SelfHealingFtdi.Config.builder(SelfHealingFtdi.Config.of("0x401:0x66")).build();
		Assert.equal(config.finder, c0.finder);
	}

	@Test
	public void shouldCreateFromProperties() {
		var properties = Testing.properties("ftdi");
		var config1 = new SelfHealingFtdi.Properties(properties, "ftdi.1").config();
		var config2 = new SelfHealingFtdi.Properties(properties, "ftdi.2").config();
		Assert.equal(config1.finder, LibUsbFinder.builder().vendor(0x401).build());
		Assert.equal(config1.iface, LibFtdi.ftdi_interface.INTERFACE_D);
		Assert.equal(config1.ftdi.baud, 19200);
		Assert.equal(config1.ftdi.bitMode,
			FtdiBitMode.builder(LibFtdi.ftdi_mpsse_mode.BITMODE_FT1284).mask(0x3f).build());
		Assert.equal(config1.ftdi.params.dataBits(), LibFtdi.ftdi_data_bits_type.BITS_7);
		Assert.equal(config1.ftdi.params.breakType(), LibFtdi.ftdi_break_type.BREAK_ON);
		Assert.equal(config1.selfHealing.fixRetryDelayMs, 33);
		Assert.equal(config1.selfHealing.recoveryDelayMs, 777);
		Assert.equal(config2.finder, LibFtdiUtil.FINDER);
		Assert.equal(config2.iface, LibFtdi.ftdi_interface.INTERFACE_ANY);
		Assert.equal(config2.ftdi.baud, 38400);
		Assert.equal(config2.ftdi.bitMode, null);
		Assert.equal(config2.selfHealing.fixRetryDelayMs, 444);
		Assert.equal(config2.selfHealing.recoveryDelayMs, 88);
	}

	@Test
	public void shouldOpenFtdiDevice() throws IOException {
		init();
		con.open();
		Assert.ordered(lib.transferOut.values(),
			List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()), // open:ftdi_usb_reset()
			List.of(0x40, 0x03, 0x4138, 0, ByteProvider.empty())); // open:ftdi_set_baudrate()
		lib.transferIn.assertCalls(0);
	}

	@Test
	public void shouldProvideDescriptors() throws IOException {
		connect();
		var desc = con.descriptor();
		Assert.equal(desc.manufacturer(), "FTDI");
		Assert.equal(desc.description(), "FT245R USB FIFO");
		Assert.equal(desc.serial(), "A7047D8V");
	}

	@Test
	public void shouldResetUsb() throws IOException {
		connect();
		con.usbReset();
		Assert.ordered(lib.transferOut.values(),
			List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()));
	}

	@Test
	public void shouldConfigureFtdi() throws IOException {
		connect();
		con.bitMode(FtdiBitMode.OFF);
		lib.transferOut.assertAuto(List.of(0x40, 0x0b, 0x0000, 1, ByteProvider.empty()));
		con.baud(19200);
		lib.transferOut.assertAuto(List.of(0x40, 0x03, 0x809c, 0, ByteProvider.empty()));
		con.bitMode(FtdiBitMode.of(LibFtdi.ftdi_mpsse_mode.BITMODE_CBUS));
		lib.transferOut.assertAuto(List.of(0x40, 0x0b, 0x20ff, 1, ByteProvider.empty()));
		con.baud(19200);
		lib.transferOut.assertAuto(List.of(0x40, 0x03, 0xc027, 0, ByteProvider.empty()));
		con.flowControl(FtdiFlowControl.xonXoff);
		lib.transferOut.assertAuto(List.of(0x40, 0x02, 0, 0x0401, ByteProvider.empty()));
		con.line(FtdiLineParams.builder().dataBits(LibFtdi.ftdi_data_bits_type.BITS_7)
			.breakType(LibFtdi.ftdi_break_type.BREAK_ON).build());
		lib.transferOut.assertAuto(List.of(0x40, 0x04, 0x4007, 1, ByteProvider.empty()));
	}

	@Test
	public void shouldSetDtrRts() throws IOException {
		connect();
		con.rts(true);
		lib.transferOut.assertAuto(List.of(0x40, 0x01, 0x0202, 1, ByteProvider.empty()));
		con.dtr(true);
		lib.transferOut.assertAuto(List.of(0x40, 0x01, 0x0101, 1, ByteProvider.empty()));
	}

	@Test
	public void shouldReadPins() throws IOException {
		connect();
		lib.transferIn.autoResponses(ByteProvider.of(0xa5));
		Assert.equal(con.readPins(), 0xa5);
		lib.transferIn.assertAuto(List.of(0xc0, 0x0c, 0x0000, 1, 1));
	}

	@Test
	public void shouldPollModemStatus() throws IOException {
		connect();
		lib.transferIn.autoResponses(ByteProvider.of(0xe0, 0x8f));
		Assert.equal(con.pollModemStatus(), 0x8fe0);
	}

	@Test
	public void shouldProvideLatencyTimer() throws IOException {
		connect();
		con.latencyTimer(123);
		lib.transferOut.assertAuto(List.of(0x40, 0x09, 123, 1, ByteProvider.empty()));
		lib.transferIn.autoResponses(ByteProvider.of(123));
		Assert.equal(con.latencyTimer(), 123);
	}

	@Test
	public void shouldProvideChunkSizes() throws IOException {
		connect();
		con.readChunkSize(123);
		Assert.equal(con.readChunkSize(), 123);
		con.writeChunkSize(456);
		Assert.equal(con.writeChunkSize(), 456);
	}

	@Test
	public void shouldPurgeBuffers() throws IOException {
		connect();
		con.purgeReadBuffer();
		lib.transferOut.assertAuto(List.of(0x40, 0, 1, 1, ByteProvider.empty()));
		con.purgeWriteBuffer();
		lib.transferOut.assertAuto(List.of(0x40, 0, 2, 1, ByteProvider.empty()));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadBytes() throws IOException {
		connect();
		lib.transferIn.autoResponses(ByteProvider.of(1, 2, 3, 4, 5));
		Assert.array(con.in().readNBytes(3), 3, 4, 5); // 2B status + 3B data
		lib.transferIn.assertAuto(List.of(0x81, 5));
		lib.transferIn.autoResponses(ByteProvider.of(6, 7, 8));
		Assert.equal(con.in().read(), 8); // 2B status + 1B data
		lib.transferIn.assertAuto(List.of(0x81, 3));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteBytes() throws IOException {
		connect();
		con.out().write(Array.bytes.of(1, 2, 3));
		lib.transferOut.assertAuto(List.of(0x02, ByteProvider.of(1, 2, 3)));
	}

	@Test
	public void shouldReadAsynchronously() throws IOException {
		connect();
		try (var m = new Memory(3)) {
			var xfer = con.readSubmit(m, 3); // full async test under Ftdi tests
			Assert.equal(xfer.dataDone(), 3);
		}
	}

	@Test
	public void shouldWriteAsynchronously() throws IOException {
		connect();
		try (var m = new Memory(3)) {
			var xfer = con.writeSubmit(m, 3); // full async test under Ftdi tests
			Assert.equal(xfer.dataDone(), 3);
		}
	}

	@Test
	public void shouldReadStream() throws IOException {
		init();
		sampleConfig.desc.bcdDevice = 0x700; // fifo-enabled device
		con.open();
		// lib.transferOut.reset();
		Captor.Bi<FtdiProgressInfo, ByteBuffer> captor = Captor.ofBi();
		con.readStream((i, b) -> {
			captor.accept(i, b);
			return false;
		}, 1, 1);
		Assert.isNull(captor.first.values.get(0));
		Assert.notNull(captor.second.values.get(0));
		Assert.notNull(captor.first.values.get(1));
		Assert.isNull(captor.second.values.get(1));
	}

	@Test
	public void shouldRetryOnFailure() throws InterruptedException {
		init();
		LogModifier.run(() -> {
			ValueCondition<StateChange> sync = ValueCondition.of();
			try (var _ = con.listeners().enclose(sync::signal)) {
				lib.transferOut.error.setFrom(JnaTesting.LEX);
				Assert.thrown(con::open);
				sync.await(StateChange.broken);
				lib.transferOut.awaitAuto();
				lib.transferOut.awaitAuto();
				lib.transferOut.awaitAuto();
				lib.transferOut.error.clear();
				sync.await(StateChange.fixed);
			}
		}, Level.OFF, SelfHealing.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailIfNotConnected() {
		init();
		Assert.thrown(() -> con.in().read()); // not connected, set broken, then fix
		Functional.muteRun(() -> con.in().read()); // may have been fixed
		Functional.muteRun(() -> con.readPins()); // may have been fixed
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOverrideConstruction() {
		var testFtdi = TestFtdi.of();
		Assert.same(testFtdi.config().ftdi(), testFtdi);
	}

	/**
	 * Connect and reset state.
	 */
	private void connect() throws IOException {
		init();
		con.open();
		lib.transferOut.reset();
	}

	private void init() {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		sampleConfig = LibUsbSampleData.ftdiConfig();
		lib.data.addConfig(sampleConfig);
		con = SelfHealingFtdi.of(config);
	}
}
