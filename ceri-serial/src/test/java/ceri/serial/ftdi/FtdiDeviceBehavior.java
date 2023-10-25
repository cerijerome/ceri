package ceri.serial.ftdi;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.provider;
import static ceri.jna.test.JnaTestUtil.assertMemory;
import static ceri.jna.test.JnaTestUtil.assertPointer;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type.BREAK_ON;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type.BITS_7;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.ODD;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type.STOP_BIT_2;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_MEM;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_PIPE;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_SUCCESS;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_type.LIBUSB_TRANSFER_TYPE_BULK;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteUtil;
import ceri.common.data.ByteWriter;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.jna.util.GcMemory;
import ceri.log.test.LogModifier;
import ceri.serial.ftdi.jna.LibFtdi;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.LibUsbTestData.DeviceConfig;
import ceri.serial.libusb.test.TestLibUsbNative;
import ceri.serial.libusb.test.TestLibUsbNative.TransferEvent;

public class FtdiDeviceBehavior {
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;
	private FtdiDevice ftdi;
	private DeviceConfig config;

	@Before
	public void before() {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		config = LibUsbSampleData.ftdiConfig();
		lib.data.addConfig(config);
	}

	@After
	public void after() {
		config = null;
		if (ftdi != null) ftdi.close();
		ftdi = null;
		enc.close();
	}

	@Test
	public void shouldDetermineIfFatalError() {
		assertFalse(FtdiDevice.isFatal(null));
		assertFalse(FtdiDevice.isFatal(new IOException("test")));
		assertFalse(FtdiDevice.isFatal(LibUsbException.of(LIBUSB_ERROR_PIPE, "test")));
		assertTrue(FtdiDevice.isFatal(LibUsbException.of(LIBUSB_ERROR_NO_DEVICE, "test")));
		assertTrue(FtdiDevice.isFatal(LibUsbException.of(LIBUSB_ERROR_NOT_FOUND, "test")));
		assertTrue(FtdiDevice.isFatal(LibUsbException.of(LIBUSB_ERROR_NO_MEM, "test")));
	}

