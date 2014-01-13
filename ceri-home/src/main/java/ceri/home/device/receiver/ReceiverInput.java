package ceri.home.device.receiver;

import ceri.speech.grammar.SpeechItem;

public class ReceiverInput extends SpeechItem<ReceiverInput> {
	public final int volume;

	public ReceiverInput(int number, String name, int volume, String... spokenWords) {
		super(ReceiverInput.class, number, name, spokenWords);
		this.volume = volume;
	}

}
