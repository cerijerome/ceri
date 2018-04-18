package ceri.common.factory;

/**
 * Factories converting between number types.
 */
public class NumberFactories {

	private NumberFactories() {}

	public static final Factory<Long, Number> TO_LONG = new Factory.Base<>() {
		@Override
		protected Long createNonNull(Number from) {
			return from.longValue();
		}
	};

	public static final Factory<Integer, Number> TO_INTEGER = new Factory.Base<>() {
		@Override
		protected Integer createNonNull(Number from) {
			return from.intValue();
		}
	};

	public static final Factory<Short, Number> TO_SHORT = new Factory.Base<>() {
		@Override
		protected Short createNonNull(Number from) {
			return from.shortValue();
		}
	};

	public static final Factory<Byte, Number> TO_BYTE = new Factory.Base<>() {
		@Override
		protected Byte createNonNull(Number from) {
			return from.byteValue();
		}
	};

	public static final Factory<Double, Number> TO_DOUBLE = new Factory.Base<>() {
		@Override
		protected Double createNonNull(Number from) {
			return from.doubleValue();
		}
	};

	public static final Factory<Float, Number> TO_FLOAT = new Factory.Base<>() {
		@Override
		protected Float createNonNull(Number from) {
			return from.floatValue();
		}
	};

}