	@Test
	public void shouldFailIfClosed() throws LibUsbException {
		ftdi = FtdiDevice.open();
		ftdi.close();
		assertThrown(() -> ftdi.usbReset());
		assertThrown(() -> ftdi.in().read());
		lib.transferOut.assertValues( // open() leftovers:
			List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()), // ftdi_usb_reset
			List.of(0x40, 0x03, 0x4138, 0, ByteProvider.empty())); // ftdi_set_baudrate 9600
		lib.transferIn.assertCalls(0);
	}

	@Test
	public void shouldSetFtdiConfiguration() throws IOException {
		ftdi = open();
		ftdi.usbReset();
		ftdi.bitBang(false);
		ftdi.bitBang(true);
		ftdi.baud(250000);
		ftdi.line(FtdiLineParams.builder().dataBits(BITS_7).stopBits(STOP_BIT_2).parity(ODD)
			.breakType(BREAK_ON).build());
		ftdi.latencyTimer(100);
		ftdi.purgeBuffers();
		lib.transferOut.assertValues( //
			List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()), // usbReset
			List.of(0x40, 0x0b, 0, 1, ByteProvider.empty()), // bitBang
			List.of(0x40, 0x0b, 0x01ff, 1, ByteProvider.empty()), // bitBang
			List.of(0x40, 0x03, 0x3, 0, ByteProvider.empty()), // baudRate
			List.of(0x40, 0x04, 0x5107, 1, ByteProvider.empty()), // lineParams
			List.of(0x40, 0x09, 100, 1, ByteProvider.empty()), // latencyTimer
			List.of(0x40, 0x00, 1, 1, ByteProvider.empty()), // purgeRxBuffer
			List.of(0x40, 0x00, 2, 1, ByteProvider.empty())); // purgeTxBuffer
	}

	@Test
	public void shouldGetFtdiConfiguration() throws LibUsbException {
		ftdi = FtdiDevice.open(LibFtdiUtil.FINDER, ftdi_interface.INTERFACE_A);
		lib.transferIn.autoResponses(ByteProvider.of(99));
		assertEquals(ftdi.latencyTimer(), 99);
		lib.transferIn.autoResponses(ByteProvider.of(0x12, 0x34));
		assertEquals(ftdi.pollModemStatus(), 0x3412);
		lib.transferIn.assertValues( //
			List.of(0xc0, 0x0a, 0, 1, 1), // latencyTimer
			List.of(0xc0, 0x05, 0, 1, 2)); // pollModemStatus
	}

	@Test
	public void shouldAccessDescriptors() throws IOException {
		ftdi = open();
		assertEquals(ftdi.descriptor().manufacturer(), "FTDI");
		assertEquals(ftdi.descriptor().description(), "FT245R USB FIFO");
		assertEquals(ftdi.descriptor().serial(), "A7047D8V");
	}

	@Test
	public void shouldControlDtrRts() throws IOException {
		ftdi = open();
		ftdi.rts(true);
		ftdi.rts(false);
		ftdi.dtr(true);
		ftdi.dtr(false);
		lib.transferOut.assertValues( //
			List.of(0x40, 0x01, 0x202, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x200, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x101, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x100, 1, ByteProvider.empty()));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteData() throws IOException {
		ftdi = open();
		ftdi.out().write(bytes(1, 2, 3));
		ftdi.out().write(bytes(4, 5));
		lib.transferOut.assertValues( //
			List.of(0x02, provider(1, 2, 3)), //
			List.of(0x02, provider(4, 5)));
		lib.transferOut.autoResponses((Integer) null); // set transferred to 0
		assertThrown(() -> ftdi.out().write(bytes(1, 2, 3))); // incomplete i/o exception
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReadData() throws IOException {
		ftdi = open();
		lib.transferIn.autoResponses( //
			provider(0, 0, 0xab), // read() => 2B status + 1B data
			provider(0, 0), // read() => 2B status + 0B data
			provider(0, 0, 1, 2, 3), // read(3) => 2B status + 3B data
			provider(0, 0, 4, 5), provider()); // read(3) => 2B status + 2B data
		assertEquals(ftdi.in().read(), 0xab); // R1
		assertEquals(ftdi.in().read(), -1); // R2
		assertArray(ftdi.in().readNBytes(3), 1, 2, 3); // R3
		assertArray(ftdi.in().readNBytes(3), 4, 5); // R4
		lib.transferIn.assertValues( //
			List.of(0x81, 3), // R1
			List.of(0x81, 3), // R2
			List.of(0x81, 5), // R3
			List.of(0x81, 5), // R4: readNBytes[1] -> ftdi_read_data[1] -> 4
			List.of(0x81, 3), // R4: readNBytes[1] -> ftdi_read_data[2] -> 0
			List.of(0x81, 3)); // R4: : readNBytes[2] -> ftdi_read_data[1] -> 0
	}

	@Test
	public void shouldWriteAsync() throws IOException {
		ftdi = open();
		ftdi.writeChunkSize(2);
		var enc = ByteArray.Encoder.of();
		lib.handleTransferEvent.autoResponse(te -> assertBulkWrite(te, 0x02, enc));
		var control = ftdi.writeSubmit(1, 2, 3, 4, 5);
		assertEquals(control.dataDone(), 5);
		assertArray(enc.bytes(), 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldWriteAsyncUntilFailure() throws IOException {
		ftdi = open();
		ftdi.writeChunkSize(2);
		var enc = ByteArray.Encoder.of();
		lib.submitTransfer.autoResponses(LIBUSB_SUCCESS, LIBUSB_SUCCESS, LIBUSB_ERROR_IO);
		lib.handleTransferEvent.autoResponse(te -> assertBulkWrite(te, 0x02, enc));
		LogModifier.run(() -> {
			var control = ftdi.writeSubmit(1, 2, 3, 4, 5);
			assertEquals(control.dataDone(), 4);
			assertArray(enc.bytes(), 1, 2, 3, 4); // 2 chunks successful
		}, Level.OFF, LibFtdi.class);
	}

	@Test
	public void shouldReadAsync() throws IOException {
		ftdi = open();
		ftdi.ftdi().max_packet_size = 7;
		ftdi.readChunkSize(4);
		var reader = ByteProvider.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).reader(0);
		lib.handleTransferEvent.autoResponse(te -> assertBulkRead(te, 0x81, reader));
		var m = GcMemory.malloc(5).clear();
		var control = ftdi.readSubmit(m.m, 5);
		assertEquals(control.dataDone(), 5);
		assertMemory(m.m, 0, 3, 4, 7, 8, 11); // 2-byte gaps for chunk headers
	}

	@Test
	public void shouldReadAsyncUntilFailure() throws IOException {
		ftdi = open();
		ftdi.readChunkSize(4);
		var reader = ByteProvider.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).reader(0);
		lib.submitTransfer.autoResponses(LIBUSB_SUCCESS, LIBUSB_SUCCESS, LIBUSB_ERROR_IO);
		lib.handleTransferEvent.autoResponse(te -> assertBulkRead(te, 0x81, reader));
		LogModifier.run(() -> {
			var m = GcMemory.malloc(5).clear();
			var control = ftdi.readSubmit(m.m, 5);
			assertEquals(control.dataDone(), 4);
			assertPointer(m.m, 0, 3, 4, 7, 8); // 2 chunks successful
		}, Level.OFF, LibFtdi.class);
	}

	@Test
	public void shouldCancelAsync() throws IOException {
		ftdi = open();
		ftdi.readChunkSize(8);
		var m = GcMemory.malloc(5).clear();
		lib.handleTransferEvent.autoResponse(te -> {
			te.buffer().put(ArrayUtil.bytes(1, 2, 3, 4, 5, 6, 7));
			return LIBUSB_TRANSFER_COMPLETED;
		});
		var control = ftdi.readSubmit(m.m, 5);
		control.dataCancel(Duration.ofMillis(1000));
		assertEquals(control.dataDone(), 0);
	}

	@Test
	public void shouldFailToReadStreamForInvalidFtdiChip() throws IOException {
		ftdi = open();
		assertThrown(() -> ftdi.readStream((prog, buffer) -> true, 1, 1));
	}

	@Test
	public void shouldReadStreamData() throws IOException {
		ftdi = openFtdiForStreaming(0x700, 5);
		AtomicInteger n = new AtomicInteger();
		lib.handleTransferEvent.autoResponse(event -> fill(event.buffer(), n));
		ByteArray.Encoder encoder = ByteArray.Encoder.of();
		FtdiDevice.StreamCallback callback = (prog, buffer) -> collect(encoder, buffer, 24);
		ftdi.readStream(callback, 2, 3);
		assertArray(encoder.bytes(), 3, 4, 5, 8, 9, 10, 13, 14, 15, 18, 19, 20, 23, 24, 25, 28, 29,
			30, 33, 34, 35, 38, 39, 40, 43, 44, 45, 53, 54, 55); // 48, 49, 50 dropped
	}

	@Test
	public void shouldUpdateStreamProgress() throws LibUsbException {
		ftdi = openFtdiForStreaming(0x700, 5);
		AtomicInteger n = new AtomicInteger();
		lib.handleTransferEvent.autoResponse(event -> fill(event.buffer(), n));
		CallSync.Function<FtdiProgressInfo, Boolean> sync =
			CallSync.function(null, true, true, false);
		FtdiDevice.StreamCallback callback =
			(prog, buffer) -> prog == null ? true : sync.apply(prog);
		ftdi.readStream(callback, 2, 3, 0.0);
		var prog = sync.value();
		assertEquals(prog.currentTotalBytes(), 54L);
		assertEquals(prog.previousTotalBytes(), 54L);
		assertEquals(prog.firstTotalBytes(), 0L);
	}

	private FtdiDevice openFtdiForStreaming(int device, int packetSize) throws LibUsbException {
		config.desc.bcdDevice = (short) device;
		ftdi = open();
		ftdi.ftdi().max_packet_size = packetSize;
		return ftdi;
	}

	private FtdiDevice open() throws LibUsbException {
		var ftdi = FtdiDevice.open();
		lib.transferOut.reset(); // clear original open()
		return ftdi;
	}

	private libusb_transfer_status assertBulkWrite(TransferEvent te, int endpoint,
		ByteWriter<?> writer) {
		assertEquals(te.endPoint(), endpoint);
		assertEquals(te.type(), LIBUSB_TRANSFER_TYPE_BULK);
		writer.writeFrom(ByteUtil.bytes(te.buffer()));
		return LIBUSB_TRANSFER_COMPLETED;
	}

	private libusb_transfer_status assertBulkRead(TransferEvent te, int endpoint,
		ByteReader reader) {
		assertEquals(te.endPoint(), endpoint);
		assertEquals(te.type(), LIBUSB_TRANSFER_TYPE_BULK);
		te.buffer().put(reader.readBytes(te.buffer().remaining()));
		return LIBUSB_TRANSFER_COMPLETED;
	}

	private static boolean collect(ByteArray.Encoder encoder, ByteBuffer buffer, int max) {
		if (buffer == null) return true;
		encoder.writeFrom(ByteUtil.bytes(buffer));
		return encoder.length() < max;
	}

	private static libusb_transfer_status fill(ByteBuffer buffer, AtomicInteger n) {
		while (buffer.position() < buffer.capacity())
			buffer.put((byte) n.incrementAndGet());
		return LIBUSB_TRANSFER_COMPLETED;
	}
}
