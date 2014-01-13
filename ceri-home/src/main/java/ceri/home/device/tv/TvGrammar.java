package ceri.home.device.tv;

import java.util.Collection;
import java.util.List;
import ceri.home.device.receiver.ReceiverGrammar;
import ceri.speech.grammar.ActionGrammar;
import ceri.speech.grammar.ContextGrammar;
import ceri.speech.grammar.GrammarUtil;
import ceri.speech.grammar.NumberGrammar;
import ceri.speech.grammar.SpeechItem;

public class TvGrammar extends ActionGrammar {
	private static final String GRAMMAR_NAME = TvGrammar.class.getName();
	private static final String CONTEXUAL_GRAMMAR_NAME = GRAMMAR_NAME + ".context";
	private static final String CONTEXT_RULE = "<tv>";
	private static final String OPTIONAL_CONTEXT_RULE = "[<tv>]";
	private static final String CHANNEL_NAMES = "CHANNEL_NAMES";
	private static final String INPUT_NAMES = "INPUT_NAMES";
	private static final String CONTEXT = "CONTEXT";
	private final Tv tv;
	private final ContextGrammar contextGrammar;

	private TvGrammar(String name, String jsgf, Tv tv, ContextGrammar contextGrammar) {
		super(name, jsgf);
		this.tv = tv;
		this.contextGrammar = contextGrammar;
	}

	public static ActionGrammar create(Collection<TvChannel> channels, Collection<TvInput> inputs,
		Tv tv, ContextGrammar contextGrammar) {
		String jsgf = getJsgf(channels, inputs, CONTEXT_RULE);
		return new TvGrammar(GRAMMAR_NAME, jsgf, tv, contextGrammar);
	}

	public static ActionGrammar createContextual(Collection<TvChannel> channels,
		Collection<TvInput> inputs, Tv tv) {
		String jsgf = getJsgf(channels, inputs, OPTIONAL_CONTEXT_RULE);
		return new TvGrammar(CONTEXUAL_GRAMMAR_NAME, jsgf, tv, null);
	}

	public static void setTvContext(ContextGrammar contextGrammar) {
		contextGrammar.setContext(CONTEXUAL_GRAMMAR_NAME);
	}

	private static String getJsgf(Collection<TvChannel> channels, Collection<TvInput> inputs,
		String contextRule) {
		String channelNameRule = SpeechItem.createNameRule(channels);
		String inputNameRule = SpeechItem.createNameRule(inputs);
		String jsgf = GrammarUtil.loadJsgfResource(ReceiverGrammar.class);
		jsgf = jsgf.replaceAll(CHANNEL_NAMES, channelNameRule);
		jsgf = jsgf.replaceAll(INPUT_NAMES, inputNameRule);
		jsgf = jsgf.replaceAll(CONTEXT, contextRule);
		return jsgf;
	}

	@Override
	public boolean parseRule(String rule, List<String> tags) {
		if (contextGrammar != null) setTvContext(contextGrammar);
		if (CONTEXT.toLowerCase().equals(rule)) return true;
		if ("off".equals(rule)) tv.setOn(false);
		else if ("on".equals(rule)) tv.setOn(true);
		else if ("mute".equals(rule)) tv.setMute(true);
		else if ("unmute".equals(rule)) tv.setMute(false);
		else if ("channelUp".equals(rule)) tv.channelShift(1);
		else if ("channelDown".equals(rule)) tv.channelShift(-1);
		else if ("channelLast".equals(rule)) tv.lastChannel();
		else if ("reset".equals(rule)) tv.reset();
		else if ("input".equals(rule)) {
			int value = Integer.parseInt(tags.get(0));
			tv.setInput(value);
		} else if ("channelSet".equals(rule)) {
			int value = (int) NumberGrammar.getLongFromTags(tags);
			tv.setChannel(value);
		} else if ("volumeSet".equals(rule)) {
			int value = (int) NumberGrammar.getLongFromTags(tags);
			tv.setVolume(value);
		} else if ("volumeUp".equals(rule)) {
			int value = tags.isEmpty() ? 1 : (int) NumberGrammar.getLongFromTags(tags);
			tv.volumeShift(value);
		} else if ("volumeDown".equals(rule)) {
			int value = tags.isEmpty() ? -1 : -(int) NumberGrammar.getLongFromTags(tags);
			tv.volumeShift(value);
		} else return false;
		return true;
	}

}
