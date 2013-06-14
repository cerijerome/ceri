package ceri.common.factory;

import java.util.Date;

public class DateFactories {

	public static final Factory<Date, Long> FROM_LONG = new Factory.Base<Date, Long>() {
		@Override
		protected Date createNonNull(Long value) {
			return new Date(value);
		}
	};

	public static final Factory<Long, Date> TO_LONG = new Factory.Base<Long, Date>() {
		@Override
		protected Long createNonNull(Date value) {
			return value.getTime();
		}
	};

}
