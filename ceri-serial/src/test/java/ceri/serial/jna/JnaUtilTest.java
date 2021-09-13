package ceri.serial.jna;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertBuffer;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.serial.jna.JnaTestUtil.assertMemory;
import static ceri.serial.jna.JnaTestUtil.assertTestStruct;
import static ceri.serial.jna.JnaTestUtil.populate;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.ByteBuffer;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.ShortByReference;
import ceri.common.collection.ArrayUtil;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.jna.JnaTestUtil.TestStruct;

public class JnaUtilTest {

	@Test
	public void testSetProtected() {
		boolean prot = Native.isProtected();
		boolean result = JnaUtil.setProtected();
		if (prot != result) Native.setProtected(prot);
	}

	@Test
	public void testLazyBuffer() {
		var lazy = JnaUtil.lazyBuffer(5);
		assertEquals(lazy.get().size(), 5L);
	}

	@Test
	public void testPointerOffset() {
		assertNull(JnaUtil.offset(null, 1));
		assertNull(JnaUtil.offset(null, 0, 2));
		Memory m = CUtil.mallocBytes(1, 2, 3);
		assertMemory(JnaUtil.offset(m, 0), 1, 2, 3);
		assertMemory(JnaUtil.offset(m, 0, 2), 1, 2);
		assertMemory(JnaUtil.offset(m, 1), 2, 3);
	}

	@Test
	public void testShareNullMemory() {
		assertNull(JnaUtil.share(null, 0, 0));
	}

	@Test
	public void testSize() {
		assertEquals(JnaUtil.size(null), 0);
		assertEquals(JnaUtil.size(new Memory(5)), 5);
	}

	@Test
	public void testArrayByRef() {
		assertArray(JnaUtil.arrayByRef(null, TestStruct::new, TestStruct[]::new));
		assertArray(JnaUtil.arrayByRef(null, TestStruct::new, TestStruct[]::new, 1),
			new TestStruct[1]);
	}

	@Test
	public void testByRef() {
		TestStruct[] inners = { new TestStruct(100), null, new TestStruct(300) };
		Pointer[] ps = CUtil.mallocArray(3);
		for (int i = 0; i < ps.length; i++)
			ps[i].setPointer(0, Struct.pointer(inners[i]));
		assertEquals(JnaUtil.byRef(ps[0], 0, TestStruct::new), inners[0]);
		assertEquals(JnaUtil.byRef(ps[0], 1, TestStruct::new), null);
		assertEquals(JnaUtil.byRef(ps[0], 2, TestStruct::new), inners[2]);
	}

	@Test
	public void testArrayByValForNullPointer() {
		assertArray(
			JnaUtil.arrayByVal(null, TestStruct::new, TestStruct[]::new, 0, TestStruct.SIZE));
		assertArray(
			JnaUtil.arrayByVal(null, TestStruct::new, TestStruct[]::new, 1, TestStruct.SIZE),
			new TestStruct[1]);
	}

	@Test
	public void testArrayByVal() {
		TestStruct[] array0 = Struct.arrayByVal(() -> new TestStruct(), TestStruct[]::new, 3);
		populate(array0[0], 100, null, 1);
		populate(array0[1], 200, null, 2);
		populate(array0[2], 300, null, 3);
		Pointer p = Struct.pointer(Struct.write(array0));
		TestStruct[] array = Struct
			.read(JnaUtil.arrayByVal(p, TestStruct::new, TestStruct[]::new, 3, TestStruct.SIZE));
		assertTestStruct(array[0], 100, null, 1, 0, 0);
		assertTestStruct(array[1], 200, null, 2, 0, 0);
		assertTestStruct(array[2], 300, null, 3, 0, 0);
	}

	@Test
	public void testByVal() {
		TestStruct[] array0 = Struct.arrayByVal(() -> new TestStruct(), TestStruct[]::new, 3);
		populate(array0[0], 100, null, 1);
		populate(array0[1], 200, null, 2);
		populate(array0[2], 300, null, 3);
		Pointer p = Struct.pointer(Struct.write(array0));
		assertTestStruct(Struct.<TestStruct>read( //
			JnaUtil.byVal(p, 0, TestStruct::new, TestStruct.SIZE)), 100, null, 1, 0, 0);
		assertTestStruct(Struct.<TestStruct>read( //
			JnaUtil.byVal(p, 1, TestStruct::new, TestStruct.SIZE)), 200, null, 2, 0, 0);
		assertTestStruct(Struct.<TestStruct>read( //
			JnaUtil.byVal(p, 2, TestStruct::new, TestStruct.SIZE)), 300, null, 3, 0, 0);
	}

	@Test
	public void testUbyte() {
		ByteByReference ref = new ByteByReference((byte) 0x80);
		assertEquals(JnaUtil.ubyte(ref), (short) 0x80);
		assertEquals(JnaUtil.ubyte(ref.getPointer(), 0), (short) 0x80);
	}

	@Test
	public void testUshort() {
		ShortByReference ref = new ShortByReference((short) 0x8000);
		assertEquals(JnaUtil.ushort(ref), 0x8000);
		assertEquals(JnaUtil.ushort(ref.getPointer(), 0), 0x8000);
	}

