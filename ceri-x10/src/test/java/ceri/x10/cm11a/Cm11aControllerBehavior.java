package ceri.x10.cm11a;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.x10.cm11a.protocol.Data;
import ceri.x10.command.CommandFactory;
import ceri.x10.command.CommandListener;

public class Cm11aControllerBehavior {
	private Cm11aTestConnector connector;
	private CommandListener listener;
	private Cm11aController controller;

	@Before
	public void init() throws IOException {
		connector = new Cm11aTestConnector(1, 3000);
		listener = mock(CommandListener.class);
		controller = new Cm11aController(connector, listener);
	}

	@After
	public void stop() {
		controller.close();
	}

	@Test
	public void shouldDispatchUnitCommands() {
		controller.command(CommandFactory.on("K9"));
		assertThat(connector.from.readShortMsb(), is((short) 0x0437));
		connector.to.writeByte(Data.shortChecksum(0x0437));
		assertThat(connector.from.readByte(), is((byte) 0));
		connector.to.writeByte(0x55);
		assertThat(connector.from.readShortMsb(), is((short) 0x0632));
		connector.to.writeByte(Data.shortChecksum(0x0632));
		assertThat(connector.from.readByte(), is((byte) 0));
		connector.to.writeByte(0x55);
		verify(listener, timeout(1000)).on(CommandFactory.on("K9"));
	}

}
