package ceri.serial.libusb.jna;

import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_UNKNOWN;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ImmutableUtil;
import ceri.common.function.Fluent;
import ceri.common.text.ToString;
import ceri.serial.jna.Struct;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_iso_packet_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_speed;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_endpoint_companion_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_version;

public class LibUsbTestData {
	private final List<Context> contexts = new ArrayList<>();
	private final List<DeviceList> deviceLists = new ArrayList<>();
	private final List<Device> devices = new ArrayList<>();
	private final List<DeviceHandle> deviceHandles = new ArrayList<>();
	private final List<Transfer> transfers = new ArrayList<>();
	public final List<DeviceConfig> deviceConfigs = new ArrayList<>();
	public libusb_version version = version("http://libusb.info", "", 1, 0, 24, 11584);
	public int capabilities;
	public String locale;

	public static LastErrorException lastError(LibUsb.libusb_error error) {
		return new LastErrorException(error.value);
	}

	private static Pointer pointer() {
		return new Memory(Pointer.SIZE);
	}

	public static class Data {
		private static final AtomicInteger ids = new AtomicInteger(0);
		public final int id;
		public final Pointer p;

		private Data() {
			this(pointer());
		}

		private Data(Pointer p) {
			id = ids.addAndGet(1);
			this.p = p;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, id, p);
		}
	}

	public static class Context extends Data {
		public int debugLevel = 0;
		public boolean usbDk = false;
		public boolean weakAuth = false;
		public Lock eventLock = new ReentrantLock();

		private Context(Pointer p) {
			super(p);
		}
	}

	public static class DeviceList extends Data {
		public final int size;
		public Context context;

		private DeviceList(int size) {
			super(new Memory(Pointer.SIZE * (size + 1)));
			this.size = size;
		}

		private Pointer p(int i) {
			if (i < 0 || i >= size) return null;
			return p.share(i * Pointer.SIZE);
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

	public static class Transfer extends Data {
		public boolean submitted = false;

		private Transfer(Pointer p) {
			super(p);
		}

		public libusb_transfer transfer() {
			return Struct.read(new libusb_transfer(p));
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

	public Context createContextDef() {
		Context def = find(contexts, null);
		if (def == null) {
			def = new Context(null);
			contexts.add(def);
		}
		return def;
	}

	public Context createContext() {
		Context context = new Context(pointer());
		contexts.add(context);
		return context;
	}

	public Context context(Pointer p) {
		return find(contexts, p);
	}

	public void removeContext(Pointer p) {
		var context = find(contexts, p);
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
			deviceList.p(i).setPointer(0, device.p);
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

	public Transfer createTransfer(int isoPackets) {
		libusb_transfer xfer = new libusb_transfer(null);
		xfer.num_iso_packets = isoPackets;
		xfer.iso_packet_desc = new libusb_iso_packet_descriptor[isoPackets];
		for (int i = 0; i < isoPackets; i++)
			xfer.iso_packet_desc[i] = new libusb_iso_packet_descriptor(null);
		Transfer transfer = new Transfer(Struct.write(xfer).getPointer());
		transfers.add(transfer);
		return transfer;
	}

	public Transfer transfer(Pointer p) {
		return find(transfers, p);
	}

	public List<Transfer> transfers() {
		return ImmutableUtil.copyAsList(transfers);
	}

	public void removeTransfer(Transfer transfer) {
		transfers.remove(transfer);
	}

	private static <T extends Data> void forEach(List<T> list, Predicate<? super T> filter,
		Consumer<? super T> action) {
		list.stream().filter(filter).forEach(action);
	}

	private static <T extends Data> T find(List<T> list, Pointer p) {
		return find(list, t -> Objects.equals(t.p, p),
			() -> new LastErrorException(LIBUSB_ERROR_NOT_FOUND.value));
	}

	private static <E extends Exception, T extends Data> T find(List<T> list,
		Predicate<? super T> filter, Supplier<E> exFn) throws E {
		T t = list.stream().filter(filter).findFirst().orElse(null);
		if (t != null || exFn == null) return t;
		throw exFn.get();
	}

}
