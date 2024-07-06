package ceri.common.data;

import ceri.common.util.BasicUtil;

/**
 * Encapsulates on/off or unknown states.
 */
public enum BinaryState {
	unknown(-1),
	off(0),
	on(1);

	public static final TypeTranscoder<BinaryState> xcoder =
		TypeTranscoder.of(t -> t.value, BinaryState.class);
	public final int value;

	public static boolean known(BinaryState state) {
		return state != null && state.known();
	}

	public static BinaryState invert(BinaryState state) {
		return state == null ? null : state.invert();
	}

	public static Boolean bool(BinaryState state) {
		return state == null ? null : state.bool();
	}

	public static BinaryState from(Boolean isOn) {
		return BasicUtil.conditional(isOn, on, off, unknown);
	}

	private BinaryState(int value) {
		this.value = value;
	}

	public BinaryState invert() {
		if (!known()) return this;
		return this == on ? off : on;
	}
	
	public boolean known() {
		return this != unknown;
	}

	public Boolean bool() {
		if (!known()) return null;
		return this == off ? Boolean.FALSE : Boolean.TRUE;
	}
}
