package ceri.serial.libusb;

import static ceri.common.collection.ImmutableUtil.collectAsList;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.ushort;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_bos_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_config_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_container_id_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_ss_endpoint_companion_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_ss_usb_device_capability_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_free_usb_2_0_extension_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_container_id_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_ss_endpoint_companion_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_ss_usb_device_capability_descriptor;
import static ceri.serial.libusb.jna.LibUsb.libusb_get_usb_2_0_extension_descriptor;
import java.io.Closeable;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.data.ByteProvider;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_dev_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_bos_type;
import ceri.serial.libusb.jna.LibUsb.libusb_class_code;
import ceri.serial.libusb.jna.LibUsb.libusb_config_attributes;
import ceri.serial.libusb.jna.LibUsb.libusb_config_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_container_id_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction;
import ceri.serial.libusb.jna.LibUsb.libusb_interface;
import ceri.serial.libusb.jna.LibUsb.libusb_interface_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_iso_sync_type;
import ceri.serial.libusb.jna.LibUsb.libusb_iso_usage_type;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_endpoint_companion_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_usb_device_capability_attributes;
import ceri.serial.libusb.jna.LibUsb.libusb_ss_usb_device_capability_descriptor;
import ceri.serial.libusb.jna.LibUsb.libusb_supported_speed;
import ceri.serial.libusb.jna.LibUsb.libusb_transfer_type;
import ceri.serial.libusb.jna.LibUsb.libusb_usb_2_0_extension_attributes;
import ceri.serial.libusb.jna.LibUsb.libusb_usb_2_0_extension_descriptor;
import ceri.serial.libusb.jna.LibUsbException;

/**
 * Collection of descriptor types. Device descriptor is accessible from libusb_device. Interface and
 * end-point descriptors are accessible from libusb_device. Bos descriptors are accessible from
 * libusb_device_handle.
 * 
 * <pre>
 * - libusb_device 
 *   - libusb_device_descriptor
 *   - libusb_config_descriptor
 *     - libusb_interface[]
 *       - libusb_interface_descriptor[]
 *         - libusb_endpoint_descriptor[]
 *           - libusb_ss_endpoint_companion_descriptor
 *           
 * - libusb_device_handle
 *   - libusb_bos_descriptor
 *     - libusb_bos_dev_capability_descriptor[]
 *       - libusb_usb_2_0_extension_descriptor
 *       - libusb_ss_usb_device_capability_descriptor
 *       - libusb_container_id_descriptor
 * </pre>
 */
public class UsbDescriptors {
	private static final Logger logger = LogManager.getLogger();

	public static class Device {
		private final libusb_device_descriptor descriptor;

		Device(libusb_device_descriptor descriptor) {
			this.descriptor = descriptor;
		}

		public int usbVersion() {
			return ushort(descriptor.bcdUSB);
		}

		public libusb_class_code classCode() {
			return descriptor.bDeviceClass();
		}

		public int subClass() {
			return ubyte(descriptor.bDeviceSubClass);
		}

		public int protocol() {
			return ubyte(descriptor.bDeviceProtocol);
		}

		public int maxPacketSize0() {
			return ubyte(descriptor.bMaxPacketSize0);
		}

		public int vendorId() {
			return ushort(descriptor.idVendor);
		}

		public int productId() {
			return ushort(descriptor.idProduct);
		}

		public int deviceVersion() {
			return ushort(descriptor.bcdDevice);
		}

		public String manufacturer(UsbDeviceHandle handle) throws LibUsbException {
			return handle.stringDescriptorAscii(ubyte(descriptor.iManufacturer));
		}

		public String product(UsbDeviceHandle handle) throws LibUsbException {
			return handle.stringDescriptorAscii(ubyte(descriptor.iProduct));
		}

		public String serialNumber(UsbDeviceHandle handle) throws LibUsbException {
			return handle.stringDescriptorAscii(ubyte(descriptor.iSerialNumber));
		}

		public int configurationCount() {
			return ubyte(descriptor.bNumConfigurations);
		}
	}

	public static class Config implements Closeable {
		private final UsbDevice device;
		private libusb_config_descriptor descriptor;
		private List<Interface> interfaces = null;

		Config(UsbDevice device, libusb_config_descriptor descriptor) {
			this.device = device;
			this.descriptor = descriptor;
		}

		public int value() {
			return ubyte(descriptor().bConfigurationValue);
		}

		public String description(UsbDeviceHandle handle) throws LibUsbException {
			return handle.stringDescriptorAscii(ubyte(descriptor.iConfiguration));
		}

		public Set<libusb_config_attributes> attributes() {
			return descriptor().bmAttributes();
		}

		public int maxPower() {
			return ubyte(descriptor().bMaxPower);
		}

		public int interfaceCount() {
			return ubyte(descriptor().bNumInterfaces);
		}

		public List<Interface> interfaces() {
			if (interfaces == null) interfaces = collectAsList(
				Stream.of(descriptor().interfaces()).map(t -> new Interface(this, t)));
			return interfaces;
		}

		public ByteProvider extra() {
			return wrap(descriptor.extra());
		}

