package ceri.home.device.receiver;

import java.util.Collection;
import java.util.List;
import ceri.speech.grammar.ActionGrammar;
import ceri.speech.grammar.ContextGrammar;
import ceri.speech.grammar.GrammarUtil;
import ceri.speech.grammar.NumberGrammar;
import ceri.speech.grammar.SpeechItem;

public class ReceiverGrammar extends ActionGrammar {
	private static final String GRAMMAR_NAME = ReceiverGrammar.class.getName();
	private static final String CONTEXUAL_GRAMMAR_NAME = GRAMMAR_NAME + ".context";
	private static final String CONTEXT_RULE = "<receiver>";
	private static final String OPTIONAL_CONTEXT_RULE = "[<receiver>]";
	private static final String INPUT_NAMES = "INPUT_NAMES";
	private static final String CONTEXT = "CONTEXT";
	private final Receiver receiver;
	private final ContextGrammar contextGrammar;

	private ReceiverGrammar(String name, String jsgf, Receiver receiver,
		ContextGrammar contextGrammar) {
		super(name, jsgf);
		this.receiver = receiver;
		this.contextGrammar = contextGrammar;
	}

	public static ActionGrammar create(Collection<ReceiverInput> inputs, Receiver receiver,
		ContextGrammar contextGrammar) {
		String jsgf = getJsgf(inputs, CONTEXT_RULE);
		return new ReceiverGrammar(GRAMMAR_NAME, jsgf, receiver, contextGrammar);
	}

	public static ActionGrammar
		createContextual(Collection<ReceiverInput> inputs, Receiver receiver) {
		String jsgf = getJsgf(inputs, OPTIONAL_CONTEXT_RULE);
		return new ReceiverGrammar(CONTEXUAL_GRAMMAR_NAME, jsgf, receiver, null);
	}

	public static void setReceiverContext(ContextGrammar contextGrammar) {
		contextGrammar.setContext(CONTEXUAL_GRAMMAR_NAME);
	}

	private static String getJsgf(Collection<ReceiverInput> inputs, String contextRule) {
		String inputNameRule = SpeechItem.createNameRule(inputs);
		String jsgf = GrammarUtil.loadJsgfResource(ReceiverGrammar.class);
		jsgf = jsgf.replaceAll(INPUT_NAMES, inputNameRule);
		jsgf = jsgf.replaceAll(CONTEXT, contextRule);
		return jsgf;
	}

	@Override
	public boolean parseRule(String rule, List<String> tags) {
		if (contextGrammar != null) setReceiverContext(contextGrammar);
		if (CONTEXT.toLowerCase().equals(rule)) return true;
		if ("off".equals(rule)) receiver.setOn(false);
		else if ("on".equals(rule)) receiver.setOn(true);
		else if ("mute".equals(rule)) receiver.setMute(true);
		else if ("unmute".equals(rule)) receiver.setMute(false);
		else if ("reset".equals(rule)) receiver.reset();
		else if ("volumeSet".equals(rule)) receiver.setVolume((int) NumberGrammar
			.getLongFromTags(tags));
		else if ("volumeUp".equals(rule)) {
			int value = tags.isEmpty() ? 1 : (int) NumberGrammar.getLongFromTags(tags);
			receiver.volumeShift(value);
		} else if ("volumeDown".equals(rule)) {
			int value = tags.isEmpty() ? 1 : (int) NumberGrammar.getLongFromTags(tags);
			receiver.volumeShift(-value);
		} else if ("input".equals(rule)) receiver.setInput(Integer.parseInt(tags.get(0)));
		else return false;
		return true;
	}

}
