package ceri.serial.clib;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import ceri.common.data.TypeTranscoder;
import ceri.common.math.MathUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

public class Mode {
	private final int mode;

	public static enum Mask {
		xoth(0001),
		woth(0002),
		roth(0004),
		rwxo(0007),
		xgrp(0010),
		wgrp(0020),
		rgrp(0040),
		rwxg(0070),
		xusr(0100),
		wusr(0200),
		rusr(0400),
		rwxu(0700),
		svtx(01000),
		sgid(02000),
		suid(04000),
		fifo(010000),
		fchr(020000),
		fdir(040000),
		fblk(060000),
		freg(0100000),
		flnk(0120000),
		fsock(0140000),
		fmt(0170000);

		private static final TypeTranscoder<Mask> xcoder =
			TypeTranscoder.of(t -> t.value, BasicUtil.enumsReversed(Mask.class));
		private final int value;

		public static int encode(Mask... masks) {
			return encode(Arrays.asList(masks));
		}

		public static int encode(Collection<Mask> masks) {
			return xcoder.encode(masks);
		}

		public static Set<Mask> decode(int value) {
			return xcoder.decodeAll(value);
		}

		public static String string(int value) {
			return StringUtil.join("|", decode(value));
		}

		private Mask(int value) {
			this.value = value;
		}
	}

	public static Mode of(int mode) {
		return new Mode(MathUtil.ushortExact(mode));
	}

	public static Mode of(Mask... masks) {
		return of(Arrays.asList(masks));
	}

	public static Mode of(Collection<Mask> masks) {
		return of(Mask.encode(masks));
	}

	public static class Builder {
		final Set<Mask> masks = new TreeSet<>();

		Builder() {}

		public Builder add(Mask... masks) {
			return add(Arrays.asList(masks));
		}

		public Builder add(Collection<Mask> masks) {
			this.masks.addAll(masks);
			return this;
		}

		public Mode build() {
			return new Mode(Mask.encode(masks));
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Mode(int mode) {
		this.mode = mode;
	}

	public int value() {
		return mode;
	}

	public boolean has(Mask... masks) {
		return has(Arrays.asList(masks));
	}

	public boolean has(Collection<Mask> masks) {
		int value = Mask.encode(masks);
		return (mode & value) == value;
	}

	public Set<Mask> masks() {
		return Mask.decode(mode);
	}

	public String maskString() {
		return Mask.string(mode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Mode)) return false;
		Mode other = (Mode) obj;
		if (mode != other.mode) return false;
		return true;
	}

	@Override
	public String toString() {
		return "mode:0" + Integer.toOctalString(mode);
	}

}
