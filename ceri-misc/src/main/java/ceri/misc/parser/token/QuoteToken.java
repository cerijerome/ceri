package ceri.misc.parser.token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class QuoteToken extends Token.Base {
	static final Pattern PATTERN = Pattern.compile("^\"([^\"]*)\"");
	public static final Token.Factory FACTORY = new Token.Factory() {
		@Override
		public Token create(String str, Index i) {
			Matcher m = TokenUtil.matcher(PATTERN, str, i);
			if (m == null) return null;
			return new QuoteToken(m.group(1));
		}
	};
	private final int hashCode;
	public final String value;
	
	public QuoteToken(String value) {
		super(Type.Quote);
		this.value = value;
		hashCode = HashCoder.hash(type(), value);
	}
	
	@Override
	public int hashCode() {
		return hashCode;
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
