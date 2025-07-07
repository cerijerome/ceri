package ceri.common.util;

import ceri.common.data.TypeTranscoder;

/**
 * Encapsulates a yes/no/maybe response.
 */
public enum Truth {
	maybe(-1, '?'),
	no(0, 'N'),
	yes(1, 'Y');

	public static final TypeTranscoder<Truth> xcoder = TypeTranscoder.of(t -> t.value, Truth.class);
	public final int value;
	public final char symbol;

	public static boolean known(Truth truth) {
		return truth != null && truth.known();
	}

	public static Truth invert(Truth truth) {
		return truth == null ? null : truth.invert();
	}

	public static Boolean bool(Truth truth) {
		return truth == null ? null : truth.bool();
	}

	public static Truth from(Boolean isYes) {
		return BasicUtil.ternary(isYes, yes, no, maybe);
	}

	private Truth(int value, char symbol) {
		this.value = value;
		this.symbol = symbol;
	}

	public Truth invert() {
		if (!known()) return this;
		return this == yes ? no : yes;
	}

	public boolean known() {
		return this != maybe;
	}

	public boolean yes() {
		return this == yes;
	}

	public boolean no() {
		return this == no;
	}

	public Boolean bool() {
		if (!known()) return null;
		return this == no ? Boolean.FALSE : Boolean.TRUE;
	}
}
