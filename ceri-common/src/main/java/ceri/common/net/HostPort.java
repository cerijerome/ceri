package ceri.common.net;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.RegexUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class HostPort {
	private static final Pattern HOST_REGEX = Pattern.compile("([^:]+)(?::(\\d+))?");
	public final String host;
	public final Integer port;

	public static HostPort parse(String value) {
		Matcher m = RegexUtil.matched(HOST_REGEX, value);
		if (m == null) return null;
		return of(m.group(1), RegexUtil.intGroup(m, 2));
	}

	public static HostPort of(String host) {
		return of(host, null);
	}

	public static HostPort of(String host, Integer port) {
		validateNotNull(host);
		return new HostPort(host, port);
	}

	private HostPort(String host, Integer port) {
		this.host = host;
		this.port = port;
	}

	public InetAddress asAddress() throws UnknownHostException {
		return InetAddress.getByName(host);
	}

	public int port(int def) {
		return port != null ? port : def;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(host, port);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof HostPort)) return false;
		HostPort other = (HostPort) obj;
		if (!EqualsUtil.equals(host, other.host)) return false;
		if (!EqualsUtil.equals(port, other.port)) return false;
		return true;
	}

	@Override
	public String toString() {
		return port == null ? host : host + ":" + port;
	}

}
