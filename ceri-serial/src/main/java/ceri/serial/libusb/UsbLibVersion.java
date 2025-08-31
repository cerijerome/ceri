package ceri.serial.libusb;

import ceri.common.math.MathUtil;
import ceri.common.text.Strings;
import ceri.common.util.BasicUtil;
import ceri.serial.libusb.jna.LibUsb;

public class UsbLibVersion {
	private final LibUsb.libusb_version version;

	UsbLibVersion(LibUsb.libusb_version version) {
		this.version = version;
	}

	public int major() {
		return MathUtil.ushort(version.major);
	}

	public int minor() {
		return MathUtil.ushort(version.minor);
	}

	public int micro() {
		return MathUtil.ushort(version.micro);
	}

	public int nano() {
		return MathUtil.ushort(version.nano);
	}

	public String describe() {
		return BasicUtil.def(Strings.trim(version.describe), "");
	}

	public String rcSuffix() {
		return BasicUtil.def(Strings.trim(version.rc), "");
	}

	@Override
	public String toString() {
		return major() + "." + minor() + "." + micro() + "." + nano();
	}
}
