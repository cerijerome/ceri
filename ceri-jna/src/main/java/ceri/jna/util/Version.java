package ceri.jna.util;

import java.util.Comparator;
import ceri.common.math.MathUtil;

/**
 * Represents a multi-level version.
 */
public record Version(int version, Integer major, Integer minor, String rev)
	implements Comparable<Version> {

	public static final Comparator<Version> COMPARATOR = Comparator.comparingInt(Version::version)
		.thenComparing(Comparator.comparing(Version::major))
		.thenComparing(Comparator.nullsFirst(Comparator.comparing(Version::major)))
		.thenComparing(Comparator.nullsFirst(Comparator.comparing(Version::minor)))
		.thenComparing(Comparator.nullsFirst(Comparator.comparing(Version::rev)));

	public static Version kernel(int value) {
		int minor = MathUtil.ubyte(value);
		int major = MathUtil.ubyte(value >> Byte.SIZE);
		int ver = MathUtil.ushort(value >> Short.SIZE);
		return new Version(ver, major, minor, null);
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
