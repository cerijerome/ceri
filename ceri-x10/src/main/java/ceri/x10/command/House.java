package ceri.x10.command;

import ceri.common.data.TypeTranscoder;

/**
 * The house code.
 */
public enum House {
	A,
	B,
	C,
	D,
	E,
	F,
	G,
	H,
	I,
	J,
	K,
	L,
	M,
	N,
	O,
	P;

	private static final TypeTranscoder<House> xcoder = TypeTranscoder.of(t -> t.id, House.class);
	public final char value;
	public final int id;

	public static House fromId(int id) {
		return xcoder.decodeValid(id, House.class.getSimpleName());
	}

	public static House from(char ch) {
		return fromId(Character.toUpperCase(ch) + 1 - 'A');
	}

	House() {
		value = name().charAt(0);
		id = value + 1 - 'A';
	}

}
