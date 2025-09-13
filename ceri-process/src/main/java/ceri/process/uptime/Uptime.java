package ceri.process.uptime;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import ceri.common.process.Output;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;
import ceri.common.text.Numbers;
import ceri.common.text.Regex;
import ceri.common.time.DateUtil;
import ceri.common.util.OsUtil;
import ceri.process.net.Net;

/**
 * Linux/OSX command 'uptime'.
 */
public class Uptime {
	private static final String UPTIME = "uptime";
	private static final Pattern REGEX =
		Pattern.compile("up\\s+(?:(\\d+)\\s+days?,\\s+)?(?:(\\d+):(\\d++))?(?:(\\d+)\\s+min)?");
	private final Processor processor;

	/**
	 * Determine system uptime in milliseconds. Calls 'uptime' for Linux/OSX, and 'net stats srv'
	 * for Windows. If process call fails, JVM uptime is used.
	 */
	public static long systemUptimeMs() {
		return systemUptimeMs(Processor.DEFAULT);
	}

	/**
	 * Determine system uptime in milliseconds. Calls 'uptime' for Linux/OSX, and 'net stats srv'
	 * for Windows. If process call fails, JVM uptime is used.
	 */
	public static long systemUptimeMs(Processor processor) {
		try {
			var os = OsUtil.os();
			if (os.linux || os.mac) return of(processor).uptimeMs().parse();
			long startTime =
				Net.of(processor).stats.server().parse().since.toInstant().toEpochMilli();
			return System.currentTimeMillis() - startTime;
		} catch (IOException | RuntimeException e) {
			// Uptime not available
		}
		return DateUtil.jvmUptimeMs(); // Use JVM uptime by default
	}

	public static Uptime of() {
		return of(Processor.DEFAULT);
	}

	public static Uptime of(Processor processor) {
		return new Uptime(processor);
	}

	private Uptime(Processor processor) {
		this.processor = processor;
	}

	public Output<Long> uptimeMs() throws IOException {
		return Output.of(exec(), Uptime::extractMs);
	}

	private String exec() throws IOException {
		return processor.exec(Parameters.of(UPTIME));
	}

	private static long extractMs(String output) {
		var m = Regex.findValid(REGEX, output, "output");
		int i = 1;
		int days = Numbers.Parse.toInt(m.group(i++), 0);
		int hours = Numbers.Parse.toInt(m.group(i++), 0);
		int minutes = Numbers.Parse.toInt(m.group(i++), 0);
		minutes += Numbers.Parse.toInt(m.group(i++), 0);
		return TimeUnit.DAYS.toMillis(days) + TimeUnit.HOURS.toMillis(hours)
			+ TimeUnit.MINUTES.toMillis(minutes);
	}
}
