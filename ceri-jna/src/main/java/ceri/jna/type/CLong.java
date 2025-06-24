package ceri.jna.type;

import java.util.function.Supplier;
import com.sun.jna.Pointer;

/**
 * Signed native long.
 */
@SuppressWarnings("serial")
public class CLong extends IntType<CLong> {
	private static final Supplier<CLong> CONSTRUCTOR = CLong::new;
	public static final int SIZE = JnaSize.LONG.get();

	public static CLong readFrom(Pointer p, long offset) {
		return new CLong().read(p, offset);
	}
	
	public static class ByRef extends IntType.ByRef<CLong> {
		public ByRef() {
			this(new CLong());
		}

		public ByRef(long value) {
			this(new CLong(value));
		}

		public ByRef(CLong value) {
			super(CONSTRUCTOR, value);
		}
	}

	/**
	 * Create with value 0.
	 */
	public CLong() {
		this(0);
	}

	/**
	 * Create signed with value.
	 */
	public CLong(long value) {
		super(SIZE, value, false);
	}
}
