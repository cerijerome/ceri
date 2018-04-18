package ceri.process.scutil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;

/**
 * Wrapper for the results of {@code scutil --nc statistics <service-name>}:
 * 
 * <pre>
 * 	{@code <dictionary>} {
 * 		PPP : {@code <dictionary>} {
 * 			BytesIn : 20337
 * 			BytesOut : 16517
 * 			ErrorsIn : 0
 * 			ErrorsOut : 0
 * 			PacketsIn : 77
 * 			PacketsOut : 118
 * 		}
 * 	}
 * </pre>
 */
public class NcStatistics {
	private static final Pattern FIELD_REGEX = Pattern.compile("(\\w+)\\s*:\\s*(\\d+)");
	public final int bytesIn;
	public final int bytesOut;
	public final int errorsIn;
	public final int errorsOut;
	public final int packetsIn;
	public final int packetsOut;

	public static NcStatistics from(String output) {
		Matcher m = FIELD_REGEX.matcher(output);
		Builder b = builder();
		while (m.find())
			setValue(b, m.group(1), Integer.parseInt(m.group(2)));
		return b.build();
	}

	private static Builder setValue(Builder b, String name, int value) {
		switch (name) {
		case "BytesIn":
			return b.bytesIn(value);
		case "BytesOut":
			return b.bytesOut(value);
		case "ErrorsIn":
			return b.errorsIn(value);
		case "ErrorsOut":
			return b.errorsOut(value);
		case "PacketsIn":
			return b.packetsIn(value);
		case "PacketsOut":
			return b.packetsOut(value);
		}
		return b;
	}

	public static class Builder {
		int bytesIn;
		int bytesOut;
		int errorsIn;
		int errorsOut;
		int packetsIn;
		int packetsOut;

		Builder() {}

		public Builder bytesIn(int bytesIn) {
			this.bytesIn = bytesIn;
			return this;
		}

		public Builder bytesOut(int bytesOut) {
			this.bytesOut = bytesOut;
			return this;
		}

		public Builder errorsIn(int errorsIn) {
			this.errorsIn = errorsIn;
			return this;
		}

		public Builder errorsOut(int errorsOut) {
			this.errorsOut = errorsOut;
			return this;
		}

		public Builder packetsIn(int packetsIn) {
			this.packetsIn = packetsIn;
			return this;
		}

		public Builder packetsOut(int packetsOut) {
			this.packetsOut = packetsOut;
			return this;
		}

		public NcStatistics build() {
			return new NcStatistics(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	NcStatistics(Builder builder) {
		bytesIn = builder.bytesIn;
		bytesOut = builder.bytesOut;
		errorsIn = builder.errorsIn;
		errorsOut = builder.errorsOut;
		packetsIn = builder.packetsIn;
		packetsOut = builder.packetsOut;
	}

	public double packetErrorRateIn() {
		if (packetsIn == 0) return 0.0;
		return (double) errorsIn / packetsIn;
	}

	public double packetErrorRateOut() {
		if (packetsOut == 0) return 0.0;
		return (double) errorsOut / packetsOut;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(bytesIn, bytesOut, errorsIn, errorsOut, packetsIn, packetsOut);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof NcStatistics)) return false;
		NcStatistics other = (NcStatistics) obj;
		if (bytesIn != other.bytesIn) return false;
		if (bytesOut != other.bytesOut) return false;
		if (errorsIn != other.errorsIn) return false;
		if (errorsOut != other.errorsOut) return false;
		if (packetsIn != other.packetsIn) return false;
		if (packetsOut != other.packetsOut) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, bytesIn, bytesOut, errorsIn, errorsOut,
			packetsIn, packetsOut).toString();
	}

}
