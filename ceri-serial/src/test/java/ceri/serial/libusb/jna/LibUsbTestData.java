package ceri.serial.libusb.jna;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INVALID_PARAM;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import static ceri.serial.libusb.jna.LibUsbTestUtil.ex;
import static ceri.serial.libusb.jna.LibUsbTestUtil.version;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.function.Fluent;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_version;

public class LibUsbTestData {
	private Context contextDef = null;
	private final List<Context> contexts = new ArrayList<>();
	private final List<DeviceList> deviceLists = new ArrayList<>();
	private final List<Device> devices = new ArrayList<>();
	private final List<DeviceHandle> deviceHandles = new ArrayList<>();
	public final List<DeviceConfig> deviceConfigs = new ArrayList<>();
	public libusb_version version = version("http://libusb.info", null, 1, 0, 17, 0x2c85);
	public int capabilities;
	public String locale;

	public static class Data {
		public final Pointer ptr;

		protected Data() {
			this(new Memory(Pointer.SIZE));
		}

		protected Data(Pointer ptr) {
			this.ptr = ptr;
		}
	}

	public static class Context extends Data {
		public int debugLevel = 0;
	}

	public static class DeviceList extends Data {
		public final int size;
		public Context context;

		public DeviceList(int size) {
			super(new Memory(Pointer.SIZE * (size + 1)));
			this.size = size;
		}

		public Pointer ptr(int i) {
			if (i < 0 || i >= size) return null;
			return ptr.share(i * Pointer.SIZE);
		}
	}

	public static class Device extends Data {
		public DeviceList deviceList;
		private int refs = 1;
		public DeviceConfig config;
	}

	public static class DeviceHandle extends Data {
		public Device device;
		public int configuration;
		public int claimedInterface;
		public int altSetting;
		
		public void reset() {
			configuration = device.config.configuration;
			resetInterface();
		}
		
		public void resetInterface() {
			claimedInterface = -1;
			altSetting = -1;
		}
	}

	public static class DeviceConfig implements Fluent<DeviceConfig> {
		public DeviceConfig parent = null;
		public int busNumber = 0;
		public byte[] portNumbers = { 0 };
		public int address = 0;
		public int speed = 0;
		public int configuration = 0;
		public libusb_device_descriptor desc = new libusb_device_descriptor();
		public List<String> descriptorStrings = List.of();
		public libusb_config_descriptor[] configDescriptors = new libusb_config_descriptor[0];

		public int portNumber() {
			return portNumbers[portNumbers.length - 1];
		}

		public String descriptorString(int i) {
			if (i <= 0 || i > descriptorStrings.size()) return null;
			return descriptorStrings.get(i - 1);
		}

		public libusb_config_descriptor configDescriptor(int i) {
			if (i < 0 || i >= configDescriptors.length) return null;
			return configDescriptors[i];
		}

		public libusb_config_descriptor configDescriptorByValue(int value) {
			for (var c : configDescriptors)
				if (c.bConfigurationValue == (byte) value) return c;
			return null;
		}
	}

	public LibUsbTestData() {
		version("http://libusb.info", null, 1, 0, 17, 0x2c85);
		reset();
	}

	public void reset() {
		contexts.clear();
		deviceLists.clear();
		devices.clear();
		deviceHandles.clear();
		capabilities = 0;
		locale = "en-US";
	}

	public Context createContext(boolean def) {
		Context context = new Context();
		if (def) contextDef = context;
		contexts.add(context);
		return context;
	}

	public Context context(Pointer p) {
		if (p != null) return find(contexts, p);
		if (contextDef != null) return contextDef;
		throw ex(LIBUSB_ERROR_INVALID_PARAM);
	}

	public void removeContext(Context context) {
		if (context == contextDef) contextDef = null;
		contexts.remove(context);
		deviceLists.removeIf(t -> t.context == context);
		devices.removeIf(t -> t.deviceList.context == context);
		deviceHandles.removeIf(t -> t.device.deviceList.context == context);
	}

	public DeviceList createDeviceList(Context context) {
		var deviceList = new DeviceList(deviceConfigs.size());
		deviceList.context = context;
		deviceLists.add(deviceList);
		for (int i = 0; i < deviceList.size; i++) {
			Device device = createDevice(deviceList, deviceConfigs.get(i));
			devices.add(device);
			deviceList.ptr(i).setPointer(0, device.ptr);
		}
		return deviceList;
	}

	public DeviceList deviceList(Pointer p) {
		return find(deviceLists, p);
	}

	public void removeDeviceList(DeviceList deviceList, boolean unref) {
		deviceLists.remove(deviceList);
		if (!unref) return;
		forEach(devices, t -> t.deviceList == deviceList, t -> t.refs--);
		purgeDevices();
	}

	private Device createDevice(DeviceList devList, DeviceConfig config) {
		Device device = new Device();
		device.deviceList = devList;
		device.config = config;
		devices.add(device);
		return device;
	}

	public Device device(Pointer p) {
		return find(devices, p);
	}

	public Device device(Predicate<? super Device> filter) {
		return find(devices, filter, null);
	}

	public Device parentDevice(Device device) {
		if (device.config.parent == null) return null;
		return find(devices,
			d -> device.deviceList == d.deviceList && device.config.parent == d.config, null);
	}

	public void refDevice(Device device, int refs) {
		device.refs += refs;
		if (device.refs < 0) removeDevice(device);
	}

	private void purgeDevices() {
		devices.removeIf(d -> d.refs <= 0);
	}

	public void removeDevice(Device device) {
		devices.remove(device);
	}

	public DeviceHandle createDeviceHandle(Device device) {
		DeviceHandle handle = new DeviceHandle();
		deviceHandles.add(handle);
		handle.device = device;
		handle.reset();
		refDevice(device, 1);
		return handle;
	}

	public DeviceHandle deviceHandle(Pointer p) {
		return find(deviceHandles, p);
	}

	public void removeDeviceHandle(DeviceHandle handle) {
		refDevice(handle.device, -1);
		deviceHandles.remove(handle);
	}

	private static <T extends Data> void forEach(List<T> list, Predicate<? super T> filter,
		Consumer<? super T> action) {
		list.stream().filter(filter).forEach(action);
	}

	private static <T extends Data> T find(List<T> list, Pointer p) {
		return find(list, p == null ? null : t -> t.ptr.equals(p),
			() -> ex(LIBUSB_ERROR_NOT_FOUND));
	}

	private static <E extends Exception, T extends Data> T find(List<T> list,
		Predicate<? super T> filter, Supplier<E> exFn) throws E {
		T t = list.stream().filter(filter).findFirst().orElse(null);
		if (t != null || exFn == null) return t;
		throw exFn.get();
	}

}
