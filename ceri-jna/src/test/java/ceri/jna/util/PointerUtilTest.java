package ceri.jna.util;

import static ceri.jna.test.JnaTestUtil.assertPointer;
import static ceri.jna.test.JnaTestUtil.deref;
import static ceri.jna.test.JnaTestUtil.mem;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import ceri.common.test.Assert;
import ceri.jna.type.JnaSize;

public class PointerUtilTest {

	public static class TestPointer extends PointerType {}

	@Test
	public void testPointer() {
		Assert.isNull(PointerUtil.pointer(0));
	}

	@Test
	public void testPeer() {
		Assert.equal(PointerUtil.peer(null), 0L);
		try (Memory m = new Memory(1)) {
			Assert.equal(PointerUtil.peer(m), Pointer.nativeValue(m));
		}
	}

	@Test
	public void testPointerTypePointer() {
		Assert.isNull(PointerUtil.pointer((PointerType) null));
		IntByReference ref = new IntByReference();
		Assert.equal(PointerUtil.pointer(ref), ref.getPointer());
	}

	@Test
	public void testPointerOffset() {
		Assert.isNull(PointerUtil.offset(null, 0));
		Pointer p = deref(mem(1, 2, 3).m);
		assertPointer(PointerUtil.offset(p, 0), 0, 1, 2, 3);
		assertPointer(PointerUtil.offset(p, 1), 0, 2, 3);
	}

	@Test
	public void testDiff() {
		try (Memory m = new Memory(16)) {
			Pointer p0 = deref(m);
			Pointer p1 = ceri.jna.test.JnaTestUtil.deref(m, 11);
			Assert.equal(PointerUtil.diff(null, null), null);
			Assert.equal(PointerUtil.diff(null, p0), null);
			Assert.equal(PointerUtil.diff(p1, null), null);
			Assert.equal(PointerUtil.diff(p1, p0), 11L);
		}
	}

	@Test
	public void testCount() {
		Pointer[] array = PointerUtil.callocArray(3);
		Assert.equal(PointerUtil.count(null), 0);
		Assert.equal(PointerUtil.count(array[0]), 0);
		array[0].setPointer(0, GcMemory.malloc(1).m);
		Assert.equal(PointerUtil.count(array[0]), 1);
		array[1].setPointer(0, GcMemory.malloc(1).m);
		Assert.equal(PointerUtil.count(array[0]), 2);
	}

	@Test
	public void testOverlap() {
		Assert.equal(PointerUtil.overlap(null, null, 0), false);
		Assert.equal(PointerUtil.overlap(null, 1, null, 0, 1), false);
		Assert.equal(PointerUtil.overlap(null, 0, null, 1, 1), false);
		Assert.equal(PointerUtil.overlap(null, 1, null, 0, 2), true);
		Assert.equal(PointerUtil.overlap(null, 0, null, 1, 2), true);
	}

	@Test
	public void testMallocArray() {
		Assert.array(PointerUtil.mallocArray(0));
		Pointer[] array = PointerUtil.callocArray(3, 6);
		Assert.array(array, array[0], array[0].share(6), array[0].share(12));
	}

	@Test
	public void testMallocPointerTypeArray() {
		TestPointer[] array = PointerUtil.mallocArray(TestPointer::new, TestPointer[]::new, 3);
		Assert.equal(array[1].getPointer(), array[0].getPointer().share(JnaSize.POINTER.get()));
		Assert.equal(array[2].getPointer(), array[1].getPointer().share(JnaSize.POINTER.get()));
		Assert.equal(array.length, 3);
	}

	@Test
	public void testCallocArray() {
		Assert.array(PointerUtil.callocArray(0));
		Pointer[] array = PointerUtil.callocArray(3, 6);
		Assert.array(array, array[0], array[0].share(6), array[0].share(12));
		assertPointer(array[0], 0, 0, 0, 0, 0, 0, 0);
		assertPointer(array[1], 0, 0, 0, 0, 0, 0, 0);
		assertPointer(array[2], 0, 0, 0, 0, 0, 0, 0);
	}

