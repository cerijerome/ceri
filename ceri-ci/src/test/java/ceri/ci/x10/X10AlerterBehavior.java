package ceri.ci.x10;

import static ceri.common.test.AssertUtil.assertThrown;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import ceri.x10.command.Address;
import ceri.x10.command.Command;
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
		assertThrown(() -> builder.address(null, "A1"));
		assertThrown(() -> builder.address("x", "A0"));
	}

	@Test
	public void shouldTurnOnDeviceForGivenKeyAlert() throws IOException {
		X10Alerter x10 = X10Alerter.builder(controller).address("key", "F13").build();
		x10.alert("key");
		InOrder inOrder = inOrder(controller);
		inOrder.verify(controller).command(Command.on(addr("F13")));
	}

	@Test
	public void shouldNotTurnOnDeviceForMissingKeyAlert() {
		X10Alerter x10 = X10Alerter.builder(controller).address("key", "F13").build();
		x10.alert("xxx");
		verifyZeroInteractions(controller);
	}

	@Test
	public void shouldNotTurnOnDeviceForEmptyKeyAlert() {
		X10Alerter x10 =
			X10Alerter.builder(controller).address("key1", "F13").address("key2", "P16").build();
		x10.alert();
		verifyZeroInteractions(controller);
	}

	@Test
	public void shouldTurnOffDevicesForFixedKeyAlerts() throws IOException {
		X10Alerter x10 =
			X10Alerter.builder(controller).address("key1", "F13").address("key2", "P16").build();
		x10.alert("key1");
		verify(controller).command(Command.on(addr("F13")));
		x10.alert("key2");
		verify(controller).command(Command.off(addr("F13")));
		verify(controller).command(Command.on(addr("P16")));
	}

	@Test
	public void shouldTurnOnDevicesForGivenKeyAlerts() throws IOException {
		X10Alerter x10 =
			X10Alerter.builder(controller).address("key1", "F13").address("key2", "P16").build();
		x10.alert("key2", "key1");
		verify(controller).command(Command.on(addr("P16")));
		verify(controller).command(Command.on(addr("F13")));
	}

	@Test
	public void shouldTurnOffDevicesForClearAlerts() throws IOException {
		X10Alerter x10 =
			X10Alerter.builder(controller).address("key1", "F13").address("key2", "P16").build();
		x10.alert("key1", "key2");
		x10.clear();
		verify(controller).command(Command.off(addr("F13")));
		verify(controller).command(Command.off(addr("P16")));
	}

	private static Address addr(String address) {
		return Address.from(address);
	}
}
