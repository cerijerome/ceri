package ceri.jna.util;

import static ceri.jna.test.JnaTestUtil.assertPointer;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteArray;
import ceri.common.test.Assert;

public class NulTermTest {

	@Test
	public void testTruncateString() {
		Assert.equal(NulTerm.truncate(""), "");
		Assert.equal(NulTerm.truncate("\0"), "");
		Assert.equal(NulTerm.truncate("\0\0"), "");
		Assert.equal(NulTerm.truncate("abc"), "abc");
		Assert.equal(NulTerm.truncate("\0abc\0"), "");
		Assert.equal(NulTerm.truncate("abc\0def\0\0"), "abc");
	}

	@Test
	public void testTrimString() {
		Assert.equal(NulTerm.trim(""), "");
		Assert.equal(NulTerm.trim("\0"), "");
		Assert.equal(NulTerm.trim("\0\0"), "");
		Assert.equal(NulTerm.trim("abc"), "abc");
		Assert.equal(NulTerm.trim("\0abc\0"), "\0abc");
		Assert.equal(NulTerm.trim("abc\0def\0\0"), "abc\0def");
	}

	@Test
	public void testTruncateBytes() {
		Assert.equal(NulTerm.truncate((byte[]) null), null);
		Assert.array(NulTerm.truncate(ArrayUtil.bytes.of()));
		Assert.array(NulTerm.truncate(ArrayUtil.bytes.of(0)));
		Assert.array(NulTerm.truncate(ArrayUtil.bytes.of(0, 0)));
		Assert.array(NulTerm.truncate(ArrayUtil.bytes.of(1, 2, 3)), 1, 2, 3);
		Assert.array(NulTerm.truncate(ArrayUtil.bytes.of(0, 1, 2, 3, 0)));
		Assert.array(NulTerm.truncate(ArrayUtil.bytes.of(1, 2, 3, 0, 4, 5, 6, 0, 0)), 1, 2, 3);
	}

	@Test
	public void testTrimBytes() {
		Assert.equal(NulTerm.trim((byte[]) null), null);
		Assert.array(NulTerm.trim(ArrayUtil.bytes.of()));
		Assert.array(NulTerm.trim(ArrayUtil.bytes.of(0)));
		Assert.array(NulTerm.trim(ArrayUtil.bytes.of(0, 0)));
		Assert.array(NulTerm.trim(ArrayUtil.bytes.of(1, 2, 3)), 1, 2, 3);
		Assert.array(NulTerm.trim(ArrayUtil.bytes.of(0, 1, 2, 3, 0)), 0, 1, 2, 3);
		Assert.array(NulTerm.trim(ArrayUtil.bytes.of(1, 2, 3, 0, 4, 5, 6, 0, 0)), 1, 2, 3, 0, 4, 5,
			6);
	}

	@Test
	public void testReadTruncate() {
		Assert.equal(NulTerm.readTruncate((byte[]) null), null);
		Assert.equal(NulTerm.readTruncate(b("")), "");
		Assert.equal(NulTerm.readTruncate(b("\0")), "");
		Assert.equal(NulTerm.readTruncate(b("\0\0")), "");
		Assert.equal(NulTerm.readTruncate(b("abc")), "abc");
		Assert.equal(NulTerm.readTruncate(b("\0abc\0")), "");
		Assert.equal(NulTerm.readTruncate(b("abc\0def\0\0")), "abc");
	}

	@Test
	public void testReadTruncateFromMemory() {
		Assert.equal(NulTerm.readTruncate(Pointer.NULL, 0), null);
		Assert.equal(NulTerm.readTruncate(m(null).m), null);
		Assert.equal(NulTerm.readTruncate(m("").m), null);
		Assert.equal(NulTerm.readTruncate(m("\0").m), "");
		Assert.equal(NulTerm.readTruncate(m("\0\0").m), "");
		Assert.equal(NulTerm.readTruncate(m("abc").m), "abc");
		Assert.equal(NulTerm.readTruncate(m("\0abc\0").m), "");
		Assert.equal(NulTerm.readTruncate(m("abc\0def\0\0").m), "abc");
		Assert.equal(NulTerm.readTruncate(m("abc").m, 0), "");
	}

	@Test
	public void testReadTrim() {
		Assert.equal(NulTerm.readTrim((byte[]) null), null);
		Assert.equal(NulTerm.readTrim(b("")), "");
		Assert.equal(NulTerm.readTrim(b("\0")), "");
		Assert.equal(NulTerm.readTrim(b("\0\0")), "");
		Assert.equal(NulTerm.readTrim(b("abc")), "abc");
		Assert.equal(NulTerm.readTrim(b("\0abc\0")), "\0abc");
		Assert.equal(NulTerm.readTrim(b("abc\0def\0\0")), "abc\0def");
	}

	@Test
	public void testReadTrimFromMemory() {
		Assert.equal(NulTerm.readTrim(Pointer.NULL, 0), null);
		Assert.equal(NulTerm.readTrim(m(null).m), null);
		Assert.equal(NulTerm.readTrim(m("").m), null);
		Assert.equal(NulTerm.readTrim(m("\0").m), "");
		Assert.equal(NulTerm.readTrim(m("\0\0").m), "");
		Assert.equal(NulTerm.readTrim(m("abc").m), "abc");
		Assert.equal(NulTerm.readTrim(m("\0abc\0").m), "\0abc");
		Assert.equal(NulTerm.readTrim(m("abc\0def\0\0").m), "abc\0def");
		Assert.equal(NulTerm.readTrim(m("abc").m, 0), "");
	}

	@Test
	public void testWrite() {
		Assert.equal(NulTerm.write(null, new byte[1]), 0);
		Assert.equal(NulTerm.write("abc", (byte[]) null), 0);
		assertWrite(0, "abc", "");
		assertWrite(5, "abc", "abc\0");
		assertWrite(5, "abc\0", "abc\0\0");
		assertWrite(5, "abc\0def", "abc\0\0");
	}

	@Test
	public void testWriteToMemory() {
		Assert.equal(NulTerm.write(null, m("\0").m), 0);
		Assert.equal(NulTerm.write("abc", m(null).m), 0);
		Assert.equal(NulTerm.write("abc", m("\0").m, 0), 0);
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
		Assert.equal(NulTerm.writePad("abc", m("\0").m, 0), 0);
		assertWritePadMem(5, "abc", "abc\0\0");
		assertWritePadMem(5, "abc\0", "abc\0\0");
		assertWritePadMem(5, "abc\0def", "abc\0\0");
	}

	private static void assertWrite(int size, String s, String expected) {
		byte[] dest = new byte[size];
		byte[] b = b(expected);
		Assert.equal(NulTerm.write(s, dest), b.length);
		Assert.array(ByteArray.Immutable.wrap(dest, 0, b.length), b);
	}

	private static void assertWriteMem(int size, String s, String expected) {
		var dest = GcMemory.malloc(size);
		byte[] b = b(expected);
		Assert.equal(NulTerm.write(s, dest.m), b.length);
		assertPointer(dest.m, 0, b);
	}

	private static void assertWritePad(int size, String s, String expected) {
		byte[] dest = new byte[size];
		byte[] b = b(expected);
		Assert.equal(NulTerm.writePad(s, dest), b.length);
		Assert.array(ByteArray.Immutable.wrap(dest, 0, b.length), b);
	}

	private static void assertWritePadMem(int size, String s, String expected) {
		var dest = GcMemory.malloc(size);
		byte[] b = b(expected);
		Assert.equal(NulTerm.writePad(s, dest.m), b.length);
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
