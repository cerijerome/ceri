package ceri.serial.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.serial.jna.JnaTestData.assertEmpty;
import org.junit.Test;
import ceri.serial.jna.JnaTestData.TestStruct;

public class ArrayPointerBehavior {
	private final JnaTestData data = JnaTestData.of();

	@Test
	public void shouldFailToChangePointer() {
		ArrayPointer<TestStruct> ap = ArrayPointer.byVal(data.structArrayByValPointer(0),
			TestStruct::new, TestStruct[]::new, 1);
		assertThrown(() -> ap.setPointer(null));
		assertThrown(() -> ap.setPointer(new TestStruct().getPointer()));
	}

	@Test
	public void shouldAccessNullTerminatedArrayByRef() {
		ArrayPointer<TestStruct> ap =
			ArrayPointer.byRef(data.structArrayByRefPointer(1), TestStruct::new, TestStruct[]::new);
		data.assertStructRead(ap.get(0), 1);
		data.assertStructRead(ap.get(1), 2);
		assertNull(ap.get(2));
		assertEquals(ap.count(), 2);
		var array = Struct.read(ap.get());
		data.assertStruct(array[0], 1);
		data.assertStruct(array[1], 2);
	}

	@Test
	public void shouldAccessFixedLengthArrayByRef() {
		ArrayPointer<TestStruct> ap = ArrayPointer.byRef(data.structArrayByRefPointer(1),
			TestStruct::new, TestStruct[]::new, 3);
		data.assertStructRead(ap.get(0), 1);
		data.assertStructRead(ap.get(1), 2);
		assertNull(ap.get(2));
		assertThrown(() -> ap.get(3));
		assertEquals(ap.count(), 3);
		var array = Struct.read(ap.get());
		data.assertStruct(array[0], 1);
		data.assertStruct(array[1], 2);
		assertNull(array[2]);
	}

	@Test
	public void shouldAccessArrayByVal() {
		ArrayPointer<TestStruct> ap = ArrayPointer.byVal(data.structArrayByValPointer(1),
			TestStruct::new, TestStruct[]::new, 3);
		data.assertStructRead(ap.get(0), 1);
		data.assertStructRead(ap.get(1), 2);
		assertEmpty(Struct.read(ap.get(2)));
		assertThrown(() -> ap.get(3));
		assertEquals(ap.count(), 3);
		var array = Struct.read(ap.get());
		data.assertStruct(array[0], 1);
		data.assertStruct(array[1], 2);
		assertEmpty(array[2]);
	}

}
