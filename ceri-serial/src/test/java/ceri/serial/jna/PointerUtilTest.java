package ceri.serial.jna;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.serial.jna.JnaTestUtil.assertMemory;
import static ceri.serial.jna.JnaTestUtil.assertPointer;
import static ceri.serial.jna.JnaTestUtil.p;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import ceri.serial.clib.jna.CUtil;
import ceri.serial.jna.JnaTestUtil.TestPointer;

public class PointerUtilTest {

	@Test
	public void testPointer() {
		assertNull(PointerUtil.pointer(0));
	}

	@Test
	public void testPeer() {
		assertEquals(PointerUtil.peer(null), 0L);
		Memory m = new Memory(1);
		assertEquals(PointerUtil.peer(m), Pointer.nativeValue(m));
	}

	@Test
	public void testPointerTypePointer() {
		assertNull(PointerUtil.pointer((PointerType) null));
		IntByReference ref = new IntByReference();
		assertEquals(PointerUtil.pointer(ref), ref.getPointer());
	}

	@Test
	public void testPointerOffset() {
		assertNull(PointerUtil.offset(null, 0));
		Pointer p = p(CUtil.mallocBytes(1, 2, 3));
		assertMemory(PointerUtil.offset(p, 0), 1, 2, 3);
		assertMemory(PointerUtil.offset(p, 1), 2, 3);
	}

	@Test
	public void testDiff() {
		Memory m = new Memory(16);
		Pointer p0 = p(m);
		Pointer p1 = p(m, 11);
		assertEquals(PointerUtil.diff(null, null), null);
		assertEquals(PointerUtil.diff(null, p0), null);
		assertEquals(PointerUtil.diff(p1, null), null);
		assertEquals(PointerUtil.diff(p1, p0), 11L);
	}

	@Test
	public void testCount() {
		Pointer[] array = CUtil.callocArray(3);
		assertEquals(PointerUtil.count(null), 0);
		assertEquals(PointerUtil.count(array[0]), 0);
		array[0].setPointer(0, new Memory(1));
		assertEquals(PointerUtil.count(array[0]), 1);
		array[1].setPointer(0, new Memory(1));
		assertEquals(PointerUtil.count(array[0]), 2);
	}

	@Test
	public void testOverlap() {
		assertEquals(PointerUtil.overlap(null, null, 0), false);
		assertEquals(PointerUtil.overlap(null, 1, null, 0, 1), false);
		assertEquals(PointerUtil.overlap(null, 0, null, 1, 1), false);
		assertEquals(PointerUtil.overlap(null, 1, null, 0, 2), true);
		assertEquals(PointerUtil.overlap(null, 0, null, 1, 2), true);
	}

	@Test
	public void testNullTermArrayByRefForPointerTypes() {
		assertArray(PointerUtil.arrayByRef(null, TestPointer::new, TestPointer[]::new));
		Pointer[] pointers = { new Memory(1), new Memory(2), new Memory(3), null };
		Pointer[] array0 = JnaTestUtil.indirect(pointers);
		var array = PointerUtil.arrayByRef(array0[0], TestPointer::new, TestPointer[]::new);
		assertPointer(array[0], pointers[0]);
		assertPointer(array[1], pointers[1]);
		assertPointer(array[2], pointers[2]);
		assertEquals(array.length, 3);
	}

	@Test
	public void testArrayByRefForPointerTypes() {
		assertArray(PointerUtil.arrayByRef(null, TestPointer::new, TestPointer[]::new, 1),
			new TestPointer[1]);
		Pointer[] pointers = { new Memory(1), new Memory(2), new Memory(3) };
		Pointer[] array0 = JnaTestUtil.indirect(pointers);
		var array = PointerUtil.arrayByRef(array0[0], TestPointer::new, TestPointer[]::new, 2);
		assertPointer(array[0], pointers[0]);
		assertPointer(array[1], pointers[1]);
		assertEquals(array.length, 2);
	}

	@Test
	public void testArrayByValForPointers() {
		assertArray(PointerUtil.arrayByVal(null, 2), new Pointer[2]);
		Pointer[] array0 = CUtil.callocArray(3);
		var array = PointerUtil.arrayByVal(array0[0], 2);
		assertEquals(array[0], array0[0]);
		assertEquals(array[1], array0[1]);
		assertEquals(array.length, 2);
	}

	@Test
	public void testArrayByValForPointerTypes() {
		assertArray(PointerUtil.arrayByVal(null, TestPointer::new, TestPointer[]::new, 1),
			new TestPointer[1]);
		Pointer[] array0 = CUtil.callocArray(3);
		var array = PointerUtil.arrayByVal(array0[0], TestPointer::new, TestPointer[]::new, 2);
		assertPointer(array[0], array0[0]);
		assertPointer(array[1], array0[1]);
		assertEquals(array.length, 2);
	}

	@Test
	public void testByRef() {
		Pointer[] pointers = { new Memory(1), new Memory(2), new Memory(3) };
		Pointer[] array0 = JnaTestUtil.indirect(pointers);
		Pointer p = array0[0];
		assertEquals(PointerUtil.byRef(null), null);
		assertEquals(PointerUtil.byRef(p), pointers[0]);
		assertEquals(PointerUtil.byRef(p, 1), pointers[1]);
		assertEquals(PointerUtil.byRef(p, 2), pointers[2]);
	}

	@Test
	public void testByVal() {
		assertEquals(PointerUtil.byVal(null, 1), null);
		Memory m = new Memory(Pointer.SIZE * 3);
		assertEquals(PointerUtil.byVal(m, 0), p(m));
		assertEquals(PointerUtil.byVal(m, 1), p(m, Pointer.SIZE));
		assertEquals(PointerUtil.byVal(m, 2), p(m, Pointer.SIZE * 2));
	}

	@Test
	public void testByValForPointerType() {
		assertEquals(PointerUtil.byVal(null, 1, TestPointer::new), null);
		Memory m = new Memory(Pointer.SIZE * 3);
		assertPointer(PointerUtil.byVal(m, 0, TestPointer::new), p(m));
		assertPointer(PointerUtil.byVal(m, 1, TestPointer::new), p(m, Pointer.SIZE));
		assertPointer(PointerUtil.byVal(m, 2, TestPointer::new), p(m, Pointer.SIZE * 2));
	}

	@Test
	public void testSetPointerType() {
		Memory m = new Memory(3);
		assertEquals(PointerUtil.set(null, m), null);
		assertPointer(PointerUtil.set(new TestPointer(), m), m);
	}

	@Test
	public void testSetPointerTypeFromRef() {
		Memory m = new Memory(3);
		PointerByReference ref = new PointerByReference(m);
		assertEquals(PointerUtil.set(null, ref), null);
		assertPointer(PointerUtil.set(new TestPointer(), ref), m);
		assertPointer(PointerUtil.set(new TestPointer(), (PointerByReference) null), null);
	}

}
