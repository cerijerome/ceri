package ceri.ci.zwave;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class ZWaveAlerterBehavior {
	private ZWaveController zwave;
	private ZWaveAlerter alerter;

	@Before
	public void before() {
		zwave = mock(ZWaveController.class);
		alerter =
			ZWaveAlerter.builder(zwave).device("1", 1).device("2", 2).device("3", 3).build();
	}

	@Test
	public void shouldIgnoreUntrackedDevices() throws IOException {
		alerter.alert(Arrays.asList("0", "3"));
		verify(zwave).on(3);
		verifyNoMoreInteractions(zwave);
		alerter.alert(Arrays.asList("4", "5"));
		verify(zwave).off(3);
		verifyNoMoreInteractions(zwave);
	}

	@Test
	public void shouldSwitchOffActiveDevicesWithBlankAlert() throws IOException {
		alerter.alert(Arrays.asList("1", "3"));
		verify(zwave).on(1);
		verify(zwave).on(3);
		verifyNoMoreInteractions(zwave);
		alerter.alert(Collections.<String>emptySet());
		verify(zwave).off(1);
		verify(zwave).off(3);
		verifyNoMoreInteractions(zwave);
	}

	@Test
	public void shouldSwitchOffAllTrackedDevicesOnClear() throws IOException {
		alerter.clear();
		verify(zwave).off(1);
		verify(zwave).off(2);
		verify(zwave).off(3);
		verifyNoMoreInteractions(zwave);
	}

	@Test
	public void shouldOnlySwitchDevicesThatChangeOnAlert() throws IOException {
		alerter.alert(Arrays.asList("2", "3"));
		verify(zwave).on(2);
		verify(zwave).on(3);
		verifyNoMoreInteractions(zwave);
		alerter.alert(Arrays.asList("1", "2"));
		verify(zwave).on(1);
		verify(zwave).off(3);
		verifyNoMoreInteractions(zwave);
	}

}
