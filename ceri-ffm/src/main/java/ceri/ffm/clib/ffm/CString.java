package ceri.ffm.clib.ffm;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.reflect.Refine.Size;
import ceri.ffm.type.IntType;

/**
 * Types and functions from {@code <string.h>} and {@code <wchar.h>}
 */
@CInclude("string.h")
@CInclude("wchar.h")
public class CString {

	private CString() {}

	/**
	 * Signed type. Native type is usually unsigned, but not always.
	 */
	@Size(type = "wchar_t")
	public static class wchar_t extends IntType<wchar_t> {
		public static final Supporter<wchar_t> $ = support(wchar_t.class);
		public static final wchar_t TERM = new wchar_t(0);
		public static final Charset CHARSET = charset($.spec().size());

		public wchar_t(Number value) {
			super(value);
		}
	}
	
	/**
	 * Returns a string that describes the error code. Returns empty string if the call fails.
	 */
	public static String strerror(int errnum) {
		// Using a caller here can cause a failure loop, due to its own use of this call
		return CLib.lib().strerror(errnum);
	}
	
	// support
	
	private static Charset charset(int size) {
		return switch (size) {
			case Byte.BYTES -> StandardCharsets.UTF_8;
			case Short.BYTES -> StandardCharsets.UTF_16;
			default -> StandardCharsets.UTF_32;
		};
	}
}
