package ceri.common.data;

import static ceri.common.test.TestUtil.reader;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.test.Assert;

public class DataUtilTest {

	@Test
	public void testRequireSize() {
		DataUtil.requireMin(reader("abc"), 3);
		DataUtil.requireMin(reader("abcd"), 3);
		Assert.thrown(() -> DataUtil.requireMin(reader("ab"), 3));
	}

	@Test
	public void testExpectAsciiChars() {
		DataUtil.expectAscii(reader("abc"), 'a', 'b', 'c');
		DataUtil.expectAscii(reader("abcd"), 'a', 'b', 'c');
		var r = reader("abcde");
		Assert.thrown(() -> DataUtil.expectAscii(r, 'a', 'a', 'c'));
		Assert.equal(r.readAscii(), "cde");
	}

	@Test
	public void testExpectAsciiString() {
		DataUtil.expectAscii(reader("abc"), "abc");
		DataUtil.expectAscii(reader("abcd"), "abc");
		Assert.thrown(() -> DataUtil.expectAscii(reader("abc"), "abd"));
		var r = reader("abcde");
		Assert.thrown(() -> DataUtil.expectAscii(r, "aac"));
		Assert.equal(r.readAscii(), "cde");
	}

	@Test
	public void testExpectAsciiAllChars() {
		DataUtil.expectAsciiAll(reader("abc"), 'a', 'b', 'c');
		DataUtil.expectAsciiAll(reader("abcd"), 'a', 'b', 'c');
		Assert.thrown(() -> DataUtil.expectAsciiAll(reader("abc"), 'a', 'b', 'd'));
		var r = reader("abcde");
		Assert.thrown(() -> DataUtil.expectAsciiAll(r, 'a', 'a', 'c'));
		Assert.equal(r.readAscii(), "de");
	}

	@Test
	public void testExpectAsciiAllString() {
		DataUtil.expectAsciiAll(reader("abc"), "abc");
		DataUtil.expectAsciiAll(reader("abcd"), "abc");
		Assert.thrown(() -> DataUtil.expectAsciiAll(reader("abc"), "abd"));
		var r = reader("abcde");
		Assert.thrown(() -> DataUtil.expectAsciiAll(r, "aac"));
		Assert.equal(r.readAscii(), "de");
	}

	@Test
	public void testExpectByteArray() {
		DataUtil.expect(reader(1, 2, 3), 1, 2, 3);
		DataUtil.expect(reader(1, 2, 3, 4), 1, 2, 3);
		DataUtil.expect(reader(1, 2, 3), ArrayUtil.bytes.of(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> DataUtil.expect(reader(1, 2, 3), 1, 2, 4));
		var r = reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> DataUtil.expect(r, 1, 1, 3));
		Assert.array(r.readBytes(), 3, 4, 5);
	}

	@Test
	public void testExpectByteProvider() {
		DataUtil.expect(reader(1, 2, 3), Immutable.wrap(1, 2, 3));
		DataUtil.expect(reader(1, 2, 3, 4), Immutable.wrap(1, 2, 3));
		DataUtil.expect(reader(1, 2, 3), Immutable.wrap(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> DataUtil.expect(reader(1, 2, 3), Immutable.wrap(1, 2, 4)));
		var r = reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> DataUtil.expect(r, Immutable.wrap(1, 1, 3)));
		Assert.array(r.readBytes(), 3, 4, 5);
	}

	@Test
	public void testExpectAllByteArray() {
		DataUtil.expectAll(reader(1, 2, 3), 1, 2, 3);
		DataUtil.expectAll(reader(1, 2, 3, 4), 1, 2, 3);
		DataUtil.expectAll(reader(1, 2, 3), ArrayUtil.bytes.of(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> DataUtil.expectAll(reader(1, 2, 3), 1, 2, 4));
		var r = reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> DataUtil.expectAll(r, 1, 1, 3));
		Assert.array(r.readBytes(), 4, 5);
	}

	@Test
	public void testExpectAllByteProvider() {
		DataUtil.expectAll(reader(1, 2, 3), Immutable.wrap(1, 2, 3));
		DataUtil.expectAll(reader(1, 2, 3, 4), Immutable.wrap(1, 2, 3));
		DataUtil.expectAll(reader(1, 2, 3), Immutable.wrap(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> DataUtil.expectAll(reader(1, 2, 3), Immutable.wrap(1, 2, 4)));
		var r = reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> DataUtil.expectAll(r, Immutable.wrap(1, 1, 3)));
		Assert.array(r.readBytes(), 4, 5);
	}

	@Test
	public void testDigits() {
		Assert.equal(DataUtil.digits(reader("09870"), 5), 9870);
		Assert.equal(DataUtil.digits(reader("09870"), 3), 98);
		Assert.thrown(() -> DataUtil.digits(reader("12"), 3));
		Assert.thrown(() -> DataUtil.digits(reader("12A"), 3));
		Assert.thrown(() -> DataUtil.digits(reader("1 3"), 3));
	}

}
