package ceri.ci.x10;

import static ceri.common.test.TestUtil.assertException;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import ceri.x10.command.CommandFactory;
import ceri.x10.util.X10Controller;

public class X10AlerterBehavior {
	X10Controller controller;

	@Before
	public void init() {
		controller = mock(X10Controller.class);
	}

	@Test
	public void shouldFailToBuildWithInvalidAddress() {
		final X10Alerter.Builder builder = X10Alerter.builder(controller);
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
	public void shouldTurnOnDeviceForGivenKeyAlert() {
		X10Alerter x10 = X10Alerter.builder(controller).address("ceri", "F13").build();
		x10.alert("ceri");
		InOrder inOrder = inOrder(controller);
		inOrder.verify(controller).command(CommandFactory.allUnitsOff('F'));
		inOrder.verify(controller).command(CommandFactory.on("F13"));
	}

	@Test
	public void shouldNotTurnOnDeviceForMissingKeyAlert() {
		X10Alerter x10 = X10Alerter.builder(controller).address("ceri", "F13").build();
		x10.alert("xxx");
		InOrder inOrder = inOrder(controller);
		inOrder.verify(controller).command(CommandFactory.allUnitsOff('F'));
	}

	@Test
	public void shouldNotTurnOnDeviceForEmptyKeyAlert() {
		X10Alerter x10 =
			X10Alerter.builder(controller).address("ceri1", "F13").address("ceri2", "P16").build();
		x10.alert();
		verify(controller).command(CommandFactory.allUnitsOff('F'));
		verify(controller).command(CommandFactory.allUnitsOff('P'));
	}

	@Test
	public void shouldTurnOnDevicesForGivenKeyAlerts() {
		X10Alerter x10 =
			X10Alerter.builder(controller).address("ceri1", "F13").address("ceri2", "P16").build();
		x10.alert("ceri2", "ceri1");
		verify(controller).command(CommandFactory.allUnitsOff('F'));
		verify(controller).command(CommandFactory.allUnitsOff('P'));
		InOrder inOrder = inOrder(controller);
		inOrder.verify(controller).command(CommandFactory.on("P16"));
		inOrder.verify(controller).command(CommandFactory.on("F13"));
	}

	@Test
	public void shouldTurnOffDevicesForClearAlerts() {
		X10Alerter x10 =
			X10Alerter.builder(controller).address("ceri1", "F13").address("ceri2", "P16").build();
		x10.clear();
		verify(controller).command(CommandFactory.allUnitsOff('F'));
		verify(controller).command(CommandFactory.allUnitsOff('P'));
	}

}
