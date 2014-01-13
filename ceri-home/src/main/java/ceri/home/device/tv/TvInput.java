package ceri.home.device.tv;

import ceri.speech.grammar.SpeechItem;

public class TvInput extends SpeechItem<TvInput> {

	public TvInput(int number, String name, String... spokenWords) {
		super(TvInput.class, number, name, spokenWords);
	}

}
