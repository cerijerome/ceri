package ceri.serial.libusb.jna;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertToString;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.After;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.jna.util.GcMemory;
import ceri.jna.util.JnaSize;
import ceri.jna.util.PointerUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_context;

public class LibUsbFinderBehavior {
	private final libusb_context nullCtx = null;
	private TestLibUsbNative lib;
	private Enclosed<RuntimeException, TestLibUsbNative> enc;

	@After
	public void after() {
		lib = null;
		if (enc != null) enc.close();
		enc = null;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		LibUsbFinder t = LibUsbFinder.of(7, 9);
		LibUsbFinder eq0 = LibUsbFinder.of(7, 9);
		LibUsbFinder ne0 = LibUsbFinder.of(8, 9);
		LibUsbFinder ne1 = LibUsbFinder.of(7, 8);
		LibUsbFinder ne2 = LibUsbFinder.builder().vendor(7).product(9).bus(1).build();
		LibUsbFinder ne3 = LibUsbFinder.builder().vendor(7).product(9).address(2).build();
		LibUsbFinder ne4 = LibUsbFinder.builder().vendor(7).product(9).description("x").build();
		LibUsbFinder ne5 = LibUsbFinder.builder().vendor(7).product(9).serial("x").build();
		LibUsbFinder ne6 = LibUsbFinder.builder().vendor(7).product(9).index(1).build();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertToString(LibUsbFinder.builder().serial("xyz").build(), "{vendor=any, product=any, "
			+ "bus=any, address=any, description=any, serial=\"xyz\", index=0}");
	}

	@Test
	public void shouldCreateFromDescriptor() {
		var b = LibUsbFinder.builder();
		assertEquals(LibUsbFinder.from("0"), b.build());
		assertEquals(LibUsbFinder.from("0x123"), b.vendor(0x123).build());
		assertEquals(LibUsbFinder.from("0x123:0x456"), b.product(0x456).build());
		assertEquals(LibUsbFinder.from("0x123:0x456:0xbc"), b.bus(0xbc).build());
		assertEquals(LibUsbFinder.from("0x123:0x456:0xbc:0xde"), b.address(0xde).build());
		assertEquals(LibUsbFinder.from("0x123:0x456:0xbc:0xde:test"),
			b.description("test").build());
		assertEquals(LibUsbFinder.from("0x123:0x456:0xbc:0xde:test:serial#"),
			b.serial("serial#").build());
		assertEquals(LibUsbFinder.from("0x123:0x456:0xbc:0xde:test:serial#:1"), b.index(1).build());

	}

	@Test
	public void shouldCreateDescriptor() {
		var b = LibUsbFinder.builder();
		assertEquals(b.build().asDescriptor(), "0");
		assertEquals(b.vendor(0x123).build().asDescriptor(), "0x0123");
		assertEquals(b.product(0x456).build().asDescriptor(), "0x0123:0x0456");
		assertEquals(b.bus(0xbc).build().asDescriptor(), "0x0123:0x0456:0xbc");
		assertEquals(b.address(0xde).build().asDescriptor(), "0x0123:0x0456:0xbc:0xde");
		assertEquals(b.description("test").build().asDescriptor(), "0x0123:0x0456:0xbc:0xde:test");
		assertEquals(b.serial("serial#").build().asDescriptor(),
			"0x0123:0x0456:0xbc:0xde:test:serial#");
		assertEquals(b.index(1).build().asDescriptor(), "0x0123:0x0456:0xbc:0xde:test:serial#:1");
	}

	@Test
	public void shouldFailIfNotFound() throws LibUsbException {
		initLib();
		var finder = LibUsbFinder.builder().build();
		assertThrown(() -> finder.findAndOpen(null)); // device not found
		assertThrown(() -> finder.findAndRef(null)); // device not found
		libusb_context ctx =
			PointerUtil.set(new libusb_context(), GcMemory.malloc(JnaSize.POINTER.size).m);
		assertThrown(() -> finder.findAndRef(ctx, 1)); // device not found
	}

	@Test
	public void shouldFindAndRefDevice() throws LibUsbException {
		initLib();
		lib.data.deviceConfigs.add(LibUsbSampleData.mouseConfig());
		lib.data.deviceConfigs.add(LibUsbSampleData.mouseConfig());
		var finder = LibUsbFinder.builder().vendor(0x04f2).build();
		assertEquals(finder.findAndRef(null, 2).size(), 2);
		assertNotNull(finder.findAndRef(null));
	}

	@Test
	public void shouldFindWithCallback() throws LibUsbException {
		initLib();
		lib.data.deviceConfigs.add(LibUsbSampleData.mouseConfig());
		assertFalse(LibUsbFinder.builder().bus(1).build().findWithCallback(nullCtx, d -> true));
		assertFalse(LibUsbFinder.builder().address(1).build().findWithCallback(nullCtx, d -> true));
		assertFalse(LibUsbFinder.builder().index(1).build().findWithCallback(nullCtx, d -> true));
		assertFalse(
			LibUsbFinder.builder().description("x").build().findWithCallback(nullCtx, d -> true));
		assertFalse(
			LibUsbFinder.builder().serial("x").build().findWithCallback(nullCtx, d -> true));
	}

	private void initLib() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		LibUsb.libusb_init_default();
	}
}
