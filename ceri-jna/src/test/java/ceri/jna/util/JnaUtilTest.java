package ceri.jna.util;

import static ceri.jna.test.JnaTestUtil.assertMemory;
import static ceri.jna.util.JnaTestData.assertEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.junit.Test;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteUtil;
import ceri.common.reflect.ClassReloader;
import ceri.common.test.Assert;
import ceri.common.util.SystemVars;
import ceri.jna.type.IntType;
import ceri.jna.type.Struct;
import ceri.jna.util.JnaTestData.TestStruct;

public class JnaUtilTest {
	private final JnaTestData data = JnaTestData.of();

	@SuppressWarnings("serial")
	public static class Uint32 extends IntType<Uint32> {
		public Uint32(long value) {
			super(Integer.BYTES, value, true);
		}
	}

	@Test
	public void testSetProtected() {
		boolean prot = Native.isProtected();
		boolean result = JnaUtil.setProtected();
		if (prot != result) Native.setProtected(prot);
	}

	@Test
	public void testCallback() {
		try (var m = JnaUtil.mallocBytes(1, 2, 3)) {
			try (var _ = JnaUtil.callback(m)) {
				//
			}
		}
	}

	@Test
	public void testMessage() {
		Assert.equal(JnaUtil.message(null), "");
		Assert.equal(JnaUtil.message(new LastErrorException(0, null) {}), "");
		Assert.equal(JnaUtil.message(new LastErrorException("")), "");
		Assert.equal(JnaUtil.message(new LastErrorException("test")), "test");
		Assert.equal(JnaUtil.message(new LastErrorException("[100]  test")), "test");
	}

	@SuppressWarnings("resource")
	@Test
	public void testMalloc() {
		Assert.isNull(JnaUtil.malloc(0));
		Assert.equal(JnaUtil.malloc(3).size(), 3L);
	}

	@SuppressWarnings("resource")
	@Test
	public void testCalloc() {
		Assert.isNull(JnaUtil.calloc(0));
		assertMemory(JnaUtil.calloc(5), 0, 0, 0, 0, 0, 0);
	}

	@SuppressWarnings("resource")
	@Test
	public void testMallocBytes() {
		Assert.isNull(JnaUtil.mallocBytes(new byte[0]));
		Assert.isNull(JnaUtil.mallocBytes(ArrayUtil.bytes.of(1, 2, 3), 1, 0));
		assertMemory(JnaUtil.mallocBytes(ArrayUtil.bytes.of(-1, -2, -3)), 0, -1, -2, -3);
	}

