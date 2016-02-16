package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.math.MathUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class ConicalFrustum {
	public static final ConicalFrustum NULL = new ConicalFrustum(0, 0, 0);
	private final Cone cone;
	private final Cone mask;
	private final double Vm;
	public final double r0;
	public final double r1;
	public final double h;

	public static ConicalFrustum create(double r0, double r1, double h) {
		if (r1 == 0 || h == 0) return NULL;
		validateMin(r0, 0, "Minor radius");
		validateMin(r1, r0, "Major radius");
		validateMin(h, 0, "Height");
		return new ConicalFrustum(r0, r1, h);
	}

	private ConicalFrustum(double r0, double r1, double h) {
		this.r0 = r0;
		this.r1 = r1;
		this.h = h;
		cone = Cone.create(r1, r1 * h / (r1 - r0));
		mask = Cone.create(r0, r0 * h / (r1 - r0));
		Vm = mask.volume();
	}

	/**
	 * Angle of the frustum.
	 */
	public double angle() {
		return cone.angle();
	}

	/**
	 * Gradient of the frustum.
	 */
	public double gradient() {
		return cone.gradient();
	}

	/**
	 * Calculates h from given volume, with h starting opposite the base.
	 */
	public double hFromVolume(double v) {
		if (isNull()) return 0;
		if (v <= 0) return 0;
		return Math.min(h, cone.hFromVolume(v + Vm) - mask.h);
	}

	/**
	 * Calculates r from given h, with h starting opposite the base.
	 */
	public double rFromH(double h) {
		if (isNull()) return 0;
		if (h < 0 || h > this.h) return 0;
		return cone.rFromH(h + mask.h);
	}

	/**
	 * Calculates h from given r, with h starting opposite the base.
	 */
	public double hFromR(double r) {
		if (isNull()) return 0;
		if (r <= r0 || r > r1) return 0;
		return cone.hFromR(r) - mask.h;
	}

	/**
	 * Calculates the volume between planes perpendicular to the h-axis at h0 and h1.
	 */
	public double volumeBetweenH(double h0, double h1) {
		if (isNull()) return 0;
		h0 = MathUtil.within(h0, 0, h);
		h1 = MathUtil.within(h1, 0, h);
		return cone.volumeBetweenH(h0 + mask.h, h1 + mask.h);
	}

	/**
	 * Calculates the cone volume.
	 */
	public double volume() {
		if (isNull()) return 0;
		return cone.volume() - Vm;
	}

	/**
	 * Is this a null conical frustum?
	 */
	public boolean isNull() {
		return h == 0;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(cone, mask);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ConicalFrustum)) return false;
		ConicalFrustum other = (ConicalFrustum) obj;
		if (!EqualsUtil.equals(r0, other.r0)) return false;
		if (!EqualsUtil.equals(r1, other.r1)) return false;
		if (!EqualsUtil.equals(h, other.h)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, r0, r1, h).toString();
	}

}
