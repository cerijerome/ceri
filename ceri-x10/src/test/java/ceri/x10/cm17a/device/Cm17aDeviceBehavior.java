package ceri.x10.cm17a.device;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RTX;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.x10.cm17a.device.Data.code;
import static ceri.x10.command.FunctionType.on;
import static ceri.x10.command.House.A;
import static ceri.x10.command.House.C;
import static ceri.x10.command.House.D;
import static ceri.x10.command.House.K;
import static ceri.x10.command.House.L;
import static ceri.x10.command.Unit._10;
import static ceri.x10.command.Unit._13;
import static ceri.x10.command.Unit._14;
import static ceri.x10.command.Unit._5;
import static ceri.x10.command.Unit._7;
import static ceri.x10.command.Unit._9;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Test;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.common.util.CloseableUtil;
import ceri.log.test.LogModifier;
import ceri.x10.command.Command;
import ceri.x10.command.TestCommandListener;

public class Cm17aDeviceBehavior {
	private Cm17aTestConnector con;
	private Cm17aDevice cm17a;

	@After
	public void after() {
		CloseableUtil.close(cm17a, con);
		cm17a = null;
		con = null;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Cm17aDevice.Config.builder().queueSize(5).build();
		var eq0 = Cm17aDevice.Config.builder().queueSize(5).build();
		var ne0 = Cm17aDevice.Config.builder().queueSize(6).build();
		var ne1 = Cm17aDevice.Config.builder().queueSize(5).commandIntervalMicros(11).build();
		var ne2 = Cm17aDevice.Config.builder().queueSize(5).errorDelayMs(22).build();
		var ne3 = Cm17aDevice.Config.builder().queueSize(5).queuePollTimeoutMs(33).build();
		var ne4 = Cm17aDevice.Config.builder().queueSize(5).resetIntervalMicros(44).build();
		var ne5 = Cm17aDevice.Config.builder().queueSize(5).waitIntervalMicros(55).build();
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5);
	}

	@Test
	public void shouldHandleOnCommands() throws IOException {
		init();
		cm17a.command(Command.from("A10:on"));
		con.assertCodes(code(A, _10, on));
		cm17a.command(Command.from("A10:on"));
		con.assertCodes();
	}

	@Test
	public void shouldResetOnError() throws IOException {
		init();
		LogModifier.run(() -> {
			Command cmd = Command.off(K, _13, _14);
			con.dtr.error.setFrom(IOX);
			assertThrown(() -> cm17a.command(cmd));
			con.dtr.error.clear();
			cm17a.command(cmd);
			con.rts.error.setFrom(RTX);
			assertThrown(() -> cm17a.command(cmd));
			con.rts.error.clear();
			cm17a.command(cmd);
		}, Level.ERROR, Processor.class);
	}

	@Test
	public void shouldFailForUnsupportedCommands() {
		init();
		assertThrown(() -> cm17a.command(Command.allLightsOff(C)));
		assertThrown(() -> cm17a.command(Command.ext(D, 1, 2, _7)));
	}

	@Test
	public void shouldListenForCommands() throws IOException, InterruptedException {
		init();
		TestCommandListener listener = TestCommandListener.of();
		try (var _ = cm17a.listen(listener)) {
			cm17a.command(Command.dim(L, 50, _5, _9));
			assertEquals(listener.sync.await(), Command.dim(L, 50, _5, _9));
		}
	}

	@Test
	public void shouldListenForConnectorStateChange() {
		init();
		CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
		try (var _ = cm17a.listeners().enclose(sync::accept)) {
			con.listeners.accept(StateChange.broken);
			sync.assertCall(StateChange.broken);
		}
	}

	private void init() {
		con = Cm17aTestConnector.of();
		cm17a = Cm17aDevice.of(Cm17aDevice.Config.NULL, con);
	}
}
