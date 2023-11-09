package ceri.x10.cm17a.device;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RTX;
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
import org.junit.Before;
import org.junit.Test;
import ceri.common.io.StateChange;
import ceri.common.test.CallSync;
import ceri.log.test.LogModifier;
import ceri.x10.command.Command;
import ceri.x10.command.TestCommandListener;

public class Cm17aDeviceBehavior {
	private Cm17aTestConnector con;
	private Cm17aDevice cm17a;

	@Before
	public void before() {
		con = Cm17aTestConnector.of();
		cm17a = Cm17aDevice.of(Cm17aDeviceConfig.NULL, con);
	}

	@After
	public void after() throws IOException {
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
	public void shouldResetOnError() throws IOException {
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
		assertThrown(() -> cm17a.command(Command.allLightsOff(C)));
		assertThrown(() -> cm17a.command(Command.ext(D, 1, 2, _7)));
	}

	@Test
	public void shouldListenForCommands() throws IOException, InterruptedException {
		TestCommandListener listener = TestCommandListener.of();
		try (var enc = cm17a.listen(listener)) {
			cm17a.command(Command.dim(L, 50, _5, _9));
			assertEquals(listener.sync.await(), Command.dim(L, 50, _5, _9));
		}
	}

	@Test
	public void shouldListenForConnectorStateChange() {
		CallSync.Consumer<StateChange> sync = CallSync.consumer(null, true);
		try (var listener = cm17a.listeners().enclose(sync::accept)) {
			con.listeners.accept(StateChange.broken);
			sync.assertCall(StateChange.broken);
		}
	}

}
