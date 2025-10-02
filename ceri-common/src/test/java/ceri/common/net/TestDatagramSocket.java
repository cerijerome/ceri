package ceri.common.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import ceri.common.except.ExceptionAdapter;
import ceri.common.test.CallSync;

public class TestDatagramSocket extends DatagramSocket {
	public final CallSync.Supplier<InetAddress> getInetAddress = CallSync.supplier();
	public final CallSync.Supplier<Integer> getPort = CallSync.supplier(0);
	public final CallSync.Consumer<DatagramPacket> receive = CallSync.consumer(null, true);

	public static TestDatagramSocket of() throws SocketException {
		return new TestDatagramSocket();
	}

	private TestDatagramSocket() throws SocketException {}

	@Override
	public InetAddress getInetAddress() {
		return getInetAddress.get();
	}

	@Override
	public int getPort() {
		return getPort.get();
	}

	@Override
	public void receive(DatagramPacket p) throws IOException {
		receive.accept(p, ExceptionAdapter.io);
	}

}
