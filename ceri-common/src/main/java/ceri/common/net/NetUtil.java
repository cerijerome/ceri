package ceri.common.net;

import static ceri.common.collection.StreamUtil.first;
import static ceri.common.collection.StreamUtil.stream;
import static ceri.common.collection.StreamUtil.toList;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class NetUtil {
	public static final String LOCALHOST = "localhost";
	public static final String LOCALHOST_IPV4 = "127.0.0.1"; // one of 127.0.0.0/8
	public static final String LOCALHOST_IPV6 = "::1"; // one of ::1/128
	
	private NetUtil() {}

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
