package ceri.ci.audio;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
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

	public AudioContainer(BaseProperties properties, AudioListener listener) throws IOException {
		this(properties, new AudioFactoryImpl(), listener);
	}

	public AudioContainer(BaseProperties properties, AudioFactory factory, AudioListener listener)
		throws IOException {
		AudioProperties audioProperties = new AudioProperties(properties, GROUP);
		if (!audioProperties.enabled()) {
			logger.info("Audio alerter disabled");
			alerter = null;
		} else {
			logger.info("Creating audio message player");
			File soundDir =
				new File(IoUtil.getPackageDir(AudioMessages.class), audioProperties.voice());
			AudioMessages message = factory.createMessages(soundDir, audioProperties.pitch());
			logger.info("Creating audio alerter");
			alerter = factory.createAlerter(message, listener);
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
