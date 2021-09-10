package ceri.serial.jna;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.serial.jna.JnaTestUtil.assertTestStruct;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.serial.jna.JnaTestUtil.TestStruct;
import ceri.serial.jna.Struct.Fields;

public class StructBehavior {

	public static class NoAnno extends Struct {
		public int i;
	}

	@Fields({ "innerVal", "innerRef" })
	public static class Outer extends Struct {
		public TestStruct[] innerVal = { new TestStruct(), new TestStruct() };
		public TestStruct.ByRef[] innerRef = new TestStruct.ByRef[3];
	}

	@Test
	public void testPointerFromStructArray() {
		assertEquals(Struct.pointer((Outer[]) null), null);
		assertEquals(Struct.pointer(new Outer[0]), null);
		assertEquals(Struct.pointer(new Outer[3]), null);
		Outer[] array = { new Outer(), new Outer(), new Outer() };
		assertEquals(Struct.pointer(array), array[0].getPointer());
	}

	@Test
	public void testReadNullArray() {
		assertEquals(Struct.read((Struct[]) null), null);
	}

	@Test
	public void testWriteNullArray() {
		assertEquals(Struct.write((Struct[]) null), null);
	}

	@Test
	public void testWriteArray() {
		TestStruct[] array0 = { new TestStruct(100, null, 1), null, new TestStruct(300, null, 3) };
		TestStruct[] array = { new TestStruct(array0[0].getPointer()), null,
			new TestStruct(array0[2].getPointer()) };
		Struct.read(array);
		assertTestStruct(array[0], 0, null, 0, 0, 0);
		assertEquals(array[1], null);
		assertTestStruct(array[2], 0, null, 0, 0, 0);
		//
		Struct.write(array0, "b");
		Struct.read(array, "b");
		assertTestStruct(array[0], 0, null, 1, 0, 0);
		assertEquals(array[1], null);
		assertTestStruct(array[2], 0, null, 3, 0, 0);
		//
		Struct.write(array0);
		Struct.read(array);
		assertTestStruct(array[0], 100, null, 1, 0, 0);
		assertEquals(array[1], null);
		assertTestStruct(array[2], 300, null, 3, 0, 0);
	}

	@Test
	public void testAdapt() {
		assertNull(Struct.adapt(null, x -> null));
		assertNull(Struct.adapt(new TestStruct(), x -> null));
	}

	@Test
	public void testArrayByValForNullPointer() {
		assertArray(Struct.arrayByVal(Pointer.NULL, TestStruct::new, TestStruct[]::new, 0));
		assertThrown(() -> Struct.arrayByVal(Pointer.NULL, TestStruct::new, TestStruct[]::new, 1));
	}

	@Test
	public void testArrayByValForNullConstruction() {
		TestStruct t = new TestStruct(100, null, 1, 2, 3);
		assertThrown(() -> Struct.arrayByVal(t.getPointer(), p -> null, null, 3));
		assertThrown(() -> Struct.arrayByVal(() -> null, TestStruct[]::new, 3));
	}

	@Test
	public void testArrayByValAutoReadsAllArrayItems() {
		TestStruct[] array0 = Struct.arrayByVal(() -> new TestStruct(), TestStruct[]::new, 3);
		array0[0].i = 100;
		array0[1].i = 200;
		array0[2].i = 300;
		Struct.write(array0);
		TestStruct[] array =
			Struct.arrayByVal(Struct.pointer(array0), TestStruct::new, TestStruct[]::new, 3);
		assertEquals(array[0].i, 100);
		assertEquals(array[1].i, 200);
		assertEquals(array[2].i, 300);
	}

	@Test
	public void testIsByRef() {
		assertEquals(Struct.isByRef(null), false);
		assertEquals(Struct.isByRef(TestStruct.class), false);
		assertEquals(Struct.isByRef(TestStruct.ByRef.class), true);
		assertEquals(Struct.isByRef(TestStruct.ByVal.class), false);
	}

