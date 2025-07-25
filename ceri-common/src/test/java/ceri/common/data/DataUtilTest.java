package ceri.common.data;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.reader;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteArray.Immutable;

public class DataUtilTest {

	@Test
	public void testRequireSize() {
		DataUtil.requireMin(reader("abc"), 3);
		DataUtil.requireMin(reader("abcd"), 3);
		assertThrown(() -> DataUtil.requireMin(reader("ab"), 3));
	}

	@Test
	public void testExpectAsciiChars() {
		DataUtil.expectAscii(reader("abc"), 'a', 'b', 'c');
		DataUtil.expectAscii(reader("abcd"), 'a', 'b', 'c');
		var r = reader("abcde");
		assertThrown(() -> DataUtil.expectAscii(r, 'a', 'a', 'c'));
		assertEquals(r.readAscii(), "cde");
	}

	@Test
	public void testExpectAsciiString() {
		DataUtil.expectAscii(reader("abc"), "abc");
		DataUtil.expectAscii(reader("abcd"), "abc");
		assertThrown(() -> DataUtil.expectAscii(reader("abc"), "abd"));
		var r = reader("abcde");
		assertThrown(() -> DataUtil.expectAscii(r, "aac"));
		assertEquals(r.readAscii(), "cde");
	}

	@Test
	public void testExpectAsciiAllChars() {
		DataUtil.expectAsciiAll(reader("abc"), 'a', 'b', 'c');
		DataUtil.expectAsciiAll(reader("abcd"), 'a', 'b', 'c');
		assertThrown(() -> DataUtil.expectAsciiAll(reader("abc"), 'a', 'b', 'd'));
		var r = reader("abcde");
		assertThrown(() -> DataUtil.expectAsciiAll(r, 'a', 'a', 'c'));
		assertEquals(r.readAscii(), "de");
	}

	@Test
	public void testExpectAsciiAllString() {
		DataUtil.expectAsciiAll(reader("abc"), "abc");
		DataUtil.expectAsciiAll(reader("abcd"), "abc");
		assertThrown(() -> DataUtil.expectAsciiAll(reader("abc"), "abd"));
		var r = reader("abcde");
		assertThrown(() -> DataUtil.expectAsciiAll(r, "aac"));
		assertEquals(r.readAscii(), "de");
	}

	@Test
	public void testExpectByteArray() {
		DataUtil.expect(reader(1, 2, 3), 1, 2, 3);
		DataUtil.expect(reader(1, 2, 3, 4), 1, 2, 3);
		DataUtil.expect(reader(1, 2, 3), ArrayUtil.bytes.of(0, 1, 2, 3, 4), 1, 3);
		assertThrown(() -> DataUtil.expect(reader(1, 2, 3), 1, 2, 4));
		var r = reader(1, 2, 3, 4, 5);
		assertThrown(() -> DataUtil.expect(r, 1, 1, 3));
		assertArray(r.readBytes(), 3, 4, 5);
	}

	@Test
	public void testExpectByteProvider() {
		DataUtil.expect(reader(1, 2, 3), Immutable.wrap(1, 2, 3));
		DataUtil.expect(reader(1, 2, 3, 4), Immutable.wrap(1, 2, 3));
		DataUtil.expect(reader(1, 2, 3), Immutable.wrap(0, 1, 2, 3, 4), 1, 3);
		assertThrown(() -> DataUtil.expect(reader(1, 2, 3), Immutable.wrap(1, 2, 4)));
		var r = reader(1, 2, 3, 4, 5);
		assertThrown(() -> DataUtil.expect(r, Immutable.wrap(1, 1, 3)));
		assertArray(r.readBytes(), 3, 4, 5);
	}

	@Test
	public void testExpectAllByteArray() {
		DataUtil.expectAll(reader(1, 2, 3), 1, 2, 3);
		DataUtil.expectAll(reader(1, 2, 3, 4), 1, 2, 3);
		DataUtil.expectAll(reader(1, 2, 3), ArrayUtil.bytes.of(0, 1, 2, 3, 4), 1, 3);
		assertThrown(() -> DataUtil.expectAll(reader(1, 2, 3), 1, 2, 4));
		var r = reader(1, 2, 3, 4, 5);
		assertThrown(() -> DataUtil.expectAll(r, 1, 1, 3));
		assertArray(r.readBytes(), 4, 5);
	}

	@Test
	public void testExpectAllByteProvider() {
		DataUtil.expectAll(reader(1, 2, 3), Immutable.wrap(1, 2, 3));
		DataUtil.expectAll(reader(1, 2, 3, 4), Immutable.wrap(1, 2, 3));
		DataUtil.expectAll(reader(1, 2, 3), Immutable.wrap(0, 1, 2, 3, 4), 1, 3);
		assertThrown(() -> DataUtil.expectAll(reader(1, 2, 3), Immutable.wrap(1, 2, 4)));
		var r = reader(1, 2, 3, 4, 5);
		assertThrown(() -> DataUtil.expectAll(r, Immutable.wrap(1, 1, 3)));
		assertArray(r.readBytes(), 4, 5);
	}

	@Test
	public void testDigits() {
		assertEquals(DataUtil.digits(reader("09870"), 5), 9870);
		assertEquals(DataUtil.digits(reader("09870"), 3), 98);
		assertThrown(() -> DataUtil.digits(reader("12"), 3));
		assertThrown(() -> DataUtil.digits(reader("12A"), 3));
		assertThrown(() -> DataUtil.digits(reader("1 3"), 3));
	}

}
