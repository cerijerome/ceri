package ceri.misc.parser.token;

import java.util.regex.Pattern;
import ceri.common.text.ToString;

public class ExcludeToken extends Token.Base {
	static final Pattern PATTERN = Pattern.compile("^\\-");
	public static final Token.Factory FACTORY =
		(str, i) -> TokenUtil.matches(PATTERN, str, i) ? new ExcludeToken() : null;

	public ExcludeToken() {
		super(Type.Exclude);
	}

	@Override
	public String asString() {
		return "-";
	}

	@Override
	public String toString() {
		return ToString.forClass(this);
	}

}
