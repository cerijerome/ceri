package ceri.serial.libusb.jna;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.validation.ValidationUtil.validateNull;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_BT_CONTAINER_ID_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_BT_USB_2_0_EXTENSION_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_BOS_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_CONFIG_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_DEVICE_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_ENDPOINT_AUDIO_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_ENDPOINT_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_INTERFACE_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_SS_ENDPOINT_COMPANION_SIZE;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_CONTAINER_ID;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_USB_2_0_EXTENSION;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_AUDIO;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_HID;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_HUB;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_MASS_STORAGE;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_PER_INTERFACE;
import static ceri.serial.libusb.jna.LibUsb.libusb_class_code.LIBUSB_CLASS_VENDOR_SPEC;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_BOS;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_CONFIG;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_ENDPOINT;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_INTERFACE;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_SS_ENDPOINT_COMPANION;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_FULL;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_HIGH;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_LOW;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_SUPER;
import java.util.List;
import java.util.function.Consumer;
import com.sun.jna.Memory;
import com.sun.jna.Structure;
import ceri.common.data.ByteArray.Immutable;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_dev_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_container_id_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_interface;
import ceri.serial.libusb.jna.LibUsb.libusb_interface_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_endpoint_companion_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_usb_device_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_usb_2_0_extension_descriptor;
import ceri.serial.libusb.jna.LibUsbTestData.DeviceConfig;

public class LibUsbSampleData {

	private LibUsbSampleData() {}

	public static void populate(LibUsbTestData data) {
		data.deviceConfigs.clear();
		configs().forEach(data.deviceConfigs::add);
	}

	public static List<DeviceConfig> configs() {
		return List.of(sdReaderConfig(), audioConfig(), ftdiConfig(), externalUsb3HubConfig(),
			externalUsb2HubConfig(), mouseConfig(), internalHubConfig(), kbConfig());
	}

