package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.jna.test.JnaTestUtil.assertPointer;
import static ceri.jna.test.JnaTestUtil.deref;
import static ceri.jna.test.JnaTestUtil.mem;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class PointerUtilTest {

	public static class TestPointer extends PointerType {}

	@Test
	public void testPointer() {
		assertNull(PointerUtil.pointer(0));
	}

	@Test
	public void testPeer() {
		assertEquals(PointerUtil.peer(null), 0L);
		try (Memory m = new Memory(1)) {
			assertEquals(PointerUtil.peer(m), Pointer.nativeValue(m));
		}
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
		Pointer p = deref(mem(1, 2, 3).m);
		assertPointer(PointerUtil.offset(p, 0), 0, 1, 2, 3);
		assertPointer(PointerUtil.offset(p, 1), 0, 2, 3);
	}

	@Test
	public void testDiff() {
		try (Memory m = new Memory(16)) {
			Pointer p0 = deref(m);
			Pointer p1 = ceri.jna.test.JnaTestUtil.deref(m, 11);
			assertEquals(PointerUtil.diff(null, null), null);
			assertEquals(PointerUtil.diff(null, p0), null);
			assertEquals(PointerUtil.diff(p1, null), null);
			assertEquals(PointerUtil.diff(p1, p0), 11L);
		}
	}

	@Test
	public void testCount() {
		Pointer[] array = PointerUtil.callocArray(3);
		assertEquals(PointerUtil.count(null), 0);
		assertEquals(PointerUtil.count(array[0]), 0);
		array[0].setPointer(0, GcMemory.malloc(1).m);
		assertEquals(PointerUtil.count(array[0]), 1);
		array[1].setPointer(0, GcMemory.malloc(1).m);
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
	public void testMallocArray() {
		assertArray(PointerUtil.mallocArray(0));
		Pointer[] array = PointerUtil.callocArray(3, 6);
		assertArray(array, array[0], array[0].share(6), array[0].share(12));
	}

	@Test
	public void testMallocPointerTypeArray() {
		TestPointer[] array = PointerUtil.mallocArray(TestPointer::new, TestPointer[]::new, 3);
		assertEquals(array[1].getPointer(), array[0].getPointer().share(JnaSize.POINTER.get()));
		assertEquals(array[2].getPointer(), array[1].getPointer().share(JnaSize.POINTER.get()));
		assertEquals(array.length, 3);
	}

	@Test
	public void testCallocArray() {
		assertArray(PointerUtil.callocArray(0));
		Pointer[] array = PointerUtil.callocArray(3, 6);
		assertArray(array, array[0], array[0].share(6), array[0].share(12));
		assertPointer(array[0], 0, 0, 0, 0, 0, 0, 0);
		assertPointer(array[1], 0, 0, 0, 0, 0, 0, 0);
		assertPointer(array[2], 0, 0, 0, 0, 0, 0, 0);
	}

	@Test
	public void testCallocPointerTypeArray() {
		TestPointer[] array = PointerUtil.callocArray(TestPointer::new, TestPointer[]::new, 3);
		assertEquals(array[1].getPointer(), array[0].getPointer().share(JnaSize.POINTER.get()));
		assertEquals(array[2].getPointer(), array[1].getPointer().share(JnaSize.POINTER.get()));
		assertEquals(array.length, 3);
		assertNull(array[0].getPointer().getPointer(0));
		assertNull(array[1].getPointer().getPointer(0));
		assertNull(array[2].getPointer().getPointer(0));
	}

	@Test
	public void testNullTermArrayByRefForPointerTypes() {
		assertArray(PointerUtil.arrayByRef(null, TestPointer::new, TestPointer[]::new));
		Pointer[] pointers =
			{ GcMemory.malloc(1).m, GcMemory.malloc(2).m, GcMemory.malloc(3).m, null };
		Pointer[] array0 = indirect(pointers);
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
		Pointer[] pointers = { GcMemory.malloc(1).m, GcMemory.malloc(2).m, GcMemory.malloc(3).m };
		Pointer[] array0 = indirect(pointers);
		var array = PointerUtil.arrayByRef(array0[0], TestPointer::new, TestPointer[]::new, 2);
		assertPointer(array[0], pointers[0]);
		assertPointer(array[1], pointers[1]);
		assertEquals(array.length, 2);
	}

	@Test
	public void testArrayByValForPointers() {
		assertArray(PointerUtil.arrayByVal(null, 2), new Pointer[2]);
		Pointer[] array0 = PointerUtil.callocArray(3);
		var array = PointerUtil.arrayByVal(array0[0], 2);
		assertEquals(array[0], array0[0]);
		assertEquals(array[1], array0[1]);
		assertEquals(array.length, 2);
	}

	@Test
	public void testArrayByValForPointerTypes() {
		assertArray(PointerUtil.arrayByVal(null, TestPointer::new, TestPointer[]::new, 1),
			new TestPointer[1]);
		Pointer[] array0 = PointerUtil.callocArray(3);
		var array = PointerUtil.arrayByVal(array0[0], TestPointer::new, TestPointer[]::new, 2);
		assertPointer(array[0], array0[0]);
		assertPointer(array[1], array0[1]);
		assertEquals(array.length, 2);
	}

	@Test
	public void testByRef() {
		Pointer[] pointers = { GcMemory.malloc(1).m, GcMemory.malloc(2).m, GcMemory.malloc(3).m };
		Pointer[] array0 = indirect(pointers);
		Pointer p = array0[0];
		assertEquals(PointerUtil.byRef(null), null);
		assertEquals(PointerUtil.byRef(p), pointers[0]);
		assertEquals(PointerUtil.byRef(p, 1), pointers[1]);
		assertEquals(PointerUtil.byRef(p, 2), pointers[2]);
	}

	@Test
	public void testByVal() {
		assertEquals(PointerUtil.byVal(null, 1), null);
		try (Memory m = new Memory(JnaSize.POINTER.get() * 3)) {
			assertEquals(PointerUtil.byVal(m, 0), deref(m));
			assertEquals(PointerUtil.byVal(m, 1),
				ceri.jna.test.JnaTestUtil.deref(m, JnaSize.POINTER.get()));
			assertEquals(PointerUtil.byVal(m, 2),
				ceri.jna.test.JnaTestUtil.deref(m, JnaSize.POINTER.get() * 2));
		}
	}

	@Test
	public void testByValForPointerType() {
		assertEquals(PointerUtil.byVal(null, 1, TestPointer::new), null);
		try (Memory m = new Memory(JnaSize.POINTER.get() * 3)) {
			assertPointer(PointerUtil.byVal(m, 0, TestPointer::new), deref(m));
			assertPointer(PointerUtil.byVal(m, 1, TestPointer::new),
				ceri.jna.test.JnaTestUtil.deref(m, JnaSize.POINTER.get()));
			assertPointer(PointerUtil.byVal(m, 2, TestPointer::new),
				ceri.jna.test.JnaTestUtil.deref(m, JnaSize.POINTER.get() * 2));
		}
	}

	@Test
	public void testSetPointerType() {
		try (Memory m = new Memory(3)) {
			assertEquals(PointerUtil.set(null, m), null);
			assertPointer(PointerUtil.set(new TestPointer(), m), m);
		}
	}

	@Test
	public void testSetPointerTypeFromRef() {
		try (Memory m = new Memory(3)) {
			PointerByReference ref = new PointerByReference(m);
			assertEquals(PointerUtil.set(null, ref), null);
			assertPointer(PointerUtil.set(new TestPointer(), ref), m);
		}
		assertPointer(PointerUtil.set(new TestPointer(), (PointerByReference) null), null);
	}

	/**
	 * Allocates a contiguous pointer array with given pointer values. Returns indirected pointers.
	 */
	private static Pointer[] indirect(Pointer... ps) {
		var m = GcMemory.malloc(ps.length * JnaSize.POINTER.get()).clear();
		Pointer[] array = new Pointer[ps.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = m.share(i * JnaSize.POINTER.get(), JnaSize.POINTER.get()).m;
			array[i].setPointer(0, ps[i]);
		}
		return array;
	}
}
