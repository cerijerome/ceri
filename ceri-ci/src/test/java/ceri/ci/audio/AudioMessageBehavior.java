package ceri.ci.audio;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.test.TestThread;

public class AudioMessageBehavior {
	
	@Test
	public void shouldFailIfInterrupted() throws Exception {
		final AudioMessage audio = new AudioMessage();
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
		} catch (IOException e) {
			// expected
		}
	}

	
	@Test
	public void shouldPlayAudioMessages() throws IOException {
		AudioMessage audio = new AudioMessage() {
			@Override
			void play(byte[] data) throws IOException {
				// do nothing
			}
		};
		audio.playAlarm();
		audio.playJustFixed("test", "test", Arrays.asList("test"));
		audio.playJustBroken("test", "test", Arrays.asList("test"));
		audio.playStillBroken("test", "test", Arrays.asList("test"));
	}

}
