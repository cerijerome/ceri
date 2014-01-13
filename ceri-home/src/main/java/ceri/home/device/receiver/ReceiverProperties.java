package ceri.home.device.receiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import ceri.common.property.BaseProperties;
import ceri.speech.grammar.SpeechItem;

public class ReceiverProperties extends BaseProperties {
	private static final String VOLUME = "volume";
	private static final String MAX = "max";
	private static final String MIN = "min";
	private static final String INPUT = "input";
	private static final String SURROUND_MODE = "surroundMode";
	private static final String NAME = "name";
	private static final String SPEECH = "speech";
	private final List<ReceiverInput> inputs;
	private final List<SurroundMode> surroundModes;

	public ReceiverProperties(Properties properties, String prefix) {
		super(properties, prefix);
		inputs = createInputs();
		surroundModes = createSurroundModes();
	}

	public int minVolume() {
		String value = value(VOLUME, MIN);
		return Integer.parseInt(value);
	}

	public int maxVolume() {
		String value = value(VOLUME, MAX);
		return Integer.parseInt(value);
	}

	public Collection<ReceiverInput> inputs() {
		return inputs;
	}

	public ReceiverInput input(String name) {
		return SpeechItem.getByName(name, inputs());
	}

	public List<SurroundMode> surroundModes() {
		return surroundModes;
	}

	public SurroundMode surroundMode(String name) {
		return SpeechItem.getByName(name, surroundModes());
	}

	private List<ReceiverInput> createInputs() {
		List<ReceiverInput> inputs = new ArrayList<>();
		for (int i = 1;; i++) {
			String name = value(INPUT, String.valueOf(i), NAME);
			if (name == null) break;
			String volumeStr = value(INPUT, String.valueOf(i), VOLUME);
			int volume = Integer.parseInt(volumeStr);
			String speech = value(INPUT, String.valueOf(i), SPEECH);
			if (speech == null) speech = name;
			String[] spokenWords = speech.split(",\\s*");
			inputs.add(new ReceiverInput(i, name, volume, spokenWords));
		}
		return Collections.unmodifiableList(inputs);
	}

	private List<SurroundMode> createSurroundModes() {
		List<SurroundMode> surroundModes = new ArrayList<>();
		for (int i = 1;; i++) {
			String name = value(SURROUND_MODE, String.valueOf(i), NAME);
			if (name == null) break;
			String speech = value(SURROUND_MODE, String.valueOf(i), SPEECH);
			if (speech == null) speech = name;
			String[] spokenWords = speech.split(",\\s*");
			surroundModes.add(new SurroundMode(i, name, spokenWords));
		}
		return Collections.unmodifiableList(surroundModes);
	}

}
