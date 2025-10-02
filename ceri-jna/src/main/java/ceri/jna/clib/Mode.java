package ceri.jna.clib;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.collect.Enums;
import ceri.common.collect.Sets;
import ceri.common.data.Xcoder;
import ceri.common.math.Maths;
import ceri.common.text.Joiner;

public record Mode(int value) {
	public static final Mode NONE = new Mode(0);

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

		public static final Xcoder.Types<Mask> xcoder =
			Xcoder.types(Enums.of(Mask.class).reversed(), t -> t.value);
		public final int value;

		private Mask(int value) {
			this.value = value;
		}
	}

	public static Mode of(int mode) {
		return new Mode(Maths.ushortExact(mode));
	}

	public static Mode of(Mask... masks) {
		return of(Arrays.asList(masks));
	}

	public static Mode of(Collection<Mask> masks) {
		return of(Mask.xcoder.encodeInt(masks));
	}

	public static class Builder {
		final Set<Mask> masks = Sets.tree();

		Builder() {}

		public Builder add(Mask... masks) {
			return add(Arrays.asList(masks));
		}

		public Builder add(Collection<Mask> masks) {
			this.masks.addAll(masks);
			return this;
		}

		public Mode build() {
			return new Mode(Mask.xcoder.encodeInt(masks));
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public boolean has(Mask... masks) {
		return has(Arrays.asList(masks));
	}

	public boolean has(Collection<Mask> masks) {
		return Mask.xcoder.hasAll(value(), masks);
	}

	public Set<Mask> masks() {
		return Mask.xcoder.decodeAll(value());
	}

	public String maskString() {
		return Joiner.OR.join(Mask.xcoder.decodeAll(value()));
	}

	@Override
	public String toString() {
		return "mode:0" + Integer.toOctalString(value());
	}
}
