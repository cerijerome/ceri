package ceri.jna.util;

import com.sun.jna.Memory;
import ceri.common.array.Array;

/**
 * Wrapper for long-term Memory that relies on gc to free rather than close(). Avoids
 * try-with-resource warnings when returning Memory from functions.
 */
public class GcMemory {
	public static final GcMemory NULL = new GcMemory(null);
	public final Memory m;

	/**
	 * Allocates new memory, or returns null if size is 0.
	 */
	@SuppressWarnings("resource")
	public static GcMemory malloc(int size) {
		return of(Jna.malloc(size));
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static GcMemory mallocBytes(int... array) {
		return mallocBytes(Array.bytes.of(array));
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static GcMemory mallocBytes(byte[] array) {
		return mallocBytes(array, 0);
	}

	/**
	 * Allocate native memory and copy array.
	 */
	public static GcMemory mallocBytes(byte[] array, int offset) {
		return mallocBytes(array, offset, array.length - offset);
	}

	/**
	 * Allocate native memory and copy array.
	 */
	@SuppressWarnings("resource")
	public static GcMemory mallocBytes(byte[] array, int offset, int length) {
		return of(Jna.mallocBytes(array, offset, length));
	}

	public static GcMemory of(Memory m) {
		return m == null ? NULL : new GcMemory(m);
	}

	private GcMemory(Memory m) {
		this.m = m;
	}

	public boolean valid() {
		return m != null && m.valid();
	}

	public GcMemory clear() {
		if (valid()) m.clear();
		return this;
	}

	/**
	 * Provide memory sub-view.
	 */
	public GcMemory share(long offset) {
		return share(offset, size() - offset);
	}

	/**
	 * Provide memory sub-view.
	 */
	@SuppressWarnings("resource")
	public GcMemory share(long offset, long size) {
		var m = Jna.share(this.m, offset, size);
		return m == this.m ? this : of(m);
	}
	
	/**
	 * Returns the memory size, 0 for null memory.
	 */
	public long size() {
		return Jna.size(m);
	}

	/**
	 * Returns the memory size, 0 for null memory. Fails if size if larger than int.
	 */
	public int intSize() {
		return Math.toIntExact(size());
	}

	public GcMemory close() {
		if (valid()) m.close();
		return this;
	}
}
