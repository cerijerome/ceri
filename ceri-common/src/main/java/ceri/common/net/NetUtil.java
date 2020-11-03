package ceri.common.net;

import static ceri.common.collection.StreamUtil.first;
import static ceri.common.collection.StreamUtil.stream;
import static ceri.common.collection.StreamUtil.toList;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import ceri.common.exception.ExceptionAdapter;

public class NetUtil {
	private static final Pattern LOCALHOST_REGEX =
		Pattern.compile("(?i)(localhost|0*127(?:\\.[0-9]+){0,2}\\.[0-9]+|(?:0*\\:)*?:?0*1)");
	public static final String LOCALHOST = "localhost";
	public static final String LOCALHOST_IPV4 = "127.0.0.1"; // one of 127.0.0.0/8
	public static final String LOCALHOST_IPV6 = "::1"; // one of ::1/128

	private NetUtil() {}

	/**
	 * Returns the URI object for a URL, converting any syntax exception to unchecked.
	 */
	public static URI uri(URL url) {
		return ExceptionAdapter.ILLEGAL_ARGUMENT.get(url::toURI);
	}

	/**
	 * Checks if given address is a standard string representation of localhost. IPv4 must be in
	 * decimal dotted format, IPv6 in hex colon-separated format.
	 */
	public static boolean isLocalhost(String address) {
		if (address == null || address.isEmpty()) return false;
		return LOCALHOST_REGEX.matcher(address).matches();
	}

	public static InetAddress localAddress() throws SocketException {
		return findLocalAddress(InetAddress::isSiteLocalAddress);
	}

	public static InetAddress localAddressFor(NetworkInterface n) {
		return first(stream(n.getInetAddresses()).filter(InetAddress::isSiteLocalAddress));
	}

	public static NetworkInterface localInterface() throws SocketException {
		return first(stream(NetworkInterface.getNetworkInterfaces()) //
			.filter(n -> localAddressFor(n) != null));
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

}
