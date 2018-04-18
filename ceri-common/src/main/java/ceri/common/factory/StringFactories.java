package ceri.common.factory;

/**
 * Factories to convert to and from Strings.
 */
public class StringFactories {

	private StringFactories() {}

	public static final Factory<String, char[]> FROM_CHAR_ARRAY = new Factory.Base<>() {
		@Override
		protected String createNonNull(char[] from) {
			return String.valueOf(from);
		}
	};

	public static final Factory<String, Object> FROM_OBJECT = new Factory.Base<>() {
		@Override
		protected String createNonNull(Object from) {
			return String.valueOf(from);
		}
	};

	public static final Factory<char[], String> TO_CHAR_ARRAY = new Factory.Base<>() {
		@Override
		protected char[] createNonNull(String from) {
			return from.toCharArray();
		}
	};

	public static final Factory<Boolean, String> TO_BOOLEAN = new Factory.Base<>() {
		@Override
		protected Boolean createNonNull(String from) {
			return Boolean.valueOf(from.trim());
		}
	};

	public static final Factory<Byte, String> TO_BYTE = new Factory.Base<>() {
		@Override
		protected Byte createNonNull(String from) {
			return Byte.valueOf(from.trim());
		}
	};

	public static final Factory<Short, String> TO_SHORT = new Factory.Base<>() {
		@Override
		protected Short createNonNull(String from) {
			return Short.valueOf(from.trim());
		}
	};

	public static final Factory<Integer, String> TO_INTEGER = new Factory.Base<>() {
		@Override
		protected Integer createNonNull(String from) {
			return Integer.valueOf(from.trim());
		}
	};

	public static final Factory<Long, String> TO_LONG = new Factory.Base<>() {
		@Override
		protected Long createNonNull(String from) {
			return Long.valueOf(from.trim());
		}
	};

	public static final Factory<Float, String> TO_FLOAT = new Factory.Base<>() {
		@Override
		protected Float createNonNull(String from) {
			return Float.valueOf(from.trim());
		}
	};

	public static final Factory<Double, String> TO_DOUBLE = new Factory.Base<>() {
		@Override
		protected Double createNonNull(String from) {
			return Double.valueOf(from.trim());
		}
	};

}