	@Test
	public void testUint() {
		IntByReference ref = new IntByReference(0x80000000);
		assertEquals(JnaUtil.uint(ref), 0x80000000L);
		assertEquals(JnaUtil.uint(ref.getPointer(), 0), 0x80000000L);
	}

	@Test
	public void testUnlong() {
		NativeLongByReference ref = new NativeLongByReference(new NativeLong(0x80000000L));
		assertEquals(JnaUtil.unlong(ref), 0x80000000L);
		assertEquals(JnaUtil.unlong(ref.getPointer(), 0), 0x80000000L);
	}

	@Test
	public void testRefs() {
		assertEquals(JnaUtil.byteRef(0x80).getValue(), (byte) 0x80);
		assertEquals(JnaUtil.shortRef(0x8000).getValue(), (short) 0x8000);
		assertEquals(JnaUtil.intRef(0x80000000).getValue(), 0x80000000);
		assertEquals(JnaUtil.nlongRef(0x80000000).getValue(), new NativeLong(0x80000000));
		assertEquals(JnaUtil.unlongRef(0x80000000).getValue(), new NativeLong(0x80000000, true));
		assertEquals(JnaUtil.longRef(0x8000000000000000L).getValue(), 0x8000000000000000L);
	}

	@Test
	public void testRefPointers() {
		assertEquals(JnaUtil.byteRefPtr(0x80).getByte(0), (byte) 0x80);
		assertEquals(JnaUtil.shortRefPtr(0x8000).getShort(0), (short) 0x8000);
		assertEquals(JnaUtil.intRefPtr(0x80000000).getInt(0), 0x80000000);
		assertEquals(JnaUtil.nlongRefPtr(0x80000000).getNativeLong(0), new NativeLong(0x80000000));
		assertEquals(JnaUtil.unlongRefPtr(0x80000000).getNativeLong(0),
			new NativeLong(0x80000000, true));
		assertEquals(JnaUtil.longRefPtr(0x8000000000000000L).getLong(0), 0x8000000000000000L);
	}

	@Test
	public void testBytesFromPointer() {
		assertArray(JnaUtil.bytes(Pointer.NULL, 0, 0));
		assertThrown(() -> JnaUtil.bytes(Pointer.NULL, 0, 1));
	}

	@Test
	public void testBytesFromBuffer() {
		assertArray(JnaUtil.bytes((ByteBuffer) null, 0, 0));
		assertThrown(() -> JnaUtil.bytes((ByteBuffer) null, 0, 1));
		ByteBuffer bb = ByteBuffer.wrap(ArrayUtil.bytes(0x80, 0, 0xff));
		assertArray(JnaUtil.bytes(bb), 0x80, 0, 0xff);
		assertArray(JnaUtil.bytes(bb, 0, 2), 0x80, 0);
		assertArray(JnaUtil.bytes(bb, 1), 0, 0xff);
	}

	@Test
	public void testBuffer() {
		assertBuffer(JnaUtil.buffer(null, 0, 0));
		assertThrown(() -> JnaUtil.buffer(null, 0, 1));
		Memory m = CUtil.mallocBytes(0x80, 0, 0xff);
		assertBuffer(JnaUtil.buffer(m), 0x80, 0, 0xff);
	}

	@Test
	public void testStringFromBytes() {
		assertEquals(JnaUtil.string("abc".getBytes()), "abc");
		assertEquals(JnaUtil.string("abc\0def".getBytes()), "abc");
	}

	@Test
	public void testStringFromMemory() {
		Memory m = CUtil.malloc("abc\0def".getBytes(UTF_8));
		assertEquals(JnaUtil.string(UTF_8, m), "abc\0def");
	}

	@Test
	public void testStringFromBuffer() {
		assertEquals(JnaUtil.string(ByteBuffer.wrap("abc\0def".getBytes())), "abc\0def");
		assertEquals(JnaUtil.string(UTF_8, ByteBuffer.wrap("abc\0def".getBytes(UTF_8))),
			"abc\0def");
	}

	@Test
	public void testReadBytesFromPointer() {
		Memory m = CUtil.mallocBytes(0x80, 0, 0xff, 0x7f, 1);
		byte[] buffer = new byte[3];
		assertEquals(JnaUtil.read(m, buffer), 3);
		assertArray(buffer, 0x80, 0, 0xff);
	}

	@Test
	public void testReadBytesFromPointerAtIndex() {
		Memory m = CUtil.mallocBytes(0x80, 0, 0xff, 0x7f, 1);
		byte[] buffer = new byte[3];
		assertEquals(JnaUtil.read(m, 1, buffer), 3);
		assertArray(buffer, 0, 0xff, 0x7f);
	}

	@Test
	public void testWriteBytesToPointer() {
		Memory m = CUtil.calloc(5);
		assertEquals(JnaUtil.write(m, 1, 0x80, 0, 0xff), 4);
		assertMemory(m, 0, 0x80, 0, 0xff, 0);
	}

}
