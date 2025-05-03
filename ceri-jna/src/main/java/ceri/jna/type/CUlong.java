package ceri.jna.type;

import java.util.function.Supplier;
import com.sun.jna.Pointer;
import ceri.common.math.MathUtil;
import ceri.jna.util.JnaSize;

/**
 * Unsigned native long.
 */
@SuppressWarnings("serial")
public class CUlong extends IntType<CUlong> {
	private static final Supplier<CUlong> CONSTRUCTOR = CUlong::new;
	public static final int SIZE = JnaSize.LONG.get();

	public static CUlong readFrom(Pointer p, long offset) {
		return new CUlong().read(p, offset);
	}
	
	public static class ByRef extends IntType.ByRef<CUlong> {
		public ByRef() {
			this(new CUlong());
		}

		public ByRef(int value) {
			this(new CUlong(value));
		}
		
		public ByRef(long value) {
			this(new CUlong(value));
		}
		
		public ByRef(CUlong value) {
			super(CONSTRUCTOR, value);
		}
	}

	/**
	 * Create with value 0.
	 */
	public CUlong() {
		this(0L);
	}

	/**
	 * Create unsigned with value. Required if value is negative, but represents an unsigned int.
	 */
	public CUlong(int value) {
		this(MathUtil.uint(value));
	}
	
	/**
	 * Create unsigned with value.
	 */
	public CUlong(long value) {
		super(SIZE, value, true);
	}
}
