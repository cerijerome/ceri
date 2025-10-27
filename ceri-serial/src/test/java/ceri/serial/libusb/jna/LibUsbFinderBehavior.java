package ceri.serial.libusb.jna;

import org.junit.After;
import org.junit.Test;
import ceri.common.function.Enclosure;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;
import ceri.jna.type.JnaSize;
import ceri.jna.util.GcMemory;
import ceri.jna.util.PointerUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.test.LibUsbSampleData;
import ceri.serial.libusb.test.TestLibUsbNative;

public class LibUsbFinderBehavior {
	private final libusb_context nullCtx = null;
	private TestLibUsbNative lib;
	private Enclosure<TestLibUsbNative> enc;

	@After
	public void after() {
		lib = null;
		if (enc != null) enc.close();
		enc = null;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = LibUsbFinder.of(7, 9);
		var eq0 = LibUsbFinder.of(7, 9);
		var ne0 = LibUsbFinder.of(8, 9);
		var ne1 = LibUsbFinder.of(7, 8);
		var ne2 = LibUsbFinder.builder().vendor(7).product(9).bus(1).build();
		var ne3 = LibUsbFinder.builder().vendor(7).product(9).address(2).build();
		var ne4 = LibUsbFinder.builder().vendor(7).product(9).description("x").build();
		var ne5 = LibUsbFinder.builder().vendor(7).product(9).serial("x").build();
		var ne6 = LibUsbFinder.builder().vendor(7).product(9).index(1).build();
		TestUtil.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.string(LibUsbFinder.builder().serial("xyz").build(), "{vendor=any, product=any, "
			+ "bus=any, address=any, description=any, serial=\"xyz\", index=0}");
	}

	@Test
	public void shouldCopyFromFinder() {
		var b0 = LibUsbFinder.builder().vendor(0x123).product(7).bus(2).address(3)
			.description("desc").serial("serial").build();
		var b = LibUsbFinder.builder(b0).index(1).build();
		Assert.equal(b.vendor, 0x123);
		Assert.equal(b.product, 7);
		Assert.equal(b.bus, 2);
		Assert.equal(b.address, 3);
		Assert.equal(b.description, "desc");
		Assert.equal(b.serial, "serial");
		Assert.equal(b.index, 1);
	}

	@Test
	public void shouldCreateFromDescriptor() {
		var b = LibUsbFinder.builder();
		Assert.equal(LibUsbFinder.from("0"), b.build());
		Assert.equal(LibUsbFinder.from("0x123"), b.vendor(0x123).build());
		Assert.equal(LibUsbFinder.from("0x123:0x456"), b.product(0x456).build());
		Assert.equal(LibUsbFinder.from("0x123:0x456:0xbc"), b.bus(0xbc).build());
		Assert.equal(LibUsbFinder.from("0x123:0x456:0xbc:0xde"), b.address(0xde).build());
		Assert.equal(LibUsbFinder.from("0x123:0x456:0xbc:0xde:test"),
			b.description("test").build());
		Assert.equal(LibUsbFinder.from("0x123:0x456:0xbc:0xde:test:serial#"),
			b.serial("serial#").build());
		Assert.equal(LibUsbFinder.from("0x123:0x456:0xbc:0xde:test:serial#:1"), b.index(1).build());

	}

	@Test
	public void shouldCreateDescriptor() {
		var b = LibUsbFinder.builder();
		Assert.equal(b.build().asDescriptor(), "0");
		Assert.equal(b.vendor(0x123).build().asDescriptor(), "0x0123");
		Assert.equal(b.product(0x456).build().asDescriptor(), "0x0123:0x0456");
		Assert.equal(b.bus(0xbc).build().asDescriptor(), "0x0123:0x0456:0xbc");
		Assert.equal(b.address(0xde).build().asDescriptor(), "0x0123:0x0456:0xbc:0xde");
		Assert.equal(b.description("test").build().asDescriptor(), "0x0123:0x0456:0xbc:0xde:test");
		Assert.equal(b.serial("serial#").build().asDescriptor(),
			"0x0123:0x0456:0xbc:0xde:test:serial#");
		Assert.equal(b.index(1).build().asDescriptor(), "0x0123:0x0456:0xbc:0xde:test:serial#:1");
	}

	@Test
	public void shouldDetermineMatchingDevice() throws LibUsbException {
		initLib();
		var finder = LibUsbFinder.builder().vendor(0x04f2).build();
		Assert.no(finder.matches());
		Assert.equal(finder.matchCount(), 0);
		lib.data.addConfig(LibUsbSampleData.mouseConfig());
		Assert.yes(finder.matches());
		Assert.equal(finder.matchCount(), 1);
		lib.data.addConfig(LibUsbSampleData.mouseConfig());
		Assert.yes(finder.matches());
		Assert.equal(finder.matchCount(), 2);
	}

	@Test
	public void shouldFailIfVendorDoesNotMatch() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.mouseConfig());
		var finder = LibUsbFinder.builder().vendor(0xffff).build();
		Assert.thrown(() -> finder.findAndRef(null));
	}

	@Test
	public void shouldFailIfNotFound() throws LibUsbException {
		initLib();
		Assert.thrown(() -> LibUsbFinder.FIRST.findAndOpen(null)); // device not found
		Assert.thrown(() -> LibUsbFinder.FIRST.findAndRef(null)); // device not found
		libusb_context ctx =
			PointerUtil.set(new libusb_context(), GcMemory.malloc(JnaSize.POINTER.get()).m);
		Assert.thrown(() -> LibUsbFinder.FIRST.findAndRef(ctx, 1)); // device not found
	}

	@Test
	public void shouldFindAndRefDevice() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.mouseConfig());
		lib.data.addConfig(LibUsbSampleData.mouseConfig());
		var finder = LibUsbFinder.builder().vendor(0x04f2).build();
		Assert.equal(finder.findAndRef(null, 2).size(), 2);
		Assert.notNull(finder.findAndRef(null));
	}

	@Test
	public void shouldFindWithCallback() throws LibUsbException {
		initLib();
		lib.data.addConfig(LibUsbSampleData.mouseConfig());
		Assert.no(LibUsbFinder.builder().bus(1).build().findWithCallback(nullCtx, _ -> true));
		Assert.no(LibUsbFinder.builder().address(1).build().findWithCallback(nullCtx, _ -> true));
		Assert.no(LibUsbFinder.builder().index(1).build().findWithCallback(nullCtx, _ -> true));
		Assert.no(
			LibUsbFinder.builder().description("x").build().findWithCallback(nullCtx, _ -> true));
		Assert.no(
			LibUsbFinder.builder().serial("x").build().findWithCallback(nullCtx, _ -> true));
	}

	private void initLib() throws LibUsbException {
		enc = TestLibUsbNative.register();
		lib = enc.ref;
		LibUsb.libusb_init_default();
	}
}
