package ceri.jna.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.RuntimeCloseable;
import ceri.common.math.MathUtil;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.PointerUtil;
import ceri.jna.util.Struct;

/**
 * Supports tests for JNA-based code.
 */
public class JnaTestUtil {

	private JnaTestUtil() {}

	/**
	 * Provides cached memory allocation to prevent gc in tests.
	 */
	public static class MemCache implements RuntimeCloseable {
		private final Set<Memory> cache = new HashSet<>();

		private MemCache() {}

		@SuppressWarnings("resource")
		public Memory mallocBytes(int... bytes) {
			return cache(JnaUtil.mallocBytes(bytes));
		}

		@SuppressWarnings("resource")
		public Memory calloc(int size) {
			return cache(JnaUtil.calloc(size));
		}

		@Override
		public void close() {
			for (var m : cache)
				m.close();
			cache.clear();
		}

		private Memory cache(Memory m) {
			cache.add(m);
			return m;
		}
	}

	/**
	 * Provides cached memory allocation to prevent gc in tests.
	 */
	public static MemCache memCache() {
		return new MemCache();
	}

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

	/**
	 * Allocate a direct buffer containing given bytes.
	 */
	public static ByteBuffer buffer(int... bytes) {
		var buffer = ByteBuffer.allocateDirect(bytes.length);
		for (var b : bytes)
			buffer.put((byte) b);
		return buffer.position(0);
	}

	/**
	 * Allocate a randomized memory array and try to force gc.
	 */
	public static Memory[] workMemory(int n, int min, int max) {
		System.gc();
		Memory[] m = new Memory[n];
		for (int i = 0; i < n; i++)
			m[i] = JnaUtil.calloc(MathUtil.random(min, max));
		System.gc();
		return m;
	}
}
