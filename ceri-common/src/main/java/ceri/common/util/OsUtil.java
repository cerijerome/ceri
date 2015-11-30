package ceri.common.util;

import java.util.regex.Pattern;

public class OsUtil {
	public static final String OS_NAME = System.getProperty("os.name");
	public static final String OS_ARCH = System.getProperty("os.arch");
	public static final boolean IS_MAC = matches(OS_NAME, "^Mac");
	public static final boolean IS_X86 = matches(OS_ARCH, "^x86");
	public static final boolean IS_64BIT = matches(OS_ARCH, "_64$");
		
	private OsUtil() {}

	private static boolean matches(String s, String regex) {
		Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(s).find();
	}
	
}
