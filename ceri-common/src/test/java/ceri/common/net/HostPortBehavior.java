package ceri.common.net;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Test;

public class HostPortBehavior {

	@Test
	public void testParse() {
		assertHostPort(HostPort.parse("localhost"), "localhost");
		assertHostPort(requireNonNull(HostPort.parse("localhost:8080")), "localhost", 8080);
		assertHostPort(requireNonNull(HostPort.parse("127.0.0.1")), "127.0.0.1");
		assertHostPort(requireNonNull(HostPort.parse("127.0.0.1:8080")), "127.0.0.1", 8080);
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
		exerciseEquals(obj, eq0, eq1);
		assertAllNotEqual(obj, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateInetAddress() throws UnknownHostException {
		InetAddress addr = HostPort.of("0.0.0.0").asAddress();
		assertThat(addr.getHostAddress(), is("0.0.0.0"));
	}

	private static void assertHostPort(HostPort hostPort, String host) {
		assertHostPort(hostPort, host, null);
	}

	static void assertHostPort(HostPort hostPort, String host, Integer port) {
		assertThat(hostPort.host, is(host));
		if (port == null) assertNull(hostPort.port);
		else assertThat(hostPort.port, is(port));
	}
}