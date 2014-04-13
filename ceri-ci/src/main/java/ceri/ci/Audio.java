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

	public Audio(Properties properties, String prefix) {
		AudioAlerterProperties audioProperties =
			new AudioAlerterProperties(properties, prefix, "audio");
		if (!audioProperties.enabled()) {
			alerter = null;
		} else {
			File soundDir =
				new File(IoUtil.getPackageDir(AudioMessage.class), audioProperties.voice());
			AudioPlayer player = new AudioPlayer.Default();
			AudioMessage message = new AudioMessage(player, soundDir, audioProperties.pitch());
			alerter = new AudioAlerter(message);
		}
	}

}
