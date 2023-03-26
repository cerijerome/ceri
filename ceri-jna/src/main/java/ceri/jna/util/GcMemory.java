package ceri.jna.util;

import com.sun.jna.Memory;

/**
 * Wrapper for long-term Memory that relies on gc to free rather than close(). Avoids
 * try-with-resource warnings when returning Memory from functions.
 */
public class GcMemory {
	public static final GcMemory NULL = new GcMemory(null);
	public final Memory m;

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
	
	public GcMemory close() {
		if (valid()) m.close();
		return this;
	}
	
	public long size() {
		return  m == null ? 0 : m.size();
	}
	
}
