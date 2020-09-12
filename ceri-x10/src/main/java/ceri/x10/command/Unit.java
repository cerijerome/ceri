package ceri.x10.command;

import ceri.common.data.TypeTranscoder;

/**
 * The device unit code. Use level for the integer value.
 */
public enum Unit {
	_1,
	_2,
	_3,
	_4,
	_5,
	_6,
	_7,
	_8,
	_9,
	_10,
	_11,
	_12,
	_13,
	_14,
	_15,
	_16;

	private static final TypeTranscoder<Unit> xcoder = TypeTranscoder.of(t -> t.value, Unit.class);
	public final int value;

	public static Unit from(int value) {
		return xcoder.decodeValid(value, Unit.class.getSimpleName());
	}

	private Unit() {
		value = Integer.parseInt(name().substring(1));
	}

}
