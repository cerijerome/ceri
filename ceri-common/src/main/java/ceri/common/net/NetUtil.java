package ceri.common.net;

import static ceri.common.collection.StreamUtil.first;
import static ceri.common.collection.StreamUtil.stream;
import static ceri.common.collection.StreamUtil.toList;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
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
	 * Checks an internet address is resolved. Throws UnknownHostException if not.
	 */
	public static InetSocketAddress requireResolved(InetSocketAddress address)
		throws UnknownHostException {
		if (!address.isUnresolved()) return address;
		throw new UnknownHostException("Unable to resolve host: " + address.getHostString());
	}

	/**
	 * Checks if given address is a standard string representation of localhost. IPv4 must be in
	 * decimal dotted format, IPv6 in hex colon-separated format.
	 */
	public static boolean isLocalhost(String address) {
		if (address == null || address.isEmpty()) return false;
		return LOCALHOST_REGEX.matcher(address).matches();
	}

	/**
	 * Iterates network interfaces to find the first local address.
	 */
	public static InetAddress localAddress() throws SocketException {
		return findLocalAddress(InetAddress::isSiteLocalAddress);
	}

	/**
	 * Finds the first local address for the interface.
	 */
	public static InetAddress localAddressFor(NetworkInterface n) {
		return first(stream(n.getInetAddresses()).filter(InetAddress::isSiteLocalAddress));
	}

	/**
	 * Iterates network interfaces to find the first with a local address.
	 */
	public static NetworkInterface localInterface() throws SocketException {
		return first(stream(NetworkInterface.getNetworkInterfaces()) //
			.filter(n -> localAddressFor(n) != null));
	}

	/**
	 * Iterates network interfaces to find the first address matching the predicate.
	 */
	public static InetAddress findLocalAddress(Predicate<? super InetAddress> predicate)
		throws SocketException {
		return first(localAddressStream().filter(predicate));
	}

	/**
	 * Lists all network interface addresses.
	 */
	public static List<InetAddress> localAddresses() throws SocketException {
		return toList(localAddressStream());
	}

	/**
	 * Iterates network interfaces to find the first IP4 broadcast address. 
	 */
	public static InetAddress localBroadcast() throws SocketException {
		return broadcast(ifAddressStream());
	}

	/**
	 * Iterates interface addresses to find the first IP4 broadcast address. 
	 */
	public static InetAddress broadcast(NetworkInterface iface) {
		if (iface == null) return null;
		return broadcast(iface.getInterfaceAddresses().stream());
	}

	private static InetAddress broadcast(Stream<InterfaceAddress> stream) {
		return first(stream.map(InterfaceAddress::getBroadcast).filter(Objects::nonNull));
	}

	private static Stream<InetAddress> localAddressStream() throws SocketException {
		return NetworkInterface.networkInterfaces().flatMap(NetworkInterface::inetAddresses);
	}

	private static Stream<InterfaceAddress> ifAddressStream() throws SocketException {
		return NetworkInterface.networkInterfaces()
			.flatMap(n -> n.getInterfaceAddresses().stream());
	}

}
