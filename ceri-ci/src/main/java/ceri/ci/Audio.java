package ceri.ci;

import java.io.File;
import java.util.Properties;
import ceri.ci.audio.AudioAlerter;
import ceri.ci.audio.AudioAlerterProperties;
import ceri.ci.audio.AudioMessage;
import ceri.ci.audio.AudioPlayer;
import ceri.common.io.IoUtil;

/**
 * Creates audio alerter.
 */
public class Audio {
	public final AudioAlerter alerter;

	public Audio(Properties properties) {
		AudioAlerterProperties audioProperties = new AudioAlerterProperties(properties, "audio");
		if (!audioProperties.enabled()) {
			alerter = null;
		} else {
			File soundDir = IoUtil.getPackageDir(AudioMessage.class);
			AudioPlayer player = new AudioPlayer.Default();
			AudioMessage message = new AudioMessage(player, soundDir, audioProperties.pitch());
			alerter = new AudioAlerter(message);
		}
	}

}
