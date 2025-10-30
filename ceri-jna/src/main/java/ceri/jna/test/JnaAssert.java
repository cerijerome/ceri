package ceri.jna.test;

import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import ceri.common.array.Array;
import ceri.common.function.Excepts;
import ceri.common.test.Assert;
import ceri.jna.clib.jna.CException;
import ceri.jna.type.IntType;
import ceri.jna.type.Struct;
import ceri.jna.util.Jna;
import ceri.jna.util.Pointers;

/**
 * JNA assertions.
 */
public class JnaAssert {
	private JnaAssert() {}

	/**
	 * Fails unless remaining memory from offset matches bytes.
	 */
	public static void memory(Memory m, int offset, int... bytes) {
		memory(m, offset, Array.bytes.of(bytes));
	}

	/**
	 * Fails unless remaining memory from offset matches bytes.
	 */
	public static void memory(Memory m, int offset, byte[] bytes) {
		Assert.equal(m.size() - offset, (long) bytes.length);
		pointer(m, offset, bytes);
	}

	/**
	 * Fails unless memory at pointer offset matches bytes.
	 */
	public static void pointer(Pointer p, int offset, int... bytes) {
		pointer(p, offset, Array.bytes.of(bytes));
	}

	/**
	 * Fails unless memory at pointer offset matches bytes.
	 */
	public static void pointer(Pointer p, int offset, byte[] bytes) {
		valid(p);
		try {
			byte[] actual = Jna.bytes(p, offset, bytes.length);
			Assert.array(actual, bytes);
		} catch (RuntimeException e) {
			throw new AssertionError(e.getMessage(), e);
		}
	}

	/**
	 * Fails unless the type pointer equals the given pointer.
	 */
	public static void pointer(PointerType pt, Pointer p) {
		Assert.equal(Pointers.pointer(pt), p);
	}

	/**
	 * Fails unless the struct pointer equals the given pointer.
	 */
	public static void pointer(Structure t, Pointer p) {
		Assert.equal(Struct.pointer(t), p);
	}

	/**
	 * Fails if the pointer is null or zero.
	 */
	public static void valid(Pointer p) {
		Assert.notNull(p);
		Assert.notEqual(Pointer.nativeValue(p), 0L);
	}

	/**
	 * Fails unless the pointer is null or zero.
	 */
	public static void notValid(Pointer p) {
		Assert.equal(Pointer.nativeValue(p), 0L, "Peer");
	}

	/**
	 * Fails unless the int reference stores the given value.
	 */
	public static void ref(IntByReference ref, int value) {
		Assert.equal(ref.getValue(), value);
	}

	/**
	 * Fails unless the reference pointer stores the given value.
	 */
	public static void ref(Pointer p, IntType<? extends IntType<?>> intType) {
		var expected = intType.longValue();
		var actual = intType.read(p, 0).longValue();
		Assert.equal(actual, expected);
	}

	/**
	 * Fails unless the reference pointer stores the given value.
	 */
	public static void clong(Pointer p, long value) {
		Assert.equal(Jna.clong(p, 0), value);
	}

	/**
	 * Fails unless the reference pointer stores the given value.
	 */
	public static void culong(Pointer p, long value) {
		Assert.equal(Jna.culong(p, 0), value);
	}

	/**
	 * Fails unless a LastErrorException is thrown.
	 */
	public static void lastError(Excepts.Runnable<Exception> runnable) {
		Assert.thrown(LastErrorException.class, runnable);
	}

	/**
	 * Fails unless a LastErrorException with specific code is thrown.
	 */
	public static void lastError(int code, Excepts.Runnable<Exception> runnable) {
		Assert.thrown(LastErrorException.class, e -> Assert.equal(e.getErrorCode(), code),
			runnable);
	}

	/**
	 * Fails unless a CException is thrown.
	 */
	public static void cexception(Excepts.Runnable<Exception> runnable) {
		Assert.thrown(CException.class, runnable);
	}

	/**
	 * Fails unless a CException with specific code is thrown.
	 */
	public static void cexception(int code, Excepts.Runnable<Exception> runnable) {
		Assert.thrown(CException.class, e -> Assert.equal(e.code, code), runnable);
	}
}
