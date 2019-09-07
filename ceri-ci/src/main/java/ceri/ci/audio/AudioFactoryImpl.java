package ceri.ci.audio;

import java.io.IOException;

public class AudioFactoryImpl implements AudioFactory {
	private static final AudioPlayer player = Audio::play;
	private final Class<?> resourceCls;

	public AudioFactoryImpl(Class<?> resourceCls) {
		this.resourceCls = resourceCls;
	}

	@Override
	public AudioMessages createMessages(String voiceDir, float pitch) throws IOException {
		return new AudioMessages(player, resourceCls, voiceDir, pitch);
	}

	@Override
	public AudioAlerter createAlerter(AudioMessages messages, AudioListener listener) {
		return new AudioAlerter(messages, listener);
	}
}
