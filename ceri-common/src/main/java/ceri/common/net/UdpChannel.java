package ceri.common.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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

	public static record Received(InetSocketAddress address, ByteProvider bytes) {}

	public static UdpChannel of(int port) throws IOException {
		return new UdpChannel(port);
	}

	@SuppressWarnings("resource")
	private UdpChannel(int port) throws IOException {
		channel = DatagramChannel.open();
		channel.bind(new InetSocketAddress(port)).socket().setBroadcast(true);
		channel.configureBlocking(false);
		localHost = NetUtil.localIp4Address();
		this.port = channel.socket().getLocalPort();
		broadcastHost = NetUtil.localBroadcast();
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

	public void unicast(String host, int port, ByteProvider bytes) throws IOException {
		send(new InetSocketAddress(host, port), bytes);
	}

	public void unicast(InetAddress address, int port, ByteProvider bytes) throws IOException {
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
