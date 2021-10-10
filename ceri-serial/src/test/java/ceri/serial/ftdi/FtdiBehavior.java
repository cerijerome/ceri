package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.*;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_break_type.BREAK_ON;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_data_bits_type.BITS_7;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_parity_type.ODD;
import static ceri.serial.ftdi.jna.LibFtdi.ftdi_stop_bits_type.STOP_BIT_2;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.test.BinaryPrinter;
import ceri.common.test.CallSync;
import ceri.common.test.Captor;
import ceri.common.util.Enclosed;
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
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.subject;
		lib.data.deviceConfigs.add(LibUsbSampleData.ftdiConfig());
		// LibUsbSampleData.populate(lib.data);
		// ftdi = Ftdi.open();
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
		lib.controlTransferOut.assertValues( // open() leftovers:
			List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()), // ftdi_usb_reset
			List.of(0x40, 0x03, 0x4138, 0, ByteProvider.empty())); // ftdi_set_baudrate 9600
		lib.controlTransferIn.assertNoCall();
	}

	@Test
	public void shouldConfigureFtdi() throws LibUsbException {
		ftdi = Ftdi.open();
		lib.controlTransferOut.reset(); // clear original open()
		ftdi.usbReset();
		ftdi.bitBang(false);
		ftdi.bitBang(true);
		ftdi.baudRate(250000);
		ftdi.lineParams(FtdiLineParams.builder().dataBits(BITS_7).stopBits(STOP_BIT_2).parity(ODD)
			.breakType(BREAK_ON).build());
		lib.controlTransferOut.assertValues(List.of(0x40, 0x00, 0x0000, 1, ByteProvider.empty()),
			List.of(0x40, 0x0b, 0, 1, ByteProvider.empty()),
			List.of(0x40, 0x0b, 0x01ff, 1, ByteProvider.empty()),
			List.of(0x40, 0x03, 0x3, 0, ByteProvider.empty()),
			List.of(0x40, 0x04, 0x5107, 1, ByteProvider.empty()));
		lib.controlTransferIn.assertNoCall();
	}

	@Test
	public void shouldControlDtrRts() throws LibUsbException {
		ftdi = Ftdi.open();
		lib.controlTransferOut.reset(); // clear original open()
		ftdi.rts(true);
		ftdi.dtr(true);
		ftdi.dtrRts(false, false);
		lib.controlTransferOut.assertValues( //
			List.of(0x40, 0x01, 0x202, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x101, 1, ByteProvider.empty()),
			List.of(0x40, 0x01, 0x300, 1, ByteProvider.empty()));
	}

	@Test
	public void shouldWriteData() throws LibUsbException {
		ftdi = Ftdi.open();
		ftdi.write(1, 2, 3);
		ftdi.write(ByteBuffer.wrap(ArrayUtil.bytes(4, 5, 6)));
		lib.bulkTransferOut.assertValues( //
			List.of(0x02, provider(1, 2, 3)), //
			List.of(0x02, provider(4, 5, 6)));
	}

	@Test
	public void shouldReadData() throws LibUsbException {
		ftdi = Ftdi.open();
		lib.bulkTransferIn.autoResponses( //
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
		lib.bulkTransferIn.assertValues( //
			List.of(0x81, 3), //
			List.of(0x81, 3), //
			List.of(0x81, 5), //
			List.of(0x81, 5), List.of(0x81, 3), //
			List.of(0x81, 5), List.of(0x81, 3));
	}

	// TODO: check all calls to libusb_submit_transfer
	// TODO: stream
	// - allocate + submit multiple transfers
	// - handle events =>
	// - for each transfer:
	// -- copy data to buffer, update actual_length + status
	// -- invoke callback
	//
	// 1) Keep track of allocated transfers

	@Test
	public void shouldWriteAsync() throws LibUsbException {
		ftdi = Ftdi.open();
		var control = ftdi.writeSubmit(1, 2, 3, 4, 5);
		// assertEquals(control.dataDone(), 5);
	}

	/* streaming tests */

	@Test
	public void shouldFailToReadStreamForInvalidFtdiChip() throws LibUsbException {
		ftdi = Ftdi.open();
		assertThrown(() -> ftdi.readStream((prog, buffer) -> true, 1, 1));
	}
	
	//@Test
	public void shouldReadStreamData() throws LibUsbException {
		ftdi = openFtdiForStreaming(0x700, 5);
		AtomicInteger n = new AtomicInteger();
		lib.handleEvent.autoResponse(event -> fill(event.buffer(), n));
		ByteArray.Encoder encoder = ByteArray.Encoder.of();
		Ftdi.StreamCallback callback = (prog, buffer) -> collect(encoder, buffer, 24);
		ftdi.readStream(callback, 2, 3);
		assertArray(encoder.bytes(), 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4,
			4, 4, 4, 5, 5, 5, 6, 6, 6); // 3 transfers x 2 packets each until cancel
	}

	//@Test
	public void shouldUpdateStreamProgress() throws LibUsbException {
		ftdi = openFtdiForStreaming(0x700, 5);
		AtomicInteger n = new AtomicInteger();
		lib.handleEvent.autoResponse(event -> fill(event.buffer(), n));
		CallSync.Apply<FtdiProgressInfo, Boolean> sync = CallSync.function(null);
		Ftdi.StreamCallback callback = (prog, buffer) -> progress(sync, prog);
		try (var exec = threadRun(() -> ftdi.readStream(callback, 2, 3))) {
			sync.await(prog -> assertTotalBytes(prog, 18, 0, 0, true));
			sync.await(prog -> assertTotalBytes(prog, 36, 18, 0, true));
			sync.await(prog -> assertTotalBytes(prog, 54, 36, 0, false));
			exec.get();
		}
	}

	private Ftdi openFtdiForStreaming(int device, int packetSize) throws LibUsbException {
		lib.data.deviceConfigs.get(0).desc.bcdDevice = (short) device;
		ftdi = Ftdi.open();
		ftdi.ftdi().max_packet_size = packetSize;
		return ftdi;
	}

	private static boolean assertTotalBytes(FtdiProgressInfo prog, long curr, long prev, long first,
		boolean response) {
		assertEquals(prog.currentTotalBytes(), curr);
		assertEquals(prog.previousTotalBytes(), prev);
		assertEquals(prog.firstTotalBytes(), first);
		return response;
	}

	private static boolean progress(CallSync.Apply<FtdiProgressInfo, Boolean> sync,
		FtdiProgressInfo prog) {
		if (prog == null) return true;
		return sync.apply(prog);
	}

	private static boolean collect(ByteArray.Encoder encoder, ByteBuffer buffer, int max) {
		if (buffer == null) return true;
		encoder.writeFrom(ByteUtil.bytes(buffer));
		return encoder.length() < max;
	}

	private static libusb_transfer_status fill(ByteBuffer buffer, AtomicInteger n) {
		byte value = (byte) n.incrementAndGet();
		while (buffer.position() < buffer.capacity())
			buffer.put(value);
		return LIBUSB_TRANSFER_COMPLETED;
	}
}
