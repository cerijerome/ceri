package ceri.x10.type;

import java.util.EnumSet;

/**
 * A simple function that need no other parameters (ALL_UNITS_OFF, ALL_LIGHTS_OFF, ALL_LIGHTS_ON,
 * ON, OFF).
 */
public final class Function extends BaseFunction {
	private static final EnumSet<FunctionGroup> ALLOWED_GROUPS = EnumSet.of(FunctionGroup.house,
		FunctionGroup.unit);

	public Function(House house, FunctionType type) {
		super(house, type);
		if (!isAllowed(type)) throw new IllegalArgumentException("Function type not allowed: " +
			type);
	}

	public static boolean isAllowed(FunctionType type) {
		return ALLOWED_GROUPS.contains(type.group);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Function)) return false;
		return super.equals(obj);
	}

}
