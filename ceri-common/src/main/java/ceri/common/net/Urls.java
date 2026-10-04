package ceri.common.net;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import ceri.common.collect.Lists;
import ceri.common.except.ExceptionAdapter;
import ceri.common.text.Strings;

public class Urls {
	/** RFC822: http://www.faqs.org/rfcs/rfc822.html */
	private static final Pattern EMAIL_PATTERN = Pattern.compile(
		"^(?:[A-Za-z0-9-]+[_.+-]+)*[\\w-]+@(?:\\w+(?:\\-+|\\.))*\\w{1,63}\\.[a-zA-Z]{2,6}$");

	private Urls() {}

	/**
	 * A simple URI builder.
	 */
	public static class Builder {
		private static final String FORM_ENCODED_SPACE = "+";
		private static final String ENCODED_SPACE = "%20";
		private final String base;
		private final List<Param> params = Lists.of();
		private Charset charset = StandardCharsets.UTF_8;
		private boolean form = true;

		private record Param(String name, String value) {}

		private Builder(String base) {
			this.base = Strings.safe(base);
		}

		/**
		 * Changes the charset for subsequent parameters.
		 */
		public Builder charset(Charset charset) {
			if (charset != null) this.charset = charset;
			return this;
		}

		/**
		 * Determines space encoding; {@code +} with forms enabled, otherwise {@code %20}.
		 */
		public Builder form(boolean form) {
			this.form = form;
			return this;
		}

		/**
		 * Adds a query parameter. Spaces are encoded as {@code +} with forms enabled, otherwise
		 * {@code %20}.
		 */
		public Builder param(String name, Object value) {
			return addParam(encode(name), encode(value));
		}

		/**
		 * Builds the URI as a string. Does not perform URI validation.
		 */
		public String string() {
			if (params.isEmpty()) return base;
			var b = new StringBuilder(base).append('?');
			for (int i = 0; i < params.size(); i++) {
				if (i > 0) b.append('&');
				var param = params.get(i);
				b.append(param.name());
				if (param.value() != null) b.append('=').append(param.value());
			}
			return b.toString();
		}

		/**
		 * Builds the URI.
		 */
		public URI uri() {
			return URI.create(string());
		}

		/**
		 * Builds the URL.
		 */
		public URL url() {
			return Urls.url(string());
		}

		private Builder addParam(String encodedName, String encodedValue) {
			if (encodedName != null) params.add(new Param(encodedName, encodedValue));
			return this;
		}

		private String encode(Object value) {
			if (value == null) return null;
			var encoded = URLEncoder.encode(value.toString(), charset);
			if (!form) encoded = encoded.replace(FORM_ENCODED_SPACE, ENCODED_SPACE);
			return encoded;
		}
	}

	/**
	 * Starts building a URI.
	 */
	public static Builder builder(String base) {
		return new Builder(base);
	}

	/**
	 * Creates a URL object from a string, converting any syntax exception to unchecked.
	 */
	public static URL url(String url) {
		return ExceptionAdapter.illegalArg.get(() -> new URI(url).toURL());
	}

	/**
	 * Returns the URI object for a URL, converting any syntax exception to unchecked.
	 */
	public static URI uri(URL url) {
		return ExceptionAdapter.illegalArg.get(url::toURI);
	}

	/**
	 * Uses URLEncoder with UTF8 encoding. Throws IllegalArgumentException for encoding issues.
	 */
	public static String encode(String s) {
		if (Strings.isEmpty(s)) return "";
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}

	/**
	 * Uses URLDecoder with UTF8 encoding. Throws IllegalArgumentException for encoding issues.
	 */
	public static String decode(String s) {
		if (Strings.isEmpty(s)) return "";
		return URLDecoder.decode(s, StandardCharsets.UTF_8);
	}

	/**
	 * Returns true if the string matches the email address pattern.
	 */
	public static boolean isEmail(String emailAddress) {
		return EMAIL_PATTERN.matcher(emailAddress).matches();
	}
}
