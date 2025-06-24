package ceri.jna.type;

import com.sun.jna.Native;
import ceri.common.concurrent.Lazy;

/**
 * Provides native sizes, and allows overrides for testing.
 */
public class JnaSize {
	public static final Lazy.Value<RuntimeException, Integer> POINTER =
		Lazy.Value.of(Native.POINTER_SIZE);
	public static final Lazy.Value<RuntimeException, Integer> BOOL =
		Lazy.Value.of(Native.BOOL_SIZE);
	public static final Lazy.Value<RuntimeException, Integer> WCHAR =
		Lazy.Value.of(Native.WCHAR_SIZE);
	public static final Lazy.Value<RuntimeException, Integer> LONG =
		Lazy.Value.of(Native.LONG_SIZE);
	public static final Lazy.Value<RuntimeException, Integer> LONG_DOUBLE =
		Lazy.Value.of(Native.LONG_DOUBLE_SIZE);
	public static final Lazy.Value<RuntimeException, Integer> SIZE_T =
		Lazy.Value.of(Native.SIZE_T_SIZE);

	private JnaSize() {}

}
