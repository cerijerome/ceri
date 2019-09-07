package ceri.x10.type;

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

	public final int index;

	Unit() {
		index = Integer.parseInt(name().substring(1));
	}

	public static Unit fromIndex(String index) {
		return Unit.valueOf("_" + index.trim());
	}

	public static Unit fromIndex(int index) {
		return Unit.valueOf("_" + index);
	}

}
