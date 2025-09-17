package ceri.common.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.function.FunctionUtil;

public class UdpUtil {
	public static final int MAX_PACKET_DATA = 65507; // = 65535 - 8(udp) - 20(ip)

	private UdpUtil() {}

	public static HostPort hostPort(DatagramSocket socket) {
		if (socket == null) return null;
		var host = FunctionUtil.safeApply(socket.getInetAddress(), InetAddress::getHostAddress);
		return HostPort.of(host, socket.getPort());
	}

	public static DatagramPacket toPacket(ByteProvider data, InetAddress address, int port) {
		return new DatagramPacket(data.copy(0), data.length(), address, port);
	}

	public static ByteProvider fromPacket(DatagramPacket packet) {
		return ByteArray.Immutable.wrap(packet.getData(), packet.getOffset(), packet.getLength());
	}

	public static ByteProvider receive(DatagramSocket socket, byte[] buffer) throws IOException {
		try {
			var receivePacket = new DatagramPacket(buffer, buffer.length);
			socket.receive(receivePacket);
			return fromPacket(receivePacket);
		} catch (SocketTimeoutException e) {
			return null;
		}
	}
}
