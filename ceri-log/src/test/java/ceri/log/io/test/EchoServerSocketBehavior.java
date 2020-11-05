package ceri.log.io.test;

import static ceri.common.test.AssertUtil.assertAscii;
import java.io.IOException;
import java.net.Socket;
import org.junit.Test;
import ceri.common.data.ByteStream;
import io.grpc.netty.shaded.io.netty.util.NetUtil;

public class EchoServerSocketBehavior {

	@SuppressWarnings("resource")
	@Test
	public void should() throws IOException {
		try (var ss = EchoServerSocket.of()) {
			int port = ss.port();
			try (Socket s = new Socket(NetUtil.LOCALHOST, port)) {
				var in = ByteStream.reader(s.getInputStream());
				var out = ByteStream.writer(s.getOutputStream());
				out.writeAscii("test");
				assertAscii(in, "test");
			}
		}
	}

}
