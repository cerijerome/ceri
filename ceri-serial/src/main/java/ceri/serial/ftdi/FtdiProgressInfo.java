package ceri.serial.ftdi;

import java.util.concurrent.TimeUnit;
import ceri.serial.ftdi.jna.LibFtdi.size_and_time;
import ceri.serial.ftdi.jna.LibFtdiStream.ftdi_progress_info;

public class FtdiProgressInfo {
	private final ftdi_progress_info info;
	public final SizeAndTime first;
	public final SizeAndTime previous;
	public final SizeAndTime current;

	public static void main(String[] args) {
		var info = new ftdi_progress_info();
		info.first.totalBytes = 256;
		info.first.time.tv_usec.setValue(1234);
		info.prev.totalBytes = 64;
		info.prev.time.tv_usec.setValue(345);
		info.current.totalBytes = 128;
		info.current.time.tv_usec.setValue(567);
		info.totalTime = 0.013;
		info.totalRate = 234567;
		info.currentRate = 324675;
		var prog = of(info);
		System.out.println(prog);
	}

	public static class SizeAndTime {
		private final size_and_time sizeAndTime;

		private SizeAndTime(size_and_time sizeAndTime) {
			this.sizeAndTime = sizeAndTime;
		}

		public long totalBytes() {
			return sizeAndTime.totalBytes;
		}

		public long timeMicros() {
			return TimeUnit.SECONDS.toMicros(sizeAndTime.time.tv_sec.longValue()) +
				sizeAndTime.time.tv_usec.longValue();
		}

		@Override
		public String toString() {
			return String.format("%dB/%dus", totalBytes(), timeMicros());
		}
	}

	static FtdiProgressInfo of(ftdi_progress_info info) {
		return new FtdiProgressInfo(info);
	}

	private FtdiProgressInfo(ftdi_progress_info info) {
		this.info = info;
		first = new SizeAndTime(info.first);
		previous = new SizeAndTime(info.prev);
		current = new SizeAndTime(info.current);
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

	@Override
	public String toString() {
		return String.format(
			"progress=(total[%.3fs@%.0fkB/s], current[%s@%.0fkB/s], previous[%s], first[%s])",
			totalTimeSec(), totalRate() / 1000, current, currentRate() / 1000, previous, first);
	}
}
