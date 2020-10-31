package ceri.x10.cm11a.device;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorProducer.RIX;
import static ceri.x10.cm11a.protocol.Protocol.READY;
import static ceri.x10.cm11a.protocol.Protocol.RING_ENABLE;
import static ceri.x10.command.House.A;
import static ceri.x10.command.House.B;
import static ceri.x10.command.House.G;
import static ceri.x10.command.House.H;
import static ceri.x10.command.House.I;
import static ceri.x10.command.House.L;
import static ceri.x10.command.Unit._1;
import static ceri.x10.command.Unit._10;
import static ceri.x10.command.Unit._16;
import static ceri.x10.command.Unit._6;
import static ceri.x10.command.Unit._7;
import static ceri.x10.command.Unit._9;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.concurrent.ValueCondition;
import ceri.common.io.StateChange;
import ceri.log.test.LogModifier;
import ceri.x10.cm11a.protocol.Clock;
import ceri.x10.cm11a.protocol.Status;
import ceri.x10.command.Command;
import ceri.x10.command.TestCommandListener;
import ceri.x10.command.UnsupportedCommand;

public class Cm11aDeviceBehavior {
	private static final Cm11aDeviceConfig config = Cm11aDeviceConfig.builder().maxSendAttempts(3)
		.queuePollTimeoutMs(0).readPollMs(0).readTimeoutMs(10000).build();
	private Cm11aTestConnector con;
	private Cm11aDevice cm11a;

	@Before
	public void before() throws IOException {
		con = Cm11aTestConnector.of();
		con.connect();
		cm11a = Cm11aDevice.of(config, con);
	}

	@After
	public void after() throws IOException {
		cm11a.close();
		con.close();
	}

	@Test
	public void shouldListenForConnectionChanges() throws InterruptedException {
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var enc = cm11a.listeners().enclose(sync::signal)) {
			con.listeners.accept(StateChange.broken);
			assertEquals(sync.await(), StateChange.broken);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSendUnitCommand() throws IOException {
		try (var exec = SimpleExecutor.run(() -> cm11a.command(Command.on(H, _10)))) {
			assertArray(con.out.from.readBytes(2), 0x4, 0xdf);
			con.in.to.writeBytes(0xe3).flush();
			assertArray(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			assertArray(con.out.from.readBytes(2), 0x6, 0xd2);
			con.in.to.writeBytes(0xd8).flush();
			assertArray(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			exec.get();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSendDimCommand() throws IOException {
		try (var exec = SimpleExecutor.run(() -> cm11a.command(Command.dim(L, 50, _7, _9)))) {
			assertArray(con.out.from.readBytes(2), 0x4, 0xb5);
			con.in.to.writeBytes(0xb9).flush();
			assertArray(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			assertArray(con.out.from.readBytes(2), 0x4, 0xb7);
			con.in.to.writeBytes(0xbb).flush();
			assertArray(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			assertArray(con.out.from.readBytes(2), 0x5e, 0xb4);
			con.in.to.writeBytes(0x12).flush();
			assertArray(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			exec.get();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSendExtCommand() throws IOException {
		try (var exec = SimpleExecutor.run(() -> cm11a.command(Command.ext(G, 0xaa, 0xbb, _16)))) {
			assertArray(con.out.from.readBytes(2), 0x4, 0x5c);
			con.in.to.writeBytes(0x60).flush();
			assertArray(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			assertArray(con.out.from.readBytes(4), 0x7, 0x57, 0xaa, 0xbb);
			con.in.to.writeBytes(0xc3).flush();
			assertArray(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			exec.get();
		}
	}

	@Test
	public void shouldFailForUnsupportedcommand() {
		assertThrown(() -> cm11a.command(UnsupportedCommand.hailReq(I, _1)));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldRequestStatus() throws IOException {
		try (var exec = SimpleExecutor.call(() -> cm11a.requestStatus())) {
			assertArray(con.out.from.readBytes(1), 0x8b);
			con.in.to.writeBytes(0xff, 0xff, 30, 100, 7, 140, 4, 0x6a, 0, 0xf, 0, 0xa, 0, 0x3).flush();
			Status status = exec.get();
			assertEquals(status.batteryTimer, 0xffff);
			assertEquals(status.house, A);
			assertEquals(status.addressed, 0x000f);
			assertEquals(status.onOff, 0x000a);
			assertEquals(status.dim, 0x0003);
			assertEquals(status.date.getSecond(), 30);
			assertEquals(status.date.getMinute(), 40);
			assertEquals(status.date.getHour(), 15);
			assertEquals(status.date.getMonth(), Month.MAY);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProcessClockRequestFromDevice() throws IOException {
		con.in.to.writeByte(0xa5).flush();
		Clock clock = Clock.decode(con.out.from);
		assertEquals(clock.house, A);
		assertFalse(clock.clearBatteryTimer);
		assertFalse(clock.clearMonitoredStatus);
		assertFalse(clock.purgeTimer);
		long diffMs = Duration.between(clock.date, LocalDateTime.now()).toMillis();
		assertTrue(diffMs < 1000);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReceiveDataFromDevice() throws InterruptedException, IOException {
		TestCommandListener listener = TestCommandListener.of();
		try (var enclosed = cm11a.listen(listener)) {
			con.in.to.writeByte(0x5a).flush();
			assertArray(con.out.from.readBytes(1), 0xc3);
			con.in.to.writeBytes(5, 0x04, 0xe9, 0xe5, 0xe5, 0x58);
			listener.sync.await(Command.bright(B, 42, _6, _7));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldIgnoreUnsupportedDataFromDevice() throws IOException {
		LogModifier.run(() -> {
			con.in.to.writeBytes(READY.value, RING_ENABLE.value, READY.value).flush();
			con.in.awaitFeed();
		}, Level.ERROR, Processor.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailCommandIfRetriesExceeded() throws IOException {
		LogModifier.run(() -> {
			// Run twice to check logging logic
			Command cmd = Command.on(H, _10);
			try (var exec = SimpleExecutor.run(() -> cm11a.command(cmd))) {
				assertArray(con.out.from.readBytes(2), 0x4, 0xdf);
				con.in.to.writeBytes(READY.value, READY.value, READY.value).flush();
				con.in.awaitFeed();
				assertThrown(() -> exec.get());
			}
			try (var exec = SimpleExecutor.run(() -> cm11a.command(cmd))) {
				assertArray(con.out.from.readBytes(2), 0x4, 0xdf);
				con.in.to.writeBytes(READY.value, READY.value, READY.value).flush();
				con.in.awaitFeed();
				assertThrown(() -> exec.get());
			}
		}, Level.OFF, Processor.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCloseOnInterrupt() throws IOException {
		try (Cm11aTestConnector con = Cm11aTestConnector.of()) {
			con.connect();
			try (Cm11aDevice cm11a = Cm11aDevice.of(config, con)) {
				con.in.read.error.setFrom(RIX);
				con.in.to.writeByte(0).flush();
				con.in.awaitFeed();
			}
		}
	}

	@Test
	public void shouldFailConstructionGracefully() {
		assertThrown(() -> Cm11aDevice.of(config, null));
	}

}
