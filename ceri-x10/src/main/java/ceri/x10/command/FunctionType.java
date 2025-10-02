package ceri.x10.command;

import java.util.Map;
import ceri.common.collect.Enums;
import ceri.common.data.Xcoder;
import ceri.common.text.Strings;

/**
 * Function type, and its grouping.
 */
public enum FunctionType {
	allUnitsOff(1, FunctionGroup.house),
	allLightsOn(2, FunctionGroup.house),
	on(3, FunctionGroup.unit),
	off(4, FunctionGroup.unit),
	dim(5, FunctionGroup.dim),
	bright(6, FunctionGroup.dim),
	allLightsOff(7, FunctionGroup.house),
	ext(8, FunctionGroup.ext),
	hailReq(9, FunctionGroup.unsupported),
	hailAck(10, FunctionGroup.unsupported),
	presetDim1(11, FunctionGroup.unsupported),
	presetDim2(12, FunctionGroup.unsupported),
	extDataXfer(13, FunctionGroup.unsupported),
	statusOn(14, FunctionGroup.unsupported),
	statusOff(15, FunctionGroup.unsupported),
	statusReq(16, FunctionGroup.unsupported);

	private static final Map<String, FunctionType> names =
		Enums.map(f -> f.name().toLowerCase(), FunctionType.class);
	private static final Xcoder.Type<FunctionType> xcoder =
		Xcoder.type(FunctionType.class, t -> t.id);
	public final FunctionGroup group;
	public final int id;

	public static FunctionType from(String name) {
		var type = names.get(Strings.lower(name));
		if (type != null) return type;
		throw new IllegalArgumentException("Invalid function type: " + name);
	}

	public static FunctionType from(int id) {
		return xcoder.decodeValid(id, "Function type");
	}

	FunctionType(int id, FunctionGroup group) {
		this.group = group;
		this.id = id;
	}
}
