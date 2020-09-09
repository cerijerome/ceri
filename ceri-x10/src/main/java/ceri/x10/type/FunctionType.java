package ceri.x10.type;

import ceri.common.data.TypeTranscoder;

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
	extended(8, FunctionGroup.extended),
	hailRequest(9, FunctionGroup.unsupported),
	hailAcknowledge(10, FunctionGroup.unsupported),
	presetDim1(11, FunctionGroup.unsupported),
	presetDim2(12, FunctionGroup.unsupported),
	extendedDataXfer(13, FunctionGroup.unsupported),
	statusOn(14, FunctionGroup.unsupported),
	statusOff(15, FunctionGroup.unsupported),
	statusRequest(16, FunctionGroup.unsupported);

	private static final TypeTranscoder<FunctionType> xcoder =
		TypeTranscoder.of(t -> t.id, FunctionType.class);
	public final FunctionGroup group;
	public final int id;

	public static FunctionType from(int id) {
		return xcoder.decodeValid(id, "Function type");
	}

	FunctionType(int id, FunctionGroup group) {
		this.group = group;
		this.id = id;
	}

}
