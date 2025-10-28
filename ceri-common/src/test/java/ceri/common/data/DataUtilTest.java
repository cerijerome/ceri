package ceri.common.data;

import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class DataUtilTest {

	@Test
	public void testRequireSize() {
		DataUtil.requireMin(Testing.reader("abc"), 3);
		DataUtil.requireMin(Testing.reader("abcd"), 3);
		Assert.thrown(() -> DataUtil.requireMin(Testing.reader("ab"), 3));
	}

	@Test
	public void testExpectAsciiChars() {
		DataUtil.expectAscii(Testing.reader("abc"), 'a', 'b', 'c');
		DataUtil.expectAscii(Testing.reader("abcd"), 'a', 'b', 'c');
		var r = Testing.reader("abcde");
		Assert.thrown(() -> DataUtil.expectAscii(r, 'a', 'a', 'c'));
		Assert.equal(r.readAscii(), "cde");
	}

	@Test
	public void testExpectAsciiString() {
		DataUtil.expectAscii(Testing.reader("abc"), "abc");
		DataUtil.expectAscii(Testing.reader("abcd"), "abc");
		Assert.thrown(() -> DataUtil.expectAscii(Testing.reader("abc"), "abd"));
		var r = Testing.reader("abcde");
		Assert.thrown(() -> DataUtil.expectAscii(r, "aac"));
		Assert.equal(r.readAscii(), "cde");
	}

	@Test
	public void testExpectAsciiAllChars() {
		DataUtil.expectAsciiAll(Testing.reader("abc"), 'a', 'b', 'c');
		DataUtil.expectAsciiAll(Testing.reader("abcd"), 'a', 'b', 'c');
		Assert.thrown(() -> DataUtil.expectAsciiAll(Testing.reader("abc"), 'a', 'b', 'd'));
		var r = Testing.reader("abcde");
		Assert.thrown(() -> DataUtil.expectAsciiAll(r, 'a', 'a', 'c'));
		Assert.equal(r.readAscii(), "de");
	}

	@Test
	public void testExpectAsciiAllString() {
		DataUtil.expectAsciiAll(Testing.reader("abc"), "abc");
		DataUtil.expectAsciiAll(Testing.reader("abcd"), "abc");
		Assert.thrown(() -> DataUtil.expectAsciiAll(Testing.reader("abc"), "abd"));
		var r = Testing.reader("abcde");
		Assert.thrown(() -> DataUtil.expectAsciiAll(r, "aac"));
		Assert.equal(r.readAscii(), "de");
	}

	@Test
	public void testExpectByteArray() {
		DataUtil.expect(Testing.reader(1, 2, 3), 1, 2, 3);
		DataUtil.expect(Testing.reader(1, 2, 3, 4), 1, 2, 3);
		DataUtil.expect(Testing.reader(1, 2, 3), ArrayUtil.bytes.of(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> DataUtil.expect(Testing.reader(1, 2, 3), 1, 2, 4));
		var r = Testing.reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> DataUtil.expect(r, 1, 1, 3));
		Assert.array(r.readBytes(), 3, 4, 5);
	}

	@Test
	public void testExpectByteProvider() {
		DataUtil.expect(Testing.reader(1, 2, 3), Immutable.wrap(1, 2, 3));
		DataUtil.expect(Testing.reader(1, 2, 3, 4), Immutable.wrap(1, 2, 3));
		DataUtil.expect(Testing.reader(1, 2, 3), Immutable.wrap(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> DataUtil.expect(Testing.reader(1, 2, 3), Immutable.wrap(1, 2, 4)));
		var r = Testing.reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> DataUtil.expect(r, Immutable.wrap(1, 1, 3)));
		Assert.array(r.readBytes(), 3, 4, 5);
	}

	@Test
	public void testExpectAllByteArray() {
		DataUtil.expectAll(Testing.reader(1, 2, 3), 1, 2, 3);
		DataUtil.expectAll(Testing.reader(1, 2, 3, 4), 1, 2, 3);
		DataUtil.expectAll(Testing.reader(1, 2, 3), ArrayUtil.bytes.of(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> DataUtil.expectAll(Testing.reader(1, 2, 3), 1, 2, 4));
		var r = Testing.reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> DataUtil.expectAll(r, 1, 1, 3));
		Assert.array(r.readBytes(), 4, 5);
	}

	@Test
	public void testExpectAllByteProvider() {
		DataUtil.expectAll(Testing.reader(1, 2, 3), Immutable.wrap(1, 2, 3));
		DataUtil.expectAll(Testing.reader(1, 2, 3, 4), Immutable.wrap(1, 2, 3));
		DataUtil.expectAll(Testing.reader(1, 2, 3), Immutable.wrap(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> DataUtil.expectAll(Testing.reader(1, 2, 3), Immutable.wrap(1, 2, 4)));
		var r = Testing.reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> DataUtil.expectAll(r, Immutable.wrap(1, 1, 3)));
		Assert.array(r.readBytes(), 4, 5);
	}

	@Test
	public void testDigits() {
		Assert.equal(DataUtil.digits(Testing.reader("09870"), 5), 9870);
		Assert.equal(DataUtil.digits(Testing.reader("09870"), 3), 98);
		Assert.thrown(() -> DataUtil.digits(Testing.reader("12"), 3));
		Assert.thrown(() -> DataUtil.digits(Testing.reader("12A"), 3));
		Assert.thrown(() -> DataUtil.digits(Testing.reader("1 3"), 3));
	}
}
