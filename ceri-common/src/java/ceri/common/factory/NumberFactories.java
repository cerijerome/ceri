package ceri.common.factory;

public class NumberFactories {

	public static final Factory<Long, Number> TO_LONG = new Factory.Base<Long, Number>() {
		@Override
		protected Long createNonNull(Number from) {
			return from.longValue();
		}
	};

	public static final Factory<Integer, Number> TO_INTEGER =
		new Factory.Base<Integer, Number>() {
			@Override
			protected Integer createNonNull(Number from) {
				return from.intValue();
			}
		};

	public static final Factory<Short, Number> TO_SHORT =
		new Factory.Base<Short, Number>() {
			@Override
			protected Short createNonNull(Number from) {
				return from.shortValue();
			}
		};

	public static final Factory<Byte, Number> TO_BYTE = new Factory.Base<Byte, Number>() {
		@Override
		protected Byte createNonNull(Number from) {
			return from.byteValue();
		}
	};

	public static final Factory<Double, Number> TO_DOUBLE =
		new Factory.Base<Double, Number>() {
			@Override
			protected Double createNonNull(Number from) {
				return from.doubleValue();
			}
		};

	public static final Factory<Float, Number> TO_FLOAT =
		new Factory.Base<Float, Number>() {
			@Override
			protected Float createNonNull(Number from) {
				return from.floatValue();
			}
		};

}
