package ceri.misc.parser.token;

import java.util.regex.Pattern;
import ceri.common.text.ToStringHelper;

public class WildcardToken extends Token.Base {
	static final Pattern PATTERN = Pattern.compile("^\\*");
	public static final Token.Factory FACTORY = new Token.Factory() {
		@Override
		public Token create(String str, Index i) {
			return TokenUtil.matches(PATTERN, str, i) ? new WildcardToken() : null;
		}
	};

	public WildcardToken() {
		super(Type.Wildcard);
	}

	@Override
	public String asString() {
		return "*";
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this).toString();
	}

}
