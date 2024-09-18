package ceri.serial.libusb.test;

import static ceri.common.validation.ValidationUtil.validateNull;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_BT_CONTAINER_ID_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_BT_USB_2_0_EXTENSION_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_BOS_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_CONFIG_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_DEVICE_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_ENDPOINT_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_INTERFACE_SIZE;
import static ceri.serial.libusb.jna.LibUsb.LIBUSB_DT_SS_ENDPOINT_COMPANION_SIZE;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_CONTAINER_ID;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_bos_type.LIBUSB_BT_USB_2_0_EXTENSION;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_BOS;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_CONFIG;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_DEVICE_CAPABILITY;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_ENDPOINT;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_INTERFACE;
import static ceri.serial.libusb.jna.LibUsb.libusb_descriptor_type.LIBUSB_DT_SS_ENDPOINT_COMPANION;
import static ceri.serial.libusb.jna.LibUsb.libusb_error.LIBUSB_ERROR_NOT_FOUND;
import static ceri.serial.libusb.jna.LibUsb.libusb_speed.LIBUSB_SPEED_UNKNOWN;
import static ceri.serial.libusb.test.TestLibUsbNative.lastError;
import java.util.ArrayList;
import java.util.Collections;
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
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteProvider;
import ceri.common.function.Fluent;
import ceri.common.validation.ValidationUtil;
import ceri.jna.util.GcMemory;
import ceri.jna.util.JnaSize;
import ceri.jna.util.PointerUtil;
import ceri.jna.util.Struct;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_dev_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_container_id_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_hotplug_callback_fn;
import ceri.serial.libusb.jna.LibUsb.libusb_interface;
import ceri.serial.libusb.jna.LibUsb.libusb_interface_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_iso_packet_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_added_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_pollfd_removed_cb;
import ceri.serial.libusb.jna.LibUsb.libusb_speed;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_endpoint_companion_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_usb_device_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer;
import ceri.serial.libusb.jna.LibUsb.libusb_usb_2_0_extension_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_version;

/**
 * Test data storage for TestLibUsbNative. Keeps track of sample device configurations, and
 * user-initiated state.
 */
public class LibUsbTestData {
	private final List<Context> contexts = new ArrayList<>();
	private final List<DeviceList> deviceLists = new ArrayList<>();
	private final List<Device> devices = new ArrayList<>();
	private final List<DeviceHandle> deviceHandles = new ArrayList<>();
	private final List<Transfer> transfers = new ArrayList<>();
	private final List<HotPlug> hotPlugs = new ArrayList<>();
	private final List<DeviceConfig> deviceConfigs = new ArrayList<>();
	private libusb_version version;
	private int capabilities;
	private String locale;

	/**
	 * Utilities for building sample data.
	 */
	public static class Util {

		private Util() {}

		public static DeviceConfig device(Consumer<DeviceConfig> populator) {
			var device = new DeviceConfig();
			device.desc.bLength = LIBUSB_DT_DEVICE_SIZE;
			device.desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE.value;
			populator.accept(device);
			return device;
		}

		public static byte string(DeviceConfig dc, int i, String s) {
			validateNull(dc.descriptorStrings.put(i, s));
			return (byte) i;
		}

		public static void extra(libusb_interface_descriptor desc, int... bytes) {
			desc.extra = GcMemory.mallocBytes(bytes).m;
			desc.extra_length = bytes.length;
		}

		public static void extra(libusb_endpoint_descriptor desc, int... bytes) {
			desc.extra = GcMemory.mallocBytes(bytes).m;
			desc.extra_length = bytes.length;
		}

