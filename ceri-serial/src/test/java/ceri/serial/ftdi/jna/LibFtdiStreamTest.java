package ceri.serial.ftdi.jna;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INTERRUPTED;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_IO;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NO_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_SUCCESS;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_OVERFLOW;
import static ceri.serial.libusb.test.TestLibUsbNative.lastError;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.data.ByteArray;
import ceri.common.function.Enclosure;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.ErrorGen;
import ceri.common.test.Testing;
import ceri.jna.util.Jna;
import ceri.log.test.LogModifier;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_context;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdiStream.FTDIProgressInfo;
import ceri.serial.ftdi.jna.LibFtdiStream.FTDIStreamCallback;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_status;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.TestLibUsbNative;

public class LibFtdiStreamTest {
	private static final FTDIStreamCallback<?> trueCallback = (_, _, _, _) -> true;
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;
	private ftdi_context ftdi;

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		var config = LibUsbSampleData.ftdiConfig();
		config.desc.bcdDevice = 0x700;
		lib.data.addConfig(config);
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
		var n = new AtomicInteger();
		lib.handleTransferEvent.autoResponse(event -> fill(event.buffer(), n));
		var encoder = ByteArray.Encoder.of();
		FTDIStreamCallback<?> callback = (buf, len, _, _) -> collect(encoder, buf, len, 24);
		LibFtdiStream.ftdi_readstream(ftdi, callback, null, 2, 3);
		Assert.array(encoder.bytes(), 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4,
			4, 4, 4, 5, 5, 5, 6, 6, 6); // 3 transfers x 2 packets each until cancel
	}

	@Test
	public void shouldUpdateProgress() {
		var n = new AtomicInteger();
		lib.handleTransferEvent.autoResponse(event -> fill(event.buffer(), n));
		var sync = CallSync.<FTDIProgressInfo, Boolean>function(null);
		FTDIStreamCallback<?> callback = (_, _, prog, _) -> progress(sync, prog);
		try (var exec = Testing
			.threadRun(() -> LibFtdiStream.ftdi_readstream(ftdi, callback, null, 2, 3, 0.0))) {
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
		Assert.thrown(() -> LibFtdiStream.ftdi_readstream(ftdi, trueCallback, null, 2, 3));
	}

	@Test
	public void shouldFailOnEventHandlingError() {
		lib.handleTransferEvent.error.set(lastError(LIBUSB_ERROR_INTERRUPTED),
			lastError(LIBUSB_ERROR_NO_DEVICE));
		Assert.thrown(() -> LibFtdiStream.ftdi_readstream(ftdi, trueCallback, null, 2, 3));
		lib.handleTransferEvent.error.setFrom(ErrorGen.RTX);
		Assert.thrown(() -> LibFtdiStream.ftdi_readstream(ftdi, trueCallback, null, 2, 3));
	}

	@Test
	public void shouldFailOnBadTransferStatus() {
		LogModifier.run(() -> {
			lib.handleTransferEvent.autoResponses(LIBUSB_TRANSFER_OVERFLOW);
			Assert.thrown(() -> LibFtdiStream.ftdi_readstream(ftdi, trueCallback, null, 2, 3));
		}, Level.OFF, LibFtdiStream.class);
	}

	@Test
	public void shouldFailOnCallbackError() {
		FTDIStreamCallback<?> callback =
			(_, _, prog, _) -> prog == null ? true : Assert.throwRuntime();
		Assert.thrown(() -> LibFtdiStream.ftdi_readstream(ftdi, callback, null, 2, 3, 0.0));
	}

	private static boolean assertTotalBytes(FTDIProgressInfo prog, long curr, long prev, long first,
		boolean response) {
		Assert.equal(prog.current.totalBytes, curr);
		Assert.equal(prog.prev.totalBytes, prev);
		Assert.equal(prog.first.totalBytes, first);
		return response;
	}

	private static boolean progress(CallSync.Function<FTDIProgressInfo, Boolean> sync,
		FTDIProgressInfo prog) {
		if (prog == null) return true;
		return sync.apply(prog);
	}

	private static boolean collect(ByteArray.Encoder encoder, Pointer p, int len, int max) {
		if (p == null) return true;
		encoder.writeFrom(Jna.bytes(p, 0, len));
		return encoder.length() < max;
	}

	private static libusb_transfer_status fill(ByteBuffer buffer, AtomicInteger n) {
		byte value = (byte) n.incrementAndGet();
		while (buffer.position() < buffer.capacity())
			buffer.put(value);
		return LIBUSB_TRANSFER_COMPLETED;
	}
}
