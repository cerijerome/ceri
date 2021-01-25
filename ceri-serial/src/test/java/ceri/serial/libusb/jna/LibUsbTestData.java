package ceri.serial.libusb.jna;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_INVALID_PARAM;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_UNKNOWN;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.function.Fluent;
import ceri.common.text.ToString;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_speed;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_endpoint_companion_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_version;

public class LibUsbTestData {
	private Context contextDef = null;
	private final List<Context> contexts = new ArrayList<>();
	private final List<DeviceList> deviceLists = new ArrayList<>();
	private final List<Device> devices = new ArrayList<>();
	private final List<DeviceHandle> deviceHandles = new ArrayList<>();
	public final List<DeviceConfig> deviceConfigs = new ArrayList<>();
	public libusb_version version = version("http://libusb.info", "", 1, 0, 24, 11584);
	public int capabilities;
	public String locale;

	public static class Data {
		private static final AtomicInteger ids = new AtomicInteger(0);
		public final int id;
		public final Pointer ptr;

		protected Data() {
			this(new Memory(Pointer.SIZE));
		}

		protected Data(Pointer ptr) {
			id = ids.addAndGet(1);
			this.ptr = ptr;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, id, ptr);
		}
	}

	public static class Context extends Data {
		public int debugLevel = 0;
		public boolean usbDk = false;
		public boolean weakAuth = false;
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
		public libusb_speed speed = LIBUSB_SPEED_UNKNOWN;
		public int configuration = 1;
		public libusb_device_descriptor desc = new libusb_device_descriptor(null);
		public Map<Integer, String> descriptorStrings = new TreeMap<>();
		public libusb_config_descriptor[] configDescriptors = new libusb_config_descriptor[0];
		public Map<Pointer, libusb_ss_endpoint_companion_descriptor> ssEpCompDescs =
			new HashMap<>();
		public libusb_bos_descriptor bos = null;

		public int portNumber() {
			return portNumbers[portNumbers.length - 1];
		}

		public String descriptorString(int i) {
			return descriptorStrings.get(i);
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

	public Context createContext(boolean def) {
		Context context = new Context();
		if (def) contextDef = context;
		contexts.add(context);
		return context;
	}

	public Context context(Pointer p) {
		if (p != null) return find(contexts, p);
		if (contextDef != null) return contextDef;
		throw new LastErrorException(LIBUSB_ERROR_INVALID_PARAM.value);
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

	public libusb_ss_endpoint_companion_descriptor ssEpCompDesc(Pointer p) {
		for (var dc : deviceConfigs) {
			var desc = dc.ssEpCompDescs.get(p);
			if (desc != null) return desc;
		}
		return null;
	}

	private static <T extends Data> void forEach(List<T> list, Predicate<? super T> filter,
		Consumer<? super T> action) {
		list.stream().filter(filter).forEach(action);
	}

	private static <T extends Data> T find(List<T> list, Pointer p) {
		return find(list, p == null ? null : t -> t.ptr.equals(p),
			() -> new LastErrorException(LIBUSB_ERROR_NOT_FOUND.value));
	}

	private static <E extends Exception, T extends Data> T find(List<T> list,
		Predicate<? super T> filter, Supplier<E> exFn) throws E {
		T t = list.stream().filter(filter).findFirst().orElse(null);
		if (t != null || exFn == null) return t;
		throw exFn.get();
	}

}
