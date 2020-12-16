package ceri.serial.libusb.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class LibUsbFinderBehavior {

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

}
