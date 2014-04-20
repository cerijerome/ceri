package ceri.ci.audio;

import java.io.Closeable;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.IoUtil;
import ceri.common.property.BaseProperties;

/**
 * Creates the audio alerter and its required components.
 */
public class AudioContainer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final String GROUP = "audio";
	public final AudioAlerter alerter;

	public AudioContainer(BaseProperties properties) {
		AudioAlerterProperties audioProperties = new AudioAlerterProperties(properties, GROUP);
		if (!audioProperties.enabled()) {
			logger.info("Audio alerter disabled");
			alerter = null;
		} else {
			logger.info("Creating audio message player");
			File soundDir =
				new File(IoUtil.getPackageDir(AudioMessage.class), audioProperties.voice());
			AudioPlayer player = new AudioPlayer.Default();
			AudioMessage message = new AudioMessage(player, soundDir, audioProperties.pitch());
			logger.info("Creating audio alerter");
			alerter = new AudioAlerter(message);
		}
	}

	@Override
	public void close() {
		if (alerter != null) {
			logger.info("Closing audio alerter");
			IoUtil.close(alerter);
		}
	}

}
