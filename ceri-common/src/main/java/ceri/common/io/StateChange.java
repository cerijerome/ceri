package ceri.common.io;

import ceri.common.data.Xcoder;
import ceri.common.util.Basics;

/**
 * Event type for a connection/device state change.
 */
public enum StateChange {
	none(0),
	fixed(1),
	broken(2);

	public static final Xcoder.Type<StateChange> xcoder = Xcoder.type(StateChange.class);
	public final int value;

	public static StateChange from(Boolean isFixed) {
		return Basics.ternary(isFixed, fixed, broken, none);
	}

	private StateChange(int value) {
		this.value = value;
	}
}