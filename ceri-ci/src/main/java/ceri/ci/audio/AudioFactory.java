package ceri.ci.audio;

import java.io.File;
import java.io.IOException;

public interface AudioFactory {

	AudioMessages createMessages(File soundDir, float pitch) throws IOException;
	AudioAlerter createAlerter(AudioMessages message, AudioListener listener);
	
}
