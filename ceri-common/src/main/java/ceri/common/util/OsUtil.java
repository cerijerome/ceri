package ceri.common.util;

import java.util.regex.Pattern;
import ceri.common.text.StringUtil;

/**
 * Utility methods and constants to help with OS-specific logic.
 */
public class OsUtil {
	public static final String OS_NAME = SystemVars.sys("os.name");
	public static final String OS_ARCH = SystemVars.sys("os.arch");
	public static final String OS_VERSION = SystemVars.sys("os.version");
	public static final boolean IS_MAC = matches(OS_NAME, "^Mac");
	public static final boolean IS_LINUX = matches(OS_NAME, "^Linux");
	public static final boolean IS_X86 = matches(OS_ARCH, "^x86");
	public static final boolean IS_64BIT = matches(OS_ARCH, "64$");
	public static final boolean IS_ARM = matches(OS_ARCH, "arm");
	public static final String FULL_DESCRIPTOR = String.format( //
		"%s; %s; %s", OS_NAME, OS_ARCH, OS_VERSION);
	public static final boolean IS_AWS = propertyIsSet("AWS_PATH");

	private OsUtil() {}

	public static <T> T mac(T mac, T other) {
		return BasicUtil.conditional(IS_MAC, mac, other);
	}

	public static <T> T linux(T linux, T other) {
		return BasicUtil.conditional(IS_LINUX, linux, other);
	}

	public static int macInt(int mac, int other) {
		return BasicUtil.conditionalInt(IS_MAC, mac, other);
	}

	public static int linuxInt(int linux, int other) {
		return BasicUtil.conditionalInt(IS_LINUX, linux, other);
	}

	private static boolean matches(String s, String regex) {
		Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(s).find();
	}

	static boolean propertyIsSet(String name) {
		return !StringUtil.isBlank(SystemVars.sys(name));
	}
}
