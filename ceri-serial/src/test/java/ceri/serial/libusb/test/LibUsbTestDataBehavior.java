package ceri.serial.libusb.test;

import org.junit.After;
import org.junit.Test;
import ceri.common.function.Enclosure;
import ceri.common.test.Assert;
import ceri.jna.test.JnaAssert;
import ceri.log.util.Logs;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_version;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder;
import ceri.serial.libusb.test.LibUsbTestData.Util;

public class LibUsbTestDataBehavior {
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;

	@After
	public void after() {
		Logs.close(enc);
		lib = null;
		enc = null;
	}

	@Test
	public void testUtil() {
		Assert.privateConstructor(Util.class);
		Util.device(dc -> {
			Util.configDescriptors(dc);
			Util.configDescriptors(dc, cd -> {
				Util.interfaces(cd);
				Util.interfaces(cd, i -> {
					Util.interfaceDescriptors(cd, i);
					Util.interfaceDescriptors(cd, i, id -> { // x.0.0
						Util.endPointDescriptors(id);
					});
				});
			});
			Util.bosDescriptor(dc, _ -> {});
		});
	}

	@Test
	public void shouldSetSampleData() throws LibUsbException {
		initLib();
		LibUsbSampleData.populate(lib.data);
		var list = LibUsb.libusb_get_device_list(null);
		Assert.equal(list.count(), 8);
	}

	@Test
	public void shouldCreateDefaultContext() throws LibUsbException {
		registerLib();
		LibUsb.libusb_init();
		Assert.isNull(LibUsb.libusb_init_default());
		Assert.isNull(LibUsb.libusb_init_default());
	}

	@Test
	public void shouldRemoveContexts() throws LibUsbException {
		initLib();
		var ctx = LibUsb.libusb_init();
		LibUsb.libusb_get_device_list(null);
		LibUsb.libusb_get_device_list(ctx); // will be freed with exit
		LibUsb.libusb_hotplug_register_callback(null, 0, 0, 0, 0, 0, null, null);
		LibUsb.libusb_exit(ctx);
		LibUsb.libusb_exit(ctx);
		lib.data.removeContext(null);
	}

	@Test
	public void shouldRemoveDevice() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.internalHubConfig());
		var h = LibUsbFinder.FIRST.findAndOpen(null);
		var d = LibUsb.libusb_get_device(h);
		LibUsb.libusb_unref_device(d);
	}

	@Test
	public void shouldFindParentInDeviceList() throws LibUsbException {
		initLib();
		var hub = LibUsbSampleData.internalHubConfig();
		var kb = LibUsbSampleData.kbConfig();
		kb.parent = hub;
		lib.data.addConfig(kb, hub);
		var list0 = LibUsb.libusb_get_device_list(null);
		var list1 = LibUsb.libusb_get_device_list(null);
		Assert.equal(LibUsb.libusb_get_parent(list0.get(0)), list0.get(1));
		Assert.equal(LibUsb.libusb_get_parent(list1.get(0)), list1.get(1));
	}

	@Test
	public void shouldOnlyAccessHotPlugWithContext() throws LibUsbException {
		initLib();
		var ctx = LibUsb.libusb_init();
		LibUsb.libusb_hotplug_register_callback(null, 0, 0, 0, 0, 0, null, null);
		var h = LibUsb.libusb_hotplug_register_callback(ctx, 0, 0, 0, 0, 0, null, null);
		JnaAssert.lastError(() -> lib.libusb_hotplug_get_user_data(null, h.value));
		LibUsb.libusb_hotplug_deregister_callback(null, h); // fails
	}

	@Test
	public void shouldSetVersion() throws LibUsbException {
		initLib();
		lib.data.version("desc", "rc", 1, 2, 3, 4);
		assertVersion(LibUsb.libusb_get_version(), "desc", "rc", 1, 2, 3, 4);
		lib.data.version("desc", "rc", 1, 2, 3);
		assertVersion(LibUsb.libusb_get_version(), "desc", "rc", 1, 2, 3, 0);
		lib.data.version("desc", "rc", 1, 2);
		assertVersion(LibUsb.libusb_get_version(), "desc", "rc", 1, 2, 0, 0);
		lib.data.version("desc", "rc", 1);
		assertVersion(LibUsb.libusb_get_version(), "desc", "rc", 1, 0, 0, 0);
		lib.data.version("desc", "rc");
		assertVersion(LibUsb.libusb_get_version(), "desc", "rc", 0, 0, 0, 0);
	}

	private static void assertVersion(libusb_version version, String desc, String rc, int... ns) {
		Assert.equal(version.describe, desc);
		Assert.equal(version.rc, rc);
		Assert.equal(version.major, (short) ns[0]);
		Assert.equal(version.minor, (short) ns[1]);
		Assert.equal(version.micro, (short) ns[2]);
		Assert.equal(version.nano, (short) ns[3]);
	}

	private void registerLib() {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
	}

	private void initLib() throws LibUsbException {
		registerLib();
		LibUsb.libusb_init_default();
	}
}
