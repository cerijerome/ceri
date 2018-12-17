package ceri.serial.ftdi.jna;

import static ceri.common.util.BasicUtil.isEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ceri.serial.ftdi.jna.LibFtdiFinder.ftdi_find_criteria;

public class LibFtdiFinderTest {

	@Test
	public void testEmptyCriteriaFromString() {
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string(null),
			LibFtdiFinder.ftdi_find_criteria());
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string(""),
			LibFtdiFinder.ftdi_find_criteria());
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string(":::::::::"),
			LibFtdiFinder.ftdi_find_criteria());
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string("::::"),
			LibFtdiFinder.ftdi_find_criteria());
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string("0:0:0:0:::0"),
			LibFtdiFinder.ftdi_find_criteria());
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string("0:0:0:0:\"\":\"\":0"),
			LibFtdiFinder.ftdi_find_criteria());
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string("0:0:0:0:\"\":\"\""),
			LibFtdiFinder.ftdi_find_criteria());
	}

	@Test
	public void testCriteriaNumericsFromString() {
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string("555"),
			LibFtdiFinder.ftdi_find_criteria().vendor(555));
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string("0x0403:100"),
			LibFtdiFinder.ftdi_find_criteria().vendor(0x0403).product(100));
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string(":0x2:0xff:128"),
			LibFtdiFinder.ftdi_find_criteria().product(0x2).busNumber(0xff).deviceAddress(128));
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string("::1:0x10:a b c"), LibFtdiFinder
			.ftdi_find_criteria().busNumber(1).deviceAddress(0x10).description("a b c"));
		assertCriteria(LibFtdiFinder.ftdi_find_criteria_string("::::\":a:b:\":a \"b\":1"),
			LibFtdiFinder.ftdi_find_criteria().description(":a:b:").serial("a \"b\"").index(1));
	}

	private void assertCriteria(ftdi_find_criteria actual, ftdi_find_criteria expected) {
		assertThat(actual.vendor, is(expected.vendor));
		assertThat(actual.product, is(expected.product));
		assertThat(actual.busNumber, is(expected.busNumber));
		assertThat(actual.deviceAddress, is(expected.deviceAddress));
		if (isEmpty(expected.description)) assertTrue(isEmpty(actual.description));
		else assertThat(actual.description, is(expected.description));
		if (isEmpty(expected.serial)) assertTrue(isEmpty(actual.serial));
		else assertThat(actual.serial, is(expected.serial));
		assertThat(actual.index, is(expected.index));
	}

}
