package ceri.common.net;

import java.net.Inet4Address;
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
import java.util.regex.Pattern;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Functions;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;

public class NetUtil {
	private static final Pattern LOCALHOST_REGEX =
		Pattern.compile("(?i)(localhost|0*127(?:\\.[0-9]+){0,2}\\.[0-9]+|(?:0*\\:)*?:?0*1)");
	public static final String LOCALHOST = "localhost";
	public static final String LOCALHOST_IPV4 = "127.0.0.1"; // one of 127.0.0.0/8
	public static final String LOCALHOST_IPV6 = "::1"; // one of ::1/128

	private NetUtil() {}

	/**
	 * Creates a URL object from a string, converting any syntax exception to unchecked.
	 */
	public static URL url(String url) {
		return ExceptionAdapter.illegalArg.get(() -> new URI(url).toURL());
	}

	/**
	 * Returns the URI object for a URL, converting any syntax exception to unchecked.
	 */
	public static URI uri(URL url) {
		return ExceptionAdapter.illegalArg.get(url::toURI);
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
	 * Iterates network interfaces to find the first local address.
	 */
	public static Inet4Address localIp4Address() throws SocketException {
		return inetAddresses().filter(InetAddress::isSiteLocalAddress).instances(Inet4Address.class)
			.next();
	}

	/**
	 * Finds the first local address for the interface.
	 */
	public static InetAddress localAddressFor(NetworkInterface n) {
		return inetAddresses(n).filter(InetAddress::isSiteLocalAddress).next();
	}

	/**
	 * Returns true if any local address exists for the network interface.
	 */
	public static boolean hasLocalAddress(NetworkInterface n) {
		return Objects.nonNull(localAddressFor(n));
	}

	/**
	 * Finds the first local address for the interface.
	 */
	public static Inet4Address localIp4AddressFor(NetworkInterface n) {
		return inetAddresses(n).filter(InetAddress::isSiteLocalAddress)
			.instances(Inet4Address.class).next();
	}

	/**
	 * Iterates network interfaces to find the first with a local address.
	 */
	public static NetworkInterface localInterface() throws SocketException {
		return networkInterfaces().filter(NetUtil::hasLocalAddress).next();
	}

	/**
	 * Iterates network interfaces to find the first address matching the predicate.
	 */
	public static InetAddress findLocalAddress(Functions.Predicate<? super InetAddress> predicate)
		throws SocketException {
		return inetAddresses().filter(predicate).next();
	}

	/**
	 * Lists all network interface addresses.
	 */
	public static List<InetAddress> localAddresses() throws SocketException {
		return inetAddresses().toList();
	}

	/**
	 * Iterates network interfaces to find the first IP4 broadcast address.
	 */
	public static Inet4Address localBroadcast() throws SocketException {
		return broadcast(ifaceAddresses());
	}

	/**
	 * Iterates interface addresses to find the first IP4 broadcast address.
	 */
	public static Inet4Address broadcast(NetworkInterface iface) {
		return broadcast(ifaceAddresses(iface));
	}

	private static Inet4Address broadcast(Stream<RuntimeException, InterfaceAddress> stream) {
		return stream.map(InterfaceAddress::getBroadcast).instances(Inet4Address.class).next();
	}

	private static Stream<RuntimeException, InetAddress> inetAddresses() throws SocketException {
		return networkInterfaces().map(NetworkInterface::inetAddresses).flatMap(Stream::from);
	}

	private static Stream<RuntimeException, InterfaceAddress> ifaceAddresses()
		throws SocketException {
		return networkInterfaces().map(NetworkInterface::getInterfaceAddresses)
			.flatMap(Stream::from);
	}

	private static Stream<RuntimeException, InetAddress> inetAddresses(NetworkInterface iface) {
		if (iface == null) return Stream.empty();
		return Stream.from(iface.inetAddresses());
	}

	private static Stream<RuntimeException, InterfaceAddress>
		ifaceAddresses(NetworkInterface iface) {
		if (iface == null) return Stream.empty();
		return Stream.from(iface.getInterfaceAddresses());
	}

	private static Stream<RuntimeException, NetworkInterface> networkInterfaces()
		throws SocketException {
		return Streams.from(NetworkInterface.networkInterfaces());
	}
}
