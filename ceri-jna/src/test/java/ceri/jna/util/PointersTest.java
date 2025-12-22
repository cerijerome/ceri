package ceri.jna.util;

import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import ceri.common.test.Assert;
import ceri.jna.test.JnaAssert;
import ceri.jna.test.JnaTesting;
import ceri.jna.type.JnaSize;

public class PointersTest {

	public static class TestPointer extends PointerType {}

	@Test
	public void testPointer() {
		Assert.isNull(Pointers.pointer(0));
	}

	@Test
	public void testPeer() {
		Assert.equal(Pointers.peer(null), 0L);
		try (var m = new Memory(1)) {
			Assert.equal(Pointers.peer(m), Pointer.nativeValue(m));
		}
	}

	@Test
	public void testPointerTypePointer() {
		Assert.isNull(Pointers.pointer((PointerType) null));
		var ref = new IntByReference();
		Assert.equal(Pointers.pointer(ref), ref.getPointer());
	}

	@Test
	public void testPointerOffset() {
		Assert.isNull(Pointers.offset(null, 0));
		var p = JnaTesting.deref(JnaTesting.mem(1, 2, 3).m);
		JnaAssert.pointer(Pointers.offset(p, 0), 0, 1, 2, 3);
		JnaAssert.pointer(Pointers.offset(p, 1), 0, 2, 3);
	}

	@Test
	public void testDiff() {
		try (var m = new Memory(16)) {
			var p0 = JnaTesting.deref(m);
			var p1 = ceri.jna.test.JnaTesting.deref(m, 11);
			Assert.equal(Pointers.diff(null, null), null);
			Assert.equal(Pointers.diff(null, p0), null);
			Assert.equal(Pointers.diff(p1, null), null);
			Assert.equal(Pointers.diff(p1, p0), 11L);
		}
	}

	@Test
	public void testCount() {
		var array = Pointers.callocArray(3);
		Assert.equal(Pointers.count(null), 0);
		Assert.equal(Pointers.count(array[0]), 0);
		array[0].setPointer(0, GcMemory.malloc(1).m);
		Assert.equal(Pointers.count(array[0]), 1);
		array[1].setPointer(0, GcMemory.malloc(1).m);
		Assert.equal(Pointers.count(array[0]), 2);
	}

	@Test
	public void testOverlap() {
		Assert.equal(Pointers.overlap(null, null, 0), false);
		Assert.equal(Pointers.overlap(null, 1, null, 0, 1), false);
		Assert.equal(Pointers.overlap(null, 0, null, 1, 1), false);
		Assert.equal(Pointers.overlap(null, 1, null, 0, 2), true);
		Assert.equal(Pointers.overlap(null, 0, null, 1, 2), true);
	}

	@Test
	public void testMallocArray() {
		Assert.array(Pointers.mallocArray(0));
		var array = Pointers.callocArray(3, 6);
		Assert.array(array, array[0], array[0].share(6), array[0].share(12));
	}

	@Test
	public void testMallocPointerTypeArray() {
		var array = Pointers.mallocArray(TestPointer::new, TestPointer.class, 3);
		Assert.equal(array[1].getPointer(), array[0].getPointer().share(JnaSize.POINTER.get()));
		Assert.equal(array[2].getPointer(), array[1].getPointer().share(JnaSize.POINTER.get()));
		Assert.equal(array.length, 3);
	}

	@Test
	public void testCallocArray() {
		Assert.array(Pointers.callocArray(0));
		var array = Pointers.callocArray(3, 6);
		Assert.array(array, array[0], array[0].share(6), array[0].share(12));
		JnaAssert.pointer(array[0], 0, 0, 0, 0, 0, 0, 0);
		JnaAssert.pointer(array[1], 0, 0, 0, 0, 0, 0, 0);
		JnaAssert.pointer(array[2], 0, 0, 0, 0, 0, 0, 0);
	}

	@Test
	public void testCallocPointerTypeArray() {
		var array = Pointers.callocArray(TestPointer::new, TestPointer.class, 3);
		Assert.equal(array[1].getPointer(), array[0].getPointer().share(JnaSize.POINTER.get()));
		Assert.equal(array[2].getPointer(), array[1].getPointer().share(JnaSize.POINTER.get()));
		Assert.equal(array.length, 3);
		Assert.isNull(array[0].getPointer().getPointer(0));
		Assert.isNull(array[1].getPointer().getPointer(0));
		Assert.isNull(array[2].getPointer().getPointer(0));
	}

