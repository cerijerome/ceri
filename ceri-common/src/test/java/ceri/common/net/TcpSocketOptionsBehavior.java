package ceri.common.net;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestTcpSocket;

public class TcpSocketOptionsBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = TcpSocketOptions.of().set(TcpSocketOption.ipTos, 123)
			.set(TcpSocketOption.soReuseAddr, false);
		var eq0 = TcpSocketOptions.of().set(TcpSocketOption.ipTos, 123)
			.set(TcpSocketOption.soReuseAddr, false);
		var eq1 = t.immutable();
		TcpSocketOptions ne0 = TcpSocketOptions.of().set(TcpSocketOption.ipTos, 123);
		exerciseEquals(t);
		assertEquals(t, eq0);
		assertEquals(t, eq1);
		assertAllNotEqual(t, ne0);
	}

	@Test
	public void shouldCreateFromSocket() throws IOException {
		try (TestTcpSocket s = TestTcpSocket.of()) {
			s.option(TcpSocketOption.soKeepAlive, true);
			s.option(TcpSocketOption.soLinger, 123);
			var options = TcpSocketOptions.from(s);
			assertUnordered(options.options(), TcpSocketOption.soKeepAlive,
				TcpSocketOption.soLinger);
			assertTrue(options.has(TcpSocketOption.soKeepAlive));
			assertTrue(options.has(TcpSocketOption.soLinger));
			assertFalse(options.has(TcpSocketOption.soTimeout));
			assertEquals(options.get(TcpSocketOption.soKeepAlive), true);
			assertEquals(options.get(TcpSocketOption.soLinger), 123);
		}
	}

	@Test
	public void shouldApplyOptions() throws IOException {
		try (TestTcpSocket s = TestTcpSocket.of()) {
			var options = TcpSocketOptions.of().set(TcpSocketOption.ipTos, 123)
				.set(TcpSocketOptions.of().set(TcpSocketOption.soReuseAddr, false)).immutable();
			options.applyAll(s);
			options.apply(TcpSocketOption.tcpNoDelay, s); // no value
			assertEquals(s.options(), options);
		}
	}

}
