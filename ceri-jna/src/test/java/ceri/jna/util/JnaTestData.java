package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.collection.ArrayUtil;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.test.JnaTestUtil.MemCache;
import ceri.jna.util.Struct.Fields;

/**
 * Provides JNA test data. Prevents gc that may unexpectedly cause a test to fail.
 *
 * <pre>
 * memoryArrayByVal = { { -1, -2, -3 }, { -4, -5, -6 }, { -7, -8, -9 } }
 * structByVal = {
 * 	{ 0x1111, memoryArrayByVal[0], { -1, -2, -3 } }
 * 	{ 0x2222, memoryArrayByVal[1], { -4, -5, -6 } }
 * 	{ 0x3333, memoryArrayByVal[2], { -1, -2, -3 } }
 * }
 * </pre>
 */
public class JnaTestData {
	private static final int ARRAY_SIZE = 3;
	private static final int I_MULTIPLIER = 0x1111;
	private final MemCache mc = JnaTestUtil.memCache(); // prevent gc on memory
	public final Memory[] memoryArrayByVal;
	public final TestStruct[] structArrayByVal;
	public final Pointer[] pointerArrayByVal;

	@Fields({ "i", "b", "p" })
	public static class TestStruct extends Struct {
		public static final int SIZE = new TestStruct().size();
		public static final int BYTES = 3;
		public int i;
		public byte[] b = new byte[BYTES];
		public Pointer p;

		public static class ByRef extends TestStruct implements Structure.ByReference {
			public ByRef(Pointer p) {
				super(p);
			}
		}

		public static class ByVal extends TestStruct implements Structure.ByValue {}

		public TestStruct() {}

		public TestStruct(Pointer p) {
			super(p);
		}

		public TestStruct(int i, Pointer p, int... b) {
			populate(i, p, b);
		}

		public TestStruct populate(int i, Pointer p, int... bytes) {
			return populate(i, p, ArrayUtil.bytes(bytes));
		}

		public TestStruct populate(int i, Pointer p, byte[] bytes) {
			this.i = i;
			this.p = p;
			ArrayUtil.copy(bytes, 0, this.b, 0, bytes.length);
			return this;
		}

		@Override
		public int fieldOffset(String name) {
			return super.fieldOffset(name);
		}
	}

	/**
	 * Assert struct fields.
	 */
	public static void assertStruct(TestStruct t, TestStruct expected) {
		assertStruct(t, expected.i, expected.p, expected.b);
	}

	/**
	 * Assert struct fields.
	 */
	public static void assertStruct(TestStruct t, int i, Pointer p, int... bytes) {
		assertStruct(t, i, p, ArrayUtil.bytes(bytes));
	}

	/**
	 * Assert struct fields.
	 */
	public static void assertStruct(TestStruct t, int i, Pointer p, byte[] bytes) {
		assertNotNull(t);
		assertEquals(t.i, i);
		assertEquals(t.p, p);
		assertArray(t.b, bytes);
	}

	public static void assertEmpty(TestStruct t) {
		assertStruct(t, new TestStruct());
	}

	public static JnaTestData of() {
		return new JnaTestData();
	}

	private JnaTestData() {
		memoryArrayByVal = memoryArrayByVal();
		structArrayByVal = structArrayByVal(memoryArrayByVal);
		pointerArrayByVal = pointerArrayByVal(structArrayByVal);
	}

	public Pointer structArrayByValPointer(int i) {
		return JnaTestUtil.deref(structArrayByVal[0].getPointer().share(i * TestStruct.SIZE));
	}

	public Pointer structArrayByRefPointer(int i) {
		return JnaTestUtil.deref(pointerArrayByVal[0].share(i * Native.POINTER_SIZE));
	}

	public Pointer pointerArrayByValPointer(int i) {
		return structArrayByRefPointer(i);
	}

	public void assertStructRead(TestStruct t, int i) {
		assertStruct(Struct.read(t), i);
	}

	public void assertStruct(TestStruct t, int i) {
		assertStruct(t, structArrayByVal[i]);
	}

	/**
	 * Create contiguous memory array.
	 */
	@SuppressWarnings("resource")
	private Memory[] memoryArrayByVal() {
		Memory m = mc.calloc(ARRAY_SIZE * TestStruct.BYTES);
		for (int i = 0; i < m.size(); i++)
			m.setByte(i, (byte) -(i + 1));
		Memory[] array = new Memory[ARRAY_SIZE];
		for (int i = 0; i < array.length; i++)
			array[i] = JnaUtil.share(m, i * TestStruct.BYTES, TestStruct.BYTES);
		return array;
	}

	/**
	 * Create contiguous struct array.
	 */
	@SuppressWarnings("resource")
	private TestStruct[] structArrayByVal(Memory[] ps) {
		TestStruct[] array = new TestStruct[ARRAY_SIZE];
		Memory m = mc.calloc((array.length + 1) * TestStruct.SIZE);
		for (int i = 0; i < array.length; i++) {
			Pointer p = m.share(i * TestStruct.SIZE);
			array[i] = new TestStruct(p);
			array[i].populate((i + 1) * I_MULTIPLIER, ps[i],
				JnaUtil.bytes(ps[i], 0, TestStruct.BYTES));
			array[i].write();
		}
		return array;
	}

	/**
	 * Create null-terminated contiguous pointer array referencing structs.
	 */
	@SuppressWarnings("resource")
	private Pointer[] pointerArrayByVal(TestStruct[] ts) {
		Pointer[] array = new Pointer[ts.length];
		Memory m = mc.calloc((array.length + 1) * Native.POINTER_SIZE); // null-terminated
		for (int i = 0; i < array.length; i++) {
			array[i] = m.share(i * Native.POINTER_SIZE);
			array[i].setPointer(0, ts[i].getPointer());
		}
		return array;
	}

}
