package ceri.x10.type;

import static ceri.x10.type.FunctionGroup.dim;
import static ceri.x10.type.FunctionGroup.extended;
import static ceri.x10.type.FunctionGroup.house;
import static ceri.x10.type.FunctionGroup.unit;
import static ceri.x10.type.FunctionGroup.unsupported;

/**
 * Function type, and its grouping.
 */
public enum FunctionType {
	ALL_UNITS_OFF(house),
	ALL_LIGHTS_ON(house),
	ON(unit),
	OFF(unit),
	DIM(dim),
	BRIGHT(dim),
	ALL_LIGHTS_OFF(house),
	EXTENDED(extended),
	HAIL_REQUEST(unsupported),
	HAIL_ACKNOWLEDGE(unsupported),
	PRESET_DIM_1(unsupported),
	PRESET_DIM_2(unsupported),
	EXTENDED_DATA_XFER(unsupported),
	STATUS_ON(unsupported),
	STATUS_OFF(unsupported),
	STATUS_REQUEST(unsupported);

	public final FunctionGroup group;
	
	private FunctionType(FunctionGroup group) {
		this.group = group;
	}
	
}
