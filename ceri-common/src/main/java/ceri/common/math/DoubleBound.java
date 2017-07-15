package ceri.common.math;

import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class DoubleBound {
	public static final DoubleBound UNBOUND = new DoubleBound(null, null);
	public final Double value;
	public final BoundType type;

	public static DoubleBound inclusive(double value) {
		return new DoubleBound(value, BoundType.inclusive);
	}
	
	public static DoubleBound exclusive(double value) {
		return new DoubleBound(value, BoundType.exclusive);
	}
	
	private DoubleBound(Double value, BoundType type) {
		this.value = value;
		this.type = type;
	}

	public boolean unbound() {
		return value == null;
	}
	
	public boolean upperFor(double value) {
		if (unbound()) return true;
		if (value < this.value.doubleValue()) return true;
		if (type == BoundType.exclusive) return false;
		return EqualsUtil.equals(value, this.value.doubleValue());
	}
	
	public boolean lowerFor(double value) {
		if (unbound()) return true;
		if (value > this.value.doubleValue()) return true;
		if (type == BoundType.exclusive) return false;
		return EqualsUtil.equals(value, this.value.doubleValue());
	}
	
	@Override
	public int hashCode() {
		return HashCoder.hash(value, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof DoubleBound)) return false;
		DoubleBound other = (DoubleBound) obj;
		if (!EqualsUtil.equals(value, other.value)) return false;
		if (!EqualsUtil.equals(type, other.type)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, value, type).toString();
	}

}
