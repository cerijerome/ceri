package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.jna.test.JnaTestUtil.assertPointer;
import static ceri.jna.util.JnaTestData.assertEmpty;
import static ceri.jna.util.JnaTestData.assertStruct;
import java.util.function.Function;
import org.junit.Test;
import com.sun.jna.Pointer;
import com.sun.jna.Union;
import ceri.common.collection.ArrayUtil;
import ceri.jna.util.JnaTestData.TestStruct;
import ceri.jna.util.Struct.Fields;

public class StructBehavior {
	private final JnaTestData data = JnaTestData.of();

	public static class NoAnno extends Struct {
		public int i;
	}

	@Fields({ "innerVal", "innerRef" })
	public static class Outer extends Struct {
		public TestStruct[] innerVal = { new TestStruct(), new TestStruct() };
		public TestStruct.ByRef[] innerRef = new TestStruct.ByRef[3];
	}

	@Fields({ "innerVal", "innerRef" })
	public static class TestUnion extends Union {
		public int i;
		public Pointer p;
		public byte[] b = new byte[4];

		public TestUnion(int i, Pointer p, int... bytes) {
			this.i = i;
			this.p = p;
			ArrayUtil.copy(ArrayUtil.bytes(bytes), 0, this.b, 0, bytes.length);
		}

		public TestUnion(Pointer p) {
			super(p);
		}
	}

	@Test
	public void testSetUnionTypeByName() {
		@SuppressWarnings("resource")
		var t0 = new TestUnion(123, JnaUtil.mallocBytes(5, 6, 7), -1, -2, -3, -4);
		var p = Struct.write(Struct.type(t0, "i")).getPointer();
		var t = Struct.read(new TestUnion(p));
		assertEquals(t.i, 123);
	}

	@Test
	public void testSetUnionTypeByClass() {
		@SuppressWarnings("resource")
		var t0 = new TestUnion(123, JnaUtil.mallocBytes(5, 6, 7), -1, -2, -3, -4);
		var p = Struct.write(Struct.type(t0, Pointer.class)).getPointer();
		var t = Struct.read(new TestUnion(p));
		assertPointer(t.p, 0, 5, 6, 7);
	}

