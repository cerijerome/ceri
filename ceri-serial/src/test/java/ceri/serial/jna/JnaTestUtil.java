package ceri.serial.jna;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import java.util.function.Function;
import java.util.stream.Stream;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import ceri.common.collection.ArrayUtil;
import ceri.serial.jna.Struct.Fields;

public class JnaTestUtil {

	private JnaTestUtil() {}

	public static class TestPointer extends PointerType {}

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

		public TestStruct(int i) {
			this(i, null);
		}

		public TestStruct(int i, Pointer p, int... b) {
			populate(this, i, p, b);
		}

		public TestStruct(Pointer p) {
			super(p);
		}

		@Override
		public int fieldOffset(String name) {
			return super.fieldOffset(name);
		}
	}

	/**
	 * Populate struct fields.
	 */
	public static TestStruct populate(TestStruct t, int i, Pointer p, int... bytes) {
		return populate(t, i, p, ArrayUtil.bytes(bytes));
	}

	/**
	 * Populate struct fields.
	 */
	public static TestStruct populate(TestStruct t, int i, Pointer p, byte[] bytes) {
		t.i = i;
		t.p = p;
		ArrayUtil.copy(bytes, 0, t.b, 0, bytes.length);
		return t;
	}

	/**
	 * Assert struct fields.
	 */
	public static void assertTestStruct(TestStruct t, TestStruct expected) {
		assertTestStruct(t, expected.i, expected.p, expected.b);
	}

	/**
	 * Assert struct fields.
	 */
	public static void assertTestStruct(TestStruct t, int i, Pointer p, int... bytes) {
		assertTestStruct(t, i, p, ArrayUtil.bytes(bytes));
	}

	/**
	 * Assert struct fields.
	 */
	public static void assertTestStruct(TestStruct t, int i, Pointer p, byte[] bytes) {
		assertNotNull(t);
		assertEquals(t.i, i);
		assertEquals(t.p, p);
		assertArray(t.b, bytes);
	}

	/**
	 * Asserts bytes at pointer address.
	 */
	public static void assertMemory(Pointer p, int... bytes) {
		assertArray(JnaUtil.bytes(p, 0, bytes.length), bytes);
	}

	/**
	 * Asserts pointer type pointer.
	 */
	public static void assertPointer(PointerType pt, Pointer p) {
		assertEquals(PointerUtil.pointer(pt), p);
	}

	/**
	 * Asserts struct pointer.
	 */
	public static void assertPointer(Structure t, Pointer p) {
		assertEquals(Struct.pointer(t), p);
	}

	/**
	 * Writes struct to memory, then auto-reads from memory into a new instance.
	 */
	public static <T extends Structure> T ref(T t, Function<Pointer, T> constructor) {
		return Struct.adapt(Struct.write(t), constructor);
	}

	/**
	 * Writes struct to memory, then auto-reads from memory into a new instance.
	 */
	public static TestStruct ref(TestStruct t) {
		return ref(t, TestStruct::new);
	}

	/**
	 * Convenience constructor for native long.
	 */
	public static NativeLong nlong(long value) {
		return new NativeLong(value);
	}

	/**
	 * Convenience constructor for unsigned native long.
	 */
	public static NativeLong unlong(long value) {
		return new NativeLong(value, true);
	}

	/**
	 * Creates a new pointer copy. Be careful of gc on original object.
	 */
	public static Pointer p(Pointer p) {
		return p(p, 0);
	}

	/**
	 * Creates a new pointer copy at offset. Be careful of gc on original object.
	 */
	public static Pointer p(Pointer p, long offset) {
		return PointerUtil.pointer(PointerUtil.peer(p) + offset);
	}

	/**
	 * Allocates a contiguous pointer array with given pointer values. Returns indirected pointers.
	 */
	public static Pointer[] indirect(Pointer... ps) {
		Memory m = JnaUtil.calloc(ps.length * Pointer.SIZE);
		Pointer[] array = new Pointer[ps.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = m.share(i * Pointer.SIZE, Pointer.SIZE);
			array[i].setPointer(0, ps[i]);
		}
		return array;
	}

	/**
	 * Allocates a contiguous pointer array with given type pointer values. Returns indirected
	 * pointers.
	 */
	public static Pointer[] indirect(Structure... ts) {
		Struct.writeAuto(ts);
		return indirect(Stream.of(ts).map(Struct::pointer).toArray(Pointer[]::new));
	}
}
