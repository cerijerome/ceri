package ceri.misc.parser.token;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;

public class KeywordToken extends Token.Base {
	static final Pattern PATTERN = Pattern.compile("^[^\"\\s\\*\\(\\)]+");
	public static final Token.Factory FACTORY = (str, i) -> {
		Matcher m = TokenUtil.matcher(PATTERN, str, i);
		if (m == null) return null;
		return new KeywordToken(m.group());
	};
	public final String value;

	public KeywordToken(String value) {
		super(Type.Keyword);
		this.value = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type(), value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof KeywordToken)) return false;
		KeywordToken token = (KeywordToken) obj;
		return EqualsUtil.equals(value, token.value);
	}

	@Override
	public String asString() {
		return value;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, value).toString();
	}

}
