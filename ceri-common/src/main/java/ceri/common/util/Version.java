package ceri.common.util;

import java.util.Comparator;
import ceri.common.function.Compares;
import ceri.common.math.Maths;

/**
 * Represents a multi-level version.
 */
public record Version(int version, Integer major, Integer minor, String rev)
	implements Comparable<Version> {

	public static final Comparator<Version> COMPARATOR =
		Compares.asInt(Version::version).thenComparing(Compares.as(Version::major))
			.thenComparing(Compares.as(Version::minor)).thenComparing(Compares.as(Version::rev));

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
		int minor = Maths.ubyte(value);
		int major = Maths.ubyte(value >> Byte.SIZE);
		int ver = Maths.ushort(value >> Short.SIZE);
		return of(ver, major, minor);
	}

	/**
	 * Encodes version, major and minor into a 32-bit kernel version value (16-8-8 bits).
	 */
	public int kernel() {
		int version = Maths.ushort(version());
		int major = major() == null ? 0 : Maths.ubyte(major());
		int minor = minor() == null ? 0 : Maths.ubyte(minor());
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
