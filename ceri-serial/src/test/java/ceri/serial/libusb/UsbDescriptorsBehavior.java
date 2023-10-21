package ceri.serial.libusb;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.util.Enclosed;
import ceri.serial.libusb.jna.LibUsb.libusb_class_code;
import ceri.serial.libusb.jna.LibUsb.libusb_config_attributes;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction;
import ceri.serial.libusb.jna.LibUsb.libusb_iso_sync_type;
import ceri.serial.libusb.jna.LibUsb.libusb_iso_usage_type;
import ceri.serial.libusb.jna.LibUsb.libusb_supported_speed;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_type;
import ceri.serial.libusb.jna.LibUsb.libusb_usb_2_0_extension_attributes;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.TestLibUsbNative;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbDescriptorsBehavior {
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;
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
		lib.data.deviceConfigs.add(LibUsbSampleData.externalUsb2HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open()) {
			var desc = device.descriptor();
			assertEquals(desc.usbVersion(), 0x210);
			assertEquals(desc.classCode(), libusb_class_code.LIBUSB_CLASS_HUB);
			assertEquals(desc.subClass(), 0);
			assertEquals(desc.protocol(), 0x02);
			assertEquals(desc.maxPacketSize0(), 64);
			assertEquals(desc.vendorId(), 0x5e3);
			assertEquals(desc.deviceVersion(), 0x9223);
			assertEquals(desc.manufacturer(handle), "GenesysLogic");
			assertEquals(desc.product(handle), "USB2.0 Hub");
			assertEquals(desc.serialNumber(handle), null);
			assertEquals(desc.configurationCount(), 1);
		}
	}

	@Test
	public void shouldProvideConfigDescriptor() throws LibUsbException {
		lib.data.deviceConfigs.add(LibUsbSampleData.sdReaderConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var config = device.activeConfig()) {
			assertEquals(config.value(), 1);
			assertEquals(config.description(handle), null);
			assertCollection(config.attributes(), libusb_config_attributes.LIBUSB_CA_REMOTE_WAKEUP);
			assertEquals(config.maxPower(), 0x70);
			assertEquals(config.extra(), ByteProvider.empty());
			config.close();
			assertThrown(() -> config.value());
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideInterfaceDescriptor() throws LibUsbException {
		lib.data.deviceConfigs.add(LibUsbSampleData.kbConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var config = device.activeConfig()) {
			assertEquals(handle.bosDescriptor(), null);
			assertEquals(config.interfaceCount(), 3);
			assertEquals(config.interfaces().size(), 3);
			var iface = config.interfaces().get(1);
			assertEquals(iface.altSettingCount(), 1);
			assertEquals(iface.altSettings().size(), 1);
			var alt = iface.altSettings().get(0);
			assertEquals(alt.number(), 1);
			assertEquals(alt.description(handle), "Touchpad");
			assertEquals(alt.altSetting(), 0);
			assertEquals(alt.classCode(), libusb_class_code.LIBUSB_CLASS_HID);
			assertEquals(alt.subClass(), 0);
			assertEquals(alt.protocol(), 0);
			assertArray(alt.extra(), 0x09, 0x21, 0x11, 0x01, 0x00, 0x01, 0x22, 0x1b, 0x00);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideEndPointDescriptor() throws LibUsbException {
		lib.data.deviceConfigs.add(LibUsbSampleData.audioConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var config = device.activeConfig()) {
			var alt = config.interfaces().get(2).altSettings().get(1);
			assertEquals(alt.endPointCount(), 1);
			assertEquals(alt.endPoints().size(), 1);
			var ep = alt.endPoints().get(0);
			assertEquals(ep.ssEndPointCompanion(), null);
			assertEquals(ep.address(), 0x82);
			assertEquals(ep.number(), 0x2);
			assertEquals(ep.direction(), libusb_endpoint_direction.LIBUSB_ENDPOINT_IN);
			assertEquals(ep.attributes(), 0x0d);
			assertEquals(ep.transferType(), libusb_transfer_type.LIBUSB_TRANSFER_TYPE_ISOCHRONOUS);
			assertEquals(ep.isoSyncType(), libusb_iso_sync_type.LIBUSB_ISO_SYNC_TYPE_SYNC);
			assertEquals(ep.isoUsageType(), libusb_iso_usage_type.LIBUSB_ISO_USAGE_TYPE_DATA);
			assertEquals(ep.maxPacketSize(), 100);
			assertEquals(ep.pollInterval(), 1);
			assertEquals(ep.audioRefreshRate(), 0);
			assertEquals(ep.audioSyncAddress(), 0);
			assertArray(ep.extra(), 0x7, 0x25, 0x1, 0x1, 0x0, 0x0, 0x0);
		}
	}

	@Test
	public void shouldProvideSsEndPointDescriptor() throws LibUsbException {
		lib.data.deviceConfigs.add(LibUsbSampleData.externalUsb3HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var config = device.activeConfig(); var ss = config.interfaces().get(0).altSettings()
				.get(0).endPoints().get(0).ssEndPointCompanion()) {
			assertEquals(ss.maxBurstPackets(), 0);
			assertEquals(ss.attributes(), 0);
			assertEquals(ss.maxBulkStreams(), 0);
			assertEquals(ss.isoMult(), 0);
			assertEquals(ss.bytesPerInterval(), 2);
			ss.close();
			assertThrown(() -> ss.bytesPerInterval());
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideBosDescriptor() throws LibUsbException {
		lib.data.deviceConfigs.add(LibUsbSampleData.externalUsb3HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var bos = handle.bosDescriptor()) {
			assertEquals(bos.capabilityCount(), 3);
			assertArray(bos.capabilities().get(1).capabilityData(), 0x0, 0xe, 0x0, 0x1, 0x8, 0xbe,
				0x0);
			assertEquals(bos.capabilities().get(1).usb20Extension(), null);
			assertEquals(bos.capabilities().get(2).ssUsbDeviceCapability(), null);
			assertEquals(bos.capabilities().get(0).containerId(), null);
			bos.close();
			assertThrown(() -> bos.capabilityCount());
		}
	}

	@Test
	public void shouldProvideBosUsb2ExtDescriptor() throws LibUsbException {
		lib.data.deviceConfigs.add(LibUsbSampleData.externalUsb3HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var bos = handle.bosDescriptor();
			var usb2Ext = bos.capabilities().get(0).usb20Extension()) {
			assertCollection(usb2Ext.attributes(),
				libusb_usb_2_0_extension_attributes.LIBUSB_BM_LPM_SUPPORT);
			usb2Ext.close();
			assertThrown(() -> usb2Ext.attributes());
			bos.close();
			assertThrown(() -> bos.capabilityCount());
		}
	}

	@Test
	public void shouldProvideBosSsDescriptor() throws LibUsbException {
		lib.data.deviceConfigs.add(LibUsbSampleData.externalUsb3HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var bos = handle.bosDescriptor();
			var ssUsb = bos.capabilities().get(1).ssUsbDeviceCapability()) {
			assertCollection(ssUsb.attributes());
			assertCollection(ssUsb.supportedSpeeds(),
				libusb_supported_speed.LIBUSB_FULL_SPEED_OPERATION,
				libusb_supported_speed.LIBUSB_HIGH_SPEED_OPERATION,
				libusb_supported_speed.LIBUSB_SUPER_SPEED_OPERATION);
			assertEquals(ssUsb.functionalitySupport(), 1);
			assertEquals(ssUsb.u1DeviceExitLatency(), 0x08);
			assertEquals(ssUsb.u2DeviceExitLatency(), 0xbe);
			ssUsb.close();
			assertThrown(() -> ssUsb.attributes());
		}
	}

	@Test
	public void shouldProvideBosContainerDescriptor() throws LibUsbException {
		lib.data.deviceConfigs.add(LibUsbSampleData.externalUsb3HubConfig());
		try (var devices = usb.deviceList(); var device = devices.devices().get(0);
			var handle = device.open(); var bos = handle.bosDescriptor();
			var containerId = bos.capabilities().get(2).containerId()) {
			assertArray(containerId.uuid(), 0x96, 0xd6, 0x67, 0xd6, 0x5, 0x44, 0x42, 0xa5, 0x9f,
				0x29, 0xf4, 0x85, 0xe5, 0x26, 0xbb, 0x58);
			containerId.close();
			assertThrown(() -> containerId.uuid());
		}
	}

}
