package ceri.speech.synthesis.custom;

import java.util.List;

import junit.framework.TestCase;
import ceri.common.collection.ArrayUtil;
import ceri.speech.synthesis.custom.Numbers;

public class NumbersTest extends TestCase {

	public void testDigits() {
		assertEquals(ArrayUtil.asList("1", "2", "3", "4", "5"),
			showDigits(12345));
	}
	
	public void testLongs() {
		assertEquals(ArrayUtil.asList("0"), showLong(0));
		assertEquals(ArrayUtil.asList("13"), showLong(13));
		assertEquals(ArrayUtil.asList("30", "3"), showLong(33));
		assertEquals(ArrayUtil.asList("1", "00"), showLong(100));
		assertEquals(ArrayUtil.asList("1", "00", "and", "1"), showLong(101));
		assertEquals(ArrayUtil.asList("1", "00", "and", "11"), showLong(111));
		assertEquals(ArrayUtil.asList("9", "00", "and", "90", "9"),
			showLong(999));
		assertEquals(ArrayUtil.asList("1", "000"), showLong(1000));
		assertEquals(ArrayUtil.asList("2", "000", "and", "1"), showLong(2001));
		assertEquals(ArrayUtil.asList(
			"9", "00", "and", "90", "9", "000", "9", "00", "and", "90", "9"),
			showLong(999999));
		assertEquals(ArrayUtil.asList("9", "00", "000000000", "and", "9"),
			showLong(900000000009L));
		assertEquals(ArrayUtil.asList("minus", "9", "00", "000000000000000"),
			showLong(-900000000000000000L));
	}
	
	public void testOrdinals() {
		assertEquals(ArrayUtil.asList("0th"), showOrdinal(0));
		assertEquals(ArrayUtil.asList("13th"), showOrdinal(13));
		assertEquals(ArrayUtil.asList("30", "3rd"), showOrdinal(33));
		assertEquals(ArrayUtil.asList("1", "00th"), showOrdinal(100));
		assertEquals(ArrayUtil.asList("1", "00", "and", "1st"),
			showOrdinal(101));
		assertEquals(ArrayUtil.asList("1", "00", "and", "11th"),
			showOrdinal(111));
		assertEquals(ArrayUtil.asList("9", "00", "and", "90", "9th"),
			showOrdinal(999));
		assertEquals(ArrayUtil.asList("1", "000th"), showOrdinal(1000));
		assertEquals(ArrayUtil.asList("2", "000", "and", "1st"),
			showOrdinal(2001));
		assertEquals(ArrayUtil.asList(
			"9", "00", "and", "90", "9", "000", "9", "00", "and", "90", "9th"),
			showOrdinal(999999));
		assertEquals(ArrayUtil.asList("9", "00", "000000000", "and", "9th"),
			showOrdinal(900000000009L));
	}
	
	private List<String> showLong(long number) {
		return Numbers.INSTANCE.getLongAsWords(number);
	}
	
	private List<String> showOrdinal(long number) {
		return Numbers.INSTANCE.getOrdinalAsWords(number);
	}
	
	private List<String> showDigits(long number) {
		return Numbers.INSTANCE.getDigitsAsWords(number);
	}
	
}
