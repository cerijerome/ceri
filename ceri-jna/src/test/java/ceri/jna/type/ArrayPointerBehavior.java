package ceri.jna.type;

import static ceri.common.test.Assert.assertEquals;
import static ceri.jna.util.JnaTestData.assertEmpty;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.jna.util.JnaTestData;
import ceri.jna.util.JnaTestData.TestStruct;

public class ArrayPointerBehavior {
	private final JnaTestData data = JnaTestData.of();

	@Test
	public void shouldFailToChangePointer() {
		ArrayPointer<TestStruct> ap = ArrayPointer.byVal(data.structArrayByValPointer(0),
			TestStruct::new, TestStruct[]::new, 1);
		Assert.thrown(() -> ap.setPointer(null));
		Assert.thrown(() -> ap.setPointer(new TestStruct().getPointer()));
	}

	@Test
	public void shouldAccessNullTerminatedArrayByRef() {
		ArrayPointer<TestStruct> ap =
			ArrayPointer.byRef(data.structArrayByRefPointer(1), TestStruct::new, TestStruct[]::new);
		data.assertStructRead(ap.get(0), 1);
		data.assertStructRead(ap.get(1), 2);
		Assert.isNull(ap.get(2));
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
		Assert.isNull(ap.get(2));
		Assert.thrown(() -> ap.get(3));
		assertEquals(ap.count(), 3);
		var array = Struct.read(ap.get());
		data.assertStruct(array[0], 1);
		data.assertStruct(array[1], 2);
		Assert.isNull(array[2]);
	}

	@Test
	public void shouldAccessArrayByVal() {
		ArrayPointer<TestStruct> ap = ArrayPointer.byVal(data.structArrayByValPointer(1),
			TestStruct::new, TestStruct[]::new, 3);
		data.assertStructRead(ap.get(0), 1);
		data.assertStructRead(ap.get(1), 2);
		assertEmpty(Struct.read(ap.get(2)));
		Assert.thrown(() -> ap.get(3));
		assertEquals(ap.count(), 3);
		var array = Struct.read(ap.get());
		data.assertStruct(array[0], 1);
		data.assertStruct(array[1], 2);
		assertEmpty(array[2]);
	}

}
