package ceri.log.io;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestSocket;

public class SocketParamsBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		SocketParams t = SocketParams.builder().soLinger(33).build();
		SocketParams eq0 = SocketParams.builder().soLinger(33).build();
		SocketParams ne0 = SocketParams.builder().soLinger(32).build();
		SocketParams ne1 = SocketParams.builder().soLingerOff().build();
		SocketParams ne2 = SocketParams.builder().soLinger(33).keepAlive(false).build();
		SocketParams ne3 = SocketParams.DEFAULT;
		SocketParams ne4 = SocketParams.builder().soLinger(33).receiveBufferSize(1).build();
		SocketParams ne5 = SocketParams.builder().soLinger(33).sendBufferSize(1).build();
		SocketParams ne6 = SocketParams.builder().soLinger(33).tcpNoDelay(true).build();
		SocketParams ne7 = SocketParams.builder().soLinger(33).soTimeout(3).build();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6, ne7);
	}

	@Test
	public void shouldDetermineIfSoLingerIsOff() {
		assertFalse(SocketParams.builder().build().isSoLingerOff());
		assertFalse(SocketParams.builder().soLinger(0).build().isSoLingerOff());
		assertTrue(SocketParams.builder().soLingerOff().build().isSoLingerOff());
	}

	@Test
	public void shouldApplyToSocket() throws IOException {
		SocketParams.DEFAULT.applyTo(null);
		try (TestSocket s = TestSocket.of()) {
			SocketParams.DEFAULT.applyTo(s);
			SocketParams.builder().soLingerOff().soTimeout(1).receiveBufferSize(64)
				.sendBufferSize(64).tcpNoDelay(true).keepAlive(true).build().applyTo(s);
			SocketParams.builder().soLinger(1).build().applyTo(s);
		}
	}

}
