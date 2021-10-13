package ceri.serial.ftdi.jna;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.throwIt;
import static ceri.common.test.ErrorGen.RTX;
import static ceri.common.test.TestUtil.threadRun;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_SUCCESS;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_OVERFLOW;
import static ceri.serial.libusb.jna.LibUsbTestData.lastError;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.data.ByteArray;
import ceri.common.test.CallSync;
import ceri.common.util.Enclosed;
import ceri.log.test.LogModifier;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdiStream.FTDIProgressInfo;
import ceri.serial.ftdi.jna.LibFtdiStream.FTDIStreamCallback;
import ceri.serial.jna.JnaUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbSampleData;
import ceri.serial.libusb.jna.TestLibUsbNative;
import ceri.serial.libusb.jna.TestLibUsbNative.TransferEvent;

public class LibFtdiStreamTest {
	private static final FTDIStreamCallback<?> trueCallback = (buf, len, prog, u) -> true;
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;
	private ftdi_context ftdi;

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.subject;
		lib.data.deviceConfigs.add(LibUsbSampleData.ftdiConfig());
		lib.data.deviceConfigs.get(0).desc.bcdDevice = 0x700;
		ftdi = LibFtdi.ftdi_new();
		LibFtdi.ftdi_set_interface(ftdi, ftdi_interface.INTERFACE_ANY);
		LibFtdi.ftdi_usb_open_find(ftdi, LibFtdiUtil.FINDER);
		ftdi.max_packet_size = 5;
	}

	@After
	public void after() {
		LibFtdi.ftdi_free(ftdi);
		enc.close();
	}

	@Test
	public void shouldReadPacketData() throws LibUsbException {
		AtomicInteger n = new AtomicInteger();
		lib.handleEvent.autoResponse(event -> fill(event.buffer(), n));
		ByteArray.Encoder encoder = ByteArray.Encoder.of();
		FTDIStreamCallback<?> callback = (buf, len, prog, u) -> collect(encoder, buf, len, 24);
		LibFtdiStream.ftdi_readstream(ftdi, callback, null, 2, 3);
		assertArray(encoder.bytes(), 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4,
			4, 4, 4, 5, 5, 5, 6, 6, 6); // 3 transfers x 2 packets each until cancel
	}

	@Test
	public void shouldUpdateProgress() {
		AtomicInteger n = new AtomicInteger();
		lib.handleEvent.autoResponse(event -> fill(event.buffer(), n));
		CallSync.Apply<FTDIProgressInfo, Boolean> sync = CallSync.function(null);
		FTDIStreamCallback<?> callback = (buf, len, prog, u) -> progress(sync, prog);
		try (
			var exec = threadRun(() -> LibFtdiStream.ftdi_readstream(ftdi, callback, null, 2, 3, 0.0))) {
			sync.await(prog -> assertTotalBytes(prog, 18, 0, 0, true));
			sync.await(prog -> assertTotalBytes(prog, 36, 18, 0, true));
			sync.await(prog -> assertTotalBytes(prog, 54, 36, 0, false)); // cancel
			sync.await(prog -> assertTotalBytes(prog, 54, 54, 0, false)); // final progress
			exec.get();
		}
	}

	@Test
	public void shouldFailOnSubmissionError() {
		lib.submitTransfer.autoResponses(LIBUSB_SUCCESS, LIBUSB_ERROR_IO, LIBUSB_SUCCESS);
		assertThrown(() -> LibFtdiStream.ftdi_readstream(ftdi, trueCallback, null, 2, 3));
	}

	@Test
	public void shouldFailOnEventHandlingError() {
		lib.handleEvent.error.set(lastError(LIBUSB_ERROR_INTERRUPTED),
			lastError(LIBUSB_ERROR_NO_DEVICE));
		assertThrown(() -> LibFtdiStream.ftdi_readstream(ftdi, trueCallback, null, 2, 3));
		lib.handleEvent.error.setFrom(RTX);
		assertThrown(() -> LibFtdiStream.ftdi_readstream(ftdi, trueCallback, null, 2, 3));
	}

	@Test
	public void shouldFailOnBadTransferStatus() {
		LogModifier.run(() -> {
			lib.handleEvent.autoResponses(LIBUSB_TRANSFER_OVERFLOW);
			assertThrown(() -> LibFtdiStream.ftdi_readstream(ftdi, trueCallback, null, 2, 3));
		}, Level.OFF, LibFtdiStream.class);
	}

	@Test
	public void shouldFailOnCallbackError() {
		FTDIStreamCallback<?> callback = (buf, len, prog, u) -> prog == null ? true : throwIt();
		assertThrown(() -> LibFtdiStream.ftdi_readstream(ftdi, callback, null, 2, 3, 0.0));
	}

	public static libusb_transfer_status handleEvent(TransferEvent event) {
		TestLibUsbNative.handleEvent(event);
		return LIBUSB_TRANSFER_COMPLETED;
	}

	private static boolean assertTotalBytes(FTDIProgressInfo prog, long curr, long prev, long first,
		boolean response) {
		assertEquals(prog.current.totalBytes, curr);
		assertEquals(prog.prev.totalBytes, prev);
		assertEquals(prog.first.totalBytes, first);
		return response;
	}

	private static boolean progress(CallSync.Apply<FTDIProgressInfo, Boolean> sync,
		FTDIProgressInfo prog) {
		if (prog == null) return true;
		return sync.apply(prog);
	}

	private static boolean collect(ByteArray.Encoder encoder, Pointer p, int len, int max) {
		if (p == null) return true;
		encoder.writeFrom(JnaUtil.bytes(p, 0, len));
		return encoder.length() < max;
	}

	private static libusb_transfer_status fill(ByteBuffer buffer, AtomicInteger n) {
		byte value = (byte) n.incrementAndGet();
		while (buffer.position() < buffer.capacity())
			buffer.put(value);
		return LIBUSB_TRANSFER_COMPLETED;
	}

}
