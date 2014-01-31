package ceri.ci.audio;

import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.io.IoUtil;
import ceri.common.test.TestThread;

public class AudioMessageBehavior {
	private static final File dir = IoUtil.getPackageDir(AudioMessageBehavior.class);
	
	@Test
	public void shouldFailIfInterrupted() throws Throwable {
		final AudioMessage audio = new AudioMessage(dir);
		TestThread thread = new TestThread() {
			@Override
			protected void run() throws Exception {
				audio.playAlarm();
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
		AudioMessage audio = new AudioMessage(dir) {
			@Override
			void play(Audio audio) throws IOException {
				// do nothing
			}
		};
		audio.playAlarm();
		audio.playJustFixed("test", "test", Arrays.asList("test"));
		audio.playJustBroken("x", "test", Arrays.asList("test"));
		audio.playStillBroken("test", "x", Arrays.asList("test"));
	}

}
