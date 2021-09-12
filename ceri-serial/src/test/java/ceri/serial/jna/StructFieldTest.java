package ceri.serial.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.serial.jna.JnaTestUtil.assertTestStruct;
import static ceri.serial.jna.JnaTestUtil.sampleArrayByRef;
import static ceri.serial.jna.JnaTestUtil.sampleArrayByVal;
import org.junit.Before;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.serial.jna.JnaTestUtil.TestStruct;
import ceri.serial.jna.Struct.Fields;

public class StructFieldTest {
	private FieldStruct struct;

	@Fields({ "val", "ref", "n", "byVal", "byRef", "byNullTermRef" })
	public static class FieldStruct extends Struct {
		public Pointer val; // TestStruct*
		public Pointer ref; // TestStruct**
		public int n;
		public Pointer byVal; // TestStruct* n-length array
		public Pointer byRef; // TestStruct** n-length array or null-term
	}

	@Before
	public void before() {
		struct = new FieldStruct();
	}

	@Test
	public void testByVal() {
		struct.val = sampleArrayByVal().share(TestStruct.SIZE); // [1]
		StructField.Type<FieldStruct, TestStruct> field =
			StructField.byVal(t -> t.val, TestStruct::new);
		assertTestStruct(Struct.read(field.get(struct)), 200, null, 2, 0, 0);
	}

	@Test
	public void testByRef() {
		struct.ref = sampleArrayByRef().share(Pointer.SIZE * 2); // [2]
		StructField.Type<FieldStruct, TestStruct> field =
			StructField.byRef(t -> t.ref, TestStruct::new);
		assertTestStruct(Struct.read(field.get(struct)), 300, null, 3, 0, 0);
	}

	@Test
	public void testArrayByVal() {
		struct.n = 2;
		struct.byVal = sampleArrayByVal();
		StructField.Array<FieldStruct, TestStruct> field =
			StructField.arrayByVal(t -> t.byVal, t -> t.n, TestStruct::new, TestStruct[]::new);
		var array = Struct.read(field.get(struct));
		assertEquals(array.length, 2);
		assertTestStruct(array[0], 100, null, 1, 0, 0);
		assertTestStruct(array[1], 200, null, 2, 0, 0);
		assertTestStruct(Struct.read(field.get(struct, 0)), 100, null, 1, 0, 0);
		assertTestStruct(Struct.read(field.get(struct, 1)), 200, null, 2, 0, 0);
		assertThrown(() -> field.get(struct, 2));
	}

	@Test
	public void testArrayByRef() {
		struct.n = 2;
		struct.byRef = sampleArrayByRef();
		StructField.Array<FieldStruct, TestStruct> field =
			StructField.arrayByRef(t -> t.byRef, t -> t.n, TestStruct::new, TestStruct[]::new);
		var array = Struct.read(field.get(struct));
		assertEquals(array.length, 2);
		assertTestStruct(array[0], 100, null, 1, 0, 0);
		assertTestStruct(array[1], 200, null, 2, 0, 0);
		assertTestStruct(Struct.read(field.get(struct, 0)), 100, null, 1, 0, 0);
		assertTestStruct(Struct.read(field.get(struct, 1)), 200, null, 2, 0, 0);
		assertThrown(() -> field.get(struct, 2));
	}

	@Test
	public void testNullTermArrayByRef() {
		struct.byRef = sampleArrayByRef();
		StructField.Array<FieldStruct, TestStruct> field =
			StructField.arrayByRef(t -> t.byRef, TestStruct::new, TestStruct[]::new);
		var array = Struct.read(field.get(struct));
		assertEquals(array.length, 3);
		assertTestStruct(array[0], 100, null, 1, 0, 0);
		assertTestStruct(array[1], 200, null, 2, 0, 0);
		assertTestStruct(array[2], 300, null, 3, 0, 0);
		assertTestStruct(Struct.read(field.get(struct, 0)), 100, null, 1, 0, 0);
		assertTestStruct(Struct.read(field.get(struct, 1)), 200, null, 2, 0, 0);
		assertTestStruct(Struct.read(field.get(struct, 2)), 300, null, 3, 0, 0);
		assertNull(field.get(struct, 3));
	}

}
