package ceri.jna.type;

import org.junit.Before;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.test.Assert;
import ceri.jna.type.Struct.Fields;
import ceri.jna.util.JnaTestData;
import ceri.jna.util.JnaTestData.TestStruct;

public class StructFieldTest {
	private final JnaTestData data = JnaTestData.of();
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
		struct.val = data.structArrayByValPointer(1); // [1]
		StructField.Type<FieldStruct, TestStruct> field =
			StructField.byVal(t -> t.val, TestStruct::new);
		data.assertStructRead(field.get(struct), 1);
	}

	@Test
	public void testByRef() {
		struct.ref = data.structArrayByRefPointer(2); // [2]
		StructField.Type<FieldStruct, TestStruct> field =
			StructField.byRef(t -> t.ref, TestStruct::new);
		data.assertStructRead(field.get(struct), 2);
	}

	@Test
	public void testArrayByVal() {
		struct.n = 2;
		struct.byVal = data.structArrayByValPointer(0);
		StructField.Array<FieldStruct, TestStruct> field =
			StructField.arrayByVal(t -> t.byVal, t -> t.n, TestStruct::new, TestStruct[]::new);
		var array = field.get(struct);
		Assert.equal(array.length, 2);
		data.assertStructRead(array[0], 0);
		data.assertStructRead(array[1], 1);
		data.assertStructRead(field.get(struct, 0), 0);
		data.assertStructRead(field.get(struct, 1), 1);
		Assert.thrown(() -> field.get(struct, 2));
	}

	@Test
	public void testArrayByRef() {
		struct.n = 2;
		struct.byRef = data.structArrayByRefPointer(1);
		StructField.Array<FieldStruct, TestStruct> field =
			StructField.arrayByRef(t -> t.byRef, t -> t.n, TestStruct::new, TestStruct[]::new);
		var array = field.get(struct);
		Assert.equal(array.length, 2);
		data.assertStructRead(array[0], 1);
		data.assertStructRead(array[1], 2);
		data.assertStructRead(field.get(struct, 0), 1);
		data.assertStructRead(field.get(struct, 1), 2);
		Assert.thrown(() -> field.get(struct, 2));
	}

	@Test
	public void testNullTermArrayByRef() {
		struct.byRef = data.structArrayByRefPointer(1);
		StructField.Array<FieldStruct, TestStruct> field =
			StructField.arrayByRef(t -> t.byRef, TestStruct::new, TestStruct[]::new);
		var array = field.get(struct);
		Assert.equal(array.length, 2);
		data.assertStructRead(array[0], 1);
		data.assertStructRead(array[1], 2);
		data.assertStructRead(field.get(struct, 0), 1);
		data.assertStructRead(field.get(struct, 1), 2);
		Assert.isNull(field.get(struct, 2));
	}
}
