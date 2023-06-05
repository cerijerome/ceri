package ceri.jna.util;

import static ceri.common.validation.ValidationUtil.validateMin;
import com.sun.jna.Native;
import ceri.common.function.RuntimeCloseable;

/**
 * Provides native sizes, and allows overrides for testing.
 */
public class JnaSize {
	public static final Size POINTER = new Size(Native.POINTER_SIZE);
	public static final Size BOOL = new Size(Native.BOOL_SIZE);
	public static final Size WCHAR = new Size(Native.WCHAR_SIZE);
	public static final Size LONG = new Size(Native.LONG_SIZE);
	public static final Size LONG_DOUBLE = new Size(Native.LONG_DOUBLE_SIZE);
	public static final Size SIZE_T = new Size(Native.SIZE_T_SIZE);

	private JnaSize() {}
	
	public static class Size {
		public final int size;
		private volatile int sizeOverride = 0;

		private Size(int size) {
			this.size = size;
		}

		/**
		 * Returns the size, which may have been overridden.
		 */
		public int size() {
			int sizeOverride = this.sizeOverride;
			return sizeOverride > 0 ? sizeOverride : size;
		}

		/**
		 * Override native size; use 0 to remove override.
		 */
		public void size(int sizeOverride) {
			validateMin(sizeOverride, 0);
			this.sizeOverride = sizeOverride;
		}

		/**
		 * Returns a closeable instance that sets an override, then removes it on close.
		 */
		public RuntimeCloseable removable(int sizeOverride) {
			size(sizeOverride);
			return () -> size(0);
		}
	}

	/**
	 * Clear all overrides.
	 */
	public static void clear() {
		POINTER.size(0);
		BOOL.size(0);
		WCHAR.size(0);
		LONG.size(0);
		LONG_DOUBLE.size(0);
		SIZE_T.size(0);
	}
}
