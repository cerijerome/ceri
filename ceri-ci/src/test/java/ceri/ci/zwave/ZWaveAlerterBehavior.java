package ceri.ci.zwave;

import static ceri.common.test.TestUtil.assertException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class ZWaveAlerterBehavior {
	ZWaveController controller;
	private ZWaveAlerter alerter;

	@Before
	public void before() {
		controller = mock(ZWaveController.class);
		alerter =
			ZWaveAlerter.builder(controller).device("1", 1).device("2", 2).device("3", 3).build();
	}

	@Test
	public void shouldFailToBuildWithInvalidAddress() {
		final ZWaveAlerter.Builder builder = ZWaveAlerter.builder(controller);
		assertException(() -> builder.device(null, 0));
		assertException(() -> builder.device("x", -1));
	}

	@Test
	public void shouldIgnoreUntrackedDevices() throws IOException {
		alerter.alert(Arrays.asList("0", "3"));
		verify(controller).on(3);
		verifyNoMoreInteractions(controller);
		alerter.alert(Arrays.asList("4", "5"));
		verify(controller).off(3);
		verifyNoMoreInteractions(controller);
	}

	@Test
	public void shouldSwitchOffActiveDevicesWithBlankAlert() throws IOException {
		alerter.alert(Arrays.asList("1", "3"));
		verify(controller).on(1);
		verify(controller).on(3);
		verifyNoMoreInteractions(controller);
		alerter.alert(Collections.<String>emptySet());
		verify(controller).off(1);
		verify(controller).off(3);
		verifyNoMoreInteractions(controller);
	}

	@Test
	public void shouldSwitchOffAllTrackedDevicesOnClear() throws IOException {
		alerter.clear();
		verify(controller).off(1);
		verify(controller).off(2);
		verify(controller).off(3);
		verifyNoMoreInteractions(controller);
	}

	@Test
	public void shouldOnlySwitchDevicesThatChangeOnAlert() throws IOException {
		alerter.alert(Arrays.asList("2", "3"));
		verify(controller).on(2);
		verify(controller).on(3);
		verifyNoMoreInteractions(controller);
		alerter.alert(Arrays.asList("1", "2"));
		verify(controller).on(1);
		verify(controller).off(3);
		verifyNoMoreInteractions(controller);
	}

}
