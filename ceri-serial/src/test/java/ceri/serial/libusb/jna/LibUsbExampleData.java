package ceri.serial.libusb.jna;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_HID;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_HUB;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_PER_INTERFACE;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_VENDOR_SPEC;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_CONFIG;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_ENDPOINT;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_INTERFACE;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_FULL;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_LOW;
import static ceri.serial.libusb.jna.LibUsbTestUtil.configDescriptors;
import static ceri.serial.libusb.jna.LibUsbTestUtil.endpointDescriptors;
import static ceri.serial.libusb.jna.LibUsbTestUtil.extra;
import static ceri.serial.libusb.jna.LibUsbTestUtil.interfaceDescriptors;
import static ceri.serial.libusb.jna.LibUsbTestUtil.interfaces;
import java.util.List;
import ceri.serial.libusb.jna.LibUsbTestData.DeviceConfig;

public class LibUsbExampleData {

	public static void populate(LibUsbTestData data) {
		data.deviceConfigs.clear();
		configs().forEach(data.deviceConfigs::add);
	}

	public static final List<DeviceConfig> configs() {
		return List.of(ftdiConfig(), mouseConfig(), hubConfig(), kbConfig());
	}

	public static DeviceConfig ftdiConfig() {
		return new DeviceConfig().apply(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(2);
			dc.address = 0x0d;
			dc.speed = LIBUSB_SPEED_FULL.value;
			dc.configuration = 1;
			dc.desc.bLength = 18;
			dc.desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE.value;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_PER_INTERFACE.value;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x0403;
			dc.desc.idProduct = 0x6001;
			dc.desc.bcdDevice = 0x0600;
			dc.desc.iManufacturer = 1;
			dc.desc.iProduct = 2;
			dc.desc.iSerialNumber = 3;
			dc.desc.bNumConfigurations = 1;
			dc.descriptorStrings = List.of("FTDI", "FT245R USB FIFO", "A7047D8V");
			dc.configDescriptors = configDescriptors(cd -> {
				cd.bLength = 9;
				cd.bDescriptorType = (byte) LIBUSB_DT_CONFIG.value;
				cd.wTotalLength = 32;
				cd.bNumInterfaces = 1;
				cd.bConfigurationValue = 1;
				cd.bmAttributes = (byte) 0xa0;
				cd.MaxPower = 45;
				cd.interfaces = interfaces(i -> {
					i.num_altsetting = 1;
					i.altsetting = interfaceDescriptors(id -> {
						id.bLength = 9;
						id.bDescriptorType = (byte) LIBUSB_DT_INTERFACE.value;
						id.bNumEndpoints = 2;
						id.bInterfaceClass = (byte) LIBUSB_CLASS_VENDOR_SPEC.value;
						id.bInterfaceSubClass = (byte) 0xff;
						id.bInterfaceProtocol = (byte) 0xff;
						id.iInterface = 2;
						id.endpoint = endpointDescriptors(ed -> {
							ed.bLength = 7;
							ed.bDescriptorType = (byte) LIBUSB_DT_ENDPOINT.value;
							ed.bEndpointAddress = (byte) 0x81;
							ed.bmAttributes = 0x02;
							ed.wMaxPacketSize = 64;
						}, ed -> {
							ed.bLength = 7;
							ed.bDescriptorType = (byte) LIBUSB_DT_ENDPOINT.value;
							ed.bEndpointAddress = 0x02;
							ed.bmAttributes = 0x02;
							ed.wMaxPacketSize = 64;
						});
					});
				});
			});
		});
	}

	public static DeviceConfig mouseConfig() {
		return new DeviceConfig().apply(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(0x01);
			dc.address = 0x0b;
			dc.speed = LIBUSB_SPEED_LOW.value;
			dc.configuration = 1;
			dc.desc.bLength = 18;
			dc.desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE.value;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_PER_INTERFACE.value;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x04f2;
			dc.desc.idProduct = 0x0939;
			dc.desc.bcdDevice = 0x0100;
			dc.desc.iManufacturer = 1;
			dc.desc.iProduct = 2;
			dc.desc.bNumConfigurations = 1;
			dc.descriptorStrings = List.of("PixArt", "USB Optical Mouse");
			dc.configDescriptors = configDescriptors(cd -> {
				cd.bLength = 9;
				cd.bDescriptorType = (byte) LIBUSB_DT_CONFIG.value;
				cd.wTotalLength = 34;
				cd.bNumInterfaces = 1;
				cd.bConfigurationValue = 1;
				cd.bmAttributes = (byte) 0xa0;
				cd.MaxPower = 50;
				cd.interfaces = interfaces(i -> {
					i.num_altsetting = 1;
					i.altsetting = interfaceDescriptors(id -> {
						id.bLength = 9;
						id.bDescriptorType = (byte) LIBUSB_DT_INTERFACE.value;
						id.bNumEndpoints = 1;
						id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
						id.bInterfaceSubClass = 1;
						id.bInterfaceProtocol = 2;
						extra(id, 0x09, 0x21, 0x11, 0x01, 0x00, 0x01, 0x22, 0x2e, 0x00);
						id.endpoint = endpointDescriptors(ed -> {
							ed.bLength = 7;
							ed.bDescriptorType = (byte) LIBUSB_DT_ENDPOINT.value;
							ed.bEndpointAddress = (byte) 0x81;
							ed.bmAttributes = 0x03;
							ed.wMaxPacketSize = 4;
							ed.bInterval = 10;
						});
					});
				});
			});
		});
	}

	public static DeviceConfig hubConfig() {
		return new DeviceConfig().apply(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(3);
			dc.address = 0x04;
			dc.speed = LIBUSB_SPEED_FULL.value;
			dc.configuration = 1;
			dc.desc.bLength = 18;
			dc.desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE.value;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_HUB.value;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x0a5c;
			dc.desc.idProduct = 0x4500;
			dc.desc.bcdDevice = 0x0100;
			dc.desc.iManufacturer = 1;
			dc.desc.iProduct = 2;
			dc.desc.bNumConfigurations = 1;
			dc.descriptorStrings = List.of("Apple Inc.", "BRCM20702 Hub");
			dc.configDescriptors = configDescriptors(cd -> {
				cd.bLength = 9;
				cd.bDescriptorType = (byte) LIBUSB_DT_CONFIG.value;
				cd.wTotalLength = 25;
				cd.bNumInterfaces = 1;
				cd.bConfigurationValue = 1;
				cd.bmAttributes = (byte) 0xe0;
				cd.MaxPower = 47;
				cd.interfaces = interfaces(i -> {
					i.num_altsetting = 1;
					i.altsetting = interfaceDescriptors(id -> {
						id.bLength = 9;
						id.bDescriptorType = (byte) LIBUSB_DT_INTERFACE.value;
						id.bNumEndpoints = 1;
						id.bInterfaceClass = (byte) LIBUSB_CLASS_HUB.value;
						id.endpoint = endpointDescriptors(ed -> {
							ed.bLength = 7;
							ed.bDescriptorType = (byte) LIBUSB_DT_ENDPOINT.value;
							ed.bEndpointAddress = (byte) 0x81;
							ed.bmAttributes = 0x03;
							ed.wMaxPacketSize = 1;
							ed.bInterval = -1;
						});
					});
				});
			});
		});
	}

	public static DeviceConfig kbConfig() {
		return new DeviceConfig().apply(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(4);
			dc.address = 0x02;
			dc.speed = LIBUSB_SPEED_FULL.value;
			dc.configuration = 1;
			dc.desc.bLength = 18;
			dc.desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE.value;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_PER_INTERFACE.value;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x05ac;
			dc.desc.idProduct = 0x0259;
			dc.desc.bcdDevice = 0x0224;
			dc.desc.iManufacturer = 1;
			dc.desc.iProduct = 2;
			dc.desc.bNumConfigurations = 1;
			dc.descriptorStrings = List.of("Apple Inc.", "Apple Internal Keyboard / Trackpad",
				"Apple Internal Keyboard", "Touchpad");
			dc.configDescriptors = configDescriptors(cd -> {
				cd.bLength = 9;
				cd.bDescriptorType = (byte) LIBUSB_DT_CONFIG.value;
				cd.wTotalLength = 84;
				cd.bNumInterfaces = 3;
				cd.bConfigurationValue = 1;
				cd.bmAttributes = (byte) 0xa0;
				cd.MaxPower = 20;
				cd.interfaces = interfaces(i -> {
					i.num_altsetting = 1;
					i.altsetting = interfaceDescriptors(id -> {
						id.bLength = 9;
						id.bDescriptorType = (byte) LIBUSB_DT_INTERFACE.value;
						id.bNumEndpoints = 1;
						id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
						id.bInterfaceSubClass = 1;
						id.bInterfaceProtocol = 1;
						id.iInterface = 3;
						extra(id, 0x09, 0x21, 0x11, 0x01, 0x21, 0x01, 0x22, 0x9c, 0x00);
						id.endpoint = endpointDescriptors(ed -> {
							ed.bLength = 7;
							ed.bDescriptorType = (byte) LIBUSB_DT_ENDPOINT.value;
							ed.bEndpointAddress = (byte) 0x83;
							ed.bmAttributes = 0x03;
							ed.wMaxPacketSize = 10;
							ed.bInterval = 8;
						});
					});
				}, i -> {
					i.num_altsetting = 1;
					i.altsetting = interfaceDescriptors(id -> {
						id.bLength = 9;
						id.bDescriptorType = (byte) LIBUSB_DT_INTERFACE.value;
						id.bInterfaceNumber = 1;
						id.bNumEndpoints = 1;
						id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
						id.iInterface = 4;
						extra(id, 0x09, 0x21, 0x11, 0x01, 0x00, 0x01, 0x22, 0x1b, 0x00);
						id.endpoint = endpointDescriptors(ed -> {
							ed.bLength = 7;
							ed.bDescriptorType = (byte) LIBUSB_DT_ENDPOINT.value;
							ed.bEndpointAddress = (byte) 0x81;
							ed.bmAttributes = 0x03;
							ed.wMaxPacketSize = 64;
							ed.bInterval = 2;
						});
					});
				}, i -> {
					i.num_altsetting = 1;
					i.altsetting = interfaceDescriptors(id -> {
						id.bLength = 9;
						id.bDescriptorType = (byte) LIBUSB_DT_INTERFACE.value;
						id.bInterfaceNumber = 2;
						id.bNumEndpoints = 1;
						id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
						id.bInterfaceSubClass = 1;
						id.bInterfaceProtocol = 2;
						id.iInterface = 4;
						extra(id, 0x09, 0x21, 0x11, 0x01, 0x00, 0x01, 0x22, 0x34, 0x00);
						id.endpoint = endpointDescriptors(ed -> {
							ed.bLength = 7;
							ed.bDescriptorType = (byte) LIBUSB_DT_ENDPOINT.value;
							ed.bEndpointAddress = (byte) 0x84;
							ed.bmAttributes = 0x03;
							ed.wMaxPacketSize = 8;
							ed.bInterval = 8;
						});
					});
				});
			});
		});
	}

}
