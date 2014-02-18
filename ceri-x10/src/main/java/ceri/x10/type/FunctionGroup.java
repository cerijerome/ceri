package ceri.x10.type;

/**
 * Grouping of function types.
 */
public enum FunctionGroup {
	/**
	 * Functions that affect a whole house.
	 */
	house,
	/**
	 * Simple functions that affect a single unit.
	 */
	unit,
	/**
	 * Dimming functions.
	 */
	dim,
	/**
	 * Extended function.
	 */
	extended,
	/**
	 * Unsupported functions.
	 */
	unsupported;
}
