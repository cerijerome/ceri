package ceri.jna.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.RuntimeCloseable;
import ceri.common.math.MathUtil;
import ceri.common.reflect.ClassReloader;
import ceri.common.util.OsUtil;
import ceri.jna.util.GcMemory;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.PointerUtil;
import ceri.jna.util.Struct;

/**
 * Supports tests for JNA-based code.
 */
public class JnaTestUtil {
	public static final String MAC_OS = "Mac";
	public static final String LINUX_OS = "Linux";

	private JnaTestUtil() {}

	/**
	 * Provides cached memory allocation to prevent gc in tests.
	 */
	public static class MemCache implements RuntimeCloseable {
		private final Set<GcMemory> cache = new HashSet<>();

		private MemCache() {}

		public GcMemory mallocBytes(int... bytes) {
			return cache(GcMemory.mallocBytes(bytes));
		}

		public GcMemory calloc(int size) {
			return cache(GcMemory.malloc(size).clear());
		}

		@Override
		public void close() {
			for (var m : cache)
				m.close();
			cache.clear();
		}

		private GcMemory cache(GcMemory m) {
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
		assertValid(p);
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
	 * Make sure pointer is not null or zero.
	 */
	public static void assertValid(Pointer p) {
		assertNotNull(p);
		assertNotEquals(Pointer.nativeValue(p), 0L);
	}

	/**
	 * Make sure pointer is not null or zero.
	 */
	public static void assertNotValid(Pointer p) {
		assertEquals(Pointer.nativeValue(p), 0L, "Peer");
	}

	/**
	 * Make sure int reference stores the given value.
	 */
	public static void assertRef(IntByReference ref, int value) {
		assertEquals(ref.getValue(), value);
	}

	/**
	 * Make sure unsigned native long reference pointer stores the given value.
	 */
	public static void assertNlong(Pointer p, long value) {
		assertEquals(JnaUtil.nlong(p, 0), value);
	}

	/**
	 * Make sure unsigned native long reference pointer stores the given value.
	 */
	public static void assertUnlong(Pointer p, long value) {
		assertEquals(JnaUtil.unlong(p, 0), value);
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

	/**
	 * Runs the test, overriding the current OS.
	 */
	public static <E extends Exception> void testAsOs(String osName, ExceptionRunnable<E> tester)
		throws E {
		try (var x = OsUtil.os(osName, null, null)) {
			tester.run();
		}
	}

	/**
	 * Runs the test for each supported OS, overriding the current OS.
	 */
	public static <E extends Exception> void testForEachOs(ExceptionRunnable<E> tester) throws E {
		testAsOs(MAC_OS, tester);
		testAsOs(LINUX_OS, tester);
	}

	/**
	 * Reloads and instantiates the test class, overriding the current OS. Support classes are
	 * reloaded if accessed.
	 */
	public static void testAsOs(String osName, Class<?> testCls, Class<?>... supportClasses) {
		testAsOs(osName, () -> ClassReloader.reload(testCls, supportClasses));
	}

	/**
	 * Reloads and instantiates the test class for each supported OS, overriding the current OS.
	 * Support classes are reloaded if accessed.
	 */
	public static void testForEachOs(Class<?> testCls, Class<?>... supportClasses) {
		testAsOs(MAC_OS, testCls, supportClasses);
		testAsOs(LINUX_OS, testCls, supportClasses);
	}

}
