package ceri.ci.audio;

import java.io.File;
import java.io.IOException;

public class AudioFactoryImpl implements AudioFactory {
	private static final AudioPlayer player = new AudioPlayer() {
		@Override
		public void play(Audio audio) throws IOException {
			audio.play();
		}
	};

	@Override
	public AudioMessages createMessages(File soundDir, float pitch) throws IOException {
		return new AudioMessages(player, soundDir);
	}
	
	@Override
	public AudioAlerter createAlerter(AudioMessages message) {
		return null;
	}
}