		@Override
		public void close() {
			LogUtil.execute(logger, () -> libusb_free_config_descriptor(descriptor));
			descriptor = null;
		}

		private libusb_config_descriptor descriptor() {
			if (descriptor != null) return descriptor;
			throw new IllegalStateException("Config descriptor has been closed");
		}
	}

	public static class Interface {
		private final Config config;
		private libusb_interface iface;
		private List<AltSetting> descriptors = null;

		Interface(Config config, libusb_interface iface) {
			this.config = config;
			this.iface = iface;
		}

		public int altSettingCount() {
			return iface.num_altsetting;
		}

		public List<AltSetting> altSettings() {
			if (descriptors == null) descriptors =
				collectAsList(Stream.of(iface.altsettings()).map(t -> new AltSetting(this, t)));
			return descriptors;
		}
	}

	public static class AltSetting {
		private final Interface iface;
		private libusb_interface_descriptor descriptor;
		private List<EndPoint> descriptors = null;

		AltSetting(Interface iface, libusb_interface_descriptor descriptor) {
			this.iface = iface;
			this.descriptor = descriptor;
		}

		public int number() {
			return ubyte(descriptor.bInterfaceNumber);
		}

		public String description(UsbDeviceHandle handle) throws LibUsbException {
			return handle.stringDescriptorAscii(ubyte(descriptor.iInterface));
		}

		public int altSetting() {
			return ubyte(descriptor.bAlternateSetting);
		}

		public libusb_class_code classCode() {
			return descriptor.bInterfaceClass();
		}

		public int subClass() {
			return ubyte(descriptor.bInterfaceSubClass);
		}

		public int protocol() {
			return ubyte(descriptor.bInterfaceProtocol);
		}

		public int endPointCount() {
			return ubyte(descriptor.bNumEndpoints);
		}

		public List<EndPoint> endPoints() {
			if (descriptors == null) descriptors =
				collectAsList(Stream.of(descriptor.endpoints()).map(t -> new EndPoint(this, t)));
			return descriptors;
		}

		public ByteProvider extra() {
			return wrap(descriptor.extra());
		}
	}

	public static class EndPoint {
		private final AltSetting altSetting;
		private libusb_endpoint_descriptor descriptor;

		EndPoint(AltSetting altSetting, libusb_endpoint_descriptor descriptor) {
			this.altSetting = altSetting;
			this.descriptor = descriptor;
		}

		/**
		 * Returns the super-speed end-point companion descriptor. Returns null if the configuration
		 * is not supported.
		 */
		public SsEndPointCompanion ssEndPointCompanion() throws LibUsbException {
			@SuppressWarnings("resource")
			var context = altSetting.iface.config.device.usb().context();
			var ssDesc = libusb_get_ss_endpoint_companion_descriptor(context, descriptor);
			return ssDesc == null ? null : new SsEndPointCompanion(ssDesc);
		}

		public int endPointAddress() {
			return ubyte(descriptor.bEndpointAddress);
		}

		public int endPointNumber() {
			return descriptor.bEndpointNumber();
		}

		public libusb_endpoint_direction endPointDirection() {
			return descriptor.bEndpointDirection();
		}

		public int attributes() {
			return ubyte(descriptor.bmAttributes);
		}

		public libusb_transfer_type transferType() {
			return descriptor.bmAttributesTransferType();
		}

		public libusb_iso_sync_type isoSyncType() {
			return descriptor.bmAttributesIsoSyncType();
		}

		public libusb_iso_usage_type isoUsageType() {
			return descriptor.bmAttributesIsoUsageType();
		}

		public int maxPacketSize() {
			return ushort(descriptor.wMaxPacketSize);
		}

		public int pollInterval() {
			return ubyte(descriptor.bInterval);
		}

		public int audioRefreshRate() {
			return ubyte(descriptor.bRefresh);
		}

		public int audioSyncAddress() {
			return ubyte(descriptor.bSynchAddress);
		}

		public ByteProvider extra() {
			return wrap(descriptor.extra());
		}
	}

	public static class SsEndPointCompanion implements Closeable {
		private libusb_ss_endpoint_companion_descriptor descriptor;

		SsEndPointCompanion(libusb_ss_endpoint_companion_descriptor descriptor) {
			this.descriptor = descriptor;
		}

		public int maxBurstPackets() {
			return ubyte(descriptor().bMaxBurst);
		}

		public int attributes() {
			return ubyte(descriptor().bmAttributes);
		}

		public int maxBulkStreams() {
			return descriptor().bmAttributesBulkMaxStreams();
		}

		public int isoMult() {
			return descriptor().bmAttributesIsoMult();
		}

		public int bytesPerInterval() {
			return ushort(descriptor().wBytesPerInterval);
		}

		@Override
		public void close() {
			LogUtil.execute(logger, () -> libusb_free_ss_endpoint_companion_descriptor(descriptor));
			descriptor = null;
		}

		private libusb_ss_endpoint_companion_descriptor descriptor() {
			if (descriptor != null) return descriptor;
			throw new IllegalStateException("Descriptor has been closed");
		}
	}

