package ceri.common.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import ceri.common.collection.ImmutableByteArray;

public class UdpUtil {
	public static final int MAX_PACKET_DATA = 65507; // = 65535 - 8(udp) - 20(ip)

	private UdpUtil() {}

	public static DatagramPacket toPacket(ImmutableByteArray data, InetAddress address, int port) {
		return new DatagramPacket(data.copy(), data.length, address, port);
	}

	public static ImmutableByteArray fromPacket(DatagramPacket packet) {
		return ImmutableByteArray.wrap(packet.getData(), packet.getOffset(), packet.getLength());
	}

	public static ImmutableByteArray receive(DatagramSocket socket, byte[] buffer)
		throws IOException {
		try {
			DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
			socket.receive(receivePacket);
			return fromPacket(receivePacket);
		} catch (SocketTimeoutException e) {
			return null;
		}
	}

}