		@SafeVarargs
		public static void configDescriptors(DeviceConfig dc,
			Consumer<libusb_config_descriptor>... populators) {
			var descs = Struct.<libusb_config_descriptor>arrayByVal( //
				() -> new libusb_config_descriptor(null), libusb_config_descriptor[]::new,
				populators.length);
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
		public static void interfaces(libusb_config_descriptor cd,
			Consumer<libusb_interface.ByRef>... populators) {
			var interfaces = Struct.<libusb_interface.ByRef>arrayByVal(libusb_interface.ByRef::new,
				libusb_interface.ByRef[]::new, populators.length);
			for (int i = 0; i < populators.length; i++) {
				populators[i].accept(interfaces[i]);
				interfaces[i].write();
				cd.bNumInterfaces++;
			}
			cd.interfaces = ArrayUtil.at(interfaces, 0);
		}

		@SafeVarargs
		public static void interfaceDescriptors(libusb_config_descriptor cd, libusb_interface it,
			Consumer<libusb_interface_descriptor.ByRef>... populators) {
			var descs = Struct.<libusb_interface_descriptor.ByRef>arrayByVal(
				libusb_interface_descriptor.ByRef::new, libusb_interface_descriptor.ByRef[]::new,
				populators.length);
			for (int i = 0; i < populators.length; i++) {
				descs[i].bLength = LIBUSB_DT_INTERFACE_SIZE;
				descs[i].bDescriptorType = (byte) LIBUSB_DT_INTERFACE.value;
				descs[i].bInterfaceNumber = cd.bNumInterfaces;
				descs[i].bAlternateSetting = (byte) i;
				populators[i].accept(descs[i]);
				descs[i].write();
				it.num_altsetting++;
			}
			it.altsetting = ArrayUtil.at(descs, 0);
		}

		@SafeVarargs
		public static void endPointDescriptors(libusb_interface_descriptor id,
			Consumer<libusb_endpoint_descriptor.ByRef>... populators) {
			var descs = Struct.<libusb_endpoint_descriptor.ByRef>arrayByVal(
				libusb_endpoint_descriptor.ByRef::new, libusb_endpoint_descriptor.ByRef[]::new,
				populators.length);
			for (int i = 0; i < populators.length; i++) {
				descs[i].bLength = LIBUSB_DT_ENDPOINT_SIZE;
				descs[i].bDescriptorType = (byte) LIBUSB_DT_ENDPOINT.value;
				populators[i].accept(descs[i]);
				descs[i].write();
				id.bNumEndpoints++;
			}
			id.endpoint = ArrayUtil.at(descs, 0);
		}

		public static void ssEndPointCompanionDesc(DeviceConfig dc, libusb_endpoint_descriptor ep,
			Consumer<libusb_ss_endpoint_companion_descriptor> populator) {
			var desc = new libusb_ss_endpoint_companion_descriptor(null);
			desc.bLength = LIBUSB_DT_SS_ENDPOINT_COMPANION_SIZE;
			desc.bDescriptorType = (byte) LIBUSB_DT_SS_ENDPOINT_COMPANION.value;
			populator.accept(desc);
			dc.ssEpCompDescs.put(ep.getPointer(), Struct.write(desc));
		}

		@SafeVarargs
		public static void bosDescriptor(DeviceConfig dc, Consumer<libusb_bos_descriptor> populator,
			libusb_bos_dev_capability_descriptor.ByRef... capabilities) {
			dc.bos = new libusb_bos_descriptor(null);
			dc.bos.bLength = LIBUSB_DT_BOS_SIZE;
			dc.bos.bDescriptorType = (byte) LIBUSB_DT_BOS.value;
			populator.accept(dc.bos);
			dc.bos.bNumDeviceCaps = (byte) capabilities.length;
			dc.bos.dev_capability = capabilities;
			Struct.write(dc.bos);
		}

		public static libusb_bos_dev_capability_descriptor.ByRef
			bosUsb20Ext(Consumer<libusb_usb_2_0_extension_descriptor> populator) {
			libusb_usb_2_0_extension_descriptor desc =
				new libusb_usb_2_0_extension_descriptor(null);
			desc.bLength = LIBUSB_BT_USB_2_0_EXTENSION_SIZE;
			desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE_CAPABILITY.value;
			desc.bDevCapabilityType = (byte) LIBUSB_BT_USB_2_0_EXTENSION.value;
			populator.accept(desc);
			return toBdc(desc);
		}

		public static libusb_bos_dev_capability_descriptor.ByRef
			bosSsUsbDevCap(Consumer<libusb_ss_usb_device_capability_descriptor> populator) {
			libusb_ss_usb_device_capability_descriptor desc =
				new libusb_ss_usb_device_capability_descriptor(null);
			desc.bLength = LIBUSB_BT_SS_USB_DEVICE_CAPABILITY_SIZE;
			desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE_CAPABILITY.value;
			desc.bDevCapabilityType = (byte) LIBUSB_BT_SS_USB_DEVICE_CAPABILITY.value;
			populator.accept(desc);
			return toBdc(desc);
		}

		public static libusb_bos_dev_capability_descriptor.ByRef
			bosContainerId(Consumer<libusb_container_id_descriptor> populator) {
			libusb_container_id_descriptor desc = new libusb_container_id_descriptor(null);
			desc.bLength = LIBUSB_BT_CONTAINER_ID_SIZE;
			desc.bDescriptorType = (byte) LIBUSB_DT_DEVICE_CAPABILITY.value;
			desc.bDevCapabilityType = (byte) LIBUSB_BT_CONTAINER_ID.value;
			populator.accept(desc);
			return toBdc(desc);
		}

		public static void containerId(libusb_container_id_descriptor bdc, int... bytes) {
			Immutable.wrap(bytes).copyTo(0, bdc.ContainerID);
		}

		public static libusb_bos_dev_capability_descriptor.ByRef toBdc(Structure desc) {
			return Struct.adapt(Struct.write(desc),
				libusb_bos_dev_capability_descriptor.ByRef::new);
		}
	}

