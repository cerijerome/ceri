package ceri.serial.ftdi;

import static ceri.common.time.Dates.microsExact;
import java.time.Duration;
import java.time.Instant;
import ceri.serial.ftdi.jna.LibFtdiStream.FTDIProgressInfo;

/**
 * Read-only accessor for async transfer progress.
 */
public class FtdiProgressInfo {
	private final FTDIProgressInfo info;

	static FtdiProgressInfo of(FTDIProgressInfo info) {
		if (info == null) return null;
		return new FtdiProgressInfo(info);
	}

	private FtdiProgressInfo(FTDIProgressInfo info) {
		this.info = info;
	}

	public double totalTimeSec() {
		return info.totalTime;
	}

	public double totalRate() {
		return info.totalRate;
	}

	public double currentRate() {
		return info.currentRate;
	}

	public long currentTotalBytes() {
		return info.current.totalBytes;
	}

	public Instant currentTime() {
		return info.current.time;
	}

	public long previousTotalBytes() {
		return info.prev.totalBytes;
	}

	public Instant previousTime() {
		return info.prev.time;
	}

	public long firstTotalBytes() {
		return info.first.totalBytes;
	}

	public Instant firstTime() {
		return info.first.time;
	}

	@Override
	public String toString() {
		long previousMicros = microsExact(Duration.between(firstTime(), previousTime()));
		long currentMicros = microsExact(Duration.between(firstTime(), currentTime()));
		return String.format(
			"progress=(total[%.3fs@%.0fkB/s], current[%dB/%dus@%.0fkB/s], " +
				"previous[%dB/%dus], first[%dB])",
			totalTimeSec(), totalRate() / 1000, currentTotalBytes(), currentMicros,
			currentRate() / 1000, previousTotalBytes(), previousMicros, firstTotalBytes());
	}

}
