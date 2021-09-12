package ceri.serial.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.serial.jna.JnaTestUtil.assertTestStruct;
import static ceri.serial.jna.JnaTestUtil.sampleArrayByRef;
import static ceri.serial.jna.JnaTestUtil.sampleArrayByVal;
import org.junit.Test;
import ceri.serial.jna.JnaTestUtil.TestStruct;

public class ArrayPointerBehavior {

	@Test
	public void shouldFailToChangePointer() {
		ArrayPointer<TestStruct> ap =
			ArrayPointer.byVal(sampleArrayByVal(), TestStruct::new, TestStruct[]::new, 1);
		assertThrown(() -> ap.setPointer(null));
		assertThrown(() -> ap.setPointer(new TestStruct().getPointer()));
	}

	@Test
	public void shouldAccessNullTerminatedArrayByRef() {
		ArrayPointer<TestStruct> ap =
			ArrayPointer.byRef(sampleArrayByRef(), TestStruct::new, TestStruct[]::new);
		assertTestStruct(Struct.read(ap.get(0)), 100, null, 1, 0, 0);
		assertTestStruct(Struct.read(ap.get(1)), 200, null, 2, 0, 0);
		assertTestStruct(Struct.read(ap.get(2)), 300, null, 3, 0, 0);
		assertNull(ap.get(3));
		assertEquals(ap.count(), 3);
		var array = Struct.read(ap.get());
		assertTestStruct(array[0], 100, null, 1, 0, 0);
		assertTestStruct(array[1], 200, null, 2, 0, 0);
		assertTestStruct(array[2], 300, null, 3, 0, 0);
	}

	@Test
	public void shouldAccessFixedLengthArrayByRef() {
		ArrayPointer<TestStruct> ap =
			ArrayPointer.byRef(sampleArrayByRef(), TestStruct::new, TestStruct[]::new, 3);
		assertTestStruct(Struct.read(ap.get(0)), 100, null, 1, 0, 0);
		assertTestStruct(Struct.read(ap.get(1)), 200, null, 2, 0, 0);
		assertTestStruct(Struct.read(ap.get(2)), 300, null, 3, 0, 0);
		assertThrown(() -> ap.get(3));
		assertEquals(ap.count(), 3);
		var array = Struct.read(ap.get());
		assertTestStruct(array[0], 100, null, 1, 0, 0);
		assertTestStruct(array[1], 200, null, 2, 0, 0);
		assertTestStruct(array[2], 300, null, 3, 0, 0);
	}

	@Test
	public void shouldAccessArrayByVal() {
		ArrayPointer<TestStruct> ap =
			ArrayPointer.byVal(sampleArrayByVal(), TestStruct::new, TestStruct[]::new, 3);
		assertTestStruct(Struct.read(ap.get(0)), 100, null, 1, 0, 0);
		assertTestStruct(Struct.read(ap.get(1)), 200, null, 2, 0, 0);
		assertTestStruct(Struct.read(ap.get(2)), 300, null, 3, 0, 0);
		assertThrown(() -> ap.get(3));
		assertEquals(ap.count(), 3);
		var array = Struct.read(ap.get());
		assertTestStruct(array[0], 100, null, 1, 0, 0);
		assertTestStruct(array[1], 200, null, 2, 0, 0);
		assertTestStruct(array[2], 300, null, 3, 0, 0);
	}

}
