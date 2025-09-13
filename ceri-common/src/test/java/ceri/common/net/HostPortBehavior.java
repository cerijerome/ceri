package ceri.common.net;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNotEquals;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertTrue;
import java.net.UnknownHostException;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class HostPortBehavior {

	@Test
	public void testParse() {
		assertHostPort(HostPort.parse("localhost"), "localhost");
		assertHostPort(HostPort.parse("localhost:8080"), "localhost", 8080);
		assertHostPort(HostPort.parse("127.0.0.1"), "127.0.0.1");
		assertHostPort(HostPort.parse("127.0.0.1:8080"), "127.0.0.1", 8080);
		assertNull(HostPort.parse(""));
		assertNull(HostPort.parse(":"));
		assertNull(HostPort.parse("localhost:"));
		assertNull(HostPort.parse(":80"));
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		HostPort obj = HostPort.of("test", 777);
		HostPort eq0 = HostPort.of("test", 777);
		HostPort eq1 = HostPort.parse("test:777");
		HostPort ne0 = HostPort.of("tests", 777);
		HostPort ne1 = HostPort.of("test", 776);
		HostPort ne2 = HostPort.of("test");
		HostPort ne3 = HostPort.localhost(777);
		TestUtil.exerciseEquals(obj, eq0, eq1);
		assertAllNotEqual(obj, ne0, ne1, ne2, ne3);
		assertNotEquals(obj.toString(), ne2.toString());
	}

	@Test
	public void shouldProvidePortOrDefault() {
		assertEquals(HostPort.of("test", 777).port(888), 777);
		assertEquals(HostPort.of("test").port(888), 888);
	}

	@Test
	public void shouldDetermineIfPortIsSpecified() {
		assertFalse(HostPort.NULL.hasPort());
		assertFalse(HostPort.LOCALHOST.hasPort());
		assertFalse(HostPort.of("test").hasPort());
		assertTrue(HostPort.localhost(123).hasPort());
		assertTrue(HostPort.of("test", 123).hasPort());
	}

	@Test
	public void shouldDetermineIfNull() {
		assertTrue(HostPort.NULL.isNull());
		assertFalse(HostPort.LOCALHOST.isNull());
		assertFalse(HostPort.of("").isNull());
	}

	@Test
	public void shouldCreateInetAddress() throws UnknownHostException {
		var addr = HostPort.of("0.0.0.0").asAddress();
		assertEquals(addr.getHostAddress(), "0.0.0.0");
	}

	@Test
	public void shouldCreateInetSocketAddress() throws UnknownHostException {
		var addr = HostPort.of("0.0.0.0").asSocketAddress();
		assertEquals(addr.getHostString(), "0.0.0.0");
	}

	static void assertHostPort(HostPort hostPort, String host) {
		assertHostPort(hostPort, host, HostPort.INVALID_PORT);
	}

	static void assertHostPort(HostPort hostPort, String host, int port) {
		if (host == null) {
			assertNull(hostPort);
			return;
		}
		assertNotNull(hostPort);
		assertEquals(hostPort.host, host);
		assertEquals(hostPort.port, port);
	}
}