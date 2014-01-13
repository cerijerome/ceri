package ceri.home.device.receiver;

import ceri.speech.grammar.SpeechItem;

public class SurroundMode extends SpeechItem<SurroundMode> {

	public SurroundMode(int number, String name, String... spokenWords) {
		super(SurroundMode.class, number, name, spokenWords);
	}

}
