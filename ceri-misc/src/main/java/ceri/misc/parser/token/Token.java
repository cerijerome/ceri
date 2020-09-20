package ceri.misc.parser.token;

public interface Token {
	Type type();

	String asString();

	abstract class Base implements Token {
		private final Type type;

		protected Base(Type type) {
			this.type = type;
		}

		@Override
		public Type type() {
			return type;
		}

		@Override
		public int hashCode() {
			return type.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Base)) return false;
			Base base = (Base) obj;
			return type == base.type;
		}
	}

	interface Factory {
		Token create(String str, Index i);
	}

}
