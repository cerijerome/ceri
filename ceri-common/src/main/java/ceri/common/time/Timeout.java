package ceri.common.time;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import ceri.common.function.ExceptionBiFunction;

/**
 * Represent a timeout value with time unit.
 */
public class Timeout {
	public static final Timeout ZERO = of(0, TimeUnit.MILLISECONDS);
	public final long timeout;
	public final TimeUnit unit;

	public static Timeout of(long timeout, TimeUnit unit) {
		Objects.requireNonNull(unit);
		return new Timeout(timeout, unit);
	}

	private Timeout(long timeout, TimeUnit unit) {
		this.timeout = timeout;
		this.unit = unit;
	}

	public Timeout convert(TimeUnit unit) {
		Objects.requireNonNull(unit);
		if (this.unit == unit) return this;
		return of(unit.convert(timeout, this.unit), unit);
	}

	public <E extends Exception, R> R applyTo(ExceptionBiFunction<E, Long, TimeUnit, R> fn)
		throws E {
		return fn.apply(timeout, unit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(timeout, unit);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Timeout)) return false;
		Timeout other = (Timeout) obj;
		if (timeout != other.timeout) return false;
		if (!Objects.equals(unit, other.unit)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "" + timeout + DateUtil.symbol(unit);
	}

}
