package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.provider;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type.BREAK_ON;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type.BITS_7;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.ODD;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type.STOP_BIT_2;
import static ceri.serial.jna.test.JnaTestUtil.assertMemory;
import static ceri.serial.jna.test.JnaTestUtil.assertPointer;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_SUCCESS;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_type.LIBUSB_TRANSFER_TYPE_BULK;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReader;
import ceri.common.data.ByteUtil;
import ceri.common.data.ByteWriter;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.log.test.LogModifier;
import ceri.serial.ftdi.jna.LibFtdi;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbSampleData;
import ceri.serial.libusb.jna.TestLibUsbNative;
import ceri.serial.libusb.jna.TestLibUsbNative.TransferEvent;

public class FtdiBehavior {
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;
	private Ftdi ftdi;

	@Before
	public void before() {
		enc = TestLibUsbNative.register();
		lib = enc.subject;
		lib.data.deviceConfigs.add(LibUsbSampleData.ftdiConfig());
	}

	@After
	public void after() {
		if (ftdi != null) ftdi.close();
		ftdi = null;
		enc.close();
	}

	@Test
	public void shouldFailIfClosed() throws LibUsbException {
		ftdi = Ftdi.open();
		ftdi.close();
		assertThrown(() -> ftdi.read());
		lib.syncTransferOut.assertValues( // open() leftovers:
			List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()), // ftdi_usb_reset
			List.of(0x40, 0x03, 0x4138, 0, ByteProvider.empty())); // ftdi_set_baudrate 9600
		lib.syncTransferIn.assertNoCall();
	}

	@Test
	public void shouldSetFtdiConfiguration() throws LibUsbException {
		ftdi = open();
		ftdi.usbReset();
		ftdi.bitBang(false);
		ftdi.bitBang(true);
		ftdi.baudRate(250000);
		ftdi.lineParams(FtdiLineParams.builder().dataBits(BITS_7).stopBits(STOP_BIT_2).parity(ODD)
			.breakType(BREAK_ON).build());
		ftdi.latencyTimer(100);
		ftdi.purgeBuffers();
		lib.syncTransferOut.assertValues( //
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
		ftdi = Ftdi.open(LibFtdiUtil.FINDER, ftdi_interface.INTERFACE_A);
		lib.syncTransferIn.autoResponses(ByteProvider.of(99));
		assertEquals(ftdi.latencyTimer(), 99);
		lib.syncTransferIn.autoResponses(ByteProvider.of(0x12, 0x34));
		assertEquals(ftdi.pollModemStatus(), 0x3412);
		lib.syncTransferIn.assertValues( //
			List.of(0xc0, 0x0a, 0, 1, 1), // latencyTimer
			List.of(0xc0, 0x05, 0, 1, 2)); // pollModemStatus
	}

	@Test
	public void shouldAccessDescriptors() throws LibUsbException {
		ftdi = open();
		assertEquals(ftdi.manufacturer(), "FTDI");
		assertEquals(ftdi.description(), "FT245R USB FIFO");
		assertEquals(ftdi.serial(), "A7047D8V");
	}

	@Test
	public void shouldControlDtrRts() throws LibUsbException {
		ftdi = open();
		ftdi.rts(true);
		ftdi.rts(false);
		ftdi.dtr(true);
		ftdi.dtr(false);
		ftdi.dtrRts(true, true);
		ftdi.dtrRts(true, false);
		ftdi.dtrRts(false, true);
		ftdi.dtrRts(false, false);
		lib.syncTransferOut.assertValues( //
			List.of(0x40, 0x01, 0x202, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x200, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x101, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x100, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x303, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x301, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x302, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x300, 1, ByteProvider.empty()));
	}

	@Test
	public void shouldWriteData() throws LibUsbException {
		ftdi = open();
		assertEquals(ftdi.write(1, 2, 3), 3);
		assertEquals(ftdi.write(ByteBuffer.wrap(ArrayUtil.bytes(4, 5, 6))), 3);
		lib.syncTransferOut.assertValues( //
			List.of(0x02, provider(1, 2, 3)), //
			List.of(0x02, provider(4, 5, 6)));
		lib.syncTransferOut.autoResponses((Integer) null); // set transferred to 0
		assertEquals(ftdi.write(1, 2, 3), 0);
	}

	@Test
	public void shouldReadData() throws LibUsbException {
		ftdi = open();
		lib.syncTransferIn.autoResponses( //
			provider(0, 0, 0xab), // read() => 2B status + 1B data
			provider(0, 0), // read() => 2B status + 0B data
			provider(0, 0, 1, 2, 3), // read(3) => 2B status + 3B data
			provider(0, 0, 4, 5), provider(), // read(3) => 2B status + 2B data
			provider(0, 0, 6, 7), provider()); // read(buffer[3]) => 2B status + 2B data
		assertEquals(ftdi.read(), 0xab);
		assertEquals(ftdi.read(), -1);
		assertArray(ftdi.read(3), 1, 2, 3);
		assertArray(ftdi.read(3), 4, 5);
		ByteBuffer bb = ByteBuffer.allocate(3);
		assertEquals(ftdi.read(bb), 2);
		assertArray(bb.array(), 6, 7, 0);
		lib.syncTransferIn.assertValues( //
			List.of(0x81, 3), //
			List.of(0x81, 3), //
			List.of(0x81, 5), //
			List.of(0x81, 5), List.of(0x81, 3), //
			List.of(0x81, 5), List.of(0x81, 3));
	}

	@Test
	public void shouldWriteAsync() throws LibUsbException {
		ftdi = open();
		ftdi.writeChunkSize(2);
		var enc = ByteArray.Encoder.of();
		lib.handleTransferEvent.autoResponse(te -> assertBulkWrite(te, 0x02, enc));
		var control = ftdi.writeSubmit(1, 2, 3, 4, 5);
		assertEquals(control.dataDone(), 5);
		assertArray(enc.bytes(), 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldWriteAsyncUntilFailure() throws LibUsbException {
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
	public void shouldReadAsync() throws LibUsbException {
		ftdi = open();
		ftdi.ftdi().max_packet_size = 7;
		ftdi.readChunkSize(4);
		var reader = ByteProvider.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).reader(0);
		lib.handleTransferEvent.autoResponse(te -> assertBulkRead(te, 0x81, reader));
		Memory m = JnaUtil.calloc(5);
		var control = ftdi.readSubmit(m, 5);
		assertEquals(control.dataDone(), 5);
		assertMemory(m, 0, 3, 4, 7, 8, 11); // 2-byte gaps for chunk headers
	}

	@Test
	public void shouldReadAsyncUntilFailure() throws LibUsbException {
		ftdi = open();
		ftdi.readChunkSize(4);
		var reader = ByteProvider.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).reader(0);
		lib.submitTransfer.autoResponses(LIBUSB_SUCCESS, LIBUSB_SUCCESS, LIBUSB_ERROR_IO);
		lib.handleTransferEvent.autoResponse(te -> assertBulkRead(te, 0x81, reader));
		LogModifier.run(() -> {
			Memory m = JnaUtil.calloc(5);
			var control = ftdi.readSubmit(m, 5);
			assertEquals(control.dataDone(), 4);
			assertPointer(m, 0, 3, 4, 7, 8); // 2 chunks successful
		}, Level.OFF, LibFtdi.class);
	}

	@Test
	public void shouldCancelAsync() throws LibUsbException {
		ftdi = open();
		ftdi.readChunkSize(8);
		Memory m = JnaUtil.calloc(5);
		lib.handleTransferEvent.autoResponse(te -> {
			te.buffer().put(ArrayUtil.bytes(1, 2, 3, 4, 5, 6, 7));
			return LIBUSB_TRANSFER_COMPLETED;
		});
		var control = ftdi.readSubmit(m, 5);
		control.dataCancel(Duration.ofMillis(1000));
		assertEquals(control.dataDone(), 0);
	}

	@Test
	public void shouldFailToReadStreamForInvalidFtdiChip() throws LibUsbException {
		ftdi = open();
		assertThrown(() -> ftdi.readStream((prog, buffer) -> true, 1, 1));
	}

	@Test
	public void shouldReadStreamData() throws LibUsbException {
		ftdi = openFtdiForStreaming(0x700, 5);
		AtomicInteger n = new AtomicInteger();
		lib.handleTransferEvent.autoResponse(event -> fill(event.buffer(), n));
		ByteArray.Encoder encoder = ByteArray.Encoder.of();
		Ftdi.StreamCallback callback = (prog, buffer) -> collect(encoder, buffer, 24);
		ftdi.readStream(callback, 2, 3);
		assertArray(encoder.bytes(), 3, 4, 5, 8, 9, 10, 13, 14, 15, 18, 19, 20, 23, 24, 25, 28, 29,
			30, 33, 34, 35, 38, 39, 40, 43, 44, 45, 53, 54, 55); // 48, 49, 50 dropped
	}

	@Test
	public void shouldUpdateStreamProgress() throws LibUsbException {
		ftdi = openFtdiForStreaming(0x700, 5);
		AtomicInteger n = new AtomicInteger();
		lib.handleTransferEvent.autoResponse(event -> fill(event.buffer(), n));
		CallSync.Apply<FtdiProgressInfo, Boolean> sync = CallSync.function(null, true, true, false);
		Ftdi.StreamCallback callback = (prog, buffer) -> prog == null ? true : sync.apply(prog);
		ftdi.readStream(callback, 2, 3, 0.0);
		var prog = sync.value();
		assertEquals(prog.currentTotalBytes(), 54L);
		assertEquals(prog.previousTotalBytes(), 54L);
		assertEquals(prog.firstTotalBytes(), 0L);
	}

	private Ftdi openFtdiForStreaming(int device, int packetSize) throws LibUsbException {
		lib.data.deviceConfigs.get(0).desc.bcdDevice = (short) device;
		ftdi = open();
		ftdi.ftdi().max_packet_size = packetSize;
		return ftdi;
	}

	private Ftdi open() throws LibUsbException {
		var ftdi = Ftdi.open();
		lib.syncTransferOut.reset(); // clear original open()
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
