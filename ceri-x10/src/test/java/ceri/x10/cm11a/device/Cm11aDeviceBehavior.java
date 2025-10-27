package ceri.x10.cm11a.device;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.concurrent.ValueCondition;
import ceri.common.function.Closeables;
import ceri.common.io.StateChange;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestConnector;
import ceri.common.test.TestUtil;
import ceri.log.test.LogModifier;
import ceri.x10.cm11a.protocol.Clock;
import ceri.x10.cm11a.protocol.Protocol;
import ceri.x10.cm11a.protocol.Status;
import ceri.x10.command.Command;
import ceri.x10.command.House;
import ceri.x10.command.TestCommandListener;
import ceri.x10.command.Unit;
import ceri.x10.command.UnsupportedCommand;

public class Cm11aDeviceBehavior {
	private static final Cm11aDevice.Config config = Cm11aDevice.Config.builder().maxSendAttempts(3)
		.queuePollTimeoutMs(0).readPollMs(0).readTimeoutMs(10000).build();
	private TestConnector con;
	private Cm11aDevice cm11a;

	@After
	public void after() {
		Closeables.close(cm11a, con);
		cm11a = null;
		con = null;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Cm11aDevice.Config.builder().queueSize(5).build();
		var eq0 = Cm11aDevice.Config.builder().queueSize(5).build();
		var ne0 = Cm11aDevice.Config.builder().queueSize(4).build();
		var ne1 = Cm11aDevice.Config.builder().queueSize(5).maxSendAttempts(9).build();
		var ne2 = Cm11aDevice.Config.builder().queueSize(5).queuePollTimeoutMs(1).build();
		var ne3 = Cm11aDevice.Config.builder().queueSize(5).readPollMs(1).build();
		var ne4 = Cm11aDevice.Config.builder().queueSize(5).readTimeoutMs(1).build();
		var ne5 = Cm11aDevice.Config.builder().queueSize(5).errorDelayMs(1).build();
		TestUtil.exerciseEquals(t, eq0);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldListenForConnectionChanges() throws InterruptedException, IOException {
		init();
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var _ = cm11a.listeners().enclose(sync::signal)) {
			con.listeners.accept(StateChange.broken);
			Assert.equal(sync.await(), StateChange.broken);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSendUnitCommand() throws IOException {
		init();
		try (var exec = SimpleExecutor.run(() -> cm11a.command(Command.on(House.H, Unit._10)))) {
			Assert.array(con.out.from.readBytes(2), 0x4, 0xdf);
			con.in.to.writeBytes(0xe3).flush();
			Assert.array(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			Assert.array(con.out.from.readBytes(2), 0x6, 0xd2);
			con.in.to.writeBytes(0xd8).flush();
			Assert.array(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			exec.get();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSendDimCommand() throws IOException {
		init();
		try (var exec =
			SimpleExecutor.run(() -> cm11a.command(Command.dim(House.L, 50, Unit._7, Unit._9)))) {
			Assert.array(con.out.from.readBytes(2), 0x4, 0xb5);
			con.in.to.writeBytes(0xb9).flush();
			Assert.array(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			Assert.array(con.out.from.readBytes(2), 0x4, 0xb7);
			con.in.to.writeBytes(0xbb).flush();
			Assert.array(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			Assert.array(con.out.from.readBytes(2), 0x5e, 0xb4);
			con.in.to.writeBytes(0x12).flush();
			Assert.array(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			exec.get();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldSendExtCommand() throws IOException {
		init();
		try (var exec =
			SimpleExecutor.run(() -> cm11a.command(Command.ext(House.G, 0xaa, 0xbb, Unit._16)))) {
			Assert.array(con.out.from.readBytes(2), 0x4, 0x5c);
			con.in.to.writeBytes(0x60).flush();
			Assert.array(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			Assert.array(con.out.from.readBytes(4), 0x7, 0x57, 0xaa, 0xbb);
			con.in.to.writeBytes(0xc3).flush();
			Assert.array(con.out.from.readBytes(1), 0);
			con.in.to.writeBytes(0x55).flush();
			exec.get();
		}
	}

	@Test
	public void shouldFailForUnsupportedcommand() throws IOException {
		init();
		Assert.thrown(() -> cm11a.command(UnsupportedCommand.hailReq(House.I, Unit._1)));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldRequestStatus() throws IOException {
		init();
		try (var exec = SimpleExecutor.call(() -> cm11a.requestStatus())) {
			Assert.array(con.out.from.readBytes(1), 0x8b);
			con.in.to.writeBytes(0xff, 0xff, 30, 100, 7, 140, 4, 0x6a, 0, 0xf, 0, 0xa, 0, 0x3)
				.flush();
			Status status = exec.get();
			Assert.equal(status.batteryTimer, 0xffff);
			Assert.equal(status.house, House.A);
			Assert.equal(status.addressed, 0x000f);
			Assert.equal(status.onOff, 0x000a);
			Assert.equal(status.dim, 0x0003);
			Assert.equal(status.date.getSecond(), 30);
			Assert.equal(status.date.getMinute(), 40);
			Assert.equal(status.date.getHour(), 15);
			Assert.equal(status.date.getMonth(), Month.MAY);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProcessClockRequestFromDevice() throws IOException {
		init();
		con.in.to.writeByte(0xa5).flush();
		Clock clock = Clock.decode(con.out.from);
		Assert.equal(clock.house, House.A);
		Assert.no(clock.clearBatteryTimer);
		Assert.no(clock.clearMonitoredStatus);
		Assert.no(clock.purgeTimer);
		long diffMs = Duration.between(clock.date, LocalDateTime.now()).toMillis();
		Assert.yes(diffMs < 1000);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReceiveDataFromDevice() throws InterruptedException, IOException {
		init();
		TestCommandListener listener = TestCommandListener.of();
		try (var _ = cm11a.listen(listener)) {
			con.in.to.writeByte(0x5a).flush();
			Assert.array(con.out.from.readBytes(1), 0xc3);
			con.in.to.writeBytes(5, 0x04, 0xe9, 0xe5, 0xe5, 0x58);
			listener.sync.await(Command.bright(House.B, 42, Unit._6, Unit._7));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldIgnoreUnsupportedDataFromDevice() throws IOException {
		init();
		LogModifier.run(() -> {
			con.in.to
				.writeBytes(Protocol.READY.value, Protocol.RING_ENABLE.value, Protocol.READY.value)
				.flush();
			con.in.awaitFeed();
		}, Level.ERROR, Processor.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailCommandIfRetriesExceeded() throws IOException {
		init();
		LogModifier.run(() -> {
			// Run twice to check logging logic
			Command cmd = Command.on(House.H, Unit._10);
			try (var exec = SimpleExecutor.run(() -> cm11a.command(cmd))) {
				Assert.array(con.out.from.readBytes(2), 0x4, 0xdf);
				con.in.to
					.writeBytes(Protocol.READY.value, Protocol.READY.value, Protocol.READY.value)
					.flush();
				con.in.awaitFeed();
				Assert.thrown(() -> exec.get());
			}
			try (var exec = SimpleExecutor.run(() -> cm11a.command(cmd))) {
				Assert.array(con.out.from.readBytes(2), 0x4, 0xdf);
				con.in.to
					.writeBytes(Protocol.READY.value, Protocol.READY.value, Protocol.READY.value)
					.flush();
				con.in.awaitFeed();
				Assert.thrown(() -> exec.get());
			}
		}, Level.OFF, Processor.class);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCloseOnInterrupt() throws IOException {
		try (TestConnector con = TestConnector.of()) {
			con.open();
			try (Cm11aDevice _ = Cm11aDevice.of(config, con)) {
				con.in.read.error.setFrom(ErrorGen.RIX);
				con.in.to.writeByte(0).flush();
				con.in.awaitFeed();
			}
		}
	}

	@Test
	public void shouldFailConstructionGracefully() {
		Assert.thrown(() -> Cm11aDevice.of(config, null));
	}

	private void init() throws IOException {
		con = TestConnector.of();
		con.open();
		cm11a = Cm11aDevice.of(config, con);
	}
}
