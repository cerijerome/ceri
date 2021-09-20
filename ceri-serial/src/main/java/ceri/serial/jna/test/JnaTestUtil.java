package ceri.serial.jna.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import ceri.common.collection.ArrayUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.jna.PointerUtil;
import ceri.serial.jna.Struct;

/**
 * Supports tests for JNA-based code.
 */
public class JnaTestUtil {

	private JnaTestUtil() {}

	/**
	 * Checks remaining memory from offset matches bytes.
	 */
	public static void assertMemory(Memory m, int offset, int... bytes) {
		assertMemory(m, offset, ArrayUtil.bytes(bytes));
	}

	/**
	 * Checks remaining memory from offset matches bytes.
	 */
	public static void assertMemory(Memory m, int offset, byte[] bytes) {
		assertEquals(m.size() - offset, (long) bytes.length);
		assertPointer(m, offset, bytes);
	}

	/**
	 * Checks memory at pointer offset, matches bytes.
	 */
	public static void assertPointer(Pointer p, int offset, int... bytes) {
		assertPointer(p, offset, ArrayUtil.bytes(bytes));
	}

	/**
	 * Checks memory at pointer offset, matches bytes.
	 */
	public static void assertPointer(Pointer p, int offset, byte[] bytes) {
		try {
			byte[] actual = JnaUtil.bytes(p, offset, bytes.length);
			assertArray(actual, bytes);
		} catch (RuntimeException e) {
			throw new AssertionError(e.getMessage(), e);
		}
	}

	/**
	 * Checks pointer type pointer.
	 */
	public static void assertPointer(PointerType pt, Pointer p) {
		assertEquals(PointerUtil.pointer(pt), p);
	}

	/**
	 * Checks struct pointer.
	 */
	public static void assertPointer(Structure t, Pointer p) {
		assertEquals(Struct.pointer(t), p);
	}

	/**
	 * Creates a new pointer copy at offset. Be careful of gc on original object.
	 */
	public static Pointer deref(Pointer p, long offset) {
		return PointerUtil.pointer(PointerUtil.peer(p) + offset);
	}

	/**
	 * Creates a new pointer copy. Be careful of gc on original object.
	 */
	public static Pointer deref(Pointer p) {
		return deref(p, 0);
	}

}
