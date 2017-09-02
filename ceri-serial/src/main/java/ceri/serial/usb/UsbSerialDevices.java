package ceri.serial.usb;

import java.util.HashMap;
import java.util.Map;
import ceri.common.collection.ImmutableUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class UsbSerialDevices {
	public final Map<Integer, String> devices;

	public static class Builder {
		final Map<Integer, String> devices = new HashMap<>();

		Builder() {}

		public Builder device(Integer key, String value) {
			devices.put(key, value);
			return this;
		}

		public Builder devices(Map<Integer, String> devices) {
			this.devices.putAll(devices);
			return this;
		}

		public UsbSerialDevices build() {
			return new UsbSerialDevices(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	UsbSerialDevices(Builder builder) {
		devices = ImmutableUtil.copyAsMap(builder.devices);
	}

	public String device(int locationId) {
		return devices.get(locationId);
	}
	
	@Override
	public int hashCode() {
		return HashCoder.hash(devices);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof UsbSerialDevices)) return false;
		UsbSerialDevices other = (UsbSerialDevices) obj;
		if (!EqualsUtil.equals(devices, other.devices)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, devices).toString();
	}

}
