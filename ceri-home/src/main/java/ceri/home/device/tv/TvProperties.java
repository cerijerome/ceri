package ceri.home.device.tv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import ceri.common.property.BaseProperties;
import ceri.speech.grammar.SpeechItem;

public class TvProperties extends BaseProperties {
	private static final int MAX_CHANNEL_DEF = 999;
	private static final int MAX_VOLUME_DEF = 25;
	private static final String MAX = "max";
	private static final String CHANNEL = "channel";
	private static final String NAME = "name";
	private static final String SPEECH = "speech";
	private static final String VOLUME = "volume";
	private static final String INPUT = "input";
	private static final String TV = "TV";
	private final Map<Integer, TvChannel> channelMap;
	private final List<TvInput> inputs;

	public TvProperties(Properties properties, String prefix) {
		super(properties, prefix);
		channelMap = createChannelMap();
		inputs = createInputs();
	}

	public Map<Integer, TvChannel> channelMap() {
		return channelMap;
	}

	public int maxChannel() {
		return intValue(MAX_CHANNEL_DEF, CHANNEL, MAX);
	}

	public int maxVolume() {
		return intValue(MAX_VOLUME_DEF, VOLUME, MAX);
	}

	public Collection<TvInput> inputs() {
		return inputs;
	}

	public TvInput input(String name) {
		return SpeechItem.getByName(name, inputs());
	}

	private Map<Integer, TvChannel> createChannelMap() {
		Map<Integer, TvChannel> channelMap = new LinkedHashMap<>();
		for (int i = 1; i <= maxChannel(); i++) {
			String name = value(CHANNEL, String.valueOf(i), NAME);
			if (name == null) continue;
			String speech = value(CHANNEL, String.valueOf(i), SPEECH);
			if (speech == null) speech = name;
			String[] spokenWords = speech.split(",\\s*");
			TvChannel channel = new TvChannel(i, name, spokenWords);
			channelMap.put(i, channel);
		}
		return Collections.unmodifiableMap(channelMap);
	}

	private List<TvInput> createInputs() {
		List<TvInput> inputs = new ArrayList<>();
		TvInput input = new TvInput(Tv.TV_INPUT, TV, TV);
		inputs.add(input);
		for (int i = 1;; i++) {
			String name = value(INPUT, String.valueOf(i), NAME);
			if (name == null) break;
			String speech = value(INPUT, String.valueOf(i), SPEECH);
			if (speech == null) speech = name;
			String[] spokenWords = speech.split(",\\s*");
			inputs.add(new TvInput(i, name, spokenWords));
		}
		return Collections.unmodifiableList(inputs);
	}

}
