package ceri.jna.util;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertUnsupported;
import static ceri.jna.util.JnaTestData.assertStruct;
import org.junit.Test;
import com.sun.jna.Pointer;
import com.sun.jna.Union;
import ceri.common.exception.ExceptionUtil.Rte;
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
	public void testFieldAccessor() {
		var field = UnionField.<Rte, TestUnion, TestStruct>of("struct", u -> u.struct,
			(u, t) -> u.struct = t);
		var union = new TestUnion();
		field.set(union, new TestStruct(100, null, -1, -2, -3));
		var ref = ref(union);
		assertStruct(field.get(ref), 100, null, -1, -2, -3);
	}

	@Test
	public void testFieldWithoutGetter() {
		var field =
			UnionField.<Rte, TestUnion, TestStruct>of("struct", null, (u, t) -> u.struct = t);
		var union = new TestUnion();
		field.set(union, new TestStruct(100, null, -1, -2, -3));
		assertStruct(union.struct, new TestStruct(100, null, -1, -2, -3));
		assertUnsupported(() -> field.get(union));
	}

	@Test
	public void testFieldWithoutSetter() {
		var field = UnionField.<Rte, TestUnion, TestStruct>of("struct", u -> u.struct, null);
		var union = new TestUnion();
		Struct.type(union, "struct").struct = new TestStruct(100, null, -1, -2, -3);
		union.write();
		assertUnsupported(() -> field.set(union, new TestStruct(200, null, 1, 2, 3)));
		assertStruct(field.get(union), 100, null, -1, -2, -3);
	}

	@Test
	public void testUintFieldAccessor() {
		var field = UnionField.<Rte, TestUnion>ofUint("i", u -> u.i, (u, i) -> u.i = i);
		var union = new TestUnion();
		field.set(union, -1);
		var ref = ref(union);
		assertEquals(field.getUint(ref), 0xffffffffL);
	}

	@Test
	public void testUintFieldWithoutGetter() {
		var field = UnionField.<Rte, TestUnion>ofUint("i", null, (u, i) -> u.i = i);
		var union = new TestUnion();
		field.set(union, -1);
		assertEquals(union.i, -1);
		assertUnsupported(() -> field.get(union));
	}

	@Test
	public void testUintFieldWithoutSetter() {
		var field = UnionField.<Rte, TestUnion>ofUint("i", u -> u.i, null);
		var union = new TestUnion();
		union.i = 123;
		assertUnsupported(() -> field.set(union, 333));
		assertEquals(field.get(union), 123L);
	}

	@Test
	public void testFieldLongAccessor() {
		var field =
			UnionField.<Rte, TestUnion>ofLong("b", u -> ubyte(u.b), (u, l) -> u.b = (byte) l);
		var union = new TestUnion();
		field.set(union, 0xff);
		var ref = ref(union);
		assertEquals(field.get(ref), 0xffL);
	}

	private static TestUnion ref(TestUnion union) {
		return Struct.read(new TestUnion(Struct.write(union).getPointer()));
	}
}
