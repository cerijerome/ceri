package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_STRING;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.data.ByteProvider;
import ceri.common.function.Enclosure;
import ceri.common.test.Assert;
import ceri.jna.test.JnaTesting;
import ceri.serial.libusb.jna.LibUsb.libusb_capability;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.TestLibUsbNative;

public class UsbDeviceHandleBehavior {
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;
	private Usb usb;
	private UsbDeviceHandle handle;

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		lib.data.addConfig(LibUsbSampleData.audioConfig());
		usb = Usb.of();
		handle = usb.open(LibUsbFinder.of(0x0d8c, 0));
	}

	@After
	public void after() {
		handle.close();
		usb.close();
		enc.close();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideDevice() throws LibUsbException {
		Assert.notNull(handle.device());
		Assert.notNull(handle.device());
	}

	@Test
	public void shouldConfigureKernelDriver() throws LibUsbException {
		Assert.equal(UsbDeviceHandle.canDetachKernelDriver(), false);
		lib.data.capabilities(libusb_capability.LIBUSB_CAP_SUPPORTS_DETACH_KERNEL_DRIVER.value);
		Assert.equal(UsbDeviceHandle.canDetachKernelDriver(), true);
		handle.attachKernelDriver(1);
		Assert.equal(handle.kernelDriverActive(1), true);
		handle.detachKernelDriver(1);
		Assert.equal(handle.kernelDriverActive(1), false);
		handle.autoDetachKernelDriver(true);
		handle.autoDetachKernelDriver(false);
	}

	@Test
	public void shouldConfigureDevice() throws LibUsbException {
		handle.configuration(2);
		Assert.equal(handle.configuration(), 2);
		lib.transferIn.autoResponses(ByteProvider.of(0, 'a', 0, 'b', 0, 'c'));
		Assert.array(handle.descriptor(LIBUSB_DT_STRING, 1), 0, 'a', 0, 'b', 0, 'c');
		Assert.equal(handle.stringDescriptor(1, 1), "abc");
		handle.claimInterface(2);
		handle.altSetting(2, 1);
		handle.releaseInterface(2);
		handle.clearHalt(0x01);
		handle.resetDevice();
	}

	@Test
	public void shouldExecuteSyncControlTransfer() throws LibUsbException {
		handle.controlTransfer(0x01, 0x11, 0x22, 3, Array.bytes.of(1, 2, 3), 100);
		handle.controlTransfer(0x01, 0x11, 0x22, 3, JnaTesting.buffer(1, 2, 3), 100);
		lib.transferOut.assertValues( //
			List.of(0x01, 0x11, 0x22, 3, ByteProvider.of(1, 2, 3)),
			List.of(0x01, 0x11, 0x22, 3, ByteProvider.of(1, 2, 3)));
		handle.controlTransfer(0x81, 0x11, 0x22, 3, 100);
		lib.transferIn.autoResponses(ByteProvider.of(5, 6));
		Assert.array(handle.controlTransfer(0x81, 0x11, 0x22, 3, 3, 100), 5, 6);
		var buffer = JnaTesting.buffer(0, 0, 0);
		Assert.equal(handle.controlTransfer(0x81, 0x11, 0x22, 3, buffer, 100), 2);
		Assert.buffer(buffer.flip(), 5, 6, 0);
		lib.transferIn.assertValues( //
			List.of(0x81, 0x11, 0x22, 3, 0), //
			List.of(0x81, 0x11, 0x22, 3, 3), //
			List.of(0x81, 0x11, 0x22, 3, 3));
	}

	@Test
	public void shouldExecuteSyncBulkTransfer() throws LibUsbException {
		handle.bulkTransfer(0x01, Array.bytes.of(1, 2, 3), 100);
		handle.bulkTransfer(0x01, JnaTesting.buffer(1, 2, 3), 100);
		lib.transferOut.assertValues( //
			List.of(0x01, ByteProvider.of(1, 2, 3)), //
			List.of(0x01, ByteProvider.of(1, 2, 3)));
		lib.transferIn.autoResponses(ByteProvider.of(5, 6));
		Assert.array(handle.bulkTransfer(0x81, 3, 100), 5, 6);
		var buffer = JnaTesting.buffer(0, 0, 0);
		Assert.equal(handle.bulkTransfer(0x81, buffer, 100), 2);
		Assert.buffer(buffer.flip(), 5, 6, 0);
		lib.transferIn.assertValues( //
			List.of(0x81, 3), //
			List.of(0x81, 3));
	}

	@Test
	public void shouldExecuteSyncInterruptTransfer() throws LibUsbException {
		handle.interruptTransfer(0x01, Array.bytes.of(1, 2, 3), 100);
		handle.interruptTransfer(0x01, JnaTesting.buffer(1, 2, 3), 100);
		lib.transferOut.assertValues( //
			List.of(0x01, ByteProvider.of(1, 2, 3)), //
			List.of(0x01, ByteProvider.of(1, 2, 3)));
		lib.transferIn.autoResponses(ByteProvider.of(5, 6));
		Assert.array(handle.interruptTransfer(0x81, 3, 100), 5, 6);
		var buffer = JnaTesting.buffer(0, 0, 0);
		Assert.equal(handle.interruptTransfer(0x81, buffer, 100), 2);
		Assert.buffer(buffer.flip(), 5, 6, 0);
		lib.transferIn.assertValues( //
			List.of(0x81, 3), //
			List.of(0x81, 3));
	}
}
