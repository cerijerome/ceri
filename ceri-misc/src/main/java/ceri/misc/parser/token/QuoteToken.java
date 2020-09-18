package ceri.misc.parser.token;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;

public class QuoteToken extends Token.Base {
	static final Pattern PATTERN = Pattern.compile("^\"([^\"]*)\"");
	public static final Token.Factory FACTORY = (str, i) -> {
		Matcher m = TokenUtil.matcher(PATTERN, str, i);
		if (m == null) return null;
		return new QuoteToken(m.group(1));
	};
	public final String value;

	public QuoteToken(String value) {
		super(Type.Quote);
		this.value = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type(), value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof QuoteToken)) return false;
		QuoteToken token = (QuoteToken) obj;
		return EqualsUtil.equals(value, token.value);
	}

	@Override
	public String asString() {
		return "\"" + value + "\"";
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, "\"" + value + "\"").toString();
	}

}