	/**
	 * Sample data for SD card reader.
	 */
	public static DeviceConfig sdReaderConfig() {
		return device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(0x07);
			dc.address = 0x0b;
			dc.speed = LIBUSB_SPEED_SUPER;
			dc.desc.bcdUSB = 0x0300;
			dc.desc.bMaxPacketSize0 = 9;
			dc.desc.idVendor = 0x05ac;
			dc.desc.idProduct = (short) 0x8406;
			dc.desc.bcdDevice = 0x0820;
			dc.desc.iManufacturer = string(dc, 3, "Apple");
			dc.desc.iProduct = string(dc, 4, "Internal Memory Card Reader");
			dc.desc.iSerialNumber = string(dc, 5, "000000000820");
			configDescriptors(dc, cd -> {
				cd.wTotalLength = 44;
				cd.bmAttributes = (byte) 0xa0;
				cd.bMaxPower = 112;
				interfaces(cd, i -> interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_MASS_STORAGE.value;
					id.bInterfaceSubClass = 0x06;
					id.bInterfaceProtocol = 0x50;
					endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x81;
						ed.bmAttributes = 0x02;
						ed.wMaxPacketSize = 1024;
						extra(ed, 0x6, 0x30, 0x4, 0x0, 0x0, 0x0);
						ssEndPointCompanionDesc(dc, ed, ss -> {
							ss.bMaxBurst = 4;
						});
					}, ed -> {
						ed.bEndpointAddress = 0x02;
						ed.bmAttributes = 0x02;
						ed.wMaxPacketSize = 1024;
						extra(ed, 0x6, 0x30, 0x4, 0x0, 0x0, 0x0);
						ssEndPointCompanionDesc(dc, ed, ss -> {
							ss.bMaxBurst = 4;
						});
					});
				}));
			});
			bosDescriptor(dc, bos -> {
				bos.wTotalLength = 22;
			}, bosUsb20Ext(bdc -> {
				bdc.bmAttributes = 0x2;
			}), bosSsUsbDevCap(bdc -> {
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
		return device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(0x02, 0x02);
			dc.address = 0x0a;
			dc.speed = LIBUSB_SPEED_FULL;
			dc.desc.bcdUSB = 0x0110;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x0d8c;
			dc.desc.idProduct = 0x0014;
			dc.desc.bcdDevice = 0x0100;
			dc.desc.iManufacturer = string(dc, 1, "C-Media Electronics Inc.\0\0\0\0\0\0");
			dc.desc.iProduct = string(dc, 2, "USB Audio Device\0\0\0\0\0\0\0\0\0\0\0\0\0\0");
			configDescriptors(dc, cd -> {
				cd.wTotalLength = 253;
				cd.bmAttributes = (byte) 0x80;
				cd.bMaxPower = 50;
				interfaces(cd, i -> interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_AUDIO.value;
					id.bInterfaceSubClass = 0x01;
					extra(id, 0xa, 0x24, 0x1, 0x0, 0x1, 0x64, 0x0, 0x2, 0x1, 0x2, 0xc, 0x24, 0x2,
						0x1, 0x1, 0x1, 0x0, 0x2, 0x3, 0x0, 0x0, 0x0, 0xc, 0x24, 0x2, 0x2, 0x1, 0x2,
						0x0, 0x1, 0x1, 0x0, 0x0, 0x0, 0x9, 0x24, 0x3, 0x6, 0x1, 0x3, 0x0, 0x9, 0x0,
						0x9, 0x24, 0x3, 0x7, 0x1, 0x1, 0x0, 0x8, 0x0, 0x7, 0x24, 0x5, 0x8, 0x1, 0xa,
						0x0, 0xa, 0x24, 0x6, 0x9, 0xf, 0x1, 0x1, 0x2, 0x2, 0x0, 0x9, 0x24, 0x6, 0xa,
						0x2, 0x1, 0x43, 0x0, 0x0, 0x9, 0x24, 0x6, 0xd, 0x2, 0x1, 0x3, 0x0, 0x0, 0xd,
						0x24, 0x4, 0xf, 0x2, 0x1, 0xd, 0x2, 0x3, 0x0, 0x0, 0x0, 0x0);
				}), i -> interfaceDescriptors(cd, i, id -> { // x.0.1
					id.bInterfaceClass = (byte) LIBUSB_CLASS_AUDIO.value;
					id.bInterfaceSubClass = 0x02;
				}, id -> {
					id.bInterfaceClass = (byte) LIBUSB_CLASS_AUDIO.value;
					id.bInterfaceSubClass = 0x02;
					extra(id, 0x7, 0x24, 0x1, 0x1, 0x1, 0x1, 0x0, 0xe, 0x24, 0x2, 0x1, 0x2, 0x2,
						0x10, 0x2, 0x80, 0xbb, 0x0, 0x44, 0xac, 0x0);
					endPointDescriptors(id, ed -> {
						ed.bLength = LIBUSB_DT_ENDPOINT_AUDIO_SIZE;
						ed.bEndpointAddress = 0x01;
						ed.bmAttributes = 0x09;
						ed.wMaxPacketSize = 200;
						ed.bInterval = 1;
						extra(ed, 0x7, 0x25, 0x1, 0x1, 0x1, 0x1, 0x0);
					});
				}), i -> interfaceDescriptors(cd, i, id -> { // x.0.2
					id.bInterfaceClass = (byte) LIBUSB_CLASS_AUDIO.value;
					id.bInterfaceSubClass = 0x02;
				}, id -> {
					id.bInterfaceClass = (byte) LIBUSB_CLASS_AUDIO.value;
					id.bInterfaceSubClass = 0x02;
					extra(id, 0x7, 0x24, 0x1, 0x7, 0x1, 0x1, 0x0, 0xe, 0x24, 0x2, 0x1, 0x1, 0x2,
						0x10, 0x2, 0x80, 0xbb, 0x0, 0x44, 0xac, 0x0);
					endPointDescriptors(id, ed -> {
						ed.bLength = LIBUSB_DT_ENDPOINT_AUDIO_SIZE;
						ed.bEndpointAddress = (byte) 0x82;
						ed.bmAttributes = 0x0d;
						ed.wMaxPacketSize = 100;
						ed.bInterval = 1;
						extra(ed, 0x7, 0x25, 0x1, 0x1, 0x0, 0x0, 0x0);
					});
				}), i -> interfaceDescriptors(cd, i, id -> { // x.0.3
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
					extra(id, 0x9, 0x21, 0x0, 0x1, 0x0, 0x1, 0x22, 0x3c, 0x0);
					endPointDescriptors(id, ed -> {
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
		return device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(0x02, 0x04);
			dc.address = 0x05;
			dc.speed = LIBUSB_SPEED_FULL;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x0403;
			dc.desc.idProduct = 0x6001;
			dc.desc.bcdDevice = 0x0600;
			dc.desc.iManufacturer = string(dc, 1, "FTDI");
			dc.desc.iProduct = string(dc, 2, "FT245R USB FIFO");
			dc.desc.iSerialNumber = string(dc, 3, "A7047D8V");
			configDescriptors(dc, cd -> {
				cd.wTotalLength = 32;
				cd.bmAttributes = (byte) 0xa0;
				cd.bMaxPower = 45;
				interfaces(cd, i -> interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_VENDOR_SPEC.value;
					id.bInterfaceSubClass = (byte) 0xff;
					id.bInterfaceProtocol = (byte) 0xff;
					id.iInterface = 2;
					endPointDescriptors(id, ed -> {
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
	 * Sampe data for external USB hub.
	 */
	public static DeviceConfig externalUsb3HubConfig() {
		return device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(0x06);
			dc.address = 0x09;
			dc.speed = LIBUSB_SPEED_SUPER;
			dc.desc.bcdUSB = 0x0300;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_HUB.value;
			dc.desc.bDeviceProtocol = 0x03;
			dc.desc.bMaxPacketSize0 = 9;
			dc.desc.idVendor = 0x05e3;
			dc.desc.idProduct = 0x0616;
			dc.desc.bcdDevice = (short) 0x9223;
			dc.desc.iManufacturer = string(dc, 1, "GenesysLogic");
			dc.desc.iProduct = string(dc, 2, "USB3.0 Hub");
			configDescriptors(dc, cd -> {
				cd.wTotalLength = 31;
				cd.bmAttributes = (byte) 0xe0;
				interfaces(cd, i -> interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HUB.value;
					id.iInterface = 1;
					endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x83;
						ed.bmAttributes = 0x13;
						ed.wMaxPacketSize = 2;
						ed.bInterval = 8;
						extra(ed, 0x6, 0x30, 0x0, 0x0, 0x2, 0x0);
						ssEndPointCompanionDesc(dc, ed, ss -> {
							ss.wBytesPerInterval = 2;
						});
					});
				}));
			});
			bosDescriptor(dc, bos -> {
				bos.wTotalLength = 42;
			}, bosUsb20Ext(bdc -> {
				bdc.bmAttributes = 0x00000006;
			}), bosSsUsbDevCap(bdc -> {
				bdc.wSpeedSupported = 0x000e;
				bdc.bFunctionalitySupport = 0x01;
				bdc.bU1DevExitLat = 0x08;
				bdc.wU2DevExitLat = 0x00be;
			}), bosContainerId(bdc -> {
				containerId(bdc, 0x96, 0xd6, 0x67, 0xd6, 0x5, 0x44, 0x42, 0xa5, 0x9f, 0x29, 0xf4,
					0x85, 0xe5, 0x26, 0xbb, 0x58);
			}));
		});
	}

	/**
	 * Sampe data for external USB hub.
	 */
	public static DeviceConfig externalUsb2HubConfig() {
		return device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(0x02);
			dc.address = 0x03;
			dc.speed = LIBUSB_SPEED_HIGH;
			dc.desc.bcdUSB = 0x0210;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_HUB.value;
			dc.desc.bDeviceProtocol = 0x02;
			dc.desc.bMaxPacketSize0 = 64;
			dc.desc.idVendor = 0x05e3;
			dc.desc.idProduct = 0x0610;
			dc.desc.bcdDevice = (short) 0x9223;
			dc.desc.iManufacturer = string(dc, 1, "GenesysLogic");
			dc.desc.iProduct = string(dc, 2, "USB2.0 Hub");
			configDescriptors(dc, cd -> {
				cd.wTotalLength = 41;
				cd.bmAttributes = (byte) 0xe0;
				cd.bMaxPower = 50;
				interfaces(cd, i -> interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HUB.value;
					id.bInterfaceProtocol = 0x01;
					endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x81;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 1;
						ed.bInterval = 12;
					});
				}, id -> {
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HUB.value;
					id.bInterfaceProtocol = 0x02;
					endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x81;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 1;
						ed.bInterval = 12;
					});
				}));
			});
			bosDescriptor(dc, bos -> {
				bos.wTotalLength = 42;
			}, bosUsb20Ext(bdc -> {
				bdc.bmAttributes = 0x00000006;
			}), bosSsUsbDevCap(bdc -> {
				bdc.wSpeedSupported = 0x000e;
				bdc.bFunctionalitySupport = 0x01;
				bdc.bU1DevExitLat = 0x08;
				bdc.wU2DevExitLat = 0x00be;
			}), bosContainerId(bdc -> {
				containerId(bdc, 0x96, 0xd6, 0x67, 0xd6, 0x5, 0x44, 0x42, 0xa5, 0x9f, 0x29, 0xf4,
					0x85, 0xe5, 0x26, 0xbb, 0x58);
			}));
		});
	}

	/**
	 * Sampe data for mouse.
	 */
	public static DeviceConfig mouseConfig() {
		return device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(1);
			dc.address = 0x08;
			dc.speed = LIBUSB_SPEED_LOW;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x04f2;
			dc.desc.idProduct = 0x0939;
			dc.desc.bcdDevice = 0x0100;
			dc.desc.iManufacturer = string(dc, 1, "PixArt");
			dc.desc.iProduct = string(dc, 2, "USB Optical Mouse");
			configDescriptors(dc, cd -> {
				cd.wTotalLength = 34;
				cd.bmAttributes = (byte) 0xa0;
				cd.bMaxPower = 50;
				interfaces(cd, i -> interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
					id.bInterfaceSubClass = 0x01;
					id.bInterfaceProtocol = 0x02;
					extra(id, 0x09, 0x21, 0x11, 0x01, 0x00, 0x01, 0x22, 0x2e, 0x00);
					endPointDescriptors(id, ed -> {
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
	 * Sampe data for internal USB hub.
	 */
	public static DeviceConfig internalHubConfig() {
		return device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(0x03);
			dc.address = 0x04;
			dc.speed = LIBUSB_SPEED_FULL;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_HUB.value;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x0a5c;
			dc.desc.idProduct = 0x4500;
			dc.desc.bcdDevice = 0x0100;
			dc.desc.iManufacturer = string(dc, 1, "Apple Inc.");
			dc.desc.iProduct = string(dc, 2, "BRCM20702 Hub");
			configDescriptors(dc, cd -> {
				cd.wTotalLength = 25;
				cd.bmAttributes = (byte) 0xe0;
				cd.bMaxPower = 47;
				interfaces(cd, i -> interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HUB.value;
					endPointDescriptors(id, ed -> {
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
	 * Sampe data for keyboard/touchpad.
	 */
	public static DeviceConfig kbConfig() {
		return device(dc -> {
			dc.busNumber = 0x14;
			dc.portNumbers = bytes(4);
			dc.address = 0x02;
			dc.speed = LIBUSB_SPEED_FULL;
			dc.desc.bcdUSB = 0x0200;
			dc.desc.bDeviceClass = (byte) LIBUSB_CLASS_PER_INTERFACE.value;
			dc.desc.bMaxPacketSize0 = 8;
			dc.desc.idVendor = 0x05ac;
			dc.desc.idProduct = 0x0259;
			dc.desc.bcdDevice = 0x0224;
			dc.desc.iManufacturer = string(dc, 1, "Apple Inc.");
			dc.desc.iProduct = string(dc, 2, "Apple Internal Keyboard / Trackpad");
			configDescriptors(dc, cd -> {
				cd.wTotalLength = 84;
				cd.bmAttributes = (byte) 0xa0;
				cd.bMaxPower = 20;
				interfaces(cd, i -> interfaceDescriptors(cd, i, id -> { // x.0.0
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
					id.bInterfaceSubClass = 0x01;
					id.bInterfaceProtocol = 0x01;
					id.iInterface = string(dc, 3, "Apple Internal Keyboard");
					extra(id, 0x09, 0x21, 0x11, 0x01, 0x21, 0x01, 0x22, 0x9c, 0x00);
					endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x83;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 10;
						ed.bInterval = 8;
					});
				}), i -> interfaceDescriptors(cd, i, id -> { // x.0.1
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
					id.iInterface = string(dc, 4, "Touchpad");
					extra(id, 0x09, 0x21, 0x11, 0x01, 0x00, 0x01, 0x22, 0x1b, 0x00);
					endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x81;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 64;
						ed.bInterval = 2;
					});
				}), i -> interfaceDescriptors(cd, i, id -> { // x.0.2
					id.bInterfaceClass = (byte) LIBUSB_CLASS_HID.value;
					id.bInterfaceSubClass = 0x01;
					id.bInterfaceProtocol = 0x02;
					id.iInterface = 4;
					extra(id, 0x09, 0x21, 0x11, 0x01, 0x00, 0x01, 0x22, 0x34, 0x00);
					endPointDescriptors(id, ed -> {
						ed.bEndpointAddress = (byte) 0x84;
						ed.bmAttributes = 0x03;
						ed.wMaxPacketSize = 8;
						ed.bInterval = 8;
					});
				}));
			});
		});
	}

	/**
	 * Sampe dummy data.
	 */
	public static DeviceConfig dummyConfig() {
		return null;
	}

	private static DeviceConfig device(Consumer<DeviceConfig> populator) {
		var device = new DeviceConfig();
		device.desc.bLength = LIBUSB_DT_DEVICE_SIZE;
		device.desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE.value;
		populator.accept(device);
		return device;
	}

	private static byte string(DeviceConfig dc, int i, String s) {
		validateNull(dc.descriptorStrings.put(i, s));
		return (byte) i;
	}

	private static void extra(libusb_interface_descriptor desc, int... bytes) {
		Memory m = JnaUtil.mallocBytes(bytes);
		desc.extra = m;
		desc.extra_length = bytes.length;
	}

	private static void extra(libusb_endpoint_descriptor desc, int... bytes) {
		Memory m = JnaUtil.mallocBytes(bytes);
		desc.extra = m;
		desc.extra_length = bytes.length;
	}

	@SafeVarargs
	private static void configDescriptors(DeviceConfig dc,
		Consumer<libusb_config_descriptor>... populators) {
		if (populators.length == 0) return;
		var descs = Struct.arrayByVal(() -> new libusb_config_descriptor(null),
			libusb_config_descriptor[]::new, populators.length);
		for (int i = 0; i < populators.length; i++) {
			descs[i].bLength = LIBUSB_DT_CONFIG_SIZE;
			descs[i].bDescriptorType = (byte) LIBUSB_DT_CONFIG.value;
			descs[i].bConfigurationValue = 1;
			populators[i].accept(descs[i]);
			descs[i].write();
			dc.desc.bNumConfigurations++;
		}
		dc.configDescriptors = descs;
	}

	@SafeVarargs
	private static void interfaces(libusb_config_descriptor cd,
		Consumer<libusb_interface.ByRef>... populators) {
		if (populators.length == 0) return;
		var interfaces = Struct.arrayByVal(libusb_interface.ByRef::new,
			libusb_interface.ByRef[]::new, populators.length);
		for (int i = 0; i < populators.length; i++) {
			populators[i].accept(interfaces[i]);
			interfaces[i].write();
			cd.bNumInterfaces++;
		}
		cd.interfaces = interfaces[0];
	}

	@SafeVarargs
	private static void interfaceDescriptors(libusb_config_descriptor cd, libusb_interface it,
		Consumer<libusb_interface_descriptor.ByRef>... populators) {
		if (populators.length == 0) return;
		var descs = Struct.arrayByVal(libusb_interface_descriptor.ByRef::new,
			libusb_interface_descriptor.ByRef[]::new, populators.length);
		for (int i = 0; i < populators.length; i++) {
			descs[i].bLength = LIBUSB_DT_INTERFACE_SIZE;
			descs[i].bDescriptorType = (byte) LIBUSB_DT_INTERFACE.value;
			descs[i].bInterfaceNumber = cd.bNumInterfaces;
			descs[i].bAlternateSetting = (byte) i;
			populators[i].accept(descs[i]);
			descs[i].write();
			it.num_altsetting++;
		}
		it.altsetting = descs[0];
	}

	@SafeVarargs
	private static void endPointDescriptors(libusb_interface_descriptor id,
		Consumer<libusb_endpoint_descriptor.ByRef>... populators) {
		if (populators.length == 0) return;
		var descs = Struct.arrayByVal(libusb_endpoint_descriptor.ByRef::new,
			libusb_endpoint_descriptor.ByRef[]::new, populators.length);
		for (int i = 0; i < populators.length; i++) {
			descs[i].bLength = LIBUSB_DT_ENDPOINT_SIZE;
			descs[i].bDescriptorType = (byte) LIBUSB_DT_ENDPOINT.value;
			populators[i].accept(descs[i]);
			descs[i].write();
			id.bNumEndpoints++;
		}
		id.endpoint = descs[0];
	}

	private static void ssEndPointCompanionDesc(DeviceConfig dc, libusb_endpoint_descriptor ep,
		Consumer<libusb_ss_endpoint_companion_descriptor> populator) {
		var desc = new libusb_ss_endpoint_companion_descriptor(null);
		desc.bLength = LIBUSB_DT_SS_ENDPOINT_COMPANION_SIZE;
		desc.bDescriptorType = (byte) LIBUSB_DT_SS_ENDPOINT_COMPANION.value;
		populator.accept(desc);
		dc.ssEpCompDescs.put(ep.getPointer(), Struct.write(desc));
	}

	@SafeVarargs
	private static void bosDescriptor(DeviceConfig dc, Consumer<libusb_bos_descriptor> populator,
		libusb_bos_dev_capability_descriptor.ByRef... capabilities) {
		dc.bos = new libusb_bos_descriptor(null);
		dc.bos.bLength = LIBUSB_DT_BOS_SIZE;
		dc.bos.bDescriptorType = (byte) LIBUSB_DT_BOS.value;
		populator.accept(dc.bos);
		dc.bos.bNumDeviceCaps = (byte) capabilities.length;
		dc.bos.dev_capability = capabilities;
		Struct.write(dc.bos);
	}

	private static libusb_bos_dev_capability_descriptor.ByRef
		bosUsb20Ext(Consumer<libusb_usb_2_0_extension_descriptor> populator) {
		libusb_usb_2_0_extension_descriptor desc = new libusb_usb_2_0_extension_descriptor(null);
		desc.bLength = LIBUSB_BT_USB_2_0_EXTENSION_SIZE;
		desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE_CAPABILITY.value;
		desc.bDevCapabilityType = (byte) LIBUSB_BT_USB_2_0_EXTENSION.value;
		populator.accept(desc);
		return toBdc(desc);
	}

	private static libusb_bos_dev_capability_descriptor.ByRef
		bosSsUsbDevCap(Consumer<libusb_ss_usb_device_capability_descriptor> populator) {
		libusb_ss_usb_device_capability_descriptor desc =
			new libusb_ss_usb_device_capability_descriptor(null);
		desc.bLength = LIBUSB_BT_SS_USB_DEVICE_CAPABILITY_SIZE;
		desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE_CAPABILITY.value;
		desc.bDevCapabilityType = (byte) LIBUSB_BT_SS_USB_DEVICE_CAPABILITY.value;
		populator.accept(desc);
		return toBdc(desc);
	}

	private static libusb_bos_dev_capability_descriptor.ByRef
		bosContainerId(Consumer<libusb_container_id_descriptor> populator) {
		libusb_container_id_descriptor desc = new libusb_container_id_descriptor(null);
		desc.bLength = LIBUSB_BT_CONTAINER_ID_SIZE;
		desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE_CAPABILITY.value;
		desc.bDevCapabilityType = (byte) LIBUSB_BT_CONTAINER_ID.value;
		populator.accept(desc);
		return toBdc(desc);
	}

	private static void containerId(libusb_container_id_descriptor bdc, int... bytes) {
		Immutable.wrap(bytes).copyTo(0, bdc.ContainerID);
	}

	private static libusb_bos_dev_capability_descriptor.ByRef toBdc(Structure desc) {
		return Struct.adapt(Struct.write(desc), libusb_bos_dev_capability_descriptor.ByRef::new);
	}

}
