package ceri.ci.audio;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.io.IoUtil;
import ceri.common.test.TestThread;

public class AudioMessageBehavior {
	private static final File dir = IoUtil.getPackageDir(AudioMessageBehavior.class);
	private AudioPlayer player;
	
	@Before
	public void init() {
		player = mock(AudioPlayer.class);
	}
	
	@Test
	public void shouldFailIfInterrupted() throws Throwable {
		final AudioMessage audio = new AudioMessage(player, dir);
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
		AudioMessage audio = new AudioMessage(player, dir);
		audio.playRandomAlarm();
		audio.playJustFixed("test", "test", Arrays.asList("test"));
		audio.playJustBroken("x", "test", Arrays.asList("test"));
		audio.playStillBroken("test", "x", Arrays.asList("test"));
	}

}
