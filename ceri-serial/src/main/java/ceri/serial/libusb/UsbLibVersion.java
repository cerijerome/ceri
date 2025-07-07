package ceri.serial.libusb;

import static ceri.common.math.MathUtil.ushort;
import static ceri.common.util.BasicUtil.def;
import ceri.common.text.StringUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_version;

public class UsbLibVersion {
	private final libusb_version version;

	UsbLibVersion(libusb_version version) {
		this.version = version;
	}

	public int major() {
		return ushort(version.major);
	}

	public int minor() {
		return ushort(version.minor);
	}

	public int micro() {
		return ushort(version.micro);
	}

	public int nano() {
		return ushort(version.nano);
	}

	public String describe() {
		return def(StringUtil.trim(version.describe), "");
	}

	public String rcSuffix() {
		return def(StringUtil.trim(version.rc), "");
	}

	@Override
	public String toString() {
		return major() + "." + minor() + "." + micro() + "." + nano();
	}
}
