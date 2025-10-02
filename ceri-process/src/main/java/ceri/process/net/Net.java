package ceri.process.net;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collect.Lists;
import ceri.common.function.Functions;
import ceri.common.process.Output;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;
import ceri.common.text.Regex;
import ceri.common.time.Dates;

/**
 * Windows command 'net'.
 */
public class Net {
	private static final Pattern COMPUTER_NAME_REGEX = Pattern.compile("for\\s+\\\\*(\\S+)");
	private static final Pattern SINCE_REGEX = Pattern.compile("since\\s+(.*)");
	private static final String YEAR_PATTERN = "[uuuu][uu]";
	private static final String TIME_PATTERN = " [h:mm[:ss][ ]a][H:mm[:ss]]";
	private static final DateTimeFormatter FORMATTER = formatter(Locale.getDefault());
	private static final String NET = "net";
	public final Stats stats;

	public static Net of() {
		return of(Processor.DEFAULT);
	}

	public static Net of(Processor processor) {
		return new Net(processor);
	}

	private Net(Processor processor) {
		stats = new Stats(processor);
	}

	public static class Stats {
		private static final String STATS = "stats";
		private static final String SERVER = "srv";
		private final Processor processor;

		/**
		 * Encapsulates the result of running 'net stats srv'. Example output:
		 *
		 * <pre>
		 * Server Statistics for \\Computer_Name
		 * Statistics since 1/10/2020 12:01:58 AM
		 * </pre>
		 */
		public record Server(String computerName, ZonedDateTime since) {}

		Stats(Processor processor) {
			this.processor = processor;
		}

		public Output<Server> server() throws IOException {
			return Output.of(exec(Parameters.of(SERVER)), Stats::serverFrom);
		}

		private String exec(Parameters params) throws IOException {
			return processor.exec(Parameters.of(NET, STATS).addAll(params));
		}

		private static Server serverFrom(String output) {
			var lines = Regex.Split.LINE.list(output);
			int i = 0;
			var computerName = match(lines, i++, COMPUTER_NAME_REGEX, m -> m.group(1));
			var since = match(lines, i++, SINCE_REGEX,
				m -> LocalDateTime.parse(m.group(1), FORMATTER).atZone(ZoneId.systemDefault()));
			return new Server(computerName, since);
		}

		private static <T> T match(List<String> lines, int i, Pattern p,
			Functions.Function<Matcher, T> function) {
			return Regex.findApply(p, Lists.at(lines, i), function, null);
		}
	}

	private static DateTimeFormatter formatter(Locale locale) {
		// Modifies short local date format, and appends time format. Keeps local day/month order
		var datePattern = Dates.dateTimePattern(FormatStyle.SHORT, null, locale);
		datePattern = datePattern.replaceFirst("M+", "M");
		datePattern = datePattern.replaceFirst("d+", "d");
		datePattern = datePattern.replaceFirst("[yu]+", YEAR_PATTERN);
		return new DateTimeFormatterBuilder().parseCaseInsensitive()
			.appendPattern(datePattern + TIME_PATTERN).toFormatter(locale);
	}
}
