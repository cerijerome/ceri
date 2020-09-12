package ceri.x10.cm11a.device;

import static ceri.x10.util.X10TestUtil.addr;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.x10.cm11a.entry.Data;
import ceri.x10.command.Command;
import ceri.x10.command.CommandListener;

public class Cm11aDeviceBehavior {
	public static final Cm11aDeviceConfig config =
		Cm11aDeviceConfig.builder().readPollMs(1).build();
	private static Cm11aTestConnector connector;
	private static CommandListener listener;
	private static Cm11aDevice controller;
	private static Enclosed<CommandListener> enclosed;

	@BeforeClass
	public static void beforeClass() {
		connector = new Cm11aTestConnector();
		listener = mock(CommandListener.class);
		controller = Cm11aDevice.of(config, connector);
		enclosed = controller.listen(listener);
	}

	@Before
	public void before() {
		connector.clear();
	}

	@AfterClass
	public static void afterClass() {
		enclosed.close();
		controller.close();
		connector.close();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldDispatchUnitCommands() {
		controller.command(Command.on(addr("K9")));
		assertThat(connector.from.readUshortMsb(), is(0x0437));
		connector.to.writeByte(Data.shortChecksum(0x0437));
		assertThat(connector.from.readUbyte(), is((short) 0));
		connector.to.writeByte(0x55);
		assertThat(connector.from.readUshortMsb(), is(0x0632));
		connector.to.writeByte(Data.shortChecksum(0x0632));
		assertThat(connector.from.readUbyte(), is((short) 0));
		connector.to.writeByte(0x55);
		verify(listener, timeout(1000)).on(Command.on(addr("K9")));
	}

}