	private static Pointer pointer() {
		return new Memory(JnaSize.POINTER.size);
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
	}

	public static class Context extends Data {
		public int debugLevel = 0;
		public boolean usbDk = false;
		public boolean weakAuth = false;
		public boolean eventHandling = true;
		public ReentrantLock eventLock = new ReentrantLock();
		public Lock eventWaiterLock = new ReentrantLock();
		public libusb_pollfd_added_cb pollFdAddedCb = null;
		public libusb_pollfd_removed_cb pollFdRemovedCb = null;

		private Context(Pointer p) {
			super(p);
		}
	}

	public static class DeviceList extends Data {
		public final int size;
		public Context context;

		private DeviceList(int size) {
			super(GcMemory.malloc(JnaSize.POINTER.size * (size + 1)).m);
			this.size = size;
		}

		private Pointer p(int i) {
			ValidationUtil.validateIndex(size, i);
			return p.share(i * JnaSize.POINTER.size);
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
		public int streamIds = 0;
		public int kernelDriverInterfaceBits = 0;
		public ByteProvider endPoints = ByteProvider.empty(); // mapped to stream ids

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
			return ArrayUtil.at(configDescriptors, i);
		}

		public libusb_config_descriptor configDescriptorByValue(int value) {
			for (var c : configDescriptors)
				if (c.bConfigurationValue == (byte) value) return c;
			return null;
		}
	}

	public static class Transfer extends Data {
		public boolean submitted = false;
		public int streamId;

		private Transfer(Pointer p) {
			super(p);
		}

		public libusb_transfer transfer() {
			return Struct.read(new libusb_transfer(p));
		}
	}

	public static class HotPlug extends Data {
		public Context context;
		public final int handle;
		public int events;
		public int flags;
		public int vendorId;
		public int productId;
		public int devClass;
		public libusb_hotplug_callback_fn callback;
		public Pointer userData;

		private HotPlug() {
			handle = id;
		}
	}

	public LibUsbTestData() {
		reset();
	}

