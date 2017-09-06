package ceri.misc.parser.token;

import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

public class UnknownToken extends Token.Base {
	public static final Token.Factory FACTORY = new Token.Factory() {
		@Override
		public Token create(String str, Index i) {
			char ch = str.charAt(i.value());
			i.inc();
			return new UnknownToken(ch);
		}
	};
	private final int hashCode;
	public final char ch;

	public UnknownToken(char ch) {
		super(Type.Unknown);
		this.ch = ch;
		hashCode = HashCoder.hash(type(), ch);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof UnknownToken)) return false;
		UnknownToken token = (UnknownToken) obj;
		return ch == token.ch;
	}
	
	@Override
	public String asString() {
		return "?";
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, ch).toString();
	}

}
