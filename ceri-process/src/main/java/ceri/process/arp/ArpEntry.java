package ceri.process.arp;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import ceri.common.text.RegexUtil;
import ceri.common.text.Strings;
import ceri.common.text.ToString;

/*
 ? (10.0.0.1) at 6a:ee:96:a9:67:6a on en0 ifscope [ethernet]
 ? (10.0.0.16) at d0:4d:2c:fb:2a:6e on en0 ifscope [ethernet]
 ? (10.0.0.138) at b8:27:eb:b0:9e:d9 on en0 ifscope [ethernet]
 ? (10.0.0.255) at ff:ff:ff:ff:ff:ff on en0 ifscope [ethernet]
 ? (224.0.0.251) at 1:0:5e:0:0:fb on en0 ifscope permanent [ethernet]
 ? (239.255.255.250) at 1:0:5e:7f:ff:fa on en0 ifscope permanent [ethernet]
 */
public class ArpEntry {
	public static final ArpEntry NULL = new ArpEntry(null, null, null);
	private static final Pattern IP_REGEX = Pattern.compile("\\((.*?)\\)");
	private static final Pattern MAC_REGEX = Pattern.compile(" at (\\S+)");
	private static final Pattern IFACE_REGEX = Pattern.compile(" on (\\S+)");
	private static final Pattern INCOMPLETE_REGEX = Pattern.compile("(?i)\\bincomplete\\b");
	public final String ip;
	public final String mac;
	public final String iface;

	public static List<ArpEntry> fromOuput(String output) {
		return Strings.lines(output).map(ArpEntry::fromLine).filter(a -> !a.isNull()).toList();
	}

	public static ArpEntry fromLine(String line) {
		String ip = RegexUtil.find(IP_REGEX, line);
		String mac = RegexUtil.find(MAC_REGEX, line);
		String iface = RegexUtil.find(IFACE_REGEX, line);
		return create(ip, mac, iface);
	}

	public static ArpEntry create(String ip, String mac, String iface) {
		if (ip == null && mac == null) return NULL;
		return new ArpEntry(ip, mac, iface);
	}

	private ArpEntry(String ip, String mac, String iface) {
		this.ip = ip;
		this.mac = mac;
		this.iface = iface;
	}

	public boolean isNull() {
		return ip == null && mac == null;
	}

	public boolean isMacIncomplete() {
		if (mac == null) return false;
		return INCOMPLETE_REGEX.matcher(mac).find();
	}

	@Override
	public int hashCode() {
		return Objects.hash(ip, mac, iface);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ArpEntry)) return false;
		ArpEntry other = (ArpEntry) obj;
		if (!Objects.equals(ip, other.ip)) return false;
		if (!Objects.equals(mac, other.mac)) return false;
		if (!Objects.equals(iface, other.iface)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, ip, mac, iface);
	}
}
