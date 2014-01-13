package ceri.parser.token;

import java.util.regex.Pattern;
import ceri.common.util.ToStringHelper;

public class ExcludeToken extends Token.Base {
	static final Pattern PATTERN = Pattern.compile("^\\-");
	public static final Token.Factory FACTORY = new Token.Factory() {
		@Override
		public Token create(String str, Index i) {
			return TokenUtil.matches(PATTERN, str, i) ? new ExcludeToken() : null;
		}
	};

	public ExcludeToken() {
		super(Type.Exclude);
	}
	
	@Override
	public String asString() {
		return "-";
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this).toString();
	}

}
