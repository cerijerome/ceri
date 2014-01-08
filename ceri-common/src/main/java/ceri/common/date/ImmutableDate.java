package ceri.common.date;

import java.util.Date;

/**
 * An immutable extension to Date.
 * UnsupportedOperationException is thrown for mutator methods.
 */
public class ImmutableDate extends Date {
	private static final long serialVersionUID = -8001838082219221691L;

	public ImmutableDate(Date date) {
		super(date.getTime());
	}
	
	@Override
	public void setTime(long time) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void setDate(int date) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	@Deprecated
	public void setHours(int hours) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	@Deprecated
	public void setMinutes(int minutes) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	@Deprecated
	public void setMonth(int month) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	@Deprecated
	public void setSeconds(int seconds) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	@Deprecated
	public void setYear(int year) {
		throw new UnsupportedOperationException();
	}
	
}