	/**
	 * Clears state, but does not clear device configurations (sample data).
	 */
	public void reset() {
		contexts.clear();
		deviceLists.clear();
		devices.clear();
		deviceHandles.clear();
		version("http://libusb.info", "", 1, 0, 24, 11584);
		capabilities(0);
		locale("en-US");
	}

	/**
	 * Clears device configuration (sample data).
	 */
	public void clearConfig() {
		deviceConfigs.clear();
	}

	public void addConfig(DeviceConfig... configs) {
		Collections.addAll(deviceConfigs, configs);
	}

	/**
	 * Sets the libusb version desciptor.
	 */
	public void version(String desc, String rc, int... nums) {
		version = new libusb_version();
		version.describe = desc;
		version.rc = rc;
		int i = 0;
		version.major = (short) (nums.length > i ? nums[i] : 0);
		version.minor = (short) (nums.length > ++i ? nums[i] : 0);
		version.micro = (short) (nums.length > ++i ? nums[i] : 0);
		version.nano = (short) (nums.length > ++i ? nums[i] : 0);
	}

	public libusb_version version() {
		return version;
	}

	public void capabilities(int capabilities) {
		this.capabilities = capabilities;
	}

	public int capabilities() {
		return capabilities;
	}

	public void locale(String locale) {
		this.locale = locale;
	}

	public String locale() {
		return locale;
	}

	public Context createContextDef() {
		Context def = find(contexts, t -> t.p == null, null);
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

	public Context context(libusb_context ctx) {
		return find(contexts, PointerUtil.pointer(ctx));
	}

	public void removeContext(libusb_context ctx) {
		var p = PointerUtil.pointer(ctx);
		var context = get(contexts, p);
		if (context == null) return;
		deviceLists.removeIf(t -> t.context == context);
		devices.removeIf(t -> t.deviceList.context == context);
		deviceHandles.removeIf(t -> t.device.deviceList.context == context);
		hotPlugs.removeIf(t -> t.context == context);
		if (p != null) contexts.remove(context);
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

	public Device device(libusb_device dev) {
		return find(devices, PointerUtil.pointer(dev));
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
		if (device.refs <= 0) removeDevice(device);
	}

	private void purgeDevices() {
		devices.removeIf(d -> d.refs <= 0);
	}

	private void removeDevice(Device device) {
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

	public DeviceHandle deviceHandle(libusb_device_handle handle) {
		return find(deviceHandles, PointerUtil.pointer(handle));
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
		return List.copyOf(transfers);
	}

	public void removeTransfer(Transfer transfer) {
		transfers.remove(transfer);
	}

	public HotPlug createHotPlug(Context context) {
		var hotPlug = new HotPlug();
		hotPlug.context = context;
		hotPlugs.add(hotPlug);
		return hotPlug;
	}

	public HotPlug hotPlug(int handle) {
		return find(hotPlugs, t -> 
			t.handle == handle
		, null);
	}

	public List<HotPlug> hotPlugs() {
		return List.copyOf(hotPlugs);
	}

	public void removeHotPlug(int handle) {
		hotPlugs.removeIf(t -> 
			t.handle == handle
		);
	}

	private static <T extends Data> void forEach(List<T> list, Predicate<? super T> filter,
		Consumer<? super T> action) {
		list.stream().filter(filter).forEach(action);
	}

	private static <T extends Data> T get(List<T> list, Pointer p) {
		return find(list, t -> Objects.equals(t.p, p), null);
	}

	private static <T extends Data> T find(List<T> list, Pointer p) {
		return find(list, t -> Objects.equals(t.p, p), () -> lastError(LIBUSB_ERROR_NOT_FOUND));
	}

	private static <E extends Exception, T extends Data> T find(List<T> list,
		Predicate<? super T> filter, Supplier<E> exFn) throws E {
		T t = list.stream().filter(filter).findFirst().orElse(null);
		if (t != null || exFn == null) return t;
		throw exFn.get();
	}

}
