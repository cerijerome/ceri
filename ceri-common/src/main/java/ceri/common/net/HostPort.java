package ceri.common.net;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.RegexUtil;

public class HostPort {
	public static final int INVALID_PORT = -1;
	public static final HostPort NULL = new HostPort(null, INVALID_PORT);
	public static final HostPort LOCALHOST = new HostPort(NetUtil.LOCALHOST, INVALID_PORT);
	private static final Pattern HOST_REGEX = Pattern.compile("([^:]+)(?::(\\d+))?");
	public final String host;
	public final int port;

	public static HostPort parse(String value) {
		Matcher m = RegexUtil.matched(HOST_REGEX, value);
		if (m == null) return null;
		String host = m.group(1);
		String portGroup = m.group(2);
		int port = portGroup != null ? Integer.parseInt(portGroup) : INVALID_PORT;
		return of(host, port);
	}

	public static HostPort from(InetSocketAddress address) {
		return of(address.getHostString(), address.getPort());
	}

	public static HostPort localhost(int port) {
		return of(NetUtil.LOCALHOST, port);
	}

	public static HostPort of(String host) {
		return of(host, INVALID_PORT);
	}

	public static HostPort of(String host, int port) {
		validateNotNull(host);
		return new HostPort(host, port);
	}

	private HostPort(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public InetAddress asAddress() throws UnknownHostException {
		return InetAddress.getByName(host);
	}

	public InetSocketAddress asSocketAddress() throws UnknownHostException {
		return NetUtil.requireResolved(new InetSocketAddress(host, port(0)));
	}

	public int port(int def) {
		return port != INVALID_PORT ? port : def;
	}

	public boolean isNull() {
		return host == null;
	}

	public boolean hasPort() {
		return port != INVALID_PORT;
	}

	@Override
	public int hashCode() {
		return Objects.hash(host, port);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof HostPort)) return false;
		HostPort other = (HostPort) obj;
		if (!Objects.equals(host, other.host)) return false;
		if (!Objects.equals(port, other.port)) return false;
		return true;
	}

	@Override
	public String toString() {
		return port == INVALID_PORT ? host : host + ":" + port;
	}

}
