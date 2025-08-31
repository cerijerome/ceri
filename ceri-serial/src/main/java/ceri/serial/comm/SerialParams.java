package ceri.serial.comm;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import ceri.common.text.Strings;

public class SerialParams {
	public static final Pattern PARSE_REGEX = Pattern.compile("(\\d+)\\,\\s*([5678])\\,\\s*"
		+ "(1|1\\.0|1\\.5|2|2\\.0)\\,\\s*(?i)([noems]|none|odd|even|mark|space)");
	public static final SerialParams NULL = builder().baud(0).build();
	public static final SerialParams DEFAULT = builder().build();
	private static final int START_BITS = 1;
	private static final long MICROS_PER_SECOND = TimeUnit.SECONDS.toMicros(1);
	public final int baud;
	public final DataBits dataBits;
	public final StopBits stopBits;
	public final Parity parity;

	public static SerialParams from(String s) {
		var m = PARSE_REGEX.matcher(s);
		if (!m.matches()) throw new IllegalArgumentException("Invalid format: " + s);
		int i = 1;
		var b = builder();
		b.baud(Integer.parseInt(m.group(i++)));
		b.dataBits(DataBits.from(Integer.parseInt(m.group(i++))));
		b.stopBits(StopBits.fromBits(Double.parseDouble(m.group(i++))));
		b.parity(Parity.from(m.group(i).charAt(0)));
		return b.build();
	}

	public static SerialParams of(int baud) {
		return builder().baud(baud).build();
	}

	public static SerialParams of(int baud, DataBits dataBits, StopBits stopBits, Parity parity) {
		return builder().baud(baud).dataBits(dataBits).stopBits(stopBits).parity(parity).build();
	}

	public static class Builder {
		int baud = 9600;
		DataBits dataBits = DataBits._8;
		StopBits stopBits = StopBits._1;
		Parity parity = Parity.none;

		Builder() {}

		public Builder baud(int baud) {
			this.baud = baud;
			return this;
		}

		public Builder dataBits(DataBits dataBits) {
			this.dataBits = dataBits;
			return this;
		}

		public Builder stopBits(StopBits stopBits) {
			this.stopBits = stopBits;
			return this;
		}

		public Builder parity(Parity parity) {
			this.parity = parity;
			return this;
		}

		public SerialParams build() {
			return new SerialParams(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	SerialParams(Builder builder) {
		baud = builder.baud;
		dataBits = builder.dataBits;
		stopBits = builder.stopBits;
		parity = builder.parity;
	}

	public double microsPerBit() {
		return (double) MICROS_PER_SECOND / baud;
	}

	public double sendTimeMicros(int frames) {
		return microsPerFrame() * frames;
	}

	public double microsPerFrame() {
		return microsPerBit() * bitsPerFrame();
	}

	public int bitsPerFrame() {
		return START_BITS + dataBits.bits + stopBits.minBits() + parity.bits();
	}

	@Override
	public int hashCode() {
		return Objects.hash(baud, dataBits, stopBits, parity);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof SerialParams other)) return false;
		if (baud != other.baud) return false;
		if (!Objects.equals(dataBits, other.dataBits)) return false;
		if (!Objects.equals(stopBits, other.stopBits)) return false;
		if (!Objects.equals(parity, other.parity)) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%d,%d,%s,%s", baud, dataBits.bits, Strings.compact(stopBits.bits),
			parity);
	}
}
