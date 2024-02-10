package ceri.common.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.util.CloseableUtil;

/**
 * Utility to send and receive datagrams.
 */
public class UdpChannel implements AutoCloseable {
	private final DatagramChannel channel;
	public final InetAddress localHost;
	public final int port;
	public final InetAddress broadcastHost;
	public final NetworkInterface networkIface; // for multicast
	private final Map<InetAddress, MembershipKey> joined = new HashMap<>();

	public static record Received(InetSocketAddress address, ByteProvider bytes) {}

	public static UdpChannel of(int port) throws IOException {
		return new UdpChannel(port);
	}

	@SuppressWarnings("resource")
	private UdpChannel(int port) throws IOException {
		networkIface = NetUtil.localInterface();
		channel = DatagramChannel.open();
		channel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
			.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkIface)
			.bind(new InetSocketAddress(port)).socket().setBroadcast(true);
		channel.configureBlocking(false);
		localHost = NetUtil.localIp4Address();
		broadcastHost = NetUtil.localBroadcast();
		this.port = channel.socket().getLocalPort();
	}

	@SuppressWarnings("resource")
	public void blocking(boolean enabled) throws IOException {
		channel.configureBlocking(enabled);
	}

	public void broadcast(int port, ByteProvider bytes) throws IOException {
		send(new InetSocketAddress(broadcastHost, port), bytes);
	}

	public void unicast(int port, ByteProvider bytes) throws IOException {
		send(new InetSocketAddress(port), bytes);
	}

	public void send(String host, int port, ByteProvider bytes) throws IOException {
		send(new InetSocketAddress(host, port), bytes);
	}

	public void send(InetAddress address, int port, ByteProvider bytes) throws IOException {
		send(new InetSocketAddress(address, port), bytes);
	}

	/**
	 * Blocks until a datagram is received.
	 */
	public Received select(int bufferSize) throws IOException {
		var buffer = ByteBuffer.allocate(bufferSize);
		return received(selectInto(buffer), buffer);
	}

	/**
	 * Blocks until a datagram is received into the buffer.
	 */
	public InetSocketAddress selectInto(ByteBuffer buffer) throws IOException {
		try (var selector = Selector.open()) {
			channel.register(selector, SelectionKey.OP_READ);
			selector.select();
			return receiveInto(buffer);
		}
	}

	/**
	 * Receives a datagram. Returns the address and datagram data up to the given maximum size. If
	 * non-blocking, and no datagram is available the received address will be null.
	 */
	public Received receive(int bufferSize) throws IOException {
		var buffer = ByteBuffer.allocate(bufferSize);
		return received(receiveInto(buffer), buffer);
	}

	/**
	 * Receive a datagram into the buffer. Returns null and leaves the buffer untouched if
	 * non-blocking, and no datagram is available.
	 */
	public InetSocketAddress receiveInto(ByteBuffer buffer) throws IOException {
		var address = (InetSocketAddress) channel.receive(buffer);
		buffer.flip();
		return address;
	}

	/**
	 * Attempts to join a multicast group. Returns false if already a member.
	 */
	public boolean join(InetAddress group) throws IOException {
		var key = joined.get(group);
		if (key != null) return false;
		key = channel.join(group, networkIface);
		joined.put(group, key);
		return true;
	}

	/**
	 * Drops a multicast group. Returns false if not a member.
	 */
	public boolean drop(InetAddress group) {
		var key = joined.get(group);
		if (key == null) return false;
		key.drop();
		joined.remove(group);
		return true;
	}

	@Override
	public void close() {
		CloseableUtil.close(channel);
	}

	private void send(InetSocketAddress address, ByteProvider bytes) throws IOException {
		channel.send(bytes.toBuffer(0), address);
	}

	private static Received received(InetSocketAddress address, ByteBuffer buffer) {
		return new Received(address, address == null ? ByteProvider.empty() :
			ByteArray.Immutable.wrap(buffer.array(), 0, buffer.limit()));
	}
}