	@Test
	public void testMemcpy() {
		try (Memory m = JnaUtil.mallocBytes(1, 2, 3, 4, 5, 6, 7, 8, 9)) {
			Assert.equal(JnaUtil.memcpy(m, 3, 3, 5), 5);
			assertMemory(m, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
			Assert.equal(JnaUtil.memcpy(m, 3, 0, 5), 5);
			assertMemory(m, 0, 1, 2, 3, 1, 2, 3, 4, 5, 9);
		}
	}

	@Test
	public void testMemcpyForLargeBuffer() {
		try (Memory m = JnaUtil.malloc(16 * 1024)) {
			Assert.equal(JnaUtil.memcpy(m, 1024, 0, 8 * 1024), 8 * 1024);
			Assert.equal(JnaUtil.memcpy(m, 0, 8 * 1024, 8 * 1024), 8 * 1024);
		}
	}

	@Test
	public void testMemcpyBetweenPointers() {
		try (Memory m0 = JnaUtil.mallocBytes(ByteUtil.toAscii("abcdefghijklm").copy(0));
			Memory m1 = JnaUtil.mallocBytes(ByteUtil.toAscii("ABCDEFGHIJKLM").copy(0))) {
			JnaUtil.memcpy(m0, 3, m1, 3, 3);
			Assert.equal(JnaUtil.string(m0), "abcDEFghijklm");
			Assert.equal(JnaUtil.string(m1), "ABCDEFGHIJKLM");
		}
	}

	@Test
	public void testMemcpySamePointer() {
		try (Memory m = JnaUtil.mallocBytes(ByteUtil.toAscii("abcdefghijklm").copy(0))) {
			JnaUtil.memcpy(m, 0, m, 4, 3);
			Assert.equal(JnaUtil.string(m), "efgdefghijklm");
		}
	}

	@Test
	public void testMemcpySamePointerWithOverlap() {
		try (Memory m = JnaUtil.mallocBytes(ByteUtil.toAscii("abcdefghijklm").copy(0))) {
			JnaUtil.memcpy(m, 0, m, 3, 4);
			Assert.equal(JnaUtil.string(m), "defgefghijklm");
		}
	}

	@Test
	public void testMemmove() {
		byte[] text = ByteUtil.toAscii("abcdefghijklm").copy(0);
		try (Memory m = JnaUtil.mallocBytes(text)) {
			Assert.equal(JnaUtil.memmove(m, 3, 0, 5), 5);
			Assert.equal(JnaUtil.string(m), "abcabcdeijklm");
			JnaUtil.write(m, text);
			Assert.equal(JnaUtil.memmove(m, 0, 3, 5), 5);
			Assert.equal(JnaUtil.string(m), "defghfghijklm");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testLazyMem() {
		var lazy = JnaUtil.lazyMem(5);
		Assert.equal(lazy.get().size(), 5L);
	}

	@SuppressWarnings("resource")
	@Test
	public void testShare() {
		Assert.isNull(JnaUtil.share(null, 0));
		Assert.isNull(JnaUtil.share(null, 0, 0));
		Assert.thrown(() -> JnaUtil.share(null, 1));
		Assert.thrown(() -> JnaUtil.share(null, 0, 1));
		Memory m = JnaUtil.mallocBytes(1, 2, 3);
		assertMemory(JnaUtil.share(m, 0), 0, 1, 2, 3);
		assertMemory(JnaUtil.share(m, 0, 2), 0, 1, 2);
		assertMemory(JnaUtil.share(m, 1), 0, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void testSize() {
		Assert.equal(JnaUtil.size(null), 0L);
		Assert.equal(JnaUtil.size(new Memory(5)), 5L);
	}

	@SuppressWarnings("resource")
	@Test
	public void testIntSize() {
		Assert.equal(JnaUtil.intSize(null), 0);
		Assert.equal(JnaUtil.intSize(new Memory(5)), 5);
	}

	@Test
	public void testArrayByRef() {
		Assert.array(JnaUtil.arrayByRef(null, TestStruct::new, TestStruct[]::new));
		Assert.array(JnaUtil.arrayByRef(null, TestStruct::new, TestStruct[]::new, 1),
			new TestStruct[1]);
	}

	@Test
	public void testByRef() {
		Pointer p = data.structArrayByRefPointer(0);
		data.assertStructRead(JnaUtil.byRef(p, 0, TestStruct::new), 0);
		data.assertStructRead(JnaUtil.byRef(p, 1, TestStruct::new), 1);
		data.assertStructRead(JnaUtil.byRef(p, 2, TestStruct::new), 2);
		Assert.isNull(JnaUtil.byRef(p, 3, TestStruct::new));
	}

	@Test
	public void testArrayByValForNullPointer() {
		Assert.array(
			JnaUtil.arrayByVal(null, TestStruct::new, TestStruct[]::new, 0, TestStruct.SIZE));
		Assert.array(
			JnaUtil.arrayByVal(null, TestStruct::new, TestStruct[]::new, 1, TestStruct.SIZE),
			new TestStruct[1]);
	}

	@Test
	public void testArrayByVal() {
		var p = data.structArrayByValPointer(0);
		var array = Struct
			.read(JnaUtil.arrayByVal(p, TestStruct::new, TestStruct[]::new, 4, TestStruct.SIZE));
		data.assertStruct(array[0], 0);
		data.assertStruct(array[1], 1);
		data.assertStruct(array[2], 2);
		assertEmpty(array[3]);
	}

	@Test
	public void testByVal() {
		Pointer p = data.structArrayByValPointer(0);
		data.assertStructRead(JnaUtil.byVal(p, 0, TestStruct::new, TestStruct.SIZE), 0);
		data.assertStructRead(JnaUtil.byVal(p, 1, TestStruct::new, TestStruct.SIZE), 1);
		data.assertStructRead(JnaUtil.byVal(p, 2, TestStruct::new, TestStruct.SIZE), 2);
		assertEmpty(JnaUtil.byVal(p, 3, TestStruct::new, TestStruct.SIZE));
	}

	@Test
	public void testAnd() {
		var uint32 = new Uint32(0xeff00000);
		JnaUtil.and(uint32, 0xbf000000);
		Assert.equal(uint32.longValue(), 0xaf000000L);
	}

	@Test
	public void testOr() {
		var uint32 = new Uint32(0x8ff00000);
		JnaUtil.or(uint32, 0x5f000000);
		Assert.equal(uint32.longValue(), 0xdff00000L);
	}

	@Test
	public void testUbyte() {
		ByteByReference ref = new ByteByReference((byte) 0x80);
		Assert.equal(JnaUtil.ubyte(ref), (short) 0x80);
		Assert.equal(JnaUtil.ubyte(ref.getPointer(), 0), (short) 0x80);
	}

	@Test
	public void testUshort() {
		ShortByReference ref = new ShortByReference((short) 0x8000);
		Assert.equal(JnaUtil.ushort(ref), 0x8000);
		Assert.equal(JnaUtil.ushort(ref.getPointer(), 0), 0x8000);
	}

	@Test
	public void testUint() {
		IntByReference ref = new IntByReference(0x80000000);
		Assert.equal(JnaUtil.uint(ref), 0x80000000L);
		Assert.equal(JnaUtil.uint(ref.getPointer(), 0), 0x80000000L);
	}

	@Test
	public void testRefs() {
		Assert.equal(JnaUtil.byteRef(0x80).getValue(), (byte) 0x80);
		Assert.equal(JnaUtil.shortRef(0x8000).getValue(), (short) 0x8000);
		Assert.equal(JnaUtil.intRef(0x80000000).getValue(), 0x80000000);
		Assert.equal(JnaUtil.longRef(0x8000000000000000L).getValue(), 0x8000000000000000L);
	}

	@Test
	public void testRefPointers() {
		Assert.equal(JnaUtil.byteRefPtr(0x80).getByte(0), (byte) 0x80);
		Assert.equal(JnaUtil.shortRefPtr(0x8000).getShort(0), (short) 0x8000);
		Assert.equal(JnaUtil.intRefPtr(0x80000000).getInt(0), 0x80000000);
		Assert.equal(JnaUtil.longRefPtr(0x8000000000000000L).getLong(0), 0x8000000000000000L);
	}

	@Test
	public void testBytesFromPointer() {
		Assert.array(JnaUtil.bytes(Pointer.NULL, 0, 0));
		Assert.thrown(() -> JnaUtil.bytes(Pointer.NULL, 0, 1));
	}

	@Test
	public void testBytesFromMemory() {
		@SuppressWarnings("resource")
		var m = JnaUtil.mallocBytes(-1, 0, 0x80, 1);
		Assert.array(JnaUtil.bytes(m), -1, 0, 0x80, 1);
	}

	@Test
	public void testBytesFromBuffer() {
		ByteBuffer bb = ByteBuffer.wrap(ArrayUtil.bytes.of(0x80, 0, 0xff));
		Assert.array(JnaUtil.bytes(bb), 0x80, 0, 0xff);
		Assert.array(JnaUtil.bytes(bb, 0, 2), 0x80, 0);
		Assert.array(JnaUtil.bytes(bb, 1), 0, 0xff);
		Assert.array(JnaUtil.bytes(bb, 1, 0));
	}

	@Test
	public void testBytesFromMemoryBuffer() {
		@SuppressWarnings("resource")
		var bb = JnaUtil.mallocBytes(0x80, 0, 0xff).getByteBuffer(0, 3);
		Assert.array(JnaUtil.bytes(bb), 0x80, 0, 0xff);
	}

	@Test
	public void testBytesFromNullBuffer() {
		Assert.thrown(() -> JnaUtil.bytes((ByteBuffer) null, 0, 0));
		Assert.thrown(() -> JnaUtil.bytes((ByteBuffer) null, 0, 1));
	}

	@Test
	public void testBufferPointer() {
		Assert.equal(JnaUtil.pointer(null), null);
		Assert.thrown(() -> JnaUtil.pointer(ByteBuffer.allocate(3)));
		var buffer = ByteBuffer.allocateDirect(3);
		buffer.put(ArrayUtil.bytes.of(1, 2, 3));
		Assert.array(JnaUtil.bytes(JnaUtil.pointer(buffer), 0, 3), 1, 2, 3);
	}

	@Test
	public void testBuffer() {
		Assert.buffer(JnaUtil.buffer(null, 0));
		Assert.buffer(JnaUtil.buffer(null, 0, 0));
		Assert.thrown(() -> JnaUtil.buffer(null, 0, 1));
		try (var m = JnaUtil.mallocBytes(0x80, 0, 0xff)) {
			Assert.buffer(JnaUtil.buffer(m), 0x80, 0, 0xff);
		}
	}

	@Test
	public void testStringFromBytes() {
		Assert.equal(JnaUtil.string(ArrayUtil.bytes.of('a', 0, 'b', 0)), "a\0b\0"); // no nul-term
	}

	@Test
	public void testStringFromMemory() {
		try (Memory m = JnaUtil.mallocBytes("abc\0def".getBytes(UTF_8))) {
			Assert.equal(JnaUtil.string(UTF_8, m), "abc\0def");
		}
	}

	@Test
	public void testStringFromBuffer() {
		Assert.equal(JnaUtil.string(ByteBuffer.wrap("abc\0def".getBytes())), "abc\0def");
		Assert.equal(JnaUtil.string(UTF_8, ByteBuffer.wrap("abc\0def".getBytes(UTF_8))),
			"abc\0def");
	}

	@Test
	public void testReadBytesFromPointer() {
		try (Memory m = JnaUtil.mallocBytes(0x80, 0, 0xff, 0x7f, 1)) {
			byte[] buffer = new byte[3];
			Assert.equal(JnaUtil.read(m, buffer), 3);
			Assert.array(buffer, 0x80, 0, 0xff);
		}
	}

	@Test
	public void testReadBytesFromPointerAtIndex() {
		try (Memory m = JnaUtil.mallocBytes(0x80, 0, 0xff, 0x7f, 1)) {
			byte[] buffer = new byte[3];
			Assert.equal(JnaUtil.read(m, 1, buffer), 3);
			Assert.array(buffer, 0, 0xff, 0x7f);
		}
	}

	@Test
	public void testReadFromNull() {
		JnaUtil.read(null, new byte[0]);
		Assert.thrown(() -> JnaUtil.write(null, new byte[0], 1, 0));
		Assert.thrown(() -> JnaUtil.write(null, new byte[1]));
	}

	@Test
	public void testWriteBytesToPointer() {
		try (Memory m = JnaUtil.calloc(5)) {
			Assert.equal(JnaUtil.write(m, 1, 0x80, 0, 0xff), 4L);
			assertMemory(m, 0, 0, 0x80, 0, 0xff, 0);
		}
	}

	@Test
	public void testWriteToNull() {
		JnaUtil.write(null, 0);
		Assert.thrown(() -> JnaUtil.write(null, 1));
		Assert.thrown(() -> JnaUtil.write(null, 0, 1, 2, 3));
	}

	@Test
	public void testFill() {
		JnaUtil.fill(null, 0x33);
		Assert.thrown(() -> JnaUtil.fill(null, 1, 0x33));
		@SuppressWarnings("resource")
		var m = JnaUtil.mallocBytes(-1, 0, 0x80, 1);
		JnaUtil.fill(m, 0x9f);
		Assert.array(JnaUtil.bytes(m), 0x9f, 0x9f, 0x9f, 0x9f);
	}

	@Test
	public void testAsLong() {
		Assert.equal(JnaUtil.asLong(Byte.MIN_VALUE, true), -0x80L);
		Assert.equal(JnaUtil.asLong(Byte.MIN_VALUE, false), 0x80L);
		Assert.equal(JnaUtil.asLong(Short.MIN_VALUE, true), -0x8000L);
		Assert.equal(JnaUtil.asLong(Short.MIN_VALUE, false), 0x8000L);
		Assert.equal(JnaUtil.asLong(Integer.MIN_VALUE, true), -0x80000000L);
		Assert.equal(JnaUtil.asLong(Integer.MIN_VALUE, false), 0x80000000L);
		Assert.equal(JnaUtil.asLong(Long.MIN_VALUE, true), -0x80000000_00000000L);
		Assert.equal(JnaUtil.asLong(Long.MIN_VALUE, false), 0x80000000_00000000L);
		Assert.equal(JnaUtil.asLong("1", true), null);
	}

	@Test
	public void testDefaultCharset() {
		try (var _ = SystemVars.removableProperty("jna.encoding", "test")) {
			ClassReloader.reload(CharsetTester.class, JnaUtil.class);
		}
	}

	public static class CharsetTester {
		static {
			Assert.equal(JnaUtil.DEFAULT_CHARSET, Charset.defaultCharset());
		}
	}
}