	public static class Bos implements Closeable {
		private final UsbDeviceHandle handle;
		private libusb_bos_descriptor descriptor;
		private List<BosDevCapability> capabilities = null;

		Bos(UsbDeviceHandle handle, libusb_bos_descriptor descriptor) {
			this.handle = handle;
			this.descriptor = descriptor;
		}

		public int capabilityCount() {
			return ubyte(descriptor().bNumDeviceCaps);
		}

		public List<BosDevCapability> capabilities() {
			if (capabilities == null) capabilities = collectAsList(
				Stream.of(descriptor().dev_capability).map(t -> new BosDevCapability(this, t)));
			return capabilities;
		}

		@Override
		public void close() {
			LogUtil.execute(logger, () -> libusb_free_bos_descriptor(descriptor));
			descriptor = null;
		}

		private libusb_bos_descriptor descriptor() {
			if (descriptor != null) return descriptor;
			throw new IllegalStateException("Descriptor has been closed");
		}
	}

	public static class BosDevCapability {
		private final Bos bos;
		private libusb_bos_dev_capability_descriptor descriptor;

		BosDevCapability(Bos bos, libusb_bos_dev_capability_descriptor descriptor) {
			this.bos = bos;
			this.descriptor = descriptor;
		}

		public libusb_bos_type bosType() {
			return descriptor.bDevCapabilityType();
		}

		public ByteProvider capabilityData() {
			return wrap(descriptor.dev_capability_data);
		}

		public SsUsbDeviceCapability ssUsbDeviceCapability() throws LibUsbException {
			if (bosType() != libusb_bos_type.LIBUSB_BT_SS_USB_DEVICE_CAPABILITY) return null;
			return new SsUsbDeviceCapability(
				libusb_get_ss_usb_device_capability_descriptor(context(), descriptor));
		}

		public Usb20Extension usb20Extension() throws LibUsbException {
			if (bosType() != libusb_bos_type.LIBUSB_BT_USB_2_0_EXTENSION) return null;
			return new Usb20Extension(
				libusb_get_usb_2_0_extension_descriptor(context(), descriptor));
		}

		public ContainerId containerId() throws LibUsbException {
			if (bosType() != libusb_bos_type.LIBUSB_BT_CONTAINER_ID) return null;
			return new ContainerId(libusb_get_container_id_descriptor(context(), descriptor));
		}

		private libusb_context context() {
			return bos.handle.context();
		}
	}

	public static class Usb20Extension implements Closeable {
		private libusb_usb_2_0_extension_descriptor descriptor;

		Usb20Extension(libusb_usb_2_0_extension_descriptor descriptor) {
			this.descriptor = descriptor;
		}

		public Set<libusb_usb_2_0_extension_attributes> attributes() {
			return descriptor().bmAttributes();
		}

		@Override
		public void close() {
			LogUtil.execute(logger, () -> libusb_free_usb_2_0_extension_descriptor(descriptor));
			descriptor = null;
		}

		private libusb_usb_2_0_extension_descriptor descriptor() {
			if (descriptor != null) return descriptor;
			throw new IllegalStateException("Descriptor has been closed");
		}
	}

	public static class SsUsbDeviceCapability implements Closeable {
		private libusb_ss_usb_device_capability_descriptor descriptor;

		SsUsbDeviceCapability(libusb_ss_usb_device_capability_descriptor descriptor) {
			this.descriptor = descriptor;
		}

		public Set<libusb_ss_usb_device_capability_attributes> attributes() {
			return descriptor().bmAttributes();
		}

		public Set<libusb_supported_speed> supportedSpeeds() {
			return descriptor().wSpeedSupported();
		}

		public int functionalitySupport() {
			return ubyte(descriptor().bFunctionalitySupport);
		}

		public int u1DeviceExitLatency() {
			return ubyte(descriptor().bU1DevExitLat);
		}

		public int u2DeviceExitLatency() {
			return ushort(descriptor().wU2DevExitLat);
		}

		@Override
		public void close() {
			LogUtil.execute(logger,
				() -> libusb_free_ss_usb_device_capability_descriptor(descriptor));
			descriptor = null;
		}

		private libusb_ss_usb_device_capability_descriptor descriptor() {
			if (descriptor != null) return descriptor;
			throw new IllegalStateException("Descriptor has been closed");
		}
	}

	public static class ContainerId implements Closeable {
		private libusb_container_id_descriptor descriptor;

		ContainerId(libusb_container_id_descriptor descriptor) {
			this.descriptor = descriptor;
		}

		public ByteProvider uuid() {
			return wrap(descriptor().ContainerID);
		}

		@Override
		public void close() {
			LogUtil.execute(logger, () -> libusb_free_container_id_descriptor(descriptor));
			descriptor = null;
		}

		private libusb_container_id_descriptor descriptor() {
			if (descriptor != null) return descriptor;
			throw new IllegalStateException("Descriptor has been closed");
		}
	}

	private static ByteProvider wrap(byte[] bytes) {
		if (bytes == null) return ByteProvider.empty();
		return ByteProvider.of(bytes);
	}
}
