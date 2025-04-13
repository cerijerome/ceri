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

	public static Version of(int version, int major, int minor) {
		return new Version(version, major, minor, null);
	}
	
	public static Version kernel(int value) {
		int minor = MathUtil.ubyte(value);
		int major = MathUtil.ubyte(value >> Byte.SIZE);
		int ver = MathUtil.ushort(value >> Short.SIZE);
		return of(ver, major, minor);
	}

	@Override
	public int compareTo(Version other) {
		return COMPARATOR.compare(this, other);
	}

	@Override
	public final String toString() {
		var b = new StringBuilder().append(version());
		if (major() != null) b.append('.').append(major());
		if (minor() != null) b.append('.').append(minor());
		if (rev() != null) b.append('.').append(rev());
		return b.toString();
	}
}
