package ceri.x10.type;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.util.Set;

/**
 * A simple function that need no other parameters (ALL_UNITS_OFF, ALL_LIGHTS_OFF, ALL_LIGHTS_ON,
 * ON, OFF).
 */
public final class Function extends BaseFunction {
	private static final Set<FunctionGroup> ALLOWED_GROUPS =
		Set.of(FunctionGroup.house, FunctionGroup.unit);

	public static Function of(House house, FunctionType type) {
		validateNotNull(house);
		validateNotNull(type);
		if (!ALLOWED_GROUPS.contains(type.group))
			throw new IllegalArgumentException("Function type not allowed: " + type);
		return new Function(house, type);
	}

	private Function(House house, FunctionType type) {
		super(house, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Function)) return false;
		return super.equals(obj);
	}

}
