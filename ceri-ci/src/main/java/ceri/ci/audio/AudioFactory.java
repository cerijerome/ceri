package ceri.ci.audio;

import java.io.IOException;

public interface AudioFactory {

	AudioMessages createMessages(String voiceDir, float pitch) throws IOException;
	AudioAlerter createAlerter(AudioMessages message, AudioListener listener);
	
}
