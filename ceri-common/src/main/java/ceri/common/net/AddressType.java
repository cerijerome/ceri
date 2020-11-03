package ceri.common.net;

import java.net.InetAddress;
import java.util.List;
import java.util.function.Predicate;

/**
 * Encapsulation of InetAddress types.
 */
public enum AddressType {
	loopback(InetAddress::isLoopbackAddress),
	anyLocal(InetAddress::isAnyLocalAddress),
	linkLocal(InetAddress::isLinkLocalAddress),
	siteLocal(InetAddress::isSiteLocalAddress),
	multicast(InetAddress::isMulticastAddress),
	mcGlobal(InetAddress::isMCGlobal),
	mcLinkLocal(InetAddress::isMCLinkLocal),
	mcNodeLocal(InetAddress::isMCNodeLocal),
	mcOrgLocal(InetAddress::isMCOrgLocal),
	mcSiteLocal(InetAddress::isMCSiteLocal);

	private static List<AddressType> special = List.of(anyLocal, linkLocal, loopback, multicast);
	private final Predicate<InetAddress> predicate;

	/**
	 * Returns true if the address has a special meaning.
	 */
	public static boolean isSpecial(InetAddress address) {
		return special.stream().anyMatch(t -> t.appliesTo(address));
	}

	AddressType(Predicate<InetAddress> predicate) {
		this.predicate = predicate;
	}

	public boolean appliesTo(InetAddress address) {
		return predicate.test(address);
	}
}
