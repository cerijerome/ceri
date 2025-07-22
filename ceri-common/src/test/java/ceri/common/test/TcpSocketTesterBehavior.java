package ceri.common.test;

import static ceri.common.test.AssertUtil.assertFind;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.util.CloseableUtil;

public class TcpSocketTesterBehavior {
	private Functions.Closeable fastMode;

	@Before
	public void before() {
		fastMode = ManualTester.fastMode();
	}

	@After
	public void after() {
		CloseableUtil.close(fastMode);
	}

	@Test
	public void shouldTestEcho() throws IOException {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			sys.in.print("oTEST\n~1\n!\n");
			TcpSocketTester.testEcho();
			assertFind(sys.out, "(?s)OUT >>>.*? TEST");
		}
	}

	@Test
	public void shouldTestPair() throws IOException {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			sys.in.print("oTEST\n+\n!\n");
			TcpSocketTester.testPair();
			assertFind(sys.out, "(?s)OUT >>>.*? TEST.*IN <<<.*? TEST");
		}
	}

	@Test
	public void shouldSetOptions() throws IOException {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			sys.in.print("Ot11;Ol22;Oc33;Os44;Or55;Ok1;Oa0;Ou1;Od0\n!\n");
			TcpSocketTester.testEcho();
			assertFind(sys.out,
				"(?s)SO_TIMEOUT = 11.*?SO_LINGER = 22.*?IP_TOS = 33.*?SO_SNDBUF = 44.*?"
					+ "SO_RCVBUF = 1024.*?SO_KEEPALIVE = true.*?SO_REUSEADDR = false.*?"
					+ "SO_OOBINLINE = true.*?TCP_NODELAY = false");
		}
	}

	@Test
	public void shouldGetOptions() throws IOException {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			sys.in.print("Ot11\nOt\n!\n");
			TcpSocketTester.testEcho();
			assertFind(sys.out, "(?s)SO_TIMEOUT = 11.*?SO_TIMEOUT = 11");
		}
	}

	@Test
	public void shouldGetAllOptions() throws IOException {
		try (SystemIoCaptor sys = SystemIoCaptor.of()) {
			sys.in.print("O\n!\n");
			TcpSocketTester.testEcho();
			assertFind(sys.out,
				"(?s)SO_TIMEOUT = .*?SO_LINGER = .*?SO_KEEPALIVE = .*?SO_REUSEADDR = .*?"
					+ "SO_OOBINLINE = .*?TCP_NODELAY = .*?IP_TOS = .*?SO_SNDBUF = .*?"
					+ "SO_RCVBUF = .*?");
		}
	}

}
