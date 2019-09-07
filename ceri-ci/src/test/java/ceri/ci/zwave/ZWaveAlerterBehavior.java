package ceri.ci.zwave;

import static ceri.common.test.TestUtil.assertException;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ZWaveAlerterBehavior {
	@Mock private ZWaveController controller;
	private ZWaveAlerter alerter;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		alerter =
			ZWaveAlerter.builder(controller).device("1", 1).device("2", 2).device("3", 3)
				.randomize(0, 0).build();
	}

	@Test
	public void shouldTurnOffDevicesAfterRandomize() {
		Set<Integer> activeDevices = new HashSet<>();
		controller = captureController(activeDevices);
		alerter =
			ZWaveAlerter.builder(controller).device("1", 1).device("2", 2).randomize(0, 10).build();
		alerter.alert("3");
		assertTrue(activeDevices.isEmpty());
	}

	@Test
	public void shouldFailToBuildWithInvalidAddress() {
		final ZWaveAlerter.Builder builder = ZWaveAlerter.builder(controller);
		assertException(() -> builder.device(null, 0));
		assertException(() -> builder.device("x", -1));
	}

	@Test
	public void shouldIgnoreUntrackedDevices() throws IOException {
		alerter.alert("0", "3");
		verify(controller).on(3);
		verifyNoMoreInteractions(controller);
		alerter.alert("4", "5");
		verify(controller).off(3);
		verifyNoMoreInteractions(controller);
	}

	@Test
	public void shouldSwitchOffActiveDevicesWithBlankAlert() throws IOException {
		alerter.alert("1", "3");
		verify(controller).on(1);
		verify(controller).on(3);
		verifyNoMoreInteractions(controller);
		alerter.alert(Collections.emptySet());
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
		alerter.alert("2", "3");
		verify(controller).on(2);
		verify(controller).on(3);
		verifyNoMoreInteractions(controller);
		alerter.alert("1", "2");
		verify(controller).on(1);
		verify(controller).off(3);
		verifyNoMoreInteractions(controller);
	}

	private ZWaveController captureController(final Set<Integer> activeDevices) {
		return new ZWaveController() {
			@Override
			public void on(int device) {
				activeDevices.add(device);
			}

			@Override
			public void off(int device) {
				activeDevices.remove(device);
			}
		};
	}

}
