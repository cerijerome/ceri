package ceri.ci.x10;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.util.Properties;
import org.junit.Test;
import org.mockito.InOrder;
import x10.Command;
import x10.Controller;

public class X10AlerterBehavior {
	
	@Test
	public void shouldTurnOnDeviceForGivenKeyAlert() throws IOException {
		Properties p = new Properties();
		p.setProperty("address.ceri", "F13");
		
		Controller controller = mock(Controller.class);
		try (X10Alerter x10 = new X10Alerter(p, controller)) {
			x10.alert("ceri");
		}
		InOrder inOrder = inOrder(controller);
		inOrder.verify(controller).addCommand(new Command("F1", Command.ALL_UNITS_OFF));
		inOrder.verify(controller).addCommand(new Command("F13", Command.ON));
		inOrder.verify(controller).close();
		verifyNoMoreInteractions(controller);
	}

	@Test
	public void shouldNotTurnOnDeviceForMissingKeyAlert() throws IOException {
		Properties p = new Properties();
		p.setProperty("address.ceri", "F13");
		
		Controller controller = mock(Controller.class);
		try (X10Alerter x10 = new X10Alerter(p, controller)) {
			x10.alert("xxx");
		}
		InOrder inOrder = inOrder(controller);
		inOrder.verify(controller).addCommand(new Command("F1", Command.ALL_UNITS_OFF));
		inOrder.verify(controller).close();
		verifyNoMoreInteractions(controller);
	}

	@Test
	public void shouldNotTurnOnDeviceForEmptyKeyAlert() throws IOException {
		Properties p = new Properties();
		p.setProperty("address.ceri1", "F13");
		p.setProperty("address.ceri2", "P16");
		
		Controller controller = mock(Controller.class);
		try (X10Alerter x10 = new X10Alerter(p, controller)) {
			x10.alert();
		}
		verify(controller).addCommand(new Command("F1", Command.ALL_UNITS_OFF));
		verify(controller).addCommand(new Command("P1", Command.ALL_UNITS_OFF));
		verify(controller).close();
		verifyNoMoreInteractions(controller);
	}

	@Test
	public void shouldTurnOnDevicesForGivenKeyAlerts() throws IOException {
		Properties p = new Properties();
		p.setProperty("address.ceri1", "P16");
		p.setProperty("address.ceri2", "F16");
		
		Controller controller = mock(Controller.class);
		try (X10Alerter x10 = new X10Alerter(p, controller)) {
			x10.alert("ceri1", "ceri2");
		}
		verify(controller).addCommand(new Command("F1", Command.ALL_UNITS_OFF));
		verify(controller).addCommand(new Command("P1", Command.ALL_UNITS_OFF));
		InOrder inOrder = inOrder(controller);
		inOrder.verify(controller).addCommand(new Command("P16", Command.ON));
		inOrder.verify(controller).addCommand(new Command("F16", Command.ON));
		inOrder.verify(controller).close();
		verifyNoMoreInteractions(controller);
	}

	@Test
	public void shouldTurnOffDevicesForClearAlerts() throws IOException {
		Properties p = new Properties();
		p.setProperty("address.ceri1", "P16");
		p.setProperty("address.ceri2", "F16");
		
		Controller controller = mock(Controller.class);
		try (X10Alerter x10 = new X10Alerter(p, controller)) {
			x10.clear("ceri1", "ceri2");
		}
		verify(controller).addCommand(new Command("F1", Command.ALL_UNITS_OFF));
		verify(controller).addCommand(new Command("P1", Command.ALL_UNITS_OFF));
		verify(controller).close();
		verifyNoMoreInteractions(controller);
	}

}
