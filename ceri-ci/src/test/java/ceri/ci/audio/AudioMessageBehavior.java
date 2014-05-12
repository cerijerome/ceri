package ceri.ci.audio;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.test.TestThread;

public class AudioMessageBehavior {
	private AudioPlayer player;
	
	@Before
	public void init() {
		player = mock(AudioPlayer.class);
	}
	
	@Test
	public void shouldFailIfInterrupted() throws Throwable {
		final AudioMessages audio = new AudioMessages(player, getClass(), "");
		TestThread thread = new TestThread() {
			@Override
			protected void run() throws Exception {
				audio.playRandomAlarm();
			}
		};
		thread.interrupt();
		thread.start();
		try {
			thread.stop();
			fail();
		} catch (RuntimeInterruptedException e) {
			// expected
		}
	}

	@Test
	public void shouldPlayAudioMessages() throws IOException {
		AudioMessages audio = new AudioMessages(player, getClass(), "");
		audio.playRandomAlarm();
		verify(player).play(any(Audio.class));
		audio.playJustFixed("build", "job", Arrays.asList("name"));
		verify(player, atLeast(3)).play(any(Audio.class));
		audio.playJustBroken("build2", "job", Arrays.asList("name"));
		verify(player, atLeast(3)).play(any(Audio.class));
		audio.playStillBroken("build", "job2", Arrays.asList("name"));
		verify(player, atLeast(3)).play(any(Audio.class));
	}

}
