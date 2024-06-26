package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.jna.util.JnaTestData.assertStruct;
import org.junit.Test;
import com.sun.jna.Pointer;
import com.sun.jna.Union;
import ceri.common.data.ValueField;
import ceri.common.function.Accessor;
import ceri.jna.util.JnaTestData.TestStruct;

public class UnionFieldTest {

	public static class TestUnion extends Union {
		public byte b;
		public short s;
		public int i;
		public TestStruct struct;

		public TestUnion() {}

		public TestUnion(Pointer p) {
			super(p);
		}
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(UnionField.class);
	}

	@Test
	public void testTypeAccessorByName() {
		Accessor.Typed<TestUnion, TestStruct> byName =
			UnionField.of("struct", u -> u.struct, (u, t) -> u.struct = t);
		var union = new TestUnion();
		byName.set(union, new TestStruct(100, null, -1, -2, -3));
		var ref = ref(union);
		assertStruct(byName.get(ref), 100, null, -1, -2, -3);
	}

	@Test
	public void testTypeAccessorByClass() {
		Accessor.Typed<TestUnion, TestStruct> byClass =
			UnionField.of(TestStruct.class, u -> u.struct, (u, t) -> u.struct = t);
		var union = new TestUnion();
		byClass.set(union, new TestStruct(100, null, -1, -2, -3));
		var ref = ref(union);
		assertStruct(byClass.get(ref), 100, null, -1, -2, -3);
	}

	@Test
	public void testIntAccessor() {
		var accessor =
			UnionField.<TestUnion>of("i", ValueField.Typed.ofInt(u -> u.i, (u, i) -> u.i = i));
		var union = new TestUnion();
		accessor.set(union, 0x80000000);
		var ref = ref(union);
		assertEquals(accessor.getInt(ref), 0x80000000);
	}

	@Test
	public void testIntAccessorByName() {
		var accessor =
			UnionField.<TestUnion>of("i", ValueField.Typed.ofInt(u -> u.i, (u, i) -> u.i = i));
		var union = new TestUnion();
		accessor.set(union, 0x80000000);
		var ref = ref(union);
		assertEquals(accessor.getInt(ref), 0x80000000);
	}

	@Test
	public void testIntAccessorByClass() {
		var accessor = UnionField.<TestUnion>of(short.class,
			ValueField.Typed.ofInt(u -> u.s, (u, l) -> u.s = (short) l));
		var union = new TestUnion();
		accessor.set(union, 0x12345678);
		var ref = ref(union);
		assertEquals(accessor.getInt(ref), 0x5678);
	}

	private static TestUnion ref(TestUnion union) {
		return Struct.read(new TestUnion(Struct.write(union).getPointer()));
	}
}
