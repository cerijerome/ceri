package ceri.jna.util;

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
import ceri.common.array.Array;
import ceri.common.data.Bytes;
import ceri.common.reflect.ClassReloader;
import ceri.common.test.Assert;
import ceri.common.util.SystemVars;
import ceri.jna.test.JnaAssert;
import ceri.jna.type.IntType;
import ceri.jna.type.Struct;
import ceri.jna.util.JnaTestData.TestStruct;

public class JnaTest {
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
		boolean result = Jna.setProtected();
		if (prot != result) Native.setProtected(prot);
	}

	@Test
	public void testCallback() {
		try (var m = Jna.mallocBytes(1, 2, 3)) {
			try (var _ = Jna.callback(m)) {
				//
			}
		}
	}

	@Test
	public void testMessage() {
		Assert.equal(Jna.message(null), "");
		Assert.equal(Jna.message(new LastErrorException(0, null) {}), "");
		Assert.equal(Jna.message(new LastErrorException("")), "");
		Assert.equal(Jna.message(new LastErrorException("test")), "test");
		Assert.equal(Jna.message(new LastErrorException("[100]  test")), "test");
	}

	@SuppressWarnings("resource")
	@Test
	public void testMalloc() {
		Assert.isNull(Jna.malloc(0));
		Assert.equal(Jna.malloc(3).size(), 3L);
	}

	@SuppressWarnings("resource")
	@Test
	public void testCalloc() {
		Assert.isNull(Jna.calloc(0));
		JnaAssert.memory(Jna.calloc(5), 0, 0, 0, 0, 0, 0);
	}

	@SuppressWarnings("resource")
	@Test
	public void testMallocBytes() {
		Assert.isNull(Jna.mallocBytes(new byte[0]));
		Assert.isNull(Jna.mallocBytes(Array.bytes.of(1, 2, 3), 1, 0));
		JnaAssert.memory(Jna.mallocBytes(Array.bytes.of(-1, -2, -3)), 0, -1, -2, -3);
	}

	@Test
	public void testMemcpy() {
		try (Memory m = Jna.mallocBytes(1, 2, 3, 4, 5, 6, 7, 8, 9)) {
			Assert.equal(Jna.memcpy(m, 3, 3, 5), 5);
			JnaAssert.memory(m, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
			Assert.equal(Jna.memcpy(m, 3, 0, 5), 5);
			JnaAssert.memory(m, 0, 1, 2, 3, 1, 2, 3, 4, 5, 9);
		}
	}

	@Test
	public void testMemcpyForLargeBuffer() {
		try (Memory m = Jna.malloc(16 * 1024)) {
			Assert.equal(Jna.memcpy(m, 1024, 0, 8 * 1024), 8 * 1024);
			Assert.equal(Jna.memcpy(m, 0, 8 * 1024, 8 * 1024), 8 * 1024);
		}
	}

	@Test
	public void testMemcpyBetweenPointers() {
		try (Memory m0 = Jna.mallocBytes(Bytes.toAscii("abcdefghijklm").copy(0));
			Memory m1 = Jna.mallocBytes(Bytes.toAscii("ABCDEFGHIJKLM").copy(0))) {
			Jna.memcpy(m0, 3, m1, 3, 3);
			Assert.equal(Jna.string(m0), "abcDEFghijklm");
			Assert.equal(Jna.string(m1), "ABCDEFGHIJKLM");
		}
	}

	@Test
	public void testMemcpySamePointer() {
		try (Memory m = Jna.mallocBytes(Bytes.toAscii("abcdefghijklm").copy(0))) {
			Jna.memcpy(m, 0, m, 4, 3);
			Assert.equal(Jna.string(m), "efgdefghijklm");
		}
	}

	@Test
	public void testMemcpySamePointerWithOverlap() {
		try (Memory m = Jna.mallocBytes(Bytes.toAscii("abcdefghijklm").copy(0))) {
			Jna.memcpy(m, 0, m, 3, 4);
			Assert.equal(Jna.string(m), "defgefghijklm");
		}
	}

	@Test
	public void testMemmove() {
		byte[] text = Bytes.toAscii("abcdefghijklm").copy(0);
		try (Memory m = Jna.mallocBytes(text)) {
			Assert.equal(Jna.memmove(m, 3, 0, 5), 5);
			Assert.equal(Jna.string(m), "abcabcdeijklm");
			Jna.write(m, text);
			Assert.equal(Jna.memmove(m, 0, 3, 5), 5);
			Assert.equal(Jna.string(m), "defghfghijklm");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testLazyMem() {
		var lazy = Jna.lazyMem(5);
		Assert.equal(lazy.get().size(), 5L);
	}

	@SuppressWarnings("resource")
	@Test
	public void testShare() {
		Assert.isNull(Jna.share(null, 0));
		Assert.isNull(Jna.share(null, 0, 0));
		Assert.thrown(() -> Jna.share(null, 1));
		Assert.thrown(() -> Jna.share(null, 0, 1));
		Memory m = Jna.mallocBytes(1, 2, 3);
		JnaAssert.memory(Jna.share(m, 0), 0, 1, 2, 3);
		JnaAssert.memory(Jna.share(m, 0, 2), 0, 1, 2);
		JnaAssert.memory(Jna.share(m, 1), 0, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void testSize() {
		Assert.equal(Jna.size(null), 0L);
		Assert.equal(Jna.size(new Memory(5)), 5L);
	}

	@SuppressWarnings("resource")
	@Test
	public void testIntSize() {
		Assert.equal(Jna.intSize(null), 0);
		Assert.equal(Jna.intSize(new Memory(5)), 5);
	}

	@Test
	public void testArrayByRef() {
		Assert.array(Jna.arrayByRef(null, TestStruct::new, TestStruct[]::new));
		Assert.array(Jna.arrayByRef(null, TestStruct::new, TestStruct[]::new, 1),
			new TestStruct[1]);
	}

	@Test
	public void testByRef() {
		Pointer p = data.structArrayByRefPointer(0);
		data.assertStructRead(Jna.byRef(p, 0, TestStruct::new), 0);
		data.assertStructRead(Jna.byRef(p, 1, TestStruct::new), 1);
		data.assertStructRead(Jna.byRef(p, 2, TestStruct::new), 2);
		Assert.isNull(Jna.byRef(p, 3, TestStruct::new));
	}

	@Test
	public void testArrayByValForNullPointer() {
		Assert.array(
			Jna.arrayByVal(null, TestStruct::new, TestStruct[]::new, 0, TestStruct.SIZE));
		Assert.array(
			Jna.arrayByVal(null, TestStruct::new, TestStruct[]::new, 1, TestStruct.SIZE),
			new TestStruct[1]);
	}

	@Test
	public void testArrayByVal() {
		var p = data.structArrayByValPointer(0);
		var array = Struct
			.read(Jna.arrayByVal(p, TestStruct::new, TestStruct[]::new, 4, TestStruct.SIZE));
		data.assertStruct(array[0], 0);
		data.assertStruct(array[1], 1);
		data.assertStruct(array[2], 2);
		JnaTestData.assertEmpty(array[3]);
	}

	@Test
	public void testByVal() {
		Pointer p = data.structArrayByValPointer(0);
		data.assertStructRead(Jna.byVal(p, 0, TestStruct::new, TestStruct.SIZE), 0);
		data.assertStructRead(Jna.byVal(p, 1, TestStruct::new, TestStruct.SIZE), 1);
		data.assertStructRead(Jna.byVal(p, 2, TestStruct::new, TestStruct.SIZE), 2);
		JnaTestData.assertEmpty(Jna.byVal(p, 3, TestStruct::new, TestStruct.SIZE));
	}

	@Test
	public void testAnd() {
		var uint32 = new Uint32(0xeff00000);
		Jna.and(uint32, 0xbf000000);
		Assert.equal(uint32.longValue(), 0xaf000000L);
	}

	@Test
	public void testOr() {
		var uint32 = new Uint32(0x8ff00000);
		Jna.or(uint32, 0x5f000000);
		Assert.equal(uint32.longValue(), 0xdff00000L);
	}

	@Test
	public void testUbyte() {
		ByteByReference ref = new ByteByReference((byte) 0x80);
		Assert.equal(Jna.ubyte(ref), (short) 0x80);
		Assert.equal(Jna.ubyte(ref.getPointer(), 0), (short) 0x80);
	}

	@Test
	public void testUshort() {
		ShortByReference ref = new ShortByReference((short) 0x8000);
		Assert.equal(Jna.ushort(ref), 0x8000);
		Assert.equal(Jna.ushort(ref.getPointer(), 0), 0x8000);
	}

	@Test
	public void testUint() {
		IntByReference ref = new IntByReference(0x80000000);
		Assert.equal(Jna.uint(ref), 0x80000000L);
		Assert.equal(Jna.uint(ref.getPointer(), 0), 0x80000000L);
	}

	@Test
	public void testRefs() {
		Assert.equal(Jna.byteRef(0x80).getValue(), (byte) 0x80);
		Assert.equal(Jna.shortRef(0x8000).getValue(), (short) 0x8000);
		Assert.equal(Jna.intRef(0x80000000).getValue(), 0x80000000);
		Assert.equal(Jna.longRef(0x8000000000000000L).getValue(), 0x8000000000000000L);
	}

	@Test
	public void testRefPointers() {
		Assert.equal(Jna.byteRefPtr(0x80).getByte(0), (byte) 0x80);
		Assert.equal(Jna.shortRefPtr(0x8000).getShort(0), (short) 0x8000);
		Assert.equal(Jna.intRefPtr(0x80000000).getInt(0), 0x80000000);
		Assert.equal(Jna.longRefPtr(0x8000000000000000L).getLong(0), 0x8000000000000000L);
	}

	@Test
	public void testBytesFromPointer() {
		Assert.array(Jna.bytes(Pointer.NULL, 0, 0));
		Assert.thrown(() -> Jna.bytes(Pointer.NULL, 0, 1));
	}

	@Test
	public void testBytesFromMemory() {
		@SuppressWarnings("resource")
		var m = Jna.mallocBytes(-1, 0, 0x80, 1);
		Assert.array(Jna.bytes(m), -1, 0, 0x80, 1);
	}

	@Test
	public void testBytesFromBuffer() {
		ByteBuffer bb = ByteBuffer.wrap(Array.bytes.of(0x80, 0, 0xff));
		Assert.array(Jna.bytes(bb), 0x80, 0, 0xff);
		Assert.array(Jna.bytes(bb, 0, 2), 0x80, 0);
		Assert.array(Jna.bytes(bb, 1), 0, 0xff);
		Assert.array(Jna.bytes(bb, 1, 0));
	}

	@Test
	public void testBytesFromMemoryBuffer() {
		@SuppressWarnings("resource")
		var bb = Jna.mallocBytes(0x80, 0, 0xff).getByteBuffer(0, 3);
		Assert.array(Jna.bytes(bb), 0x80, 0, 0xff);
	}

	@Test
	public void testBytesFromNullBuffer() {
		Assert.thrown(() -> Jna.bytes((ByteBuffer) null, 0, 0));
		Assert.thrown(() -> Jna.bytes((ByteBuffer) null, 0, 1));
	}

	@Test
	public void testBufferPointer() {
		Assert.equal(Jna.pointer(null), null);
		Assert.thrown(() -> Jna.pointer(ByteBuffer.allocate(3)));
		var buffer = ByteBuffer.allocateDirect(3);
		buffer.put(Array.bytes.of(1, 2, 3));
		Assert.array(Jna.bytes(Jna.pointer(buffer), 0, 3), 1, 2, 3);
	}

	@Test
	public void testBuffer() {
		Assert.buffer(Jna.buffer(null, 0));
		Assert.buffer(Jna.buffer(null, 0, 0));
		Assert.thrown(() -> Jna.buffer(null, 0, 1));
		try (var m = Jna.mallocBytes(0x80, 0, 0xff)) {
			Assert.buffer(Jna.buffer(m), 0x80, 0, 0xff);
		}
	}

	@Test
	public void testStringFromBytes() {
		Assert.equal(Jna.string(Array.bytes.of('a', 0, 'b', 0)), "a\0b\0"); // no nul-term
	}

	@Test
	public void testStringFromMemory() {
		try (Memory m = Jna.mallocBytes("abc\0def".getBytes(UTF_8))) {
			Assert.equal(Jna.string(UTF_8, m), "abc\0def");
		}
	}

	@Test
	public void testStringFromBuffer() {
		Assert.equal(Jna.string(ByteBuffer.wrap("abc\0def".getBytes())), "abc\0def");
		Assert.equal(Jna.string(UTF_8, ByteBuffer.wrap("abc\0def".getBytes(UTF_8))),
			"abc\0def");
	}

	@Test
	public void testReadBytesFromPointer() {
		try (Memory m = Jna.mallocBytes(0x80, 0, 0xff, 0x7f, 1)) {
			byte[] buffer = new byte[3];
			Assert.equal(Jna.read(m, buffer), 3);
			Assert.array(buffer, 0x80, 0, 0xff);
		}
	}

	@Test
	public void testReadBytesFromPointerAtIndex() {
		try (var m = Jna.mallocBytes(0x80, 0, 0xff, 0x7f, 1)) {
			byte[] buffer = new byte[3];
			Assert.equal(Jna.read(m, 1, buffer), 3);
			Assert.array(buffer, 0, 0xff, 0x7f);
		}
	}

	@Test
	public void testReadFromNull() {
		Jna.read(null, new byte[0]);
		Assert.thrown(() -> Jna.write(null, new byte[0], 1, 0));
		Assert.thrown(() -> Jna.write(null, new byte[1]));
	}

	@Test
	public void testWriteBytesToPointer() {
		try (var m = Jna.calloc(5)) {
			Assert.equal(Jna.write(m, 1, 0x80, 0, 0xff), 4L);
			JnaAssert.memory(m, 0, 0, 0x80, 0, 0xff, 0);
		}
	}

	@Test
	public void testWriteToNull() {
		Jna.write(null, 0);
		Assert.thrown(() -> Jna.write(null, 1));
		Assert.thrown(() -> Jna.write(null, 0, 1, 2, 3));
	}

	@Test
	public void testFill() {
		Jna.fill(null, 0x33);
		Assert.thrown(() -> Jna.fill(null, 1, 0x33));
		@SuppressWarnings("resource")
		var m = Jna.mallocBytes(-1, 0, 0x80, 1);
		Jna.fill(m, 0x9f);
		Assert.array(Jna.bytes(m), 0x9f, 0x9f, 0x9f, 0x9f);
	}

	@Test
	public void testAsLong() {
		Assert.equal(Jna.asLong(Byte.MIN_VALUE, true), -0x80L);
		Assert.equal(Jna.asLong(Byte.MIN_VALUE, false), 0x80L);
		Assert.equal(Jna.asLong(Short.MIN_VALUE, true), -0x8000L);
		Assert.equal(Jna.asLong(Short.MIN_VALUE, false), 0x8000L);
		Assert.equal(Jna.asLong(Integer.MIN_VALUE, true), -0x80000000L);
		Assert.equal(Jna.asLong(Integer.MIN_VALUE, false), 0x80000000L);
		Assert.equal(Jna.asLong(Long.MIN_VALUE, true), -0x80000000_00000000L);
		Assert.equal(Jna.asLong(Long.MIN_VALUE, false), 0x80000000_00000000L);
		Assert.equal(Jna.asLong("1", true), null);
	}

	@Test
	public void testDefaultCharset() {
		try (var _ = SystemVars.removableProperty("jna.encoding", "test")) {
			ClassReloader.reload(CharsetTester.class, Jna.class);
		}
	}

	public static class CharsetTester {
		static {
			Assert.equal(Jna.DEFAULT_CHARSET, Charset.defaultCharset());
		}
	}
}
