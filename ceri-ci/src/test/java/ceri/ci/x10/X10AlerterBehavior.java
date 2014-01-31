package ceri.ci.x10;

import static ceri.common.test.TestUtil.assertException;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import x10.Command;
import x10.Controller;
import ceri.x10.X10ControllerType;

public class X10AlerterBehavior {
	Controller controller;

	@Before
	public void init() {
		controller = mock(Controller.class);
	}

	@Test
	public void shouldFailToBuildWithInvalidAddress() {
		final X10Alerter.Builder builder = X10Alerter.builder("123");
		assertException(new Runnable() {
			@Override
			public void run() {
				builder.address(null, "A1");
			}
		});
		assertException(new Runnable() {
			@Override
			public void run() {
				builder.address("x", "A0");
			}
		});
	}

	@Test
	public void shouldCreateFromProperties() throws IOException {
		Properties properties = new Properties();
		properties.put("comm.port", "123");
		properties.put("controller", "cm17a");
		properties.put("address.x", "A16");
		X10Alerter.Builder builder = X10Alerter.builder(properties, null);
		try (X10Alerter x10 = createX10Alerter(builder)) {
			assertNotNull(x10);
		}
	}

	@Test
	public void shouldTurnOnDeviceForGivenKeyAlert() throws IOException {
		X10Alerter.Builder builder = X10Alerter.builder("a").address("ceri", "F13");
		try (X10Alerter x10 = createX10Alerter(builder)) {
			x10.alert("ceri");
		}
		InOrder inOrder = inOrder(controller);
		inOrder.verify(controller).addCommand(new Command("F1", Command.ALL_UNITS_OFF));
		inOrder.verify(controller).addCommand(new Command("F13", Command.ON));
		inOrder.verify(controller).close();
		controller.close();
	}

	@Test
	public void shouldNotTurnOnDeviceForMissingKeyAlert() throws IOException {
		X10Alerter.Builder builder = X10Alerter.builder("a").address("ceri", "F13");
		try (X10Alerter x10 = createX10Alerter(builder)) {
			x10.alert("xxx");
		}
		InOrder inOrder = inOrder(controller);
		inOrder.verify(controller).addCommand(new Command("F1", Command.ALL_UNITS_OFF));
		inOrder.verify(controller).close();
		verifyNoMoreInteractions(controller);
	}

	@Test
	public void shouldNotTurnOnDeviceForEmptyKeyAlert() throws IOException {
		X10Alerter.Builder builder =
			X10Alerter.builder("a").address("ceri1", "F13").address("ceri2", "P16");
		try (X10Alerter x10 = createX10Alerter(builder)) {
			x10.alert();
		}
		verify(controller).addCommand(new Command("F1", Command.ALL_UNITS_OFF));
		verify(controller).addCommand(new Command("P1", Command.ALL_UNITS_OFF));
		verify(controller).close();
		verifyNoMoreInteractions(controller);
	}

	@Test
	public void shouldTurnOnDevicesForGivenKeyAlerts() throws IOException {
		X10Alerter.Builder builder =
			X10Alerter.builder("a").address("ceri1", "F13").address("ceri2", "P16");
		try (X10Alerter x10 = createX10Alerter(builder)) {
			x10.alert("ceri2", "ceri1");
		}
		verify(controller).addCommand(new Command("F1", Command.ALL_UNITS_OFF));
		verify(controller).addCommand(new Command("P1", Command.ALL_UNITS_OFF));
		InOrder inOrder = inOrder(controller);
		inOrder.verify(controller).addCommand(new Command("P16", Command.ON));
		inOrder.verify(controller).addCommand(new Command("F13", Command.ON));
		inOrder.verify(controller).close();
		verifyNoMoreInteractions(controller);
	}

	@Test
	public void shouldTurnOffDevicesForClearAlerts() throws IOException {
		X10Alerter.Builder builder =
			X10Alerter.builder("a").address("ceri1", "F13").address("ceri2", "P16");
		try (X10Alerter x10 = createX10Alerter(builder)) {
			x10.clear();
		}
		verify(controller).addCommand(new Command("F1", Command.ALL_UNITS_OFF));
		verify(controller).addCommand(new Command("P1", Command.ALL_UNITS_OFF));
		verify(controller).close();
		verifyNoMoreInteractions(controller);
	}

	private X10Alerter createX10Alerter(X10Alerter.Builder builder) throws IOException {
		return new X10Alerter(builder) {
			@Override
			Controller createController(String commPort, X10ControllerType controllerType)
				throws IOException {
				return controller;
			}
		};
	}
}
