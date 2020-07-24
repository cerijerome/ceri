package ceri.log.io;

import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.findsRegex;
import static ceri.common.test.TestUtil.throwIt;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.net.Socket;
import org.apache.logging.log4j.Level;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.ValueCondition;
import ceri.common.io.StateChange;
import ceri.common.util.BasicUtil;
import ceri.log.test.LogModifier;

public class SelfHealingSocketBehavior {
	private static LogModifier logMod;
	private Socket socket;

	@BeforeClass
	public static void beforeClass() {
		logMod = LogModifier.builder().set(Level.OFF, SelfHealingSocket.class).build();
	}

	@AfterClass
	public static void afterClass() {
		logMod.close();
	}

	@Before
	public void before() {
		socket = Mockito.mock(Socket.class);
	}

	@Test
	public void shouldNotifyWhenBroken() throws InterruptedException {
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (SelfHealingSocket socket = builder().recoveryDelayMs(60000).build()) {
			socket.listeners().listen(sync::signal);
			socket.broken();
			assertThat(sync.await(), is(StateChange.broken));
		}
	}

	@Test
	public void shouldNotifyWhenFixed() throws InterruptedException {
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (SelfHealingSocket socket = builder().build()) {
			socket.listeners().listen(sync::signal);
			socket.broken();
			sync.await(StateChange.fixed);
		}
	}

	@Test
	public void shouldRetryFixes() throws InterruptedException {
		ValueCondition<StateChange> sync = ValueCondition.of();
		SelfHealingSocket.Builder b = builder();
		b.socketFactory = (host, port) -> throwIt(new IOException("boom!"));
		try (SelfHealingSocket socket = b.build()) {
			socket.listeners().listen(sync::signal);
			assertThrown(() -> socket.in().readAllBytes());
			assertThrown(() -> socket.out().write(0));
			BasicUtil.delay(1);
			sync.await(StateChange.broken);
		}
	}

	@Test
	public void shouldInterruptNotifications() throws InterruptedException {
		try (SelfHealingSocket socket = builder().build()) {
			socket.listeners().listen(t -> throwIt(new RuntimeInterruptedException("boom!")));
			assertThrown(() -> socket.out().write(0));
			socket.waitUntilStopped();
		}
	}

	@Test
	public void shouldAllowNotificationErrors() throws InterruptedException {
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (SelfHealingSocket socket = builder().build()) {
			socket.listeners().listen(sync::signal);
			socket.listeners().listen(t -> throwIt(new RuntimeException("rte")));
			assertThrown(() -> socket.out().write(0));
			BasicUtil.delay(1);
			sync.await(StateChange.fixed);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldConfigureSocket() throws IOException {
		try (SelfHealingSocket socket = builder().keepAlive(true).receiveBufferSize(1)
			.sendBufferSize(2).soLinger(3).soTimeout(4).tcpNoDelay(true).build()) {
			socket.connect();
		}
		verify(socket).setKeepAlive(true);
		verify(socket).setReceiveBufferSize(1);
		verify(socket).setSendBufferSize(2);
		verify(socket).setSoLinger(true, 3);
		verify(socket).setSoTimeout(4);
		verify(socket).setTcpNoDelay(true);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldConfigureSocketLingerOff() throws IOException {
		try (SelfHealingSocket socket = builder().soLingerOff().build()) {
			socket.connect();
		}
		verify(socket).setSoLinger(false, 0);
	}

	@Test
	public void shouldHaveStringRepresentation() {
		try (SelfHealingSocket socket = builder().build()) {
			assertThat(socket.toString(), findsRegex("localhost.*12345"));
		}
	}

	private SelfHealingSocket.Builder builder() {
		SelfHealingSocket.Builder b =
			SelfHealingSocket.builder("localhost", 12345).fixRetryDelayMs(1).recoveryDelayMs(1);
		b.socketFactory = (host, port) -> socket;
		return b;
	}
}
