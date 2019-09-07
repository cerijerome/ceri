package ceri.x10.cm17a;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.x10.command.CommandFactory;
import ceri.x10.command.CommandListener;

public class Cm17aControllerBehavior {
	private CommandListener listener;
	private Cm17aController controller;

	@Before
	public void init() throws IOException {
		Cm17aConnector connector = mock(Cm17aConnector.class);
		listener = mock(CommandListener.class);
		controller = new Cm17aController(connector, listener);

	}
	
	@After
	public void stop() {
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
