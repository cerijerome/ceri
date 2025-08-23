package ceri.serial.ftdi.util;

import static ceri.common.function.FunctionUtil.runSilently;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.provider;
import static ceri.common.test.TestUtil.typedProperties;
import static ceri.jna.test.JnaTestUtil.LEX;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_interface.INTERFACE_ANY;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_interface.INTERFACE_D;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode.BITMODE_FT1284;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.MARK;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.array.ArrayUtil;
import ceri.common.concurrent.ValueCondition;
import ceri.common.data.ByteProvider;
import ceri.common.function.Functions;
import ceri.common.io.StateChange;
import ceri.common.test.Captor;
import ceri.common.util.CloseableUtil;
import ceri.common.util.Enclosure;
import ceri.log.io.SelfHealing;
import ceri.log.test.LogModifier;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.FtdiProgressInfo;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
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
		CloseableUtil.close(con, enc);
		con = null;
		enc = null;
		sampleConfig = null;
	}

	@Test
	public void shouldSetFields() {
		var params = FtdiLineParams.builder().parity(MARK).build();
		var bitMode = FtdiBitMode.of(BITMODE_FT1284);
		var ftdi = FtdiConfig.builder().params(params).bitMode(bitMode).build();
		Functions.Predicate<Exception> predicate = e -> "test".equals(e.getMessage());
		var selfHealing = SelfHealing.Config.builder().brokenPredicate(predicate).build();
		var config = SelfHealingFtdi.Config.builder().ftdi(ftdi).selfHealing(selfHealing).build();
		assertEquals(config.ftdi.params, params);
		assertEquals(config.ftdi.bitMode, FtdiBitMode.of(BITMODE_FT1284));
		assertEquals(config.selfHealing.brokenPredicate, predicate);
		assertTrue(config.selfHealing.brokenPredicate.test(new IOException("test")));
		assertFalse(config.selfHealing.brokenPredicate.test(new IOException()));
		assertFind(config.toString(), "\\bMARK\\b");
	}

	@Test
	public void shouldCreateFromDescriptor() {
		assertEquals(SelfHealingFtdi.Config.of("").finder, LibUsbFinder.of(0, 0));
		assertEquals(SelfHealingFtdi.Config.of("0x401:0x66").finder, LibUsbFinder.of(0x401, 0x66));
	}

	@Test
	public void shouldCopyFromConfig() {
		var c0 = SelfHealingFtdi.Config.of("0x401:0x66");
		var config =
			SelfHealingFtdi.Config.builder(SelfHealingFtdi.Config.of("0x401:0x66")).build();
		assertEquals(config.finder, c0.finder);
	}

	@Test
	public void shouldCreateFromProperties() {
		var properties = typedProperties("ftdi");
		var config1 = new SelfHealingFtdi.Properties(properties, "ftdi.1").config();
		var config2 = new SelfHealingFtdi.Properties(properties, "ftdi.2").config();
		assertEquals(config1.finder, LibUsbFinder.builder().vendor(0x401).build());
		assertEquals(config1.iface, INTERFACE_D);
		assertEquals(config1.ftdi.baud, 19200);
		assertEquals(config1.ftdi.bitMode, FtdiBitMode.builder(BITMODE_FT1284).mask(0x3f).build());
		assertEquals(config1.ftdi.params.dataBits(), ftdi_data_bits_type.BITS_7);
		assertEquals(config1.ftdi.params.breakType(), ftdi_break_type.BREAK_ON);
		assertEquals(config1.selfHealing.fixRetryDelayMs, 33);
		assertEquals(config1.selfHealing.recoveryDelayMs, 777);
		assertEquals(config2.finder, LibFtdiUtil.FINDER);
		assertEquals(config2.iface, INTERFACE_ANY);
		assertEquals(config2.ftdi.baud, 38400);
		assertEquals(config2.ftdi.bitMode, null);
		assertEquals(config2.selfHealing.fixRetryDelayMs, 444);
		assertEquals(config2.selfHealing.recoveryDelayMs, 88);
	}

	@Test
	public void shouldOpenFtdiDevice() throws IOException {
		init();
		con.open();
		assertOrdered(lib.transferOut.values(),
			List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()), // open:ftdi_usb_reset()
			List.of(0x40, 0x03, 0x4138, 0, ByteProvider.empty())); // open:ftdi_set_baudrate()
		lib.transferIn.assertCalls(0);
	}

	@Test
	public void shouldProvideDescriptors() throws IOException {
		connect();
		var desc = con.descriptor();
		assertEquals(desc.manufacturer(), "FTDI");
		assertEquals(desc.description(), "FT245R USB FIFO");
		assertEquals(desc.serial(), "A7047D8V");
	}

	@Test
	public void shouldResetUsb() throws IOException {
		connect();
		con.usbReset();
		assertOrdered(lib.transferOut.values(),
			List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()));
	}

	@Test
	public void shouldConfigureFtdi() throws IOException {
		connect();
		con.bitMode(FtdiBitMode.OFF);
		lib.transferOut.assertAuto(List.of(0x40, 0x0b, 0x0000, 1, ByteProvider.empty()));
		con.baud(19200);
		lib.transferOut.assertAuto(List.of(0x40, 0x03, 0x809c, 0, ByteProvider.empty()));
		con.bitMode(FtdiBitMode.of(ftdi_mpsse_mode.BITMODE_CBUS));
		lib.transferOut.assertAuto(List.of(0x40, 0x0b, 0x20ff, 1, ByteProvider.empty()));
		con.baud(19200);
		lib.transferOut.assertAuto(List.of(0x40, 0x03, 0xc027, 0, ByteProvider.empty()));
		con.flowControl(FtdiFlowControl.xonXoff);
		lib.transferOut.assertAuto(List.of(0x40, 0x02, 0, 0x0401, ByteProvider.empty()));
		con.line(FtdiLineParams.builder().dataBits(ftdi_data_bits_type.BITS_7)
			.breakType(ftdi_break_type.BREAK_ON).build());
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
		lib.transferIn.autoResponses(provider(0xa5));
		assertEquals(con.readPins(), 0xa5);
		lib.transferIn.assertAuto(List.of(0xc0, 0x0c, 0x0000, 1, 1));
	}

	@Test
	public void shouldPollModemStatus() throws IOException {
		connect();
		lib.transferIn.autoResponses(provider(0xe0, 0x8f));
		assertEquals(con.pollModemStatus(), 0x8fe0);
	}

	@Test
	public void shouldProvideLatencyTimer() throws IOException {
		connect();
		con.latencyTimer(123);
		lib.transferOut.assertAuto(List.of(0x40, 0x09, 123, 1, ByteProvider.empty()));
		lib.transferIn.autoResponses(provider(123));
		assertEquals(con.latencyTimer(), 123);
	}

	@Test
	public void shouldProvideChunkSizes() throws IOException {
		connect();
		con.readChunkSize(123);
		assertEquals(con.readChunkSize(), 123);
		con.writeChunkSize(456);
		assertEquals(con.writeChunkSize(), 456);
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
		lib.transferIn.autoResponses(provider(1, 2, 3, 4, 5));
		assertArray(con.in().readNBytes(3), 3, 4, 5); // 2B status + 3B data
		lib.transferIn.assertAuto(List.of(0x81, 5));
		lib.transferIn.autoResponses(provider(6, 7, 8));
		assertEquals(con.in().read(), 8); // 2B status + 1B data
		lib.transferIn.assertAuto(List.of(0x81, 3));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteBytes() throws IOException {
		connect();
		con.out().write(ArrayUtil.bytes.of(1, 2, 3));
		lib.transferOut.assertAuto(List.of(0x02, provider(1, 2, 3)));
	}

	@Test
	public void shouldReadAsynchronously() throws IOException {
		connect();
		try (var m = new Memory(3)) {
			var xfer = con.readSubmit(m, 3); // full async test under Ftdi tests
			assertEquals(xfer.dataDone(), 3);
		}
	}

	@Test
	public void shouldWriteAsynchronously() throws IOException {
		connect();
		try (var m = new Memory(3)) {
			var xfer = con.writeSubmit(m, 3); // full async test under Ftdi tests
			assertEquals(xfer.dataDone(), 3);
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
		assertNull(captor.first.values.get(0));
		assertNotNull(captor.second.values.get(0));
		assertNotNull(captor.first.values.get(1));
		assertNull(captor.second.values.get(1));
	}

	@Test
	public void shouldRetryOnFailure() throws InterruptedException {
		init();
		LogModifier.run(() -> {
			ValueCondition<StateChange> sync = ValueCondition.of();
			try (var _ = con.listeners().enclose(sync::signal)) {
				lib.transferOut.error.setFrom(LEX);
				assertThrown(con::open);
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
		assertThrown(() -> con.in().read()); // not connected, set broken, then fix
		runSilently(() -> con.in().read()); // may have been fixed
		runSilently(() -> con.readPins()); // may have been fixed
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldOverrideConstruction() {
		var testFtdi = TestFtdi.of();
		assertSame(testFtdi.config().ftdi(), testFtdi);
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
