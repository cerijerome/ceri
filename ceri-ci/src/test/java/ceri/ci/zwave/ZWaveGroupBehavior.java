package ceri.ci.zwave;

import static org.mockito.Mockito.verify;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.audio.AudioListener;

public class ZWaveGroupBehavior {
	@Mock private ZWaveController controller;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldCreateAudioListener() throws IOException {
		ZWaveGroup group = new ZWaveGroup(controller, 7, 33);
		AudioListener listener = group.createAudioListener();
		listener.audioStart();
		verify(controller).on(7);
		verify(controller).on(33);
		listener.audioEnd();
		verify(controller).off(7);
		verify(controller).off(33);
	}

}
