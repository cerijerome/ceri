package ceri.serial.jna;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import java.util.function.Function;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.collection.ArrayUtil;
import ceri.serial.jna.Struct.Fields;

public class JnaTestUtil {

	private JnaTestUtil() {}

	@Fields({ "i", "b", "p" })
	public static class TestStruct extends Struct {
		public int i;
		public byte[] b = new byte[3];
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
	public static void populate(TestStruct t, int i, Pointer p, int... bytes) {
		t.i = i;
		t.p = p;
		ArrayUtil.copy(bytes(bytes), 0, t.b, 0, bytes.length);
	}
	
	/**
	 * Assert struct fields.
	 */
	public static void assertTestStruct(TestStruct t, int i, Pointer p, int... bytes) {
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
	 * Writes struct to memory, then reads from memory into a new instance.
	 */
	public static <T extends Structure> T ref(T t, Function<Pointer, T> constructor) {
		return Struct.adapt(Struct.write(t), constructor);
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
	 * Creates a new pointer copy.
	 */
	public static Pointer p(Pointer p) {
		return PointerUtil.pointer(PointerUtil.peer(p));
	}

}
