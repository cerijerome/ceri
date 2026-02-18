package ceri.serial.libusb.test;

import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_ENDPOINT_AUDIO_SIZE;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_AUDIO;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_HID;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_HUB;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_MASS_STORAGE;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_PER_INTERFACE;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_VENDOR_SPEC;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_FULL;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_HIGH;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_LOW;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_SUPER;
import static ceri.serial.libusb.test.LibUsbTestData.Util.device;
import ceri.common.array.Array;
import ceri.serial.libusb.test.LibUsbTestData.DeviceConfig;
import ceri.serial.libusb.test.LibUsbTestData.Util;

/**
 * Sample device data for TestLibUsbNative.
 */
public class LibUsbSampleData {

	private LibUsbSampleData() {}

	/**
	 * Clears, creates and adds all device configurations to the test data.
	 */
	public static void populate(LibUsbTestData data) {
		data.clearConfig();
		data.addConfig(sdReaderConfig(), audioConfig(), ftdiConfig(), externalUsb3HubConfig(),
			externalUsb2HubConfig(), mouseConfig(), internalHubConfig(), kbConfig());
	}

	/**
	 * Sample data for SD card reader.
	 */
	public static DeviceConfig sdReaderConfig() {
		return device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = Array.BYTE.of(0x07);
			dc.address = 0x0b;
			dc.speed = LIBUSB_SPEED_SUPER;
			dc.desc.bcdUSB = 0x0300;
			dc.desc.bMaxPacketSize0 = 9;
			dc.desc.idVendor = 0x05ac;
			dc.desc.idProduct = (short) 0x8406;
			dc.desc.bcdDevice = 0x0820;
			dc.desc.iManufacturer = Util.string(dc, 3, "Apple");
			dc.desc.iProduct = Util.string(dc, 4, "Internal Memory Card Reader");
			dc.desc.iSerialNumber = Util.string(dc, 5, "000000000820");
			Util.configDescriptors(dc, cd -> {
				cd.wTotalLength = 44;
				cd.bmAttributes = (byte) 0xa0;
				cd.bMaxPower = 112;
				Util.interfaces(cd, i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_MASS_STORAGE.value;
					id.bInterfaceSubClass = 0x06;
					id.bInterfaceProtocol = 0x50;
					Util.endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x81;
						ed.bmAttributes = 0x02;
						ed.wMaxPacketSize = 1024;
						Util.extra(ed, 0x6, 0x30, 0x4, 0x0, 0x0, 0x0);
						Util.ssEndPointCompanionDesc(dc, ed, ss -> {
							ss.bMaxBurst = 4;
						});
					}, ed -> {
						ed.bEndpointAddress = 0x02;
						ed.bmAttributes = 0x02;
						ed.wMaxPacketSize = 1024;
						Util.extra(ed, 0x6, 0x30, 0x4, 0x0, 0x0, 0x0);
						Util.ssEndPointCompanionDesc(dc, ed, ss -> {
							ss.bMaxBurst = 4;
						});
					});
				}));
			});
			Util.bosDescriptor(dc, bos -> {
				bos.wTotalLength = 22;
			}, Util.bosUsb20Ext(bdc -> {
				bdc.bmAttributes = 0x2;
			}), Util.bosSsUsbDevCap(bdc -> {
				bdc.bmAttributes = 0x00000002;
				bdc.wSpeedSupported = 0x000e;
				bdc.bFunctionalitySupport = 0x01;
				bdc.bU1DevExitLat = 0x0a;
				bdc.wU2DevExitLat = 0x07ff;
			}));
		});
	}

	/**
	 * Sample data for USB audio jack adapter.
	 */
	public static DeviceConfig audioConfig() {
		return Util.device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = Array.BYTE.of(0x02, 0x02);
			dc.address = 0x0a;
			dc.speed = LIBUSB_SPEED_FULL;
			dc.desc.bcdUSB = 0x0110;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x0d8c;
			dc.desc.idProduct = 0x0014;
			dc.desc.bcdDevice = 0x0100;
			dc.desc.iManufacturer = Util.string(dc, 1, "C-Media Electronics Inc.\0\0\0\0\0\0");
			dc.desc.iProduct = Util.string(dc, 2, "USB Audio Device\0\0\0\0\0\0\0\0\0\0\0\0\0\0");
			Util.configDescriptors(dc, cd -> {
				cd.wTotalLength = 253;
				cd.bmAttributes = (byte) 0x80;
				cd.bMaxPower = 50;
				Util.interfaces(cd, i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_AUDIO.value;
					id.bInterfaceSubClass = 0x01;
					Util.extra(id, 0xa, 0x24, 0x1, 0x0, 0x1, 0x64, 0x0, 0x2, 0x1, 0x2, 0xc, 0x24,
						0x2, 0x1, 0x1, 0x1, 0x0, 0x2, 0x3, 0x0, 0x0, 0x0, 0xc, 0x24, 0x2, 0x2, 0x1,
						0x2, 0x0, 0x1, 0x1, 0x0, 0x0, 0x0, 0x9, 0x24, 0x3, 0x6, 0x1, 0x3, 0x0, 0x9,
						0x0, 0x9, 0x24, 0x3, 0x7, 0x1, 0x1, 0x0, 0x8, 0x0, 0x7, 0x24, 0x5, 0x8, 0x1,
						0xa, 0x0, 0xa, 0x24, 0x6, 0x9, 0xf, 0x1, 0x1, 0x2, 0x2, 0x0, 0x9, 0x24, 0x6,
						0xa, 0x2, 0x1, 0x43, 0x0, 0x0, 0x9, 0x24, 0x6, 0xd, 0x2, 0x1, 0x3, 0x0, 0x0,
						0xd, 0x24, 0x4, 0xf, 0x2, 0x1, 0xd, 0x2, 0x3, 0x0, 0x0, 0x0, 0x0);
				}), i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.1
					id.bInterfaceClass = (byte) LIBUSB_CLASS_AUDIO.value;
					id.bInterfaceSubClass = 0x02;
				}, id -> {
					id.bInterfaceClass = (byte) LIBUSB_CLASS_AUDIO.value;
					id.bInterfaceSubClass = 0x02;
					Util.extra(id, 0x7, 0x24, 0x1, 0x1, 0x1, 0x1, 0x0, 0xe, 0x24, 0x2, 0x1, 0x2,
						0x2, 0x10, 0x2, 0x80, 0xbb, 0x0, 0x44, 0xac, 0x0);
					Util.endPointDescriptors(id, ed -> {
						ed.bLength = LIBUSB_DT_ENDPOINT_AUDIO_SIZE;
						ed.bEndpointAddress = 0x01;
						ed.bmAttributes = 0x09;
						ed.wMaxPacketSize = 200;
						ed.bInterval = 1;
						Util.extra(ed, 0x7, 0x25, 0x1, 0x1, 0x1, 0x1, 0x0);
					});
				}), i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.2
					id.bInterfaceClass = (byte) LIBUSB_CLASS_AUDIO.value;
					id.bInterfaceSubClass = 0x02;
				}, id -> {
					id.bInterfaceClass = (byte) LIBUSB_CLASS_AUDIO.value;
					id.bInterfaceSubClass = 0x02;
					Util.extra(id, 0x7, 0x24, 0x1, 0x7, 0x1, 0x1, 0x0, 0xe, 0x24, 0x2, 0x1, 0x1,
						0x2, 0x10, 0x2, 0x80, 0xbb, 0x0, 0x44, 0xac, 0x0);
					Util.endPointDescriptors(id, ed -> {
						ed.bLength = LIBUSB_DT_ENDPOINT_AUDIO_SIZE;
						ed.bEndpointAddress = (byte) 0x82;
						ed.bmAttributes = 0x0d;
						ed.wMaxPacketSize = 100;
						ed.bInterval = 1;
						Util.extra(ed, 0x7, 0x25, 0x1, 0x1, 0x0, 0x0, 0x0);
					});
				}), i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.3
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
					Util.extra(id, 0x9, 0x21, 0x0, 0x1, 0x0, 0x1, 0x22, 0x3c, 0x0);
					Util.endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x87;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 4;
						ed.bInterval = 2;
					});
				}));
			});
		});
	}

	/**
	 * Sample data for FTDI device.
	 */
	public static DeviceConfig ftdiConfig() {
		return Util.device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = Array.BYTE.of(0x02, 0x04);
			dc.address = 0x05;
			dc.speed = LIBUSB_SPEED_FULL;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x0403;
			dc.desc.idProduct = 0x6001;
			dc.desc.bcdDevice = 0x0600;
			dc.desc.iManufacturer = Util.string(dc, 1, "FTDI");
			dc.desc.iProduct = Util.string(dc, 2, "FT245R USB FIFO");
			dc.desc.iSerialNumber = Util.string(dc, 3, "A7047D8V");
			Util.configDescriptors(dc, cd -> {
				cd.wTotalLength = 32;
				cd.bmAttributes = (byte) 0xa0;
				cd.bMaxPower = 45;
				Util.interfaces(cd, i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_VENDOR_SPEC.value;
					id.bInterfaceSubClass = (byte) 0xff;
					id.bInterfaceProtocol = (byte) 0xff;
					id.iInterface = 2;
					Util.endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x81;
						ed.bmAttributes = 0x02;
						ed.wMaxPacketSize = 64;
					}, ed -> {
						ed.bEndpointAddress = 0x02;
						ed.bmAttributes = 0x02;
						ed.wMaxPacketSize = 64;
					});
				}));
			});
		});
	}

	/**
	 * Sample data for external USB hub.
	 */
	public static DeviceConfig externalUsb3HubConfig() {
		return Util.device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = Array.BYTE.of(0x06);
			dc.address = 0x09;
			dc.speed = LIBUSB_SPEED_SUPER;
			dc.desc.bcdUSB = 0x0300;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_HUB.value;
			dc.desc.bDeviceProtocol = 0x03;
			dc.desc.bMaxPacketSize0 = 9;
			dc.desc.idVendor = 0x05e3;
			dc.desc.idProduct = 0x0616;
			dc.desc.bcdDevice = (short) 0x9223;
			dc.desc.iManufacturer = Util.string(dc, 1, "GenesysLogic");
			dc.desc.iProduct = Util.string(dc, 2, "USB3.0 Hub");
			Util.configDescriptors(dc, cd -> {
				cd.wTotalLength = 31;
				cd.bmAttributes = (byte) 0xe0;
				Util.interfaces(cd, i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HUB.value;
					id.iInterface = 1;
					Util.endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x83;
						ed.bmAttributes = 0x13;
						ed.wMaxPacketSize = 2;
						ed.bInterval = 8;
						Util.extra(ed, 0x6, 0x30, 0x0, 0x0, 0x2, 0x0);
						Util.ssEndPointCompanionDesc(dc, ed, ss -> {
							ss.wBytesPerInterval = 2;
						});
					});
				}));
			});
			Util.bosDescriptor(dc, bos -> {
				bos.wTotalLength = 42;
			}, Util.bosUsb20Ext(bdc -> {
				bdc.bmAttributes = 0x00000006;
			}), Util.bosSsUsbDevCap(bdc -> {
				bdc.wSpeedSupported = 0x000e;
				bdc.bFunctionalitySupport = 0x01;
				bdc.bU1DevExitLat = 0x08;
				bdc.wU2DevExitLat = 0x00be;
			}), Util.bosContainerId(bdc -> {
				Util.containerId(bdc, 0x96, 0xd6, 0x67, 0xd6, 0x5, 0x44, 0x42, 0xa5, 0x9f, 0x29,
					0xf4, 0x85, 0xe5, 0x26, 0xbb, 0x58);
			}));
		});
	}

	/**
	 * Sample data for external USB hub.
	 */
	public static DeviceConfig externalUsb2HubConfig() {
		return Util.device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = Array.BYTE.of(0x02);
			dc.address = 0x03;
			dc.speed = LIBUSB_SPEED_HIGH;
			dc.desc.bcdUSB = 0x0210;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_HUB.value;
			dc.desc.bDeviceProtocol = 0x02;
			dc.desc.bMaxPacketSize0 = 64;
			dc.desc.idVendor = 0x05e3;
			dc.desc.idProduct = 0x0610;
			dc.desc.bcdDevice = (short) 0x9223;
			dc.desc.iManufacturer = Util.string(dc, 1, "GenesysLogic");
			dc.desc.iProduct = Util.string(dc, 2, "USB2.0 Hub");
			Util.configDescriptors(dc, cd -> {
				cd.wTotalLength = 41;
				cd.bmAttributes = (byte) 0xe0;
				cd.bMaxPower = 50;
				Util.interfaces(cd, i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HUB.value;
					id.bInterfaceProtocol = 0x01;
					Util.endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x81;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 1;
						ed.bInterval = 12;
					});
				}, id -> {
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HUB.value;
					id.bInterfaceProtocol = 0x02;
					Util.endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x81;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 1;
						ed.bInterval = 12;
					});
				}));
			});
			Util.bosDescriptor(dc, bos -> {
				bos.wTotalLength = 42;
			}, Util.bosUsb20Ext(bdc -> {
				bdc.bmAttributes = 0x00000006;
			}), Util.bosSsUsbDevCap(bdc -> {
				bdc.wSpeedSupported = 0x000e;
				bdc.bFunctionalitySupport = 0x01;
				bdc.bU1DevExitLat = 0x08;
				bdc.wU2DevExitLat = 0x00be;
			}), Util.bosContainerId(bdc -> {
				Util.containerId(bdc, 0x96, 0xd6, 0x67, 0xd6, 0x5, 0x44, 0x42, 0xa5, 0x9f, 0x29,
					0xf4, 0x85, 0xe5, 0x26, 0xbb, 0x58);
			}));
		});
	}

	/**
	 * Sample data for mouse.
	 */
	public static DeviceConfig mouseConfig() {
		return Util.device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = Array.BYTE.of(1);
			dc.address = 0x08;
			dc.speed = LIBUSB_SPEED_LOW;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x04f2;
			dc.desc.idProduct = 0x0939;
			dc.desc.bcdDevice = 0x0100;
			dc.desc.iManufacturer = Util.string(dc, 1, "PixArt");
			dc.desc.iProduct = Util.string(dc, 2, "USB Optical Mouse");
			Util.configDescriptors(dc, cd -> {
				cd.wTotalLength = 34;
				cd.bmAttributes = (byte) 0xa0;
				cd.bMaxPower = 50;
				Util.interfaces(cd, i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
					id.bInterfaceSubClass = 0x01;
					id.bInterfaceProtocol = 0x02;
					Util.extra(id, 0x09, 0x21, 0x11, 0x01, 0x00, 0x01, 0x22, 0x2e, 0x00);
					Util.endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x81;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 4;
						ed.bInterval = 10;
					});
				}));
			});
		});
	}

	/**
	 * Sample data for internal USB hub.
	 */
	public static DeviceConfig internalHubConfig() {
		return Util.device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = Array.BYTE.of(0x03);
			dc.address = 0x04;
			dc.speed = LIBUSB_SPEED_FULL;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_HUB.value;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x0a5c;
			dc.desc.idProduct = 0x4500;
			dc.desc.bcdDevice = 0x0100;
			dc.desc.iManufacturer = Util.string(dc, 1, "Apple Inc.");
			dc.desc.iProduct = Util.string(dc, 2, "BRCM20702 Hub");
			Util.configDescriptors(dc, cd -> {
				cd.wTotalLength = 25;
				cd.bmAttributes = (byte) 0xe0;
				cd.bMaxPower = 47;
				Util.interfaces(cd, i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HUB.value;
					Util.endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x81;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 1;
						ed.bInterval = (byte) 0xff;
					});
				}));
			});
		});
	}

	/**
	 * Sample data for keyboard/touchpad.
	 */
	public static DeviceConfig kbConfig() {
		return Util.device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = Array.BYTE.of(4);
			dc.address = 0x02;
			dc.speed = LIBUSB_SPEED_FULL;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_PER_INTERFACE.value;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x05ac;
			dc.desc.idProduct = 0x0259;
			dc.desc.bcdDevice = 0x0224;
			dc.desc.iManufacturer = Util.string(dc, 1, "Apple Inc.");
			dc.desc.iProduct = Util.string(dc, 2, "Apple Internal Keyboard / Trackpad");
			Util.configDescriptors(dc, cd -> {
				cd.wTotalLength = 84;
				cd.bmAttributes = (byte) 0xa0;
				cd.bMaxPower = 20;
				Util.interfaces(cd, i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
					id.bInterfaceSubClass = 0x01;
					id.bInterfaceProtocol = 0x01;
					id.iInterface = Util.string(dc, 3, "Apple Internal Keyboard");
					Util.extra(id, 0x09, 0x21, 0x11, 0x01, 0x21, 0x01, 0x22, 0x9c, 0x00);
					Util.endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x83;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 10;
						ed.bInterval = 8;
					});
				}), i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.1
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
					id.iInterface = Util.string(dc, 4, "Touchpad");
					Util.extra(id, 0x09, 0x21, 0x11, 0x01, 0x00, 0x01, 0x22, 0x1b, 0x00);
					Util.endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x81;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 64;
						ed.bInterval = 2;
					});
				}), i -> Util.interfaceDescriptors(cd, i, id -> { // x.0.2
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
					id.bInterfaceSubClass = 0x01;
					id.bInterfaceProtocol = 0x02;
					id.iInterface = 4;
					Util.extra(id, 0x09, 0x21, 0x11, 0x01, 0x00, 0x01, 0x22, 0x34, 0x00);
					Util.endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x84;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 8;
						ed.bInterval = 8;
					});
				}));
			});
		});
	}
}
