package ceri.common.data;

/**
 * Sets/gets bit ranges within a given IntAccessor.
 */
public class MaskAccessor implements IntAccessor {
	private final IntAccessor accessor;
	private final int mask;
	private final int shiftBits;

	public static MaskAccessor of(IntAccessor accessor, int mask) {
		return of(accessor, mask, 0);
	}

	public static MaskAccessor of(IntAccessor accessor, int mask, int shiftBits) {
		return new MaskAccessor(accessor, mask, shiftBits);
	}

	private MaskAccessor(IntAccessor accessor, int mask, int shiftBits) {
		this.accessor = accessor;
		this.mask = mask;
		this.shiftBits = shiftBits;
	}

	@Override
	public void set(int value) {
		int i = (accessor.get() & ~mask) | (value << shiftBits & mask);
		accessor.set(i);
	}

	@Override
	public int get() {
		return (accessor.get() & mask) >> shiftBits;
	}

}
