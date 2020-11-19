package ceri.serial.libusb.jna;

import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_interface;
import ceri.serial.libusb.jna.LibUsb.libusb_interface_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_version;

public class LibUsbTestUtil {

	private LibUsbTestUtil() {}

	public static LastErrorException ex(LibUsb.libusb_error error) {
		return new LastErrorException(error.value);
	}
	
	public static Pointer ptr(PointerType pt) {
		if (pt == null) return null;
		return pt.getPointer();
	}

	public static libusb_version version(String desc, String rc, int... nums) {
		libusb_version version = new libusb_version();
		version.describe = desc;
		version.rc = rc;
		int i = 0;
		version.major = (short) (nums.length >= i ? nums[i] : 0);
		version.minor = (short) (nums.length >= ++i ? nums[i] : 0);
		version.micro = (short) (nums.length >= ++i ? nums[i] : 0);
		version.nano = (short) (nums.length >= ++i ? nums[i] : 0);
		return version;
	}

	public static libusb_device_descriptor copyDeviceDescriptor(libusb_device_descriptor from,
		libusb_device_descriptor to) {
		to.bLength = from.bLength;
		to.bDescriptorType = from.bDescriptorType;
		to.bcdUSB = from.bcdUSB;
		to.bDeviceClass = from.bDeviceClass;
		to.bDeviceSubClass = from.bDeviceSubClass;
		to.bDeviceProtocol = from.bDeviceProtocol;
		to.bMaxPacketSize0 = from.bMaxPacketSize0;
		to.idVendor = from.idVendor;
		to.idProduct = from.idProduct;
		to.bcdDevice = from.bcdDevice;
		to.iManufacturer = from.iManufacturer;
		to.iProduct = from.iProduct;
		to.iSerialNumber = from.iSerialNumber;
		to.bNumConfigurations = from.bNumConfigurations;
		return to;
	}

	public static void extra(libusb_config_descriptor desc, int... bytes) {
		Memory m = CUtil.malloc(bytes);
		desc.extra = m;
		desc.extra_length = bytes.length;
	}

	public static void extra(libusb_interface_descriptor desc, int... bytes) {
		Memory m = CUtil.malloc(bytes);
		desc.extra = m;
		desc.extra_length = bytes.length;
	}

	public static void extra(libusb_endpoint_descriptor desc, int... bytes) {
		Memory m = CUtil.malloc(bytes);
		desc.extra = m;
		desc.extra_length = bytes.length;
	}

	@SafeVarargs
	public static libusb_config_descriptor[]
		configDescriptors(Consumer<libusb_config_descriptor>... populators) {
		return array(libusb_config_descriptor::new, libusb_config_descriptor[]::new, populators);
	}

	@SafeVarargs
	public static libusb_interface.ByReference
		interfaces(Consumer<libusb_interface.ByReference>... populators) {
		return arrayRef(libusb_interface.ByReference::new, libusb_interface.ByReference[]::new,
			populators);
	}

	@SafeVarargs
	public static libusb_interface_descriptor.ByReference
		interfaceDescriptors(Consumer<libusb_interface_descriptor.ByReference>... populators) {
		return arrayRef(libusb_interface_descriptor.ByReference::new,
			libusb_interface_descriptor.ByReference[]::new, populators);
	}

	@SafeVarargs
	public static libusb_endpoint_descriptor.ByReference
		endpointDescriptors(Consumer<libusb_endpoint_descriptor.ByReference>... populators) {
		return arrayRef(libusb_endpoint_descriptor.ByReference::new,
			libusb_endpoint_descriptor.ByReference[]::new, populators);
	}

	@SafeVarargs
	private static <T extends Struct & Structure.ByReference> T arrayRef(Supplier<T> constructor,
		IntFunction<T[]> arrayConstructor, Consumer<T>... populators) {
		T[] array = array(constructor, arrayConstructor, populators);
		return array.length == 0 ? null : array[0];
	}

	@SafeVarargs
	private static <T extends Struct> T[] array(Supplier<T> constructor,
		IntFunction<T[]> arrayConstructor, Consumer<T>... populators) {
		T[] array = Struct.array(populators.length, constructor, arrayConstructor);
		for (int i = 0; i < array.length; i++) {
			populators[i].accept(array[i]);
			array[i].write();
		}
		return array;
	}

}
