package ceri.parser.token;

import java.util.regex.Pattern;
import ceri.common.text.ToStringHelper;

public class AndToken extends Token.Base {
	static final Pattern PATTERN = Pattern.compile("^\\s+");
	public static final Token.Factory FACTORY = new Token.Factory() {
		@Override
		public Token create(String str, Index i) {
			return TokenUtil.matches(PATTERN, str, i) ? new AndToken() : null;
		}
	};

	public AndToken() {
		super(Type.And);
	}
	
	@Override
	public String asString() {
		return " ";
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this).toString();
	}

}
