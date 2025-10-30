package ceri.serial.libusb;

import static ceri.jna.test.JnaTesting.buffer;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_OUT;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_recipient.LIBUSB_RECIPIENT_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_type.LIBUSB_REQUEST_TYPE_STANDARD;
import static ceri.serial.libusb.jna.LibUsb.libusb_standard_request.LIBUSB_REQUEST_GET_STATUS;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_flags.LIBUSB_TRANSFER_SHORT_NOT_OK;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_status.LIBUSB_TRANSFER_COMPLETED;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_type.LIBUSB_TRANSFER_TYPE_BULK;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_type.LIBUSB_TRANSFER_TYPE_BULK_STREAM;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_type.LIBUSB_TRANSFER_TYPE_CONTROL;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_type.LIBUSB_TRANSFER_TYPE_INTERRUPT;
import static ceri.serial.libusb.jna.LibUsb.libusb_transfer_type.LIBUSB_TRANSFER_TYPE_ISOCHRONOUS;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.function.Enclosure;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.jna.util.Jna;
import ceri.log.test.LogModifier;
import ceri.log.util.Logs;
import ceri.serial.libusb.UsbTransfer.Bulk;
import ceri.serial.libusb.UsbTransfer.BulkStream;
import ceri.serial.libusb.UsbTransfer.Control;
import ceri.serial.libusb.UsbTransfer.Interrupt;
import ceri.serial.libusb.UsbTransfer.Iso;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.TestLibUsbNative;

