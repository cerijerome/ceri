package ceri.ci.audio;

import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AudioListenerBehavior {
	@Mock
	private AudioListener listener1;
	@Mock
	private AudioListener listener2;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.openMocks(this).close();
	}

	@Test
	public void shouldPropagateMultiAudioStartToListeners() {
		AudioListener.Multi multi = new AudioListener.Multi(listener1, listener2);
		multi.audioStart();
		verify(listener1).audioStart();
		verify(listener2).audioStart();
	}

	@Test
	public void shouldPropagateMultiVoiceStartToListeners() {
		AudioListener.Multi multi = new AudioListener.Multi(listener1, listener2);
		multi.voiceStart();
		verify(listener1).voiceStart();
		verify(listener2).voiceStart();
	}

	@Test
	public void shouldPropagateMultiAudioEndToListeners() {
		AudioListener.Multi multi = new AudioListener.Multi(listener1, listener2);
		multi.audioEnd();
		verify(listener1).audioEnd();
		verify(listener2).audioEnd();
	}

}
