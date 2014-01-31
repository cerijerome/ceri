package ceri.ci.audio;

import java.io.IOException;

public interface AudioPlayer {

	void play(byte[] data) throws IOException;
	
}
