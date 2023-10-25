package ceri.serial.ftdi.util;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.function.FunctionUtil.runSilently;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.provider;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import ceri.common.concurrent.ValueCondition;
import ceri.common.data.ByteProvider;
import ceri.common.io.StateChange;
import ceri.common.test.Captor;
import ceri.common.util.Enclosed;
import ceri.log.io.SelfHealingConfig;
import ceri.log.io.SelfHealingDevice;
import ceri.log.test.LogModifier;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.FtdiLineParams;
import ceri.serial.ftdi.FtdiProgressInfo;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.LibUsbTestData.DeviceConfig;
import ceri.serial.libusb.test.TestLibUsbNative;

public class SelfHealingFtdiBehavior {
	private static final SelfHealingFtdiConfig config =
		SelfHealingFtdiConfig.builder().selfHealing(SelfHealingConfig.NULL).build();
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;
	private DeviceConfig sampleConfig;
	private SelfHealingFtdi con;

	@Before
	public void before() {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		sampleConfig = LibUsbSampleData.ftdiConfig();
		lib.data.addConfig(sampleConfig);
		con = SelfHealingFtdi.of(config);
	}

	@After
	public void after() {
		con.close();
		sampleConfig = null;
		enc.close();
	}

	@Test
	public void shouldOpenFtdiDevice() throws IOException {
		con.open();
		assertIterable(lib.transferOut.values(),
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
		assertIterable(lib.transferOut.values(),
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
		con.out().write(bytes(1, 2, 3));
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
		sampleConfig.desc.bcdDevice = 0x700; // fifo-enabled device
		connect();
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
		LogModifier.run(() -> {
			ValueCondition<StateChange> sync = ValueCondition.of();
			try (var enc = con.listeners().enclose(sync::signal)) {
				lib.transferOut.error.setFrom(() -> new LastErrorException("test"));
				assertThrown(con::open);
				sync.await(StateChange.broken);
				lib.transferOut.awaitAuto();
				lib.transferOut.awaitAuto();
				lib.transferOut.awaitAuto();
				lib.transferOut.error.clear();
				sync.await(StateChange.fixed);
			}
		}, Level.OFF, SelfHealingDevice.class);
	}

	@Test
	public void shouldFailIfNotConnected() {
		assertThrown(() -> con.in().read()); // not connected, set broken, then fix
		runSilently(() -> con.in().read()); // may have been fixed
		runSilently(() -> con.readPins()); // may have been fixed
	}

	/**
	 * Connect and reset state.
	 */
	private void connect() throws IOException {
		con.open();
		lib.transferOut.reset();
	}
}
