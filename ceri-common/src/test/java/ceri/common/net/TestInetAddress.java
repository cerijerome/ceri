package ceri.common.net;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import static ceri.common.net.AddressType.anyLocal;
import static ceri.common.net.AddressType.linkLocal;
import static ceri.common.net.AddressType.loopback;
import static ceri.common.net.AddressType.multicast;
import static ceri.common.net.AddressType.siteLocal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.Set;
import org.mockito.Mockito;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.test.CallSync;

public class TestInetAddress {
	public final CallSync.Get<byte[]> address = CallSync.supplier((byte[]) null);
	public final CallSync.Get<String> hostAddress = CallSync.supplier((String) null);
	public final CallSync.Get<String> hostName = CallSync.supplier((String) null);
	public final CallSync.Get<String> canonicalHostName = CallSync.supplier((String) null);
	public final CallSync.Get<Set<AddressType>> type = CallSync.supplier(Set.of());
	public final CallSync.Apply<List<?>, Boolean> reachable = CallSync.function(null, true);
	private InetAddress mock = null;

	public void reset() {
		address.reset();
		hostAddress.reset();
		hostName.reset();
		canonicalHostName.reset();
		type.reset();
		reachable.reset();
	}

	public InetAddress mock() {
		if (mock == null) mock = createMock();
		return mock;
	}

	private InetAddress createMock() {
		return ExceptionAdapter.RUNTIME.get(() -> {
			InetAddress address = Mockito.mock(InetAddress.class);
			when(address.getAddress()).thenAnswer(x -> this.address.get());
			when(address.getHostAddress()).thenAnswer(x -> hostAddress.get());
			when(address.getHostName()).thenAnswer(x -> hostName.get());
			when(address.getCanonicalHostName()).thenAnswer(x -> canonicalHostName.get());
			when(address.isAnyLocalAddress()).thenAnswer(x -> isType(anyLocal));
			when(address.isSiteLocalAddress()).thenAnswer(x -> isType(siteLocal));
			when(address.isLinkLocalAddress()).thenAnswer(x -> isType(linkLocal));
			when(address.isLoopbackAddress()).thenAnswer(x -> isType(loopback));
			when(address.isMulticastAddress()).thenAnswer(x -> isType(multicast));
			when(address.isReachable(anyInt())).thenAnswer(inv -> isReachable(inv.getArgument(0)));
			when(address.isReachable(any(), anyInt(), anyInt())).thenAnswer(
				inv -> isReachable(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2)));
			return address;
		});
	}

	private boolean isReachable(int timeoutMs) throws IOException {
		return reachable.apply(List.of(timeoutMs), IO_ADAPTER);
	}

	private boolean isReachable(NetworkInterface netIf, int ttl, int timeoutMs) throws IOException {
		return reachable.apply(List.of(netIf, ttl, timeoutMs), IO_ADAPTER);
	}

	private boolean isType(AddressType type) {
		Set<AddressType> set = this.type.get();
		return set != null && set.contains(type);
	}
}