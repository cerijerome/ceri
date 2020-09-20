package ceri.misc.parser.token;

import java.util.regex.Pattern;
import ceri.common.text.ToString;

public class OrToken extends Token.Base {
	static final Pattern PATTERN = Pattern.compile("(?i)^\\s+OR\\s+");
	public static final Token.Factory FACTORY =
		(str, i) -> TokenUtil.matches(PATTERN, str, i) ? new OrToken() : null;

	public OrToken() {
		super(Type.Or);
	}

	@Override
	public String asString() {
		return " OR ";
	}

	@Override
	public String toString() {
		return ToString.forClass(this);
	}

}
