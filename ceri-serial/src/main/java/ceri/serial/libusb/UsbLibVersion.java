package ceri.serial.libusb;

import static ceri.common.math.MathUtil.ushort;
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
		return version.describe == null ? "" : version.describe.trim();
	}

	public String rcSuffix() {
		return version.rc == null ? "" : version.rc.trim();
	}

	@Override
	public String toString() {
		return major() + "." + minor() + "." + micro() + "." + nano();
	}
}
