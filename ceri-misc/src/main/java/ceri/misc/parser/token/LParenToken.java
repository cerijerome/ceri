package ceri.misc.parser.token;

import java.util.regex.Pattern;
import ceri.common.text.ToStringHelper;

public class LParenToken extends Token.Base {
	static final Pattern PATTERN = Pattern.compile("^\\(\\s*");
	public static final Token.Factory FACTORY =
		(str, i) -> TokenUtil.matches(PATTERN, str, i) ? new LParenToken() : null;

	public LParenToken() {
		super(Type.LParen);
	}
	
	@Override
	public String asString() {
		return "(";
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this).toString();
	}

}
