package ceri.common.net;

import java.net.InetAddress;
import java.util.List;
import ceri.common.function.Functions;

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
	private final Functions.Predicate<InetAddress> predicate;

	/**
	 * Returns true if the address has a special meaning.
	 */
	public static boolean isSpecial(InetAddress address) {
		return special.stream().anyMatch(t -> t.appliesTo(address));
	}

	AddressType(Functions.Predicate<InetAddress> predicate) {
		this.predicate = predicate;
	}

	public boolean appliesTo(InetAddress address) {
		return predicate.test(address);
	}
}