	@Test
	public void testNullTermArrayByRefForPointerTypes() {
		Assert.array(Pointers.arrayByRef(null, TestPointer::new, TestPointer[]::new));
		Pointer[] pointers =
			{ GcMemory.malloc(1).m, GcMemory.malloc(2).m, GcMemory.malloc(3).m, null };
		var array0 = indirect(pointers);
		var array = Pointers.arrayByRef(array0[0], TestPointer::new, TestPointer[]::new);
		JnaAssert.pointer(array[0], pointers[0]);
		JnaAssert.pointer(array[1], pointers[1]);
		JnaAssert.pointer(array[2], pointers[2]);
		Assert.equal(array.length, 3);
	}

	@Test
	public void testArrayByRefForPointerTypes() {
		Assert.array(Pointers.arrayByRef(null, TestPointer::new, TestPointer[]::new, 1),
			new TestPointer[1]);
		Pointer[] pointers = { GcMemory.malloc(1).m, GcMemory.malloc(2).m, GcMemory.malloc(3).m };
		var array0 = indirect(pointers);
		var array = Pointers.arrayByRef(array0[0], TestPointer::new, TestPointer[]::new, 2);
		JnaAssert.pointer(array[0], pointers[0]);
		JnaAssert.pointer(array[1], pointers[1]);
		Assert.equal(array.length, 2);
	}

	@Test
	public void testArrayByValForPointers() {
		Assert.array(Pointers.arrayByVal(null, 2), new Pointer[2]);
		var array0 = Pointers.callocArray(3);
		var array = Pointers.arrayByVal(array0[0], 2);
		Assert.equal(array[0], array0[0]);
		Assert.equal(array[1], array0[1]);
		Assert.equal(array.length, 2);
	}

	@Test
	public void testArrayByValForPointerTypes() {
		Assert.array(Pointers.arrayByVal(null, TestPointer::new, TestPointer.class, 1),
			new TestPointer[1]);
		var array0 = Pointers.callocArray(3);
		var array = Pointers.arrayByVal(array0[0], TestPointer::new, TestPointer.class, 2);
		JnaAssert.pointer(array[0], array0[0]);
		JnaAssert.pointer(array[1], array0[1]);
		Assert.equal(array.length, 2);
	}

	@Test
	public void testByRef() {
		Pointer[] pointers = { GcMemory.malloc(1).m, GcMemory.malloc(2).m, GcMemory.malloc(3).m };
		var array0 = indirect(pointers);
		var p = array0[0];
		Assert.equal(Pointers.byRef(null), null);
		Assert.equal(Pointers.byRef(p), pointers[0]);
		Assert.equal(Pointers.byRef(p, 1), pointers[1]);
		Assert.equal(Pointers.byRef(p, 2), pointers[2]);
	}

	@Test
	public void testByVal() {
		Assert.equal(Pointers.byVal(null, 1), null);
		try (var m = new Memory(JnaSize.POINTER.get() * 3)) {
			Assert.equal(Pointers.byVal(m, 0), JnaTesting.deref(m));
			Assert.equal(Pointers.byVal(m, 1), JnaTesting.deref(m, JnaSize.POINTER.get()));
			Assert.equal(Pointers.byVal(m, 2), JnaTesting.deref(m, JnaSize.POINTER.get() * 2));
		}
	}

	@Test
	public void testByValForPointerType() {
		Assert.equal(Pointers.byVal(null, 1, TestPointer::new), null);
		try (var m = new Memory(JnaSize.POINTER.get() * 3)) {
			JnaAssert.pointer(Pointers.byVal(m, 0, TestPointer::new), JnaTesting.deref(m));
			JnaAssert.pointer(Pointers.byVal(m, 1, TestPointer::new),
				JnaTesting.deref(m, JnaSize.POINTER.get()));
			JnaAssert.pointer(Pointers.byVal(m, 2, TestPointer::new),
				JnaTesting.deref(m, JnaSize.POINTER.get() * 2));
		}
	}

	@Test
	public void testSetPointerType() {
		try (var m = new Memory(3)) {
			Assert.equal(Pointers.set(null, m), null);
			JnaAssert.pointer(Pointers.set(new TestPointer(), m), m);
		}
	}

	@Test
	public void testSetPointerTypeFromRef() {
		try (var m = new Memory(3)) {
			var ref = new PointerByReference(m);
			Assert.equal(Pointers.set(null, ref), null);
			JnaAssert.pointer(Pointers.set(new TestPointer(), ref), m);
		}
		JnaAssert.pointer(Pointers.set(new TestPointer(), (PointerByReference) null), null);
	}

	/**
	 * Allocates a contiguous pointer array with given pointer values. Returns indirected pointers.
	 */
	private static Pointer[] indirect(Pointer... ps) {
		var m = GcMemory.malloc(ps.length * JnaSize.POINTER.get()).clear();
		var array = new Pointer[ps.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = m.share(i * JnaSize.POINTER.get(), JnaSize.POINTER.get()).m;
			array[i].setPointer(0, ps[i]);
		}
		return array;
	}
}
