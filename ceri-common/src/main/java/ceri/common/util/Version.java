package ceri.common.util;

import java.util.Comparator;
import ceri.common.comparator.Comparators;
import ceri.common.math.MathUtil;

/**
 * Represents a multi-level version.
 */
public record Version(int version, Integer major, Integer minor, String rev)
	implements Comparable<Version> {

	public static final Comparator<Version> COMPARATOR = Comparator.comparingInt(Version::version)
		.thenComparing(Comparator.comparing(Version::major, Comparators.INT))
		.thenComparing(Comparator.comparing(Version::minor, Comparators.INT))
		.thenComparing(Comparator.comparing(Version::rev, Comparators.STRING));

	/**
	 * Creates an instance without revision string.
	 */
	public static Version of(int version, int major, int minor) {
		return new Version(version, major, minor, null);
	}

	/**
	 * Decodes a 32-bit kernel version value into version, major and minor (16-8-8 bits).
	 */
	public static Version kernel(int value) {
		int minor = MathUtil.ubyte(value);
		int major = MathUtil.ubyte(value >> Byte.SIZE);
		int ver = MathUtil.ushort(value >> Short.SIZE);
		return of(ver, major, minor);
	}

	/**
	 * Encodes version, major and minor into a 32-bit kernel version value (16-8-8 bits).
	 */
	public int kernel() {
		int version = MathUtil.ushort(version());
		int major = major() == null ? 0 : MathUtil.ubyte(major());
		int minor = minor() == null ? 0 : MathUtil.ubyte(minor());
		return (version << Short.SIZE) | (major << Byte.SIZE) | minor;
	}

	@Override
	public int compareTo(Version other) {
		return COMPARATOR.compare(this, other);
	}

	@Override
	public String toString() {
		var b = new StringBuilder().append(version());
		if (major() != null) b.append('.').append(major());
		if (minor() != null) b.append('.').append(minor());
		if (rev() != null) b.append('.').append(rev());
		return b.toString();
	}
}
