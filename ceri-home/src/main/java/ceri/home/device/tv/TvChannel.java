package ceri.home.device.tv;

import ceri.speech.grammar.SpeechItem;

public class TvChannel extends SpeechItem<TvChannel> {

	public TvChannel(int number, String name, String... spokenWords) {
		super(TvChannel.class, number, name, spokenWords);
	}

}
