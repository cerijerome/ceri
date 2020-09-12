package ceri.x10.command;

/**
 * Grouping of function types.
 */
public enum FunctionGroup {
	/**
	 * Functions that affect a whole house.
	 */
	house,
	/**
	 * Simple functions that affect a units.
	 */
	unit,
	/**
	 * Dimming functions.
	 */
	dim,
	/**
	 * Extended function.
	 */
	ext,
	/**
	 * Unsupported functions.
	 */
	unsupported;
}
