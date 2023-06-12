package ceri.common.io;

import ceri.common.data.TypeTranscoder;
import ceri.common.event.Listenable;

/**
 * Event type for a connection/device state change.
 */
public enum StateChange {
	none(0),
	fixed(1),
	broken(2);

	/**
	 * An interface for state-aware devices, with state change notifications.
	 */
	public static interface Fixable extends Listenable.Indirect<StateChange> {
		/**
		 * Notify the device that it is broken. For when the device itself cannot determine it is
		 * broken. Does nothing by default.
		 */
		default void broken() {}
	}

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