package ceri.jna.type;

import static ceri.jna.util.JnaTestData.assertEmpty;
import static ceri.jna.util.JnaTestData.assertStruct;
import java.util.List;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.jna.type.Struct.Align;
import ceri.jna.type.Struct.Fields;
import ceri.jna.type.Struct.Packed;
import ceri.jna.util.JnaTestData;
import ceri.jna.util.JnaTestData.TestStruct;
import ceri.jna.util.JnaUtil;

public class StructBehavior {
	private final JnaTestData data = JnaTestData.of();

	public static class NoAnno extends Struct {
		public int i;
	}

	@Fields({ "innerVal", "innerRef", "nlong" })
	public static class Outer extends Struct {
		public TestStruct[] innerVal = { new TestStruct(), new TestStruct() };
		public TestStruct.ByRef[] innerRef = new TestStruct.ByRef[3];
		public CUlong nlong = new CUlong(0x100000000L);
	}

	@Packed
	@Fields({ "b", "l" })
	public static class Aligned extends Struct {
		public byte b;
		public long l;

		public Aligned() {}

		public Aligned(Align align) {
			super(null, align);
		}
	}

	@Test
	public void testPackedStruct() {
		Assert.equal(new Aligned().size(), 9);
		Assert.equal(new Aligned(Align.none).size(), 9);
		Assert.equal(new Aligned(Align.gnuc).size(), 16);
	}

	@Test
	public void testPointerFromStructArray() {
		Assert.equal(Struct.pointer((Outer[]) null), null);
		Assert.equal(Struct.pointer(new Outer[0]), null);
		Assert.equal(Struct.pointer(new Outer[3]), null);
		Outer[] array = { new Outer(), new Outer(), new Outer() };
		Assert.equal(Struct.pointer(array), array[0].getPointer());
	}

	@Test
	public void testPeerFromStructArray() {
		Assert.equal(Struct.peer((Outer[]) null), 0L);
		Assert.equal(Struct.peer(new Outer[0]), 0L);
		Assert.equal(Struct.peer(new Outer[3]), 0L);
		Outer[] array = { new Outer(), new Outer(), new Outer() };
		Assert.equal(Struct.peer(array), Pointer.nativeValue(array[0].getPointer()));
	}

	@Test
	public void testPeerFromStruct() {
		Assert.equal(Struct.peer((Outer) null), 0L);
		Outer[] array = { new Outer(), new Outer(), new Outer() };
		Assert.equal(Struct.peer(array), Pointer.nativeValue(array[0].getPointer()));
	}

	@Test
	public void testSize() {
		Assert.equal(Struct.size((TestStruct) null), 0);
		Assert.thrown(() -> Struct.size((Functions.Function<Pointer, TestStruct>) null));
	}

	@Test
	public void testReadNullArray() {
		Assert.equal(Struct.read((Struct[]) null), null);
	}

	@Test
	public void testWriteNullArray() {
		Assert.equal(Struct.write((Struct[]) null), null);
	}

	@Test
	public void testWriteArray() {
		TestStruct[] array0 = { new TestStruct(100, null, 1), null, new TestStruct(300, null, 3) };
		TestStruct[] array = { new TestStruct(array0[0].getPointer()), null,
			new TestStruct(array0[2].getPointer()) };
		Struct.read(array);
		assertStruct(array[0], 0, null, 0, 0, 0);
		Assert.equal(array[1], null);
		assertStruct(array[2], 0, null, 0, 0, 0);
		//
		Struct.write(array0, "b");
		Struct.read(array, "b");
		assertStruct(array[0], 0, null, 1, 0, 0);
		Assert.equal(array[1], null);
		assertStruct(array[2], 0, null, 3, 0, 0);
		//
		Struct.write(array0);
		Struct.read(array);
		assertStruct(array[0], 100, null, 1, 0, 0);
		Assert.equal(array[1], null);
		assertStruct(array[2], 300, null, 3, 0, 0);
	}

	@Test
	public void testWriteAuto() {
		Assert.isNull(Struct.writeAuto((TestStruct) null));
		Assert.isNull(Struct.writeAuto((TestStruct[]) null));
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
		Assert.isNull(Struct.readAuto((TestStruct) null));
		Assert.isNull(Struct.readAuto((TestStruct[]) null));
		TestStruct t0 = Struct.writeAuto(new TestStruct(100, null, 1));
		TestStruct[] array = { new TestStruct(t0.getPointer()), new TestStruct(t0.getPointer()) };
		array[1].setAutoRead(false);
		Struct.readAuto(array);
		assertStruct(array[0], 100, null, 1, 0, 0);
		assertStruct(array[1], 0, null, 0, 0, 0);
	}

	@Test
	public void testAdapt() {
		Assert.isNull(Struct.adapt(null, _ -> null));
		Assert.isNull(Struct.adapt(new TestStruct(), _ -> null));
	}

	@Test
	public void testCopyNew() {
		try (var p = JnaUtil.calloc(TestStruct.SIZE)) {
			assertStruct(Struct.copy(null, p, TestStruct::new), 0, null, 0, 0, 0);
			var t = Struct.copy(Struct.write(new TestStruct(100, p, 1)), p, TestStruct::new);
			assertStruct(t, 100, p, 1, 0, 0);
		}
	}

	@Test
	public void testCopy() {
		try (var p = new Memory(3)) {
			var from = new TestStruct(111, p, 4, 5, 6);
			var to = new TestStruct(222, p.share(1), 7, 8, 9);
			Struct.copy(from, to);
			Assert.equal(to.i, 111);
			Assert.equal(to.p, p);
			Assert.array(to.b, 4, 5, 6);
		}
	}

	@Test
	public void testCopyTo() {
		try (var p = new Memory(3)) {
			var from = new TestStruct(111, p, 4, 5, 6);
			var to = new TestStruct(222, p.share(1), 7, 8, 9);
			Struct.copyTo(from, to.getPointer());
			Struct.read(to);
			Assert.equal(to.i, 111);
			Assert.equal(to.p, p);
			Assert.array(to.b, 4, 5, 6);
			Assert.equal(Struct.copyTo(null, null), null);
			Assert.equal(Struct.copyTo(from, null), from);
		}
	}

	@Test
	public void testCopyFrom() {
		try (var p = new Memory(3)) {
			var from = new TestStruct(111, p, 4, 5, 6);
			var to = new TestStruct(222, p.share(1), 7, 8, 9);
			Struct.write(from);
			Struct.copyFrom(from.getPointer(), to);
			Assert.equal(to.i, 111);
			Assert.equal(to.p, p);
			Assert.array(to.b, 4, 5, 6);
			Assert.equal(Struct.copyFrom(null, null), null);
			Assert.equal(Struct.copyFrom(null, to), to);
		}
	}

	@Test
	public void testCopyNullStruct() {
		try (var p = new Memory(3)) {
			var t = Struct.write(new TestStruct(111, p, 4, 5, 6));
			Assert.equal(Struct.copy(t, t), t);
			Assert.equal(Struct.copy(t, null), null);
			Assert.equal(Struct.copy(null, t), t);
			Assert.equal(Struct.copy(null, null), null);
		}
	}

	@Test
	public void testMallocArray() {
		Assert.array(Struct.mallocArray(TestStruct::new, TestStruct[]::new, 0));
		TestStruct[] array = Struct.mallocArray(TestStruct::new, TestStruct[]::new, 3);
		Assert.equal(array[1].getPointer(), array[0].getPointer().share(TestStruct.SIZE));
		Assert.equal(array[2].getPointer(), array[1].getPointer().share(TestStruct.SIZE));
		Assert.equal(array.length, 3);
	}

	@Test
	public void testCallocArray() {
		Assert.array(Struct.callocArray(TestStruct::new, TestStruct[]::new, 0));
		TestStruct[] array = Struct.callocArray(TestStruct::new, TestStruct[]::new, 3);
		Assert.equal(array[1].getPointer(), array[0].getPointer().share(TestStruct.SIZE));
		Assert.equal(array[2].getPointer(), array[1].getPointer().share(TestStruct.SIZE));
		Assert.equal(array.length, 3);
		Struct.read(array);
		assertEmpty(array[0]);
		assertEmpty(array[1]);
		assertEmpty(array[2]);
	}

	@Test
	public void testArrayByValForNullPointer() {
		Assert.array(Struct.arrayByVal(Pointer.NULL, TestStruct::new, TestStruct[]::new, 0));
		Assert.array(Struct.arrayByVal(Pointer.NULL, TestStruct::new, TestStruct[]::new, 1),
			new TestStruct[] { null });
	}

	@Test
	public void testArrayByValForNullConstruction() {
		var t = new TestStruct(100, null, 1, 2, 3);
		Assert.thrown(() -> Struct.arrayByVal(t.getPointer(), _ -> null, null, 3));
		Assert.array(Struct.arrayByVal(() -> null, TestStruct[]::new, 3), null, null, null);
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
		Assert.equal(Struct.isByRef(null), false);
		Assert.equal(Struct.isByRef(TestStruct.class), false);
		Assert.equal(Struct.isByRef(TestStruct.ByRef.class), true);
		Assert.equal(Struct.isByRef(TestStruct.ByVal.class), false);
	}

	@Test
	public void testClassIsByVal() {
		Assert.equal(Struct.isByVal((Class<?>) null), false);
		Assert.equal(Struct.isByVal(TestStruct.class), false);
		Assert.equal(Struct.isByVal(TestStruct.ByRef.class), false);
		Assert.equal(Struct.isByVal(TestStruct.ByVal.class), true);
	}

	@Test
	public void testArrayIsByVal() {
		try (var m = JnaUtil.calloc(TestStruct.SIZE * 3)) {
			var t0 = new TestStruct(m);
			var t1 = new TestStruct(m.share(TestStruct.SIZE));
			var t2 = new TestStruct(m.share(TestStruct.SIZE * 2));
			Assert.equal(Struct.isByVal((TestStruct[]) null), false);
			Assert.equal(Struct.isByVal(new TestStruct[] { null }), false);
			Assert.equal(Struct.isByVal(new TestStruct[] { t0 }), true);
			Assert.equal(Struct.isByVal(new TestStruct[] { t0, null }), false);
			Assert.equal(Struct.isByVal(new TestStruct[] { t0, t1, t2 }), true);
			Assert.equal(Struct.isByVal(new TestStruct[] { t0, t2 }), false);
		}
	}

	@Test
	public void shouldCreateEmptyArray() {
		var t = new TestStruct(100, null, 1, 2, 3);
		Assert.array(t.toArray(0));
	}

	@Test
	public void shouldProvideFieldPointer() {
		var t = Struct.write(new TestStruct(100, null, 1, 2, 3));
		Assert.array(t.fieldPointer("b").getByteArray(0, 3), 1, 2, 3);
	}

	@Test
	public void shouldProvideByteArray() {
		TestStruct t = Struct.write(new TestStruct(100, null, 1, 2, 3));
		Assert.array(t.byteArray("b", 3), 1, 2, 3);
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
		Assert.array(outer.pointerArray("innerRef", 3), ts[0].getPointer(), ts[1].getPointer(),
			null);
		Assert.array(outer.pointerArray("innerRef"), ts[0].getPointer(), ts[1].getPointer());
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
		Assert.array(outer.arrayByRef("innerRef", TestStruct::new, TestStruct[]::new, 3), ts[0],
			ts[1], null);
		Assert.array(outer.arrayByRef("innerRef", TestStruct::new, TestStruct[]::new), ts[0], ts[1]);
	}

	@Test
	public void shouldProvideTypedValArray() {
		Outer outer = new Outer();
		outer.innerVal[0].i = 100;
		outer.innerVal[1].i = 200;
		Struct.write(outer);
		Assert.array(outer.arrayByVal("innerVal", TestStruct::new, TestStruct[]::new, 2),
			outer.innerVal);
	}

	@Test
	public void shouldAccessLastField() {
		var t = new TestStruct();
		Assert.equal(t.lastField().getName(), "p");
		Assert.equal(t.lastOffset(), t.fieldOffset("p"));
	}

	@Test
	public void shouldFailIfNoFieldAnnotation() {
		Assert.thrown(() -> new NoAnno());
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.equal(Struct.toString(null, List.of(), _ -> 0), "null");
		String s = new Outer().toString();
		Assert.match(s, """
			(?s)Outer\\(@\\w+\\+\\w+\\) \\{
			.*%1$s\\[\\] innerVal = \\[%1$s\\(@.*
			.*%1$s.ByRef\\[\\] innerRef = \\[.*
			\\}""", TestStruct.class.getSimpleName());
	}

	@Test
	public void shouldProvideFieldsFromAnnotation() {
		Assert.ordered(new TestStruct().getFieldOrder(), "i", "b", "p");
		Assert.ordered(new TestStruct.ByRef(null).getFieldOrder(), "i", "b", "p");
		Assert.ordered(new TestStruct.ByVal().getFieldOrder(), "i", "b", "p");
	}
}
