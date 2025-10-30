package ceri.common.util;

import java.util.regex.Pattern;
import ceri.common.concurrent.Lazy;
import ceri.common.function.Functions;
import ceri.common.text.Strings;

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
public class Os {
	private static final Pattern MAC_REGEX = Pattern.compile("^(?:Mac|Darwin)");
	private static final Pattern LINUX_REGEX = Pattern.compile("^Linux");
	private static final Pattern X86_REGEX = Pattern.compile("^x86");
	private static final Pattern ARM_REGEX = Pattern.compile("^(?:arm|aarch)");
	private static final Pattern BIT64_REGEX = Pattern.compile("64$");
	private static final Lazy.Value<RuntimeException, Info> info = Lazy.Value.of(
		new Info(SystemVars.sys("os.name"), SystemVars.sys("os.arch"), SystemVars.sys("os.version")));

	private Os() {}

	/**
	 * Encapsulates OS info.
	 */
	public static class Info {
		public final String name;
		public final String arch;
		public final String version;
		public final boolean mac;
		public final boolean linux;
		public final boolean x86;
		public final boolean arm;
		public final boolean bit64;

		private Info(String name, String arch, String version) {
			this.name = name;
			this.arch = arch;
			this.version = version;
			mac = MAC_REGEX.matcher(name).find();
			linux = mac ? false : LINUX_REGEX.matcher(name).find();
			x86 = X86_REGEX.matcher(arch).find();
			arm = x86 ? false : ARM_REGEX.matcher(arch).find();
			bit64 = BIT64_REGEX.matcher(arch).find();
		}

		/**
		 * Convenience conditional.
		 */
		public <T> T mac(T trueVal, T falseVal) {
			return Basics.ternary(mac, trueVal, falseVal);
		}

		/**
		 * Convenience conditional.
		 */
		public <T> T linux(T trueVal, T falseVal) {
			return Basics.ternary(linux, trueVal, falseVal);
		}

		/**
		 * Convenience conditional.
		 */
		public <T> T x86(T trueVal, T falseVal) {
			return Basics.ternary(x86, trueVal, falseVal);
		}

		/**
		 * Convenience conditional.
		 */
		public <T> T arm(T trueVal, T falseVal) {
			return Basics.ternary(arm, trueVal, falseVal);
		}

		/**
		 * Convenience conditional.
		 */
		public <T> T bit64(T trueVal, T falseVal) {
			return Basics.ternary(bit64, trueVal, falseVal);
		}

		/**
		 * Provides a detailed descriptor.
		 */
		public String full() {
			return toString() + String.format("; mac=%s, linux=%s, x86=%s, arm=%s, bit64=%s", mac,
				linux, x86, arm, bit64);
		}

		@Override
		public String toString() {
			return String.format("%s; %s; %s", name, arch, version);
		}
	}

	/**
	 * Retrieve the current OS info, or override if set.
	 */
	public static Info info() {
		return info.get();
	}

	/**
	 * Retrieve the current OS info, ignoring any override.
	 */
	public static Info value() {
		return info.value();
	}

	/**
	 * Overrides OS. Use null for actual values. Removes override on close.
	 */
	public static Functions.Closeable info(String name, String arch, String version) {
		var info = Os.info.get();
		if (name == null) name = info.name;
		if (arch == null) arch = info.arch;
		if (version == null) version = info.version;
		return Os.info.override(new Info(name, arch, version));
	}

	/**
	 * Determines if context is AWS, based on path system variable.
	 */
	public static boolean aws() {
		return !Strings.isBlank(SystemVars.sys("AWS_PATH"));
	}
}
