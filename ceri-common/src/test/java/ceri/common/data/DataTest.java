package ceri.common.data;

import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class DataTest {

	@Test
	public void testRequireSize() {
		Data.requireMin(Testing.byteReader("abc"), 3);
		Data.requireMin(Testing.byteReader("abcd"), 3);
		Assert.thrown(() -> Data.requireMin(Testing.byteReader("ab"), 3));
	}

	@Test
	public void testExpectAsciiChars() {
		Data.expectAscii(Testing.byteReader("abc"), 'a', 'b', 'c');
		Data.expectAscii(Testing.byteReader("abcd"), 'a', 'b', 'c');
		var r = Testing.byteReader("abcde");
		Assert.thrown(() -> Data.expectAscii(r, 'a', 'a', 'c'));
		Assert.equal(r.readAscii(), "cde");
	}

	@Test
	public void testExpectAsciiString() {
		Data.expectAscii(Testing.byteReader("abc"), "abc");
		Data.expectAscii(Testing.byteReader("abcd"), "abc");
		Assert.thrown(() -> Data.expectAscii(Testing.byteReader("abc"), "abd"));
		var r = Testing.byteReader("abcde");
		Assert.thrown(() -> Data.expectAscii(r, "aac"));
		Assert.equal(r.readAscii(), "cde");
	}

	@Test
	public void testExpectAsciiAllChars() {
		Data.expectAsciiAll(Testing.byteReader("abc"), 'a', 'b', 'c');
		Data.expectAsciiAll(Testing.byteReader("abcd"), 'a', 'b', 'c');
		Assert.thrown(() -> Data.expectAsciiAll(Testing.byteReader("abc"), 'a', 'b', 'd'));
		var r = Testing.byteReader("abcde");
		Assert.thrown(() -> Data.expectAsciiAll(r, 'a', 'a', 'c'));
		Assert.equal(r.readAscii(), "de");
	}

	@Test
	public void testExpectAsciiAllString() {
		Data.expectAsciiAll(Testing.byteReader("abc"), "abc");
		Data.expectAsciiAll(Testing.byteReader("abcd"), "abc");
		Assert.thrown(() -> Data.expectAsciiAll(Testing.byteReader("abc"), "abd"));
		var r = Testing.byteReader("abcde");
		Assert.thrown(() -> Data.expectAsciiAll(r, "aac"));
		Assert.equal(r.readAscii(), "de");
	}

	@Test
	public void testExpectByteArray() {
		Data.expect(Testing.byteReader(1, 2, 3), 1, 2, 3);
		Data.expect(Testing.byteReader(1, 2, 3, 4), 1, 2, 3);
		Data.expect(Testing.byteReader(1, 2, 3), Array.BYTE.of(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> Data.expect(Testing.byteReader(1, 2, 3), 1, 2, 4));
		var r = Testing.byteReader(1, 2, 3, 4, 5);
		Assert.thrown(() -> Data.expect(r, 1, 1, 3));
		Assert.array(r.readBytes(), 3, 4, 5);
	}

	@Test
	public void testExpectByteProvider() {
		Data.expect(Testing.byteReader(1, 2, 3), Immutable.wrap(1, 2, 3));
		Data.expect(Testing.byteReader(1, 2, 3, 4), Immutable.wrap(1, 2, 3));
		Data.expect(Testing.byteReader(1, 2, 3), Immutable.wrap(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> Data.expect(Testing.byteReader(1, 2, 3), Immutable.wrap(1, 2, 4)));
		var r = Testing.byteReader(1, 2, 3, 4, 5);
		Assert.thrown(() -> Data.expect(r, Immutable.wrap(1, 1, 3)));
		Assert.array(r.readBytes(), 3, 4, 5);
	}

	@Test
	public void testExpectAllByteArray() {
		Data.expectAll(Testing.byteReader(1, 2, 3), 1, 2, 3);
		Data.expectAll(Testing.byteReader(1, 2, 3, 4), 1, 2, 3);
		Data.expectAll(Testing.byteReader(1, 2, 3), Array.BYTE.of(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> Data.expectAll(Testing.byteReader(1, 2, 3), 1, 2, 4));
		var r = Testing.byteReader(1, 2, 3, 4, 5);
		Assert.thrown(() -> Data.expectAll(r, 1, 1, 3));
		Assert.array(r.readBytes(), 4, 5);
	}

	@Test
	public void testExpectAllByteProvider() {
		Data.expectAll(Testing.byteReader(1, 2, 3), Immutable.wrap(1, 2, 3));
		Data.expectAll(Testing.byteReader(1, 2, 3, 4), Immutable.wrap(1, 2, 3));
		Data.expectAll(Testing.byteReader(1, 2, 3), Immutable.wrap(0, 1, 2, 3, 4), 1, 3);
		Assert.thrown(() -> Data.expectAll(Testing.byteReader(1, 2, 3), Immutable.wrap(1, 2, 4)));
		var r = Testing.byteReader(1, 2, 3, 4, 5);
		Assert.thrown(() -> Data.expectAll(r, Immutable.wrap(1, 1, 3)));
		Assert.array(r.readBytes(), 4, 5);
	}

	@Test
	public void testDigits() {
		Assert.equal(Data.digits(Testing.byteReader("09870"), 5), 9870);
		Assert.equal(Data.digits(Testing.byteReader("09870"), 3), 98);
		Assert.thrown(() -> Data.digits(Testing.byteReader("12"), 3));
		Assert.thrown(() -> Data.digits(Testing.byteReader("12A"), 3));
		Assert.thrown(() -> Data.digits(Testing.byteReader("1 3"), 3));
	}
}
