package ceri.serial.libusb;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.function.Enclosure;
import ceri.common.test.Assert;
import ceri.serial.libusb.jna.LibUsb.libusb_class_code;
import ceri.serial.libusb.jna.LibUsb.libusb_config_attributes;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction;
import ceri.serial.libusb.jna.LibUsb.libusb_iso_sync_type;
import ceri.serial.libusb.jna.LibUsb.libusb_iso_usage_type;
import ceri.serial.libusb.jna.LibUsb.libusb_supported_speed;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_type;
import ceri.serial.libusb.jna.LibUsb.libusb_usb_2_0_extension_attributes;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.TestLibUsbNative;

public class UsbDescriptorsBehavior {
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;
	private Usb usb;

	@Before
	public void before() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		usb = Usb.of();
	}

	@After
	public void after() {
		usb.close();
		enc.close();
	}

	@Test
	public void shouldProvideDeviceDescriptor() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.externalUsb2HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open()) {
			var desc = device.descriptor();
			Assert.equal(desc.usbVersion(), 0x210);
			Assert.equal(desc.classCode(), libusb_class_code.LIBUSB_CLASS_HUB);
			Assert.equal(desc.subClass(), 0);
			Assert.equal(desc.protocol(), 0x02);
			Assert.equal(desc.maxPacketSize0(), 64);
			Assert.equal(desc.vendorId(), 0x5e3);
			Assert.equal(desc.deviceVersion(), 0x9223);
			Assert.equal(desc.manufacturer(handle), "GenesysLogic");
			Assert.equal(desc.product(handle), "USB2.0 Hub");
			Assert.equal(desc.serialNumber(handle), null);
			Assert.equal(desc.configurationCount(), 1);
		}
	}

	@Test
	public void shouldProvideConfigDescriptor() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.sdReaderConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var config = device.activeConfig()) {
			Assert.equal(config.value(), 1);
			Assert.equal(config.description(handle), null);
			Assert.unordered(config.attributes(), libusb_config_attributes.LIBUSB_CA_REMOTE_WAKEUP);
			Assert.equal(config.maxPower(), 0x70);
			Assert.equal(config.extra(), ByteProvider.empty());
			config.close();
			Assert.thrown(() -> config.value());
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideInterfaceDescriptor() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.kbConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var config = device.activeConfig()) {
			Assert.equal(handle.bosDescriptor(), null);
			Assert.equal(config.interfaceCount(), 3);
			Assert.equal(config.interfaces().size(), 3);
			var iface = config.interfaces().get(1);
			Assert.equal(iface.altSettingCount(), 1);
			Assert.equal(iface.altSettings().size(), 1);
			var alt = iface.altSettings().get(0);
			Assert.equal(alt.number(), 1);
			Assert.equal(alt.description(handle), "Touchpad");
			Assert.equal(alt.altSetting(), 0);
			Assert.equal(alt.classCode(), libusb_class_code.LIBUSB_CLASS_HID);
			Assert.equal(alt.subClass(), 0);
			Assert.equal(alt.protocol(), 0);
			Assert.array(alt.extra(), 0x09, 0x21, 0x11, 0x01, 0x00, 0x01, 0x22, 0x1b, 0x00);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideEndPointDescriptor() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.audioConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var config = device.activeConfig()) {
			var alt = config.interfaces().get(2).altSettings().get(1);
			Assert.equal(alt.endPointCount(), 1);
			Assert.equal(alt.endPoints().size(), 1);
			var ep = alt.endPoints().get(0);
			Assert.equal(ep.ssEndPointCompanion(), null);
			Assert.equal(ep.address(), 0x82);
			Assert.equal(ep.number(), 0x2);
			Assert.equal(ep.direction(), libusb_endpoint_direction.LIBUSB_ENDPOINT_IN);
			Assert.equal(ep.attributes(), 0x0d);
			Assert.equal(ep.transferType(), libusb_transfer_type.LIBUSB_TRANSFER_TYPE_ISOCHRONOUS);
			Assert.equal(ep.isoSyncType(), libusb_iso_sync_type.LIBUSB_ISO_SYNC_TYPE_SYNC);
			Assert.equal(ep.isoUsageType(), libusb_iso_usage_type.LIBUSB_ISO_USAGE_TYPE_DATA);
			Assert.equal(ep.maxPacketSize(), 100);
			Assert.equal(ep.pollInterval(), 1);
			Assert.equal(ep.audioRefreshRate(), 0);
			Assert.equal(ep.audioSyncAddress(), 0);
			Assert.array(ep.extra(), 0x7, 0x25, 0x1, 0x1, 0x0, 0x0, 0x0);
		}
	}

	@Test
	public void shouldProvideSsEndPointDescriptor() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.externalUsb3HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var config = device.activeConfig(); var ss = config.interfaces().get(0).altSettings()
				.get(0).endPoints().get(0).ssEndPointCompanion()) {
			Assert.equal(ss.maxBurstPackets(), 0);
			Assert.equal(ss.attributes(), 0);
			Assert.equal(ss.maxBulkStreams(), 0);
			Assert.equal(ss.isoMult(), 0);
			Assert.equal(ss.bytesPerInterval(), 2);
			ss.close();
			Assert.thrown(() -> ss.bytesPerInterval());
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideBosDescriptor() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.externalUsb3HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var bos = handle.bosDescriptor()) {
			Assert.equal(bos.capabilityCount(), 3);
			Assert.array(bos.capabilities().get(1).capabilityData(), 0x0, 0xe, 0x0, 0x1, 0x8, 0xbe,
				0x0);
			Assert.equal(bos.capabilities().get(1).usb20Extension(), null);
			Assert.equal(bos.capabilities().get(2).ssUsbDeviceCapability(), null);
			Assert.equal(bos.capabilities().get(0).containerId(), null);
			bos.close();
			Assert.thrown(() -> bos.capabilityCount());
		}
	}

	@Test
	public void shouldProvideBosUsb2ExtDescriptor() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.externalUsb3HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var bos = handle.bosDescriptor();
			var usb2Ext = bos.capabilities().get(0).usb20Extension()) {
			Assert.unordered(usb2Ext.attributes(),
				libusb_usb_2_0_extension_attributes.LIBUSB_BM_LPM_SUPPORT);
			usb2Ext.close();
			Assert.thrown(() -> usb2Ext.attributes());
			bos.close();
			Assert.thrown(() -> bos.capabilityCount());
		}
	}

	@Test
	public void shouldProvideBosSsDescriptor() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.externalUsb3HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var bos = handle.bosDescriptor();
			var ssUsb = bos.capabilities().get(1).ssUsbDeviceCapability()) {
			Assert.unordered(ssUsb.attributes());
			Assert.unordered(ssUsb.supportedSpeeds(),
				libusb_supported_speed.LIBUSB_FULL_SPEED_OPERATION,
				libusb_supported_speed.LIBUSB_HIGH_SPEED_OPERATION,
				libusb_supported_speed.LIBUSB_SUPER_SPEED_OPERATION);
			Assert.equal(ssUsb.functionalitySupport(), 1);
			Assert.equal(ssUsb.u1DeviceExitLatency(), 0x08);
			Assert.equal(ssUsb.u2DeviceExitLatency(), 0xbe);
			ssUsb.close();
			Assert.thrown(() -> ssUsb.attributes());
		}
	}

	@Test
	public void shouldProvideBosContainerDescriptor() throws LibUsbException {
		lib.data.addConfig(LibUsbSampleData.externalUsb3HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var bos = handle.bosDescriptor();
			var containerId = bos.capabilities().get(2).containerId()) {
			Assert.array(containerId.uuid(), 0x96, 0xd6, 0x67, 0xd6, 0x5, 0x44, 0x42, 0xa5, 0x9f,
				0x29, 0xf4, 0x85, 0xe5, 0x26, 0xbb, 0x58);
			containerId.close();
			Assert.thrown(() -> containerId.uuid());
		}
	}

}
