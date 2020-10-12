package ceri.x10.cm17a.device;

import static ceri.common.test.TestUtil.assertThrown;
import static ceri.x10.cm17a.device.Data.code;
import static ceri.x10.command.FunctionType.bright;
import static ceri.x10.command.FunctionType.dim;
import static ceri.x10.command.FunctionType.off;
import static ceri.x10.command.FunctionType.on;
import static ceri.x10.command.House.A;
import static ceri.x10.command.House.B;
import static ceri.x10.command.House.C;
import static ceri.x10.command.House.D;
import static ceri.x10.command.House.K;
import static ceri.x10.command.House.L;
import static ceri.x10.command.House.P;
import static ceri.x10.command.Unit._10;
import static ceri.x10.command.Unit._13;
import static ceri.x10.command.Unit._14;
import static ceri.x10.command.Unit._15;
import static ceri.x10.command.Unit._16;
import static ceri.x10.command.Unit._3;
import static ceri.x10.command.Unit._5;
import static ceri.x10.command.Unit._7;
import static ceri.x10.command.Unit._9;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.io.StateChange;
import ceri.common.test.ErrorGen.Mode;
import ceri.common.test.TestListener;
import ceri.common.util.Enclosed;
import ceri.log.test.LogModifier;
import ceri.x10.command.Command;
import ceri.x10.command.TestCommandListener;

public class Cm17aDeviceBehavior {
	private static final Cm17aDeviceConfig config =
		Cm17aDeviceConfig.builder().commandIntervalMicros(1).resetIntervalMicros(1)
			.waitIntervalMicros(1).queuePollTimeoutMs(1).errorDelayMs(1).build();
	private static Cm17aTestConnector con;
	private static Cm17aDevice cm17a;

	@BeforeClass
	public static void beforeClass() {
		con = Cm17aTestConnector.of();
		cm17a = Cm17aDevice.of(config, con);
	}

	@Before
	public void before() {
		// Processor only sets rts/dtr to standby on start or on error,
		// so don't reset rts/dtr before each test.
		con.reset(false);
	}

	@AfterClass
	public static void afterClass() {
		cm17a.close();
		con.close();
	}

	@Test
	public void shouldHandleOnCommands() throws IOException {
		cm17a.command(Command.from("A10:on"));
		con.assertCodes(code(A, _10, on));
		cm17a.command(Command.from("A10:on"));
		con.assertCodes();
	}

	@Test
	public void shouldSendOffCommands() throws IOException {
		cm17a.command(Command.from("B3:off"));
		con.assertCodes(code(B, _3, off));
		cm17a.command(Command.from("B3:off"));
		con.assertCodes(code(B, _3, off));
	}

	@Test
	public void shouldHandleDimCommands() throws IOException {
		cm17a.command(Command.from("P[15,16]:dim:10%"));
		con.assertCodes( //
			code(P, _15, on), // 0x3448
			code(P, dim), // 0x3098,
			code(P, dim), // 0x3098,
			code(P, _16, on), // 0x3458,
			code(P, dim), // 0x3098
			code(P, dim)); // 0x3098
		cm17a.command(Command.from("P[16]:bright:1%"));
		con.assertCodes( //
			code(P, bright)); // 0x3088
	}

	@Test
	public void shouldResetOnError() throws IOException {
		LogModifier.run(() -> {
			Command cmd = Command.off(K, _13, _14);
			con.dtrError.mode(Mode.checked);
			assertThrown(() -> cm17a.command(cmd));
			con.dtrError.reset();
			cm17a.command(cmd);
			con.rtsError.mode(Mode.rt);
			assertThrown(() -> cm17a.command(cmd));
			con.rtsError.reset();
			cm17a.command(cmd);
		}, Level.ERROR, Processor.class);
	}

	@Test
	public void shouldFailForUnsupportedCommands() {
		assertThrown(() -> cm17a.command(Command.allLightsOff(C)));
		assertThrown(() -> cm17a.command(Command.ext(D, 1, 2, _7)));
	}

	@Test
	public void shouldListenForCommands() throws IOException, InterruptedException {
		TestCommandListener listener = TestCommandListener.of();
		try (Enclosed<?> enc = cm17a.listen(listener)) {
			cm17a.command(Command.dim(L, 50, _5, _9));
			assertThat(listener.sync.await(), is(Command.dim(L, 50, _5, _9)));
		}
	}

	@Test
	public void shouldListenForConnectorStateChange() throws InterruptedException {
		try (TestListener<StateChange> listener = TestListener.of(cm17a.listeners())) {
			con.listeners.accept(StateChange.broken);
			assertThat(listener.await(), is(StateChange.broken));
		}
	}

}
