package ceri.ffm.core;

/**
 * Marker interface for native callbacks. Callbacks must extend this interface with a single invoke
 * method with desired return type/void and argument types.
 */
public interface Callback {
	String METHOD_NAME = "invoke";
}
