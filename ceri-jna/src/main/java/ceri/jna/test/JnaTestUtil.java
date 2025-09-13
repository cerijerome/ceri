package ceri.jna.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertThrown;
import java.nio.ByteBuffer;
import java.util.Set;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import ceri.common.array.ArrayUtil;
import ceri.common.collection.Sets;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.ClassReInitializer;
import ceri.common.test.ErrorGen;
import ceri.common.text.Strings;
import ceri.jna.clib.jna.CException;
import ceri.jna.type.IntType;
import ceri.jna.type.Struct;
import ceri.jna.util.GcMemory;
import ceri.jna.util.JnaOs;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.PointerUtil;

/**
 * Supports tests for JNA-based code.
 */
public class JnaTestUtil {
	public static final Functions.Supplier<Exception> LEX =
		ErrorGen.errorFn(LastErrorException::new);

	private JnaTestUtil() {}

	/**
	 * Provides cached memory allocation to prevent gc in tests.
	 */
	public static class MemCache implements Functions.Closeable {
		private final Set<GcMemory> cache = Sets.of();

		private MemCache() {}

		public GcMemory mallocBytes(int... bytes) {
			return cache(mem(bytes));
		}

		public GcMemory calloc(int size) {
			return cache(memSize(size).clear());
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
	 * Allocate native memory and copy array.
	 */
	public static GcMemory mem(int... array) {
		return GcMemory.mallocBytes(array);
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static GcMemory memSize(int size) {
		return GcMemory.malloc(size);
	}

	/**
	 * Checks remaining memory from offset matches bytes.
	 */
	public static void assertMemory(Memory m, int offset, int... bytes) {
		assertMemory(m, offset, ArrayUtil.bytes.of(bytes));
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
		assertPointer(p, offset, ArrayUtil.bytes.of(bytes));
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
	 * Make sure int type reference pointer stores the given value. Given int type will be modified.
	 */
	public static void assertRef(Pointer p, IntType<? extends IntType<?>> intType) {
		var expected = intType.longValue();
		var actual = intType.read(p, 0).longValue();
		assertEquals(actual, expected);
	}

	/**
	 * Make sure unsigned native long reference pointer stores the given value.
	 */
	public static void assertCLong(Pointer p, long value) {
		assertEquals(JnaUtil.clong(p, 0), value);
	}

	/**
	 * Make sure unsigned native long reference pointer stores the given value.
	 */
	public static void assertCUlong(Pointer p, long value) {
		assertEquals(JnaUtil.culong(p, 0), value);
	}

	/**
	 * Assert a LastErrorException was thrown.
	 */
	public static void assertLastError(Excepts.Runnable<Exception> runnable) {
		assertThrown(LastErrorException.class, runnable);
	}

	/**
	 * Assert a LastErrorException with specific code was thrown.
	 */
	public static void assertLastError(int code, Excepts.Runnable<Exception> runnable) {
		assertThrown(LastErrorException.class, e -> assertEquals(e.getErrorCode(), code), runnable);
	}

	/**
	 * Assert a CException was thrown.
	 */
	public static void assertCException(Excepts.Runnable<Exception> runnable) {
		assertThrown(CException.class, runnable);
	}

	/**
	 * Assert a CException with specific code was thrown.
	 */
	public static void assertCException(int code, Excepts.Runnable<Exception> runnable) {
		assertThrown(CException.class, e -> assertEquals(e.code, code), runnable);
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
			m[i] = JnaUtil.calloc(Maths.random(min, max));
		System.gc();
		return m;
	}

	/**
	 * Create a LastErrorException from the code and standard message.
	 */
	public static LastErrorException lastError(int code) {
		return lastError(code, "test");
	}

	/**
	 * Create a LastErrorException from the code and message.
	 */
	public static LastErrorException lastError(int code, String message, Object... args) {
		return new LastErrorException("[" + code + "] " + Strings.format(message, args));
	}

	/**
	 * Copies pointer data to the struct, applies the consumer, then copies the struct back to the
	 * pointer. Useful for testing when a struct doesn't provide a pointer constructor.
	 */
	public static <E extends Exception, T extends Structure> void handleStructRef(Pointer p,
		T struct, Excepts.Consumer<E, T> consumer) throws E {
		Struct.copyFrom(p, struct);
		consumer.accept(struct);
		Struct.copyTo(struct, p);
	}

	/**
	 * Reloads and instantiates the test class, overriding the current OS. Support classes are
	 * reloaded if accessed.
	 */
	public static void testAsOs(JnaOs os, Class<?> testCls, Class<?>... reloads) {
		os.accept(_ -> ClassReInitializer.of(testCls, reloads).reinit());
	}

	/**
	 * Reloads and instantiates the test class for each supported OS, overriding the current OS.
	 * Support classes are reloaded if accessed.
	 */
	public static void testForEachOs(Class<?> testCls, Class<?>... reloads) {
		JnaOs.forEach(os -> testAsOs(os, testCls, reloads));
	}

}
