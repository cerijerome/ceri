package ceri.x10.cm17a.device;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.util.Enclosed;
import ceri.x10.command.CommandFactory;
import ceri.x10.command.CommandListener;

public class Cm17aDeviceBehavior {
	public static final Cm17aDeviceConfig config = Cm17aDeviceConfig.builder().waitIntervalMs(1)
		.resetIntervalMs(1).errorDelayMs(1).commandIntervalMs(1).build();
	private static CommandListener listener;
	private static Cm17aDevice controller;
	private static Enclosed<CommandListener> enclosed;

	@BeforeClass
	public static void beforeClass() {
		Cm17aConnector connector = mock(Cm17aConnector.class);
		listener = mock(CommandListener.class);
		controller = Cm17aDevice.of(config, connector);
		enclosed = controller.listen(listener);
	}

	@AfterClass
	public static void afterClass() {
		enclosed.close();
		controller.close();
	}

	@Test
	public void shouldDispatchUnitCommands() {
		controller.command(CommandFactory.on("K9"));
		verify(listener, timeout(1000)).on(CommandFactory.on("K9"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldFailForUnsupportedCommands() {
		controller.command(CommandFactory.allLightsOff('A'));
	}

}
