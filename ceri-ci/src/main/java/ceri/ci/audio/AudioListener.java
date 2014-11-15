package ceri.ci.audio;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import ceri.common.collection.ImmutableUtil;

public interface AudioListener {

	void audioStart();

	void voiceStart();

	void audioEnd();

	static class Multi implements AudioListener {
		private final List<AudioListener> listeners;

		public Multi(AudioListener... listeners) {
			this(Arrays.asList(listeners));
		}

		public Multi(Collection<AudioListener> listeners) {
			this.listeners = ImmutableUtil.copyAsList(listeners);
		}

		@Override
		public void audioStart() {
			for (AudioListener listener : listeners)
				listener.audioStart();
		}

		@Override
		public void voiceStart() {
			for (AudioListener listener : listeners)
				listener.voiceStart();
		}

		@Override
		public void audioEnd() {
			for (AudioListener listener : listeners)
				listener.audioEnd();
		}

	}

}
