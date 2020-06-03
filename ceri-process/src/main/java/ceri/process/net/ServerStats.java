package ceri.process.net;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.date.DateUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Encapsulates the result of running 'net stats srv'. Example output:
 * 
 * <pre>
 * Server Statistics for \\Computer_Name
 * Statistics since 1/10/2020 12:01:58 AM
 * </pre>
 */
public class ServerStats {
	private static final Pattern COMPUTER_NAME_REGEX = Pattern.compile("for\\s+\\\\*(\\S+)");
	private static final Pattern SINCE_REGEX = Pattern.compile("since\\s+(.*)");
	private static final String YEAR_PATTERN = "[uuuu][uu]";
	private static final String TIME_PATTERN = " [h:mm[:ss][ ]a][H:mm[:ss]]";
	private static final DateTimeFormatter FORMATTER = formatter(Locale.getDefault());
	public final String computerName;
	public final ZonedDateTime since;

	public static ServerStats from(String output) {
		List<String> lines = StringUtil.lines(output);
		Builder b = builder();
		int i = 0;
		match(lines, i++, COMPUTER_NAME_REGEX, m -> b.computerName(m.group(1)));
		match(lines, i++, SINCE_REGEX, m -> b.since(LocalDateTime.parse(m.group(1), FORMATTER)));
		return b.build();
	}

	private static void match(List<String> lines, int i, Pattern p, Consumer<Matcher> consumer) {
		if (i >= lines.size()) return;
		Matcher m = RegexUtil.found(p, lines.get(i));
		if (m != null) consumer.accept(m);
	}

	public static class Builder {
		String computerName;
		ZonedDateTime since;

		Builder() {}

		public Builder computerName(String computerName) {
			this.computerName = computerName;
			return this;
		}

		public Builder since(LocalDateTime since) {
			return since(since.atZone(ZoneId.systemDefault()));
		}

		public Builder since(ZonedDateTime since) {
			this.since = since;
			return this;
		}

		public ServerStats build() {
			return new ServerStats(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	ServerStats(Builder builder) {
		computerName = builder.computerName;
		since = builder.since;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(computerName, since);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ServerStats)) return false;
		ServerStats other = (ServerStats) obj;
		if (!EqualsUtil.equals(computerName, other.computerName)) return false;
		if (!EqualsUtil.equals(since, other.since)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, computerName, since).toString();
	}

	/**
	 * Modifies short local date format, and appends time format. Keeps local day/month order.
	 */
	private static DateTimeFormatter formatter(Locale locale) {
		String datePattern = DateUtil.dateTimePattern(FormatStyle.SHORT, null, locale);
		datePattern = datePattern.replaceFirst("M+", "M");
		datePattern = datePattern.replaceFirst("d+", "d");
		datePattern = datePattern.replaceFirst("[yu]+", YEAR_PATTERN);
		return new DateTimeFormatterBuilder().parseCaseInsensitive()
			.appendPattern(datePattern + TIME_PATTERN).toFormatter(locale);
	}

}
