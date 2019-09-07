package ceri.x10.type;

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

	public final char value;

	House() {
		value = name().charAt(0);
	}

	public static House fromChar(char ch) {
		return House.valueOf(String.valueOf(Character.toUpperCase(ch)));
	}

}
