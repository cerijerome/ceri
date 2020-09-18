package ceri.misc.parser.token;

import java.util.Objects;
import ceri.common.text.ToStringHelper;

public class UnknownToken extends Token.Base {
	public static final Token.Factory FACTORY = (str, i) -> {
		char ch = str.charAt(i.value());
		i.inc();
		return new UnknownToken(ch);
	};
	public final char ch;

	public UnknownToken(char ch) {
		super(Type.Unknown);
		this.ch = ch;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type(), ch);
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