	@Test
	public void testSetUnionTypedValue() {
		var t0 = new TestUnion(0, null);
		@SuppressWarnings("resource")
		var p = Struct.write(Struct.typedValue(t0, JnaUtil.mallocBytes(5, 6, 7))).getPointer();
		var t = Struct.read(new TestUnion(p));
		assertPointer(t.p, 0, 5, 6, 7);
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
	public void testSize() {
		assertEquals(Struct.size((TestStruct) null), 0);
		assertThrown(() -> Struct.size((Function<Pointer, TestStruct>) null));
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
		assertStruct(array[0], 0, null, 0, 0, 0);
		assertEquals(array[1], null);
		assertStruct(array[2], 0, null, 0, 0, 0);
		//
		Struct.write(array0, "b");
		Struct.read(array, "b");
		assertStruct(array[0], 0, null, 1, 0, 0);
		assertEquals(array[1], null);
		assertStruct(array[2], 0, null, 3, 0, 0);
		//
		Struct.write(array0);
		Struct.read(array);
		assertStruct(array[0], 100, null, 1, 0, 0);
		assertEquals(array[1], null);
		assertStruct(array[2], 300, null, 3, 0, 0);
	}

	@Test
	public void testWriteAuto() {
		assertNull(Struct.writeAuto((TestStruct) null));
		assertNull(Struct.writeAuto((TestStruct[]) null));
		TestStruct[] array0 = { new TestStruct(100, null, 1), new TestStruct(200, null, 2) };
		array0[1].setAutoWrite(false);
		Struct.writeAuto(array0);
		var array = Struct.readAuto(new TestStruct[] { new TestStruct(array0[0].getPointer()),
			new TestStruct(array0[1].getPointer()) });
		assertStruct(array[0], 100, null, 1, 0, 0);
		assertStruct(array[1], 0, null, 0, 0, 0);
	}

	@Test
	public void testReadAuto() {
		assertNull(Struct.readAuto((TestStruct) null));
		assertNull(Struct.readAuto((TestStruct[]) null));
		TestStruct t0 = Struct.writeAuto(new TestStruct(100, null, 1));
		TestStruct[] array = { new TestStruct(t0.getPointer()), new TestStruct(t0.getPointer()) };
		array[1].setAutoRead(false);
		Struct.readAuto(array);
		assertStruct(array[0], 100, null, 1, 0, 0);
		assertStruct(array[1], 0, null, 0, 0, 0);
	}

	@Test
	public void testAdapt() {
		assertNull(Struct.adapt(null, x -> null));
		assertNull(Struct.adapt(new TestStruct(), x -> null));
	}

	@Test
	public void testCopy() {
		try (var p = JnaUtil.calloc(TestStruct.SIZE)) {
			assertStruct(Struct.copy(null, p, TestStruct::new), 0, null, 0, 0, 0);
			var t = Struct.copy(Struct.write(new TestStruct(100, p, 1)), p, TestStruct::new);
			assertStruct(t, 100, p, 1, 0, 0);
		}
	}

	@Test
	public void testMallocArray() {
		assertArray(Struct.mallocArray(TestStruct::new, TestStruct[]::new, 0));
		TestStruct[] array = Struct.mallocArray(TestStruct::new, TestStruct[]::new, 3);
		assertEquals(array[1].getPointer(), array[0].getPointer().share(TestStruct.SIZE));
		assertEquals(array[2].getPointer(), array[1].getPointer().share(TestStruct.SIZE));
		assertEquals(array.length, 3);
	}

	@Test
	public void testCallocArray() {
		assertArray(Struct.callocArray(TestStruct::new, TestStruct[]::new, 0));
		TestStruct[] array = Struct.callocArray(TestStruct::new, TestStruct[]::new, 3);
		assertEquals(array[1].getPointer(), array[0].getPointer().share(TestStruct.SIZE));
		assertEquals(array[2].getPointer(), array[1].getPointer().share(TestStruct.SIZE));
		assertEquals(array.length, 3);
		Struct.read(array);
		assertEmpty(array[0]);
		assertEmpty(array[1]);
		assertEmpty(array[2]);
	}

	@Test
	public void testArrayByValForNullPointer() {
		assertArray(Struct.arrayByVal(Pointer.NULL, TestStruct::new, TestStruct[]::new, 0));
		assertArray(Struct.arrayByVal(Pointer.NULL, TestStruct::new, TestStruct[]::new, 1),
			new TestStruct[] { null });
	}

	@Test
	public void testArrayByValForNullConstruction() {
		var t = new TestStruct(100, null, 1, 2, 3);
		assertThrown(() -> Struct.arrayByVal(t.getPointer(), p -> null, null, 3));
		assertArray(Struct.<TestStruct>arrayByVal(() -> null, TestStruct[]::new, 3), null, null,
			null);
	}

	@Test
	public void testArrayByValAutoReadsAllArrayItems() {
		Pointer p = data.structArrayByValPointer(0);
		TestStruct[] array = Struct.arrayByVal(p, TestStruct::new, TestStruct[]::new, 3);
		data.assertStruct(array[0], 0);
		data.assertStruct(array[1], 1);
		data.assertStruct(array[2], 2);
	}

	@Test
	public void testByVal() {
		Pointer p = data.structArrayByValPointer(1);
		data.assertStruct(Struct.byVal(p, 0, TestStruct::new), 1);
		data.assertStruct(Struct.byVal(p, 1, TestStruct::new), 2);
		assertEmpty(Struct.byVal(p, 2, TestStruct::new)); // from zeroed mem
	}

	@Test
	public void testIsByRef() {
		assertEquals(Struct.isByRef(null), false);
		assertEquals(Struct.isByRef(TestStruct.class), false);
		assertEquals(Struct.isByRef(TestStruct.ByRef.class), true);
		assertEquals(Struct.isByRef(TestStruct.ByVal.class), false);
	}

	@Test
	public void testClassIsByVal() {
		assertEquals(Struct.isByVal((Class<?>) null), false);
		assertEquals(Struct.isByVal(TestStruct.class), false);
		assertEquals(Struct.isByVal(TestStruct.ByRef.class), false);
		assertEquals(Struct.isByVal(TestStruct.ByVal.class), true);
	}

	@Test
	public void testArrayIsByVal() {
		try (var m = JnaUtil.calloc(TestStruct.SIZE * 3)) {
			var t0 = new TestStruct(m);
			var t1 = new TestStruct(m.share(TestStruct.SIZE));
			var t2 = new TestStruct(m.share(TestStruct.SIZE * 2));
			assertEquals(Struct.isByVal((TestStruct[]) null), false);
			assertEquals(Struct.isByVal(new TestStruct[] { t0 }), true);
			assertEquals(Struct.isByVal(new TestStruct[] { t0, t1, t2 }), true);
			assertEquals(Struct.isByVal(new TestStruct[] { t0, t2 }), false);
		}
	}

	@Test
	public void shouldCreateEmptyArray() {
		var t = new TestStruct(100, null, 1, 2, 3);
		assertArray(t.toArray(0));
	}

	@Test
	public void shouldProvideFieldPointer() {
		var t = Struct.write(new TestStruct(100, null, 1, 2, 3));
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
