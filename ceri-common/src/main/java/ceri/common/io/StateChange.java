package ceri.common.io;

import ceri.common.data.TypeTranscoder;

/**
 * Event type for a connection/device state change.
 */
public enum StateChange {
	none(0),
	fixed(1),
	broken(2);

	public static final TypeTranscoder<StateChange> xcoder =
		TypeTranscoder.of(t -> t.value, StateChange.class);
	public final int value;

	StateChange(int value) {
		this.value = value;
	}

	public static StateChange from(Boolean isFixed) {
		if (isFixed == null) return none;
		return isFixed ? fixed : broken;
	}

}