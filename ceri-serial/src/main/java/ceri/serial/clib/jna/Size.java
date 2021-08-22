package ceri.serial.clib.jna;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;

public class Size {

	private Size() {}

	@SuppressWarnings("serial")
	public static class size_t extends IntegerType {
		public size_t() {
			this(0);
		}

		public size_t(long value) {
			super(Native.SIZE_T_SIZE, value, true);
		}
	}

	@SuppressWarnings("serial")
	public static class ssize_t extends IntegerType {
		public ssize_t() {
			this(0);
		}

		public ssize_t(long value) {
			super(Native.SIZE_T_SIZE, value);
		}
	}

}
