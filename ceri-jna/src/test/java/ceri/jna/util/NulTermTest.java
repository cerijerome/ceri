package ceri.jna.util;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.data.ByteArray;

public class NulTermTest {

	@Test
	public void testTruncateString() {
		assertEquals(NulTerm.truncate(""), "");
		assertEquals(NulTerm.truncate("\0"), "");
		assertEquals(NulTerm.truncate("\0\0"), "");
		assertEquals(NulTerm.truncate("abc"), "abc");
		assertEquals(NulTerm.truncate("\0abc\0"), "");
		assertEquals(NulTerm.truncate("abc\0def\0\0"), "abc");
	}

	@Test
	public void testTrimString() {
		assertEquals(NulTerm.trim(""), "");
		assertEquals(NulTerm.trim("\0"), "");
		assertEquals(NulTerm.trim("\0\0"), "");
		assertEquals(NulTerm.trim("abc"), "abc");
		assertEquals(NulTerm.trim("\0abc\0"), "\0abc");
		assertEquals(NulTerm.trim("abc\0def\0\0"), "abc\0def");
	}

	@Test
	public void testTruncateBytes() {
		assertArray(NulTerm.truncate(bytes()));
		assertArray(NulTerm.truncate(bytes(0)));
		assertArray(NulTerm.truncate(bytes(0, 0)));
		assertArray(NulTerm.truncate(bytes(1, 2, 3)), 1, 2, 3);
		assertArray(NulTerm.truncate(bytes(0, 1, 2, 3, 0)));
		assertArray(NulTerm.truncate(bytes(1, 2, 3, 0, 4, 5, 6, 0, 0)), 1, 2, 3);
	}

	@Test
	public void testTrimBytes() {
		assertArray(NulTerm.trim(bytes()));
		assertArray(NulTerm.trim(bytes(0)));
		assertArray(NulTerm.trim(bytes(0, 0)));
		assertArray(NulTerm.trim(bytes(1, 2, 3)), 1, 2, 3);
		assertArray(NulTerm.trim(bytes(0, 1, 2, 3, 0)), 0, 1, 2, 3);
		assertArray(NulTerm.trim(bytes(1, 2, 3, 0, 4, 5, 6, 0, 0)), 1, 2, 3, 0, 4, 5, 6);
	}

	@Test
	public void testReadTruncate() {
		assertEquals(NulTerm.readTruncate(b("")), "");
		assertEquals(NulTerm.readTruncate(b("\0")), "");
		assertEquals(NulTerm.readTruncate(b("\0\0")), "");
		assertEquals(NulTerm.readTruncate(b("abc")), "abc");
		assertEquals(NulTerm.readTruncate(b("\0abc\0")), "");
		assertEquals(NulTerm.readTruncate(b("abc\0def\0\0")), "abc");
	}

	@Test
	public void testReadTrim() {
		assertEquals(NulTerm.readTrim(b("")), "");
		assertEquals(NulTerm.readTrim(b("\0")), "");
		assertEquals(NulTerm.readTrim(b("\0\0")), "");
		assertEquals(NulTerm.readTrim(b("abc")), "abc");
		assertEquals(NulTerm.readTrim(b("\0abc\0")), "\0abc");
		assertEquals(NulTerm.readTrim(b("abc\0def\0\0")), "abc\0def");
	}

	@Test
	public void testWrite() {
		assertWrite(0, "abc", "");
		assertWrite(5, "abc", "abc\0");
		assertWrite(5, "abc\0", "abc\0\0");
		assertWrite(5, "abc\0def", "abc\0\0");
	}

	@Test
	public void testWritePad() {
		assertWritePad(0, "abc", "");
		assertWritePad(5, "abc", "abc\0\0");
		assertWritePad(5, "abc\0", "abc\0\0");
		assertWritePad(5, "abc\0def", "abc\0\0");
	}

	private static void assertWrite(int size, String s, String expected) {
		byte[] dest = new byte[size];
		byte[] b = b(expected);
		assertEquals(NulTerm.write(s, dest), b.length);
		assertArray(ByteArray.Immutable.wrap(dest, 0, b.length), b);
	}

	private static void assertWritePad(int size, String s, String expected) {
		byte[] dest = new byte[size];
		byte[] b = b(expected);
		assertEquals(NulTerm.writePad(s, dest), b.length);
		assertArray(ByteArray.Immutable.wrap(dest, 0, b.length), b);
	}

	private static byte[] b(String s) {
		return s.getBytes(JnaUtil.DEFAULT_CHARSET);
	}
}
