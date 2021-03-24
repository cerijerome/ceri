package ceri.common.time;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionBiFunction;

/**
 * Represent a timeout value with time unit.
 */
public class Timeout {
	public static final Timeout NULL = new Timeout(0, null);
	public static final Timeout ZERO = millis(0);
	public final long timeout;
	public final TimeUnit unit;

	public static Timeout millis(long timeoutMs) {
		return of(timeoutMs, TimeUnit.MILLISECONDS);
	}
	
	public static Timeout micros(long timeoutUs) {
		return of(timeoutUs, TimeUnit.MICROSECONDS);
	}
	
	public static Timeout nanos(long timeoutNs) {
		return of(timeoutNs, TimeUnit.NANOSECONDS);
	}
	
	public static Timeout of(long timeout, TimeUnit unit) {
		Objects.requireNonNull(unit);
		return new Timeout(timeout, unit);
	}

	private Timeout(long timeout, TimeUnit unit) {
		this.timeout = timeout;
		this.unit = unit;
	}

	public boolean isNull() {
		return unit == null;
	}

	public Timeout convert(TimeUnit unit) {
		if (isNull()) return this;
		Objects.requireNonNull(unit);
		if (this.unit == unit) return this;
		return of(unit.convert(timeout, this.unit), unit);
	}

	public <E extends Exception, R> R applyTo(ExceptionBiFunction<E, Long, TimeUnit, R> fn)
		throws E {
		return isNull() ? null : fn.apply(timeout, unit);
	}

	public <E extends Exception> void acceptBy(ExceptionBiConsumer<E, Long, TimeUnit> fn) throws E {
		if (!isNull()) fn.accept(timeout, unit);
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
		return isNull() ? "[null]" : timeout + DateUtil.symbol(unit);
	}

}