	@Test
	public void testIsByVal() {
		assertEquals(Struct.isByVal(null), false);
		assertEquals(Struct.isByVal(TestStruct.class), false);
		assertEquals(Struct.isByVal(TestStruct.ByRef.class), false);
		assertEquals(Struct.isByVal(TestStruct.ByVal.class), true);
	}

	@Test
	public void shouldCreateEmptyArray() {
		TestStruct t = new TestStruct(100, null, 1, 2, 3);
		assertArray(t.toArray(0));
	}

	@Test
	public void shouldProvideFieldPointer() {
		TestStruct t = Struct.write(new TestStruct(100, null, 1, 2, 3));
		assertArray(t.fieldPointer("b").getByteArray(0, 3), 1, 2, 3);
	}

	@Test
	public void shouldProvideByteArray() {
		TestStruct t = Struct.write(new TestStruct(100, null, 1, 2, 3));
		assertArray(t.byteArray("b", 3), 1, 2, 3);
	}

	@Test
	public void shouldProvidePointerArray() {
		TestStruct[] ts =
			{ new TestStruct(100, null, 1, 2, 3), new TestStruct(200, null, 4, 5), null };
		Outer outer = new Outer();
		outer.innerRef[0] = Struct.adapt(ts[0], TestStruct.ByRef::new);
		outer.innerRef[1] = Struct.adapt(ts[1], TestStruct.ByRef::new);
		outer.innerRef[2] = Struct.adapt(ts[2], TestStruct.ByRef::new);
		Struct.write(outer);
		assertArray(outer.pointerArray("innerRef", 3), ts[0].getPointer(), ts[1].getPointer(),
			null);
		assertArray(outer.pointerArray("innerRef"), ts[0].getPointer(), ts[1].getPointer());
	}

	@Test
	public void shouldProvideTypedRefArray() {
		TestStruct[] ts =
			{ new TestStruct(100, null, 1, 2, 3), new TestStruct(200, null, 4, 5), null };
		Outer outer = new Outer();
		outer.innerRef[0] = Struct.adapt(ts[0], TestStruct.ByRef::new);
		outer.innerRef[1] = Struct.adapt(ts[1], TestStruct.ByRef::new);
		outer.innerRef[2] = Struct.adapt(ts[2], TestStruct.ByRef::new);
		Struct.write(outer);
		assertArray(outer.arrayByRef("innerRef", TestStruct::new, TestStruct[]::new, 3), ts[0],
			ts[1], null);
		assertArray(outer.arrayByRef("innerRef", TestStruct::new, TestStruct[]::new), ts[0], ts[1]);
	}

	@Test
	public void shouldProvideTypedValArray() {
		Outer outer = new Outer();
		outer.innerVal[0].i = 100;
		outer.innerVal[1].i = 200;
		Struct.write(outer);
		assertArray(outer.arrayByVal("innerVal", TestStruct::new, TestStruct[]::new, 2),
			outer.innerVal);
	}

	@Test
	public void shouldAccessLastField() {
		var t = new TestStruct();
		assertEquals(t.lastField().getName(), "p");
		assertEquals(t.lastOffset(), t.fieldOffset("p"));
	}

	@Test
	public void shouldFailIfNoFieldAnnotation() {
		assertThrown(() -> new NoAnno());
	}

	@Test
	public void shouldProvideStringRepresentation() {
		String s = new Outer().toString();
		assertMatch(s, """
			(?s)Outer\\(@\\w+\\+\\w+\\) \\{
			.*%1$s\\[\\] innerVal = \\[%1$s\\(@.*
			.*%1$s.ByRef\\[\\] innerRef = \\[.*
			\\}""", TestStruct.class.getSimpleName());
	}

	@Test
	public void shouldProvideFieldsFromAnnotation() {
		assertIterable(new TestStruct().getFieldOrder(), "i", "b", "p");
		assertIterable(new TestStruct.ByRef(null).getFieldOrder(), "i", "b", "p");
		assertIterable(new TestStruct.ByVal().getFieldOrder(), "i", "b", "p");
	}

}
