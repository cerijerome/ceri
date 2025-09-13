package ceri.serial.libusb;

import ceri.common.math.Maths;
import ceri.common.text.Strings;
import ceri.common.util.Basics;
import ceri.serial.libusb.jna.LibUsb;

public class UsbLibVersion {
	private final LibUsb.libusb_version version;

	UsbLibVersion(LibUsb.libusb_version version) {
		this.version = version;
	}

	public int major() {
		return Maths.ushort(version.major);
	}

	public int minor() {
		return Maths.ushort(version.minor);
	}

	public int micro() {
		return Maths.ushort(version.micro);
	}

	public int nano() {
		return Maths.ushort(version.nano);
	}

	public String describe() {
		return Basics.def(Strings.trim(version.describe), "");
	}

	public String rcSuffix() {
		return Basics.def(Strings.trim(version.rc), "");
	}

	@Override
	public String toString() {
		return major() + "." + minor() + "." + micro() + "." + nano();
	}
}
