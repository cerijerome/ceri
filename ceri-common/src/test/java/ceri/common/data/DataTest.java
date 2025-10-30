package ceri.common.data;

import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class DataTest {

	@Test
	public void testRequireSize() {
		Data.requireMin(Testing.reader("abc"), 3);
		Data.requireMin(Testing.reader("abcd"), 3);
		Assert.thrown(() -> Data.requireMin(Testing.reader("ab"), 3));
	}

	@Test
	public void testExpectAsciiChars() {
		Data.expectAscii(Testing.reader("abc"), 'a', 'b', 'c');
		Data.expectAscii(Testing.reader("abcd"), 'a', 'b', 'c');
		var r = Testing.reader("abcde");
		Assert.thrown(() -> Data.expectAscii(r, 'a', 'a', 'c'));
		Assert.equal(r.readAscii(), "cde");
	}

	@Test
	public void testExpectAsciiString() {
		Data.expectAscii(Testing.reader("abc"), "abc");
		Data.expectAscii(Testing.reader("abcd"), "abc");
		Assert.thrown(() -> Data.expectAscii(Testing.reader("abc"), "abd"));
		var r = Testing.reader("abcde");
		Assert.thrown(() -> Data.expectAscii(r, "aac"));
		Assert.equal(r.readAscii(), "cde");
	}

	@Test
	public void testExpectAsciiAllChars() {
		Data.expectAsciiAll(Testing.reader("abc"), 'a', 'b', 'c');
		Data.expectAsciiAll(Testing.reader("abcd"), 'a', 'b', 'c');
		Assert.thrown(() -> Data.expectAsciiAll(Testing.reader("abc"), 'a', 'b', 'd'));
		var r = Testing.reader("abcde");
		Assert.thrown(() -> Data.expectAsciiAll(r, 'a', 'a', 'c'));
		Assert.equal(r.readAscii(), "de");
	}

	@Test
	public void testExpectAsciiAllString() {
		Data.expectAsciiAll(Testing.reader("abc"), "abc");
		Data.expectAsciiAll(Testing.reader("abcd"), "abc");
		Assert.thrown(() -> Data.expectAsciiAll(Testing.reader("abc"), "abd"));
		var r = Testing.reader("abcde");
		Assert.thrown(() -> Data.expectAsciiAll(r, "aac"));
		Assert.equal(r.readAscii(), "de");
	}

	@Test
	public void testExpectByteArray() {
		Data.expect(Testing.reader(1, 2, 3), 1, 2, 3);
		Data.expect(Testing.reader(1, 2, 3, 4), 1, 2, 3);
		Data.expect(Testing.reader(1, 2, 3), Array.bytes.of(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> Data.expect(Testing.reader(1, 2, 3), 1, 2, 4));
		var r = Testing.reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> Data.expect(r, 1, 1, 3));
		Assert.array(r.readBytes(), 3, 4, 5);
	}

	@Test
	public void testExpectByteProvider() {
		Data.expect(Testing.reader(1, 2, 3), Immutable.wrap(1, 2, 3));
		Data.expect(Testing.reader(1, 2, 3, 4), Immutable.wrap(1, 2, 3));
		Data.expect(Testing.reader(1, 2, 3), Immutable.wrap(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> Data.expect(Testing.reader(1, 2, 3), Immutable.wrap(1, 2, 4)));
		var r = Testing.reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> Data.expect(r, Immutable.wrap(1, 1, 3)));
		Assert.array(r.readBytes(), 3, 4, 5);
	}

	@Test
	public void testExpectAllByteArray() {
		Data.expectAll(Testing.reader(1, 2, 3), 1, 2, 3);
		Data.expectAll(Testing.reader(1, 2, 3, 4), 1, 2, 3);
		Data.expectAll(Testing.reader(1, 2, 3), Array.bytes.of(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> Data.expectAll(Testing.reader(1, 2, 3), 1, 2, 4));
		var r = Testing.reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> Data.expectAll(r, 1, 1, 3));
		Assert.array(r.readBytes(), 4, 5);
	}

	@Test
	public void testExpectAllByteProvider() {
		Data.expectAll(Testing.reader(1, 2, 3), Immutable.wrap(1, 2, 3));
		Data.expectAll(Testing.reader(1, 2, 3, 4), Immutable.wrap(1, 2, 3));
		Data.expectAll(Testing.reader(1, 2, 3), Immutable.wrap(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> Data.expectAll(Testing.reader(1, 2, 3), Immutable.wrap(1, 2, 4)));
		var r = Testing.reader(1, 2, 3, 4, 5);
		Assert.thrown(() -> Data.expectAll(r, Immutable.wrap(1, 1, 3)));
		Assert.array(r.readBytes(), 4, 5);
	}

	@Test
	public void testDigits() {
		Assert.equal(Data.digits(Testing.reader("09870"), 5), 9870);
		Assert.equal(Data.digits(Testing.reader("09870"), 3), 98);
		Assert.thrown(() -> Data.digits(Testing.reader("12"), 3));
		Assert.thrown(() -> Data.digits(Testing.reader("12A"), 3));
		Assert.thrown(() -> Data.digits(Testing.reader("1 3"), 3));
	}
}
