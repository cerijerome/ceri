package ceri.serial.libusb.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;
import ceri.common.text.StringUtil;
import ceri.serial.libusb.jna.LibUsbFinder.libusb_device_criteria;

public class LibUsbFinderTest {

	@Test
	public void testEmptyCriteriaFromString() {
		assertCriteria(LibUsbFinder.libusb_find_criteria_string(null),
			LibUsbFinder.libusb_find_criteria());
		assertCriteria(LibUsbFinder.libusb_find_criteria_string(""),
			LibUsbFinder.libusb_find_criteria());
		assertCriteria(LibUsbFinder.libusb_find_criteria_string(":::::::::"),
			LibUsbFinder.libusb_find_criteria());
		assertCriteria(LibUsbFinder.libusb_find_criteria_string("::::"),
			LibUsbFinder.libusb_find_criteria());
		assertCriteria(LibUsbFinder.libusb_find_criteria_string("0:0:0:0:::0"),
			LibUsbFinder.libusb_find_criteria());
		assertCriteria(LibUsbFinder.libusb_find_criteria_string("0:0:0:0:\"\":\"\":0"),
			LibUsbFinder.libusb_find_criteria());
		assertCriteria(LibUsbFinder.libusb_find_criteria_string("0:0:0:0:\"\":\"\""),
			LibUsbFinder.libusb_find_criteria());
	}

	@Test
	public void testCriteriaNumericsFromString() {
		assertCriteria(LibUsbFinder.libusb_find_criteria_string("555"),
			LibUsbFinder.libusb_find_criteria().vendor(555));
		assertCriteria(LibUsbFinder.libusb_find_criteria_string("0x0403:100"),
			LibUsbFinder.libusb_find_criteria().vendor(0x0403).product(100));
		assertCriteria(LibUsbFinder.libusb_find_criteria_string(":0x2:0xff:128"),
			LibUsbFinder.libusb_find_criteria().product(0x2).busNumber(0xff).deviceAddress(128));
		assertCriteria(LibUsbFinder.libusb_find_criteria_string("::1:0x10:a b c"), LibUsbFinder
			.libusb_find_criteria().busNumber(1).deviceAddress(0x10).description("a b c"));
		assertCriteria(LibUsbFinder.libusb_find_criteria_string("::::\":a:b:\":a \"b\":1"),
			LibUsbFinder.libusb_find_criteria().description(":a:b:").serial("a \"b\"").index(1));
	}

	private void assertCriteria(libusb_device_criteria actual, libusb_device_criteria expected) {
		assertEquals(actual.vendor, expected.vendor);
		assertEquals(actual.product, expected.product);
		assertEquals(actual.busNumber, expected.busNumber);
		assertEquals(actual.deviceAddress, expected.deviceAddress);
		if (StringUtil.isBlank(expected.description))
			assertTrue(StringUtil.isBlank(actual.description));
		else assertEquals(actual.description, expected.description);
		if (StringUtil.isBlank(expected.serial)) assertTrue(StringUtil.isBlank(actual.serial));
		else assertEquals(actual.serial, expected.serial);
		assertEquals(actual.index, expected.index);
	}

}
