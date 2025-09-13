package ceri.process.scutil;

import java.util.Map;
import java.util.Objects;
import ceri.common.collection.Maps;
import ceri.common.collection.Node;

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
	public static final NcStatistics NULL = builder().build();
	private static final String BYTES_IN = "BytesIn";
	private static final String BYTES_OUT = "BytesOut";
	private static final String ERRORS_IN = "ErrorsIn";
	private static final String ERRORS_OUT = "ErrorsOut";
	private static final String PACKETS_IN = "PacketsIn";
	private static final String PACKETS_OUT = "PacketsOut";
	private final Map<String, Integer> values;

	public static NcStatistics from(String output) {
		Builder b = builder();
		Node<?> node = Parser.parse(output).child(0, 0);
		node.namedChildren().forEach((name, n) -> n.parse().asInt().accept(i -> b.add(name, i)));
		return b.build();
	}

	public static class Builder {
		final Map<String, Integer> values = Maps.link();

		Builder() {}

		public Builder add(String key, Integer value) {
			values.put(key, value);
			return this;
		}

		public Builder add(Map<String, Integer> values) {
			this.values.putAll(values);
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
		values = Map.copyOf(builder.values);
	}

	public int value(String name, int def) {
		return values.getOrDefault(name, def);
	}

	public int bytesIn() {
		return value(BYTES_IN, 0);
	}

	public int bytesOut() {
		return value(BYTES_OUT, 0);
	}

	public int errorsIn() {
		return value(ERRORS_IN, 0);
	}

	public int errorsOut() {
		return value(ERRORS_OUT, 0);
	}

	public int packetsIn() {
		return value(PACKETS_IN, 0);
	}

	public int packetsOut() {
		return value(PACKETS_OUT, 0);
	}

	public double packetErrorRateIn() {
		int packetsIn = packetsIn();
		if (packetsIn == 0) return 0.0;
		return (double) errorsIn() / packetsIn;
	}

	public double packetErrorRateOut() {
		int packetsOut = packetsOut();
		if (packetsOut == 0) return 0.0;
		return (double) errorsOut() / packetsOut;
	}

	@Override
	public int hashCode() {
		return Objects.hash(values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof NcStatistics other)) return false;
		return Objects.equals(values, other.values);
	}

	@Override
	public String toString() {
		return String.valueOf(values);
	}
}
