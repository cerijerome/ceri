package ceri.jna.util;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.jna.test.JnaTestUtil.assertPointer;
import org.junit.Test;
import com.sun.jna.Pointer;
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
		assertEquals(NulTerm.truncate((byte[]) null), null);
		assertArray(NulTerm.truncate(bytes()));
		assertArray(NulTerm.truncate(bytes(0)));
		assertArray(NulTerm.truncate(bytes(0, 0)));
		assertArray(NulTerm.truncate(bytes(1, 2, 3)), 1, 2, 3);
		assertArray(NulTerm.truncate(bytes(0, 1, 2, 3, 0)));
		assertArray(NulTerm.truncate(bytes(1, 2, 3, 0, 4, 5, 6, 0, 0)), 1, 2, 3);
	}

	@Test
	public void testTrimBytes() {
		assertEquals(NulTerm.trim((byte[]) null), null);
		assertArray(NulTerm.trim(bytes()));
		assertArray(NulTerm.trim(bytes(0)));
		assertArray(NulTerm.trim(bytes(0, 0)));
		assertArray(NulTerm.trim(bytes(1, 2, 3)), 1, 2, 3);
		assertArray(NulTerm.trim(bytes(0, 1, 2, 3, 0)), 0, 1, 2, 3);
		assertArray(NulTerm.trim(bytes(1, 2, 3, 0, 4, 5, 6, 0, 0)), 1, 2, 3, 0, 4, 5, 6);
	}

	@Test
	public void testReadTruncate() {
		assertEquals(NulTerm.readTruncate((byte[]) null), null);
		assertEquals(NulTerm.readTruncate(b("")), "");
		assertEquals(NulTerm.readTruncate(b("\0")), "");
		assertEquals(NulTerm.readTruncate(b("\0\0")), "");
		assertEquals(NulTerm.readTruncate(b("abc")), "abc");
		assertEquals(NulTerm.readTruncate(b("\0abc\0")), "");
		assertEquals(NulTerm.readTruncate(b("abc\0def\0\0")), "abc");
	}

	@Test
	public void testReadTruncateFromMemory() {
		assertEquals(NulTerm.readTruncate(Pointer.NULL, 0), null);
		assertEquals(NulTerm.readTruncate(m(null).m), null);
		assertEquals(NulTerm.readTruncate(m("").m), null);
		assertEquals(NulTerm.readTruncate(m("\0").m), "");
		assertEquals(NulTerm.readTruncate(m("\0\0").m), "");
		assertEquals(NulTerm.readTruncate(m("abc").m), "abc");
		assertEquals(NulTerm.readTruncate(m("\0abc\0").m), "");
		assertEquals(NulTerm.readTruncate(m("abc\0def\0\0").m), "abc");
		assertEquals(NulTerm.readTruncate(m("abc").m, 0), "");
	}

	@Test
	public void testReadTrim() {
		assertEquals(NulTerm.readTrim((byte[]) null), null);
		assertEquals(NulTerm.readTrim(b("")), "");
		assertEquals(NulTerm.readTrim(b("\0")), "");
		assertEquals(NulTerm.readTrim(b("\0\0")), "");
		assertEquals(NulTerm.readTrim(b("abc")), "abc");
		assertEquals(NulTerm.readTrim(b("\0abc\0")), "\0abc");
		assertEquals(NulTerm.readTrim(b("abc\0def\0\0")), "abc\0def");
	}

	@Test
	public void testReadTrimFromMemory() {
		assertEquals(NulTerm.readTrim(Pointer.NULL, 0), null);
		assertEquals(NulTerm.readTrim(m(null).m), null);
		assertEquals(NulTerm.readTrim(m("").m), null);
		assertEquals(NulTerm.readTrim(m("\0").m), "");
		assertEquals(NulTerm.readTrim(m("\0\0").m), "");
		assertEquals(NulTerm.readTrim(m("abc").m), "abc");
		assertEquals(NulTerm.readTrim(m("\0abc\0").m), "\0abc");
		assertEquals(NulTerm.readTrim(m("abc\0def\0\0").m), "abc\0def");
		assertEquals(NulTerm.readTrim(m("abc").m, 0), "");
	}

	@Test
	public void testWrite() {
		assertEquals(NulTerm.write(null, new byte[1]), 0);
		assertEquals(NulTerm.write("abc", (byte[]) null), 0);
		assertWrite(0, "abc", "");
		assertWrite(5, "abc", "abc\0");
		assertWrite(5, "abc\0", "abc\0\0");
		assertWrite(5, "abc\0def", "abc\0\0");
	}

	@Test
	public void testWriteToMemory() {
		assertEquals(NulTerm.write(null, m("\0").m), 0);
		assertEquals(NulTerm.write("abc", m(null).m), 0);
		assertEquals(NulTerm.write("abc", m("\0").m, 0), 0);
		assertWriteMem(5, "abc", "abc\0");
		assertWriteMem(5, "abc\0", "abc\0\0");
		assertWriteMem(5, "abc\0def", "abc\0\0");
	}

	@Test
	public void testWritePad() {
		assertWritePad(0, "abc", "");
		assertWritePad(5, "abc", "abc\0\0");
		assertWritePad(5, "abc\0", "abc\0\0");
		assertWritePad(5, "abc\0def", "abc\0\0");
	}

	@Test
	public void testWritePadMem() {
		assertEquals(NulTerm.writePad("abc", m("\0").m, 0), 0);
		assertWritePadMem(5, "abc", "abc\0\0");
		assertWritePadMem(5, "abc\0", "abc\0\0");
		assertWritePadMem(5, "abc\0def", "abc\0\0");
	}

	private static void assertWrite(int size, String s, String expected) {
		byte[] dest = new byte[size];
		byte[] b = b(expected);
		assertEquals(NulTerm.write(s, dest), b.length);
		assertArray(ByteArray.Immutable.wrap(dest, 0, b.length), b);
	}

	private static void assertWriteMem(int size, String s, String expected) {
		var dest = GcMemory.malloc(size);
		byte[] b = b(expected);
		assertEquals(NulTerm.write(s, dest.m), b.length);
		assertPointer(dest.m, 0, b);
	}

	private static void assertWritePad(int size, String s, String expected) {
		byte[] dest = new byte[size];
		byte[] b = b(expected);
		assertEquals(NulTerm.writePad(s, dest), b.length);
		assertArray(ByteArray.Immutable.wrap(dest, 0, b.length), b);
	}

	private static void assertWritePadMem(int size, String s, String expected) {
		var dest = GcMemory.malloc(size);
		byte[] b = b(expected);
		assertEquals(NulTerm.writePad(s, dest.m), b.length);
		assertPointer(dest.m, 0, b);
	}

	private static byte[] b(String s) {
		if (s == null) return null;
		return s.getBytes(JnaUtil.DEFAULT_CHARSET);
	}

	private static GcMemory m(String s) {
		if (s == null) return GcMemory.NULL;
		return GcMemory.mallocBytes(b(s));
	}
}