public class UsbTransferBehavior {
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;
	private Usb usb;
	private UsbDeviceHandle handle;

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		lib.data.addConfig(LibUsbSampleData.sdReaderConfig());
		usb = Usb.of();
		handle = usb.open(LibUsbFinder.of(0x05ac, 0));
	}

	@After
	public void after() {
		handle.close();
		usb.close();
		enc.close();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldExecuteAsyncControlTransfer() throws LibUsbException {
		CallSync.Consumer<Control> callback = CallSync.consumer(null, true);
		try (var transfer = handle.controlTransfer(callback)) {
			transfer.buffer(ByteBuffer.allocateDirect(12)).length(12).data()
				.put(Array.bytes.of(1, 2, 3, 4));
			transfer.setup().recipient(LIBUSB_RECIPIENT_DEVICE).type(LIBUSB_REQUEST_TYPE_STANDARD)
				.standard(LIBUSB_REQUEST_GET_STATUS).direction(LIBUSB_ENDPOINT_OUT).value(0xff)
				.index(3);
			transfer.submit();
			usb.events().handle();
			lib.assertTransferEvent(0, LIBUSB_TRANSFER_TYPE_CONTROL, 0, 0, 0xff, 0, 3, 0, 4, 0, 1,
				2, 3, 4);
			Assert.yes(transfer == callback.value());
			Assert.equal(transfer.status(), LIBUSB_TRANSFER_COMPLETED);
			Assert.equal(transfer.actualLength(), 12);
			var setup = transfer.setup();
			Assert.equal(setup.recipient(), LIBUSB_RECIPIENT_DEVICE);
			Assert.equal(setup.type(), LIBUSB_REQUEST_TYPE_STANDARD);
			Assert.equal(setup.standard(), LIBUSB_REQUEST_GET_STATUS);
			Assert.equal(setup.direction(), LIBUSB_ENDPOINT_OUT);
			Assert.equal(setup.value(), 0xff);
			Assert.equal(setup.index(), 3);
			Assert.equal(setup.length(), 4);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAccessControlTransferFields() throws LibUsbException {
		try (var transfer = handle.controlTransfer(null)) {
			Assert.equal(transfer.data(), null);
			transfer.setup().length(3);
			transfer.buffer(ByteBuffer.allocateDirect(11)).flags(LIBUSB_TRANSFER_SHORT_NOT_OK)
				.timeoutMs(33);
			Assert.equal(transfer.endPoint(), 0);
			Assert.equal(transfer.type(), LIBUSB_TRANSFER_TYPE_CONTROL);
			Assert.unordered(transfer.flags(), LIBUSB_TRANSFER_SHORT_NOT_OK);
			Assert.equal(transfer.timeoutMs(), 33);
			Assert.equal(transfer.length(), 11);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldExecuteAsyncBulkStreamTransfer() throws LibUsbException {
		CallSync.Consumer<BulkStream> callback = CallSync.consumer(null, true);
		try (var streams = handle.bulkStreams(2, 0x81, 0x02);
			var transfer = streams.bulkTransfer(0x81, 2, callback)) {
			Assert.thrown(() -> streams.bulkTransfer(0x01, 1, _ -> {}));
			Assert.thrown(() -> streams.bulkTransfer(0x81, 3, _ -> {}));
			Assert.equal(transfer.streamId(), 2);
			transfer.buffer(buffer(1, 2, 3, 4, 5)).length(4).submit();
			usb.events().handle();
			lib.assertTransferEvent(0x81, LIBUSB_TRANSFER_TYPE_BULK_STREAM, 1, 2, 3, 4);
			Assert.yes(transfer == callback.value());
			Assert.equal(transfer.status(), LIBUSB_TRANSFER_COMPLETED);
			Assert.equal(transfer.actualLength(), 4);
			Assert.equal(transfer.length(), 4);
		}
	}

	@Test
	public void shouldFailToAllocateTransferIfHandleIsClosed() throws LibUsbException {
		LogModifier.run(() -> {
			try (var streams = handle.bulkStreams(2, 0x81, 0x02)) {
				handle.close();
				Assert.thrown(() -> streams.bulkTransfer(0x81, 1, _ -> {}));
			}
		}, Level.OFF, Logs.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldExecuteAsyncBulkTransfer() throws LibUsbException {
		CallSync.Consumer<Bulk> callback = CallSync.consumer(null, true);
		try (var transfer = handle.bulkTransfer(callback)) {
			transfer.endPoint(0x81).buffer(buffer(1, 2, 3, 4, 5)).length(4).submit();
			usb.events().handle();
			lib.assertTransferEvent(0x81, LIBUSB_TRANSFER_TYPE_BULK, 1, 2, 3, 4);
			Assert.yes(transfer == callback.value());
			Assert.equal(transfer.status(), LIBUSB_TRANSFER_COMPLETED);
			Assert.equal(transfer.actualLength(), 4);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldExecuteAsyncInterruptTransfer() throws LibUsbException {
		CallSync.Consumer<Interrupt> callback = CallSync.consumer(null, true);
		try (var transfer = handle.interruptTransfer(callback)) {
			transfer.endPoint(0x81).buffer(buffer(1, 2, 3, 4, 5)).length(4).submit();
			usb.events().handle();
			lib.assertTransferEvent(0x81, LIBUSB_TRANSFER_TYPE_INTERRUPT, 1, 2, 3, 4);
			Assert.yes(transfer == callback.value());
			Assert.equal(transfer.status(), LIBUSB_TRANSFER_COMPLETED);
			Assert.equal(transfer.actualLength(), 4);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldExecuteAsyncIsoTransfer() throws LibUsbException {
		CallSync.Consumer<Iso> callback = CallSync.consumer(null, true);
		try (var transfer = handle.isoTransfer(4, callback)) {
			Assert.equal(transfer.packetBuffer(0), null);
			transfer.endPoint(0x81).buffer(buffer(1, 2, 3, 4, 5, 6, 7, 8, 9)).packets(3)
				.packetLengths(3).packetLength(2, 1).submit();
			usb.events().handle();
			lib.assertTransferEvent(0x81, LIBUSB_TRANSFER_TYPE_ISOCHRONOUS, 1, 2, 3, 4, 5, 6, 7);
			Assert.yes(transfer == callback.value());
			Assert.equal(transfer.status(), LIBUSB_TRANSFER_COMPLETED);
			Assert.equal(transfer.actualLength(), 7);
			Assert.array(Jna.bytes(transfer.packetBufferSimple(1)), 4, 5, 6);
			Assert.array(Jna.bytes(transfer.packetBuffer(2)), 7);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCancelTransfer() throws LibUsbException {
		LogModifier.run(() -> {
			try (var transfer = handle.bulkTransfer(null)) {
				Assert.yes(transfer.handle().usb() == usb);
				transfer.submit();
				transfer.cancel();
				transfer.close();
				Assert.thrown(() -> transfer.submit());
			}
		}, Level.OFF, Logs.class);
	}

}
