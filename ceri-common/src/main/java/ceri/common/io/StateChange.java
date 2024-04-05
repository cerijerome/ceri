package ceri.common.io;

import ceri.common.data.TypeTranscoder;
import ceri.common.util.BasicUtil;

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

	public static StateChange from(Boolean isFixed) {
		return BasicUtil.conditional(isFixed, fixed, broken, none);
	}

	private StateChange(int value) {
		this.value = value;
	}
}