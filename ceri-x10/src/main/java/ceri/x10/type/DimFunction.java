package ceri.x10.type;

import ceri.common.util.HashCoder;

/**
 * A dimming function that tracks dimming level as a percent value (DIM, BRIGHT).
 */
public class DimFunction extends BaseFunction {
	public final int percent;
	private final int hashCode;

	public DimFunction(House house, FunctionType type, int percent) {
		super(house, type);
		if (!isAllowed(type)) throw new IllegalArgumentException("Type is not dimmable: " + type);
		this.percent = percent;
		hashCode = HashCoder.hash(super.hashCode(), percent);
	}

	/**
	 * Convenience method to create a dim function.
	 */
	public static DimFunction dim(House house, int percent) {
		return new DimFunction(house, FunctionType.dim, percent);
	}

	/**
	 * Convenience method to create a bright function.
	 */
	public static DimFunction bright(House house, int percent) {
		return new DimFunction(house, FunctionType.bright, percent);
	}

	/**
	 * Checks if given function type is compatible with this class.
	 */
	public static boolean isAllowed(FunctionType type) {
		return type.group == FunctionGroup.dim;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DimFunction)) return false;
		DimFunction other = (DimFunction) obj;
		return percent == other.percent && super.equals(obj);
	}

	@Override
	public String toString() {
		return super.toString() + "(" + percent + "%)";
	}

}
