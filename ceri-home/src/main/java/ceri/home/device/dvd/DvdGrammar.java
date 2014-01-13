package ceri.home.device.dvd;

import java.util.List;
import ceri.home.device.dvd.Dvd.Direction;
import ceri.home.device.dvd.Dvd.PlayState;
import ceri.speech.grammar.ActionGrammar;
import ceri.speech.grammar.ContextGrammar;
import ceri.speech.grammar.GrammarUtil;
import ceri.speech.grammar.NumberGrammar;

public class DvdGrammar extends ActionGrammar {
	private static final String GRAMMAR_NAME = DvdGrammar.class.getName();
	private static final String CONTEXUAL_GRAMMAR_NAME = GRAMMAR_NAME + ".context";
	private static final String CONTEXT_RULE = "<dvd>";
	private static final String OPTIONAL_CONTEXT_RULE = "[<dvd>]";
	private static final String CONTEXT = "CONTEXT";
	private final Dvd dvd;
	private final ContextGrammar contextGrammar;

	private DvdGrammar(String name, String jsgf, Dvd dvd, ContextGrammar contextGrammar) {
		super(name, jsgf);
		this.dvd = dvd;
		this.contextGrammar = contextGrammar;
	}

	public static ActionGrammar create(Dvd dvd, ContextGrammar contextGrammar) {
		String jsgf = getJsgf(CONTEXT_RULE);
		return new DvdGrammar(GRAMMAR_NAME, jsgf, dvd, contextGrammar);
	}

	public static ActionGrammar createContextual(Dvd dvd) {
		String jsgf = getJsgf(OPTIONAL_CONTEXT_RULE);
		return new DvdGrammar(CONTEXUAL_GRAMMAR_NAME, jsgf, dvd, null);
	}

	public static void setDvdContext(ContextGrammar contextGrammar) {
		contextGrammar.setContext(CONTEXUAL_GRAMMAR_NAME);
	}

	private static String getJsgf(String contextRule) {
		String jsgf = GrammarUtil.loadJsgfResource(DvdGrammar.class);
		return jsgf.replaceAll(CONTEXT, contextRule);
	}

	@Override
	public boolean parseRule(String rule, List<String> tags) {
		if (contextGrammar != null) setDvdContext(contextGrammar);
		if (CONTEXT.toLowerCase().equals(rule)) return true;
		if ("off".equals(rule)) dvd.setOn(false);
		else if ("on".equals(rule)) dvd.setOn(true);
		else if ("play".equals(rule)) dvd.play();
		else if ("pause".equals(rule)) dvd.pause();
		else if ("stop".equals(rule)) dvd.stop();
		else if ("eject".equals(rule)) dvd.eject();
		else if ("menu".equals(rule)) dvd.menu();
		else if ("select".equals(rule)) dvd.select();
		else if ("reset".equals(rule)) dvd.reset();
		else if ("up".equals(rule)) dvd.up();
		else if ("down".equals(rule)) dvd.down();
		else if ("left".equals(rule)) dvd.left();
		else if ("right".equals(rule)) dvd.right();
		else if ("fastForward".equals(rule)) {
			if (tags.isEmpty()) dvd.fast(Direction.FORWARD);
			else dvd.fast(Direction.FORWARD, (int) NumberGrammar.getLongFromTags(tags));
		} else if ("fastReverse".equals(rule)) {
			if (tags.isEmpty()) dvd.fast(Direction.REVERSE);
			else dvd.fast(Direction.REVERSE, (int) NumberGrammar.getLongFromTags(tags));
		} else if ("slowForward".equals(rule)) {
			if (tags.isEmpty()) dvd.fast(Direction.FORWARD);
			else dvd.slow(Direction.FORWARD, (int) NumberGrammar.getLongFromTags(tags));
		} else if ("slowReverse".equals(rule)) {
			if (tags.isEmpty()) dvd.fast(Direction.REVERSE);
			else dvd.slow(Direction.REVERSE, (int) NumberGrammar.getLongFromTags(tags));
		} else if ("skipForward".equals(rule)) {
			int value = tags.isEmpty() ? 1 : (int) NumberGrammar.getLongFromTags(tags);
			dvd.skip(Direction.FORWARD, value);
		} else if ("skipReverse".equals(rule)) {
			int value = tags.isEmpty() ? 1 : (int) NumberGrammar.getLongFromTags(tags);
			if (dvd.playState() != PlayState.PAUSED) value++;
			dvd.skip(Direction.REVERSE, value);
		} else return false;
		return true;
	}

}
