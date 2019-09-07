package ceri.misc.parser.token;

import java.util.regex.Pattern;
import ceri.common.text.ToStringHelper;

public class AndToken extends Token.Base {
	static final Pattern PATTERN = Pattern.compile("^\\s+");
	public static final Token.Factory FACTORY =
		(str, i) -> TokenUtil.matches(PATTERN, str, i) ? new AndToken() : null;

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
