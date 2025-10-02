package ceri.process.arp;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import ceri.common.process.Output;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;
import ceri.common.text.Regex;

public class Arp {
	private static final String ARP = "arp";
	private final Processor processor;

	/**
	 * Arp entry. Examples:
	 * <pre>
	 * ? (10.0.0.1) at 6a:ee:96:a9:67:6a on en0 ifscope [ethernet]  
	 * ? (10.0.0.16) at d0:4d:2c:fb:2a:6e on en0 ifscope [ethernet]
	 * ? (10.0.0.138) at b8:27:eb:b0:9e:d9 on en0 ifscope [ethernet]
	 * ? (10.0.0.255) at ff:ff:ff:ff:ff:ff on en0 ifscope [ethernet]
	 * ? (224.0.0.251) at 1:0:5e:0:0:fb on en0 ifscope permanent [ethernet]
	 * ? (239.255.255.250) at 1:0:5e:7f:ff:fa on en0 ifscope permanent [ethernet]
	 * </pre>
	 */
	public record Entry(String ip, String mac, String iface) {

		public static final Entry NULL = new Entry(null, null, null);
		private static final Pattern IP_REGEX = Pattern.compile("\\((.*?)\\)");
		private static final Pattern MAC_REGEX = Pattern.compile(" at (\\S+)");
		private static final Pattern IFACE_REGEX = Pattern.compile(" on (\\S+)");
		private static final Pattern INCOMPLETE_REGEX = Pattern.compile("(?i)\\bincomplete\\b");

		public static List<Entry> fromOutput(String output) {
			return Regex.Split.LINE.stream(output).map(Entry::fromLine).filter(Entry::nonNull)
				.toList();
		}

		public static Entry fromLine(String line) {
			var ip = Regex.findGroup(IP_REGEX, line, 1);
			var mac = Regex.findGroup(MAC_REGEX, line, 1);
			var iface = Regex.findGroup(IFACE_REGEX, line, 1);
			return of(ip, mac, iface);
		}

		public static Entry of(String ip, String mac, String iface) {
			if (ip == null && mac == null) return NULL;
			return new Entry(ip, mac, iface);
		}

		public boolean nonNull() {
			return ip != null || mac != null;
		}

		public boolean isMacIncomplete() {
			return Regex.match(INCOMPLETE_REGEX, mac).find();
		}
	}

	public static Arp of() {
		return of(Processor.DEFAULT);
	}

	public static Arp of(Processor processor) {
		return new Arp(processor);
	}

	private Arp(Processor processor) {
		this.processor = processor;
	}

	public Output<List<Entry>> all() throws IOException {
		return Output.of(exec(Parameters.of("-a")), Entry::fromOutput);
	}

	private String exec(Parameters params) throws IOException {
		return processor.exec(Parameters.of(ARP).addAll(params));
	}
}
