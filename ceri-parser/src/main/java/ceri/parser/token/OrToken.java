package ceri.parser.token;

import java.util.regex.Pattern;
import ceri.common.text.ToStringHelper;

public class OrToken extends Token.Base {
	static final Pattern PATTERN = Pattern.compile("(?i)^\\s+OR\\s+");
	public static final Token.Factory FACTORY = new Token.Factory() {
		@Override
		public Token create(String str, Index i) {
			return TokenUtil.matches(PATTERN, str, i) ? new OrToken() : null;
		}
	};

	public OrToken() {
		super(Type.Or);
	}

	@Override
	public String asString() {
		return " OR ";
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this).toString();
	}

}
