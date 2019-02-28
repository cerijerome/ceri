package ceri.common.net;

import static ceri.common.collection.StreamUtil.first;
import static ceri.common.collection.StreamUtil.stream;
import static ceri.common.collection.StreamUtil.toList;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NetUtil {
	private static final Pattern LOCALHOST_REGEX = Pattern.compile(
		"(?i)(localhost|0*127(?:\\.[0-9]+){0,2}\\.[0-9]+|(?:0*\\:)*?:?0*1)");
	public static final String LOCALHOST = "localhost";
	public static final String LOCALHOST_IPV4 = "127.0.0.1"; // one of 127.0.0.0/8
	public static final String LOCALHOST_IPV6 = "::1"; // one of ::1/128
	
	private NetUtil() {}

	/**
	 * Checks if given address is a standard string representation of localhost.
	 * IPv4 must be in decimal dotted format, IPv6 in hex colon-separated format.
	 */
	public static boolean isLocalhost(String address) {
		if (address == null || address.isEmpty()) return false;
		return LOCALHOST_REGEX.matcher(address).matches();
	}
	
	public static InetAddress regularAddress() throws SocketException {
		return findLocalAddress(NetUtil::isRegularAddress);
	}

	public static InetAddress regularAddressFor(NetworkInterface n) {
		return first(stream(n.getInetAddresses()).filter(NetUtil::isRegularAddress));
	}

	public static NetworkInterface regularInterface() throws SocketException {
		return first(stream(NetworkInterface.getNetworkInterfaces()) //
			.filter(n -> regularAddressFor(n) != null));
	}

	public static InetAddress findLocalAddress(Predicate<? super InetAddress> predicate)
		throws SocketException {
		return first(localAddressStream().filter(predicate));
	}

	public static List<InetAddress> localAddresses() throws SocketException {
		return toList(localAddressStream());
	}

	private static Stream<InetAddress> localAddressStream() throws SocketException {
		return stream(NetworkInterface.getNetworkInterfaces())
			.flatMap(n -> stream(n.getInetAddresses()));
	}

	public static boolean isRegularAddress(InetAddress i) {
		if (!i.isSiteLocalAddress()) return false;
		if (i.isAnyLocalAddress()) return false;
		if (i.isLinkLocalAddress()) return false;
		if (i.isLoopbackAddress()) return false;
		if (i.isMulticastAddress()) return false;
		return true;
	}

}
