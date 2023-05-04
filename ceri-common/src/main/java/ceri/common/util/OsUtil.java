package ceri.common.util;

import java.util.regex.Pattern;
import ceri.common.function.RuntimeCloseable;
import ceri.common.text.StringUtil;

/**
 * Utility methods and constants to help with OS-specific logic. Provides overrides to help with
 * testing.
 * <p/>
 * Example os.name, os.arch, os.version values:
 * 
 * <pre>
 * "Mac OS X", "aarch64", "13.2.1" (mac m1)
 * "Mac OS X", "x86_64", "13.2.1"
 * "Linux", "amd64", "5.15.0-67-generic"
 * "Linux", "aarch64", "5.15.49-linuxkit"
 * "Linux", "arm", "5.10.63-v7l+" (raspberry pi)
 * "Windows 11", "amd64", "10.0"
 * </pre>
 */
public class OsUtil {
	private static final Pattern MAC_REGEX = Pattern.compile("^(?:Mac|Darwin)");
	private static final Pattern LINUX_REGEX = Pattern.compile("^Linux");
	private static final Pattern X86_REGEX = Pattern.compile("^x86");
	private static final Pattern ARM_REGEX = Pattern.compile("^(?:arm|aarch)");
	private static final Pattern BIT64_REGEX = Pattern.compile("64$");
	private static final Os os = new Os(SystemVars.sys("os.name"),
		SystemVars.sys("os.arch"), SystemVars.sys("os.version"));
	private static volatile Os osOverride = null;

	private OsUtil() {}

	public static class Os {
		public final String name;
		public final String arch;
		public final String version;
		public final boolean mac;
		public final boolean linux;
		public final boolean x86;
		public final boolean arm;
		public final boolean bit64;
		
		private Os(String name, String arch, String version) {
			this.name = name;
			this.arch = arch;
			this.version = version;
			mac = MAC_REGEX.matcher(name).find();
			linux = mac ? false : LINUX_REGEX.matcher(name).find();
			x86 = X86_REGEX.matcher(arch).find();
			arm = x86 ? false : ARM_REGEX.matcher(arch).find();
			bit64 = BIT64_REGEX.matcher(arch).find();
		}
		
		public String descriptor() {
			return String.format("%s; %s; %s", name, arch, version);
		}
	}
	
	public static Os os() {
		return BasicUtil.defaultValue(osOverride, os);
	}

	public static boolean aws() {
		return !StringUtil.blank(SystemVars.sys("AWS_PATH"));
	}

	/**
	 * Overrides OS. Use null for actual values. 
	 */
	public static RuntimeCloseable os(String name, String arch, String version) {
		if (name == null) name = os.name;
		if (arch == null) arch = os.arch;
		if (version == null) version = os.version;
		osOverride = new Os(name, arch, version);
		return () -> osOverride = null;
	}	
}
