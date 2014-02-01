package ceri.ci.audio;

import java.io.IOException;

public interface AudioPlayer {

	void play(Audio audio) throws IOException;
	
	static class Default implements AudioPlayer {
		@Override
		public void play(Audio audio) throws IOException {
			audio.play();
		}
	}
}