	@Test
	public void testCallocPointerTypeArray() {
		TestPointer[] array = PointerUtil.callocArray(TestPointer::new, TestPointer[]::new, 3);
		Assert.equal(array[1].getPointer(), array[0].getPointer().share(JnaSize.POINTER.get()));
		Assert.equal(array[2].getPointer(), array[1].getPointer().share(JnaSize.POINTER.get()));
		Assert.equal(array.length, 3);
		Assert.isNull(array[0].getPointer().getPointer(0));
		Assert.isNull(array[1].getPointer().getPointer(0));
		Assert.isNull(array[2].getPointer().getPointer(0));
	}

	@Test
	public void testNullTermArrayByRefForPointerTypes() {
		Assert.array(PointerUtil.arrayByRef(null, TestPointer::new, TestPointer[]::new));
		Pointer[] pointers =
			{ GcMemory.malloc(1).m, GcMemory.malloc(2).m, GcMemory.malloc(3).m, null };
		Pointer[] array0 = indirect(pointers);
		var array = PointerUtil.arrayByRef(array0[0], TestPointer::new, TestPointer[]::new);
		assertPointer(array[0], pointers[0]);
		assertPointer(array[1], pointers[1]);
		assertPointer(array[2], pointers[2]);
		Assert.equal(array.length, 3);
	}

	@Test
	public void testArrayByRefForPointerTypes() {
		Assert.array(PointerUtil.arrayByRef(null, TestPointer::new, TestPointer[]::new, 1),
			new TestPointer[1]);
		Pointer[] pointers = { GcMemory.malloc(1).m, GcMemory.malloc(2).m, GcMemory.malloc(3).m };
		Pointer[] array0 = indirect(pointers);
		var array = PointerUtil.arrayByRef(array0[0], TestPointer::new, TestPointer[]::new, 2);
		assertPointer(array[0], pointers[0]);
		assertPointer(array[1], pointers[1]);
		Assert.equal(array.length, 2);
	}

	@Test
	public void testArrayByValForPointers() {
		Assert.array(PointerUtil.arrayByVal(null, 2), new Pointer[2]);
		Pointer[] array0 = PointerUtil.callocArray(3);
		var array = PointerUtil.arrayByVal(array0[0], 2);
		Assert.equal(array[0], array0[0]);
		Assert.equal(array[1], array0[1]);
		Assert.equal(array.length, 2);
	}

	@Test
	public void testArrayByValForPointerTypes() {
		Assert.array(PointerUtil.arrayByVal(null, TestPointer::new, TestPointer[]::new, 1),
			new TestPointer[1]);
		Pointer[] array0 = PointerUtil.callocArray(3);
		var array = PointerUtil.arrayByVal(array0[0], TestPointer::new, TestPointer[]::new, 2);
		assertPointer(array[0], array0[0]);
		assertPointer(array[1], array0[1]);
		Assert.equal(array.length, 2);
	}

	@Test
	public void testByRef() {
		Pointer[] pointers = { GcMemory.malloc(1).m, GcMemory.malloc(2).m, GcMemory.malloc(3).m };
		Pointer[] array0 = indirect(pointers);
		Pointer p = array0[0];
		Assert.equal(PointerUtil.byRef(null), null);
		Assert.equal(PointerUtil.byRef(p), pointers[0]);
		Assert.equal(PointerUtil.byRef(p, 1), pointers[1]);
		Assert.equal(PointerUtil.byRef(p, 2), pointers[2]);
	}

	@Test
	public void testByVal() {
		Assert.equal(PointerUtil.byVal(null, 1), null);
		try (Memory m = new Memory(JnaSize.POINTER.get() * 3)) {
			Assert.equal(PointerUtil.byVal(m, 0), deref(m));
			Assert.equal(PointerUtil.byVal(m, 1),
				ceri.jna.test.JnaTestUtil.deref(m, JnaSize.POINTER.get()));
			Assert.equal(PointerUtil.byVal(m, 2),
				ceri.jna.test.JnaTestUtil.deref(m, JnaSize.POINTER.get() * 2));
		}
	}

	@Test
	public void testByValForPointerType() {
		Assert.equal(PointerUtil.byVal(null, 1, TestPointer::new), null);
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
			Assert.equal(PointerUtil.set(null, m), null);
			assertPointer(PointerUtil.set(new TestPointer(), m), m);
		}
	}

	@Test
	public void testSetPointerTypeFromRef() {
		try (Memory m = new Memory(3)) {
			PointerByReference ref = new PointerByReference(m);
			Assert.equal(PointerUtil.set(null, ref), null);
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
