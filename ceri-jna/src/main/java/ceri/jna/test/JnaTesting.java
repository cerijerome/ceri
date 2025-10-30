package ceri.jna.test;

import java.nio.ByteBuffer;
import java.util.Set;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.common.collect.Sets;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.ClassReInitializer;
import ceri.common.test.ErrorGen;
import ceri.common.text.Strings;
import ceri.jna.type.Struct;
import ceri.jna.util.GcMemory;
import ceri.jna.util.JnaOs;
import ceri.jna.util.Jna;
import ceri.jna.util.Pointers;

/**
 * Supports tests for JNA-based code.
 */
public class JnaTesting {
	public static final Functions.Supplier<Exception> LEX =
		ErrorGen.errorFn(LastErrorException::new);

	private JnaTesting() {}

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
	 * Creates a new pointer copy at offset. Be careful of gc on original object.
	 */
	public static Pointer deref(Pointer p, long offset) {
		return Pointers.pointer(Pointers.peer(p) + offset);
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
			m[i] = Jna.calloc(Maths.random(min, max));
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
