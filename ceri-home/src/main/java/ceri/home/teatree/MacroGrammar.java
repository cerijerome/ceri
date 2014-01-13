package ceri.home.teatree;

import java.io.IOException;
import java.util.List;
import ceri.common.io.IoUtil;
import ceri.home.device.dvd.Dvd;
import ceri.home.device.dvd.DvdGrammar;
import ceri.home.device.receiver.Receiver;
import ceri.home.device.receiver.ReceiverProperties;
import ceri.home.device.tv.Tv;
import ceri.home.device.tv.TvGrammar;
import ceri.home.device.tv.TvProperties;
import ceri.speech.grammar.ActionGrammar;
import ceri.speech.grammar.ContextGrammar;

public class MacroGrammar extends ActionGrammar {
	private static final String GRAMMAR_NAME = MacroGrammar.class.getName();
	private final Tv tv;
	private final TvProperties tvProperties;
	private final Receiver receiver;
	private final ReceiverProperties receiverProperties;
	private final Dvd dvd;
	private final ContextGrammar contextGrammar;

	MacroGrammar(Builder builder) {
		super(GRAMMAR_NAME, createJsgf());
		tv = builder.tv;
		tvProperties = builder.tvProperties;
		receiver = builder.receiver;
		receiverProperties = builder.receiverProperties;
		dvd = builder.dvd;
		contextGrammar = builder.contextGrammar;
	}

	public static class Builder {
		Tv tv;
		TvProperties tvProperties;
		Receiver receiver;
		ReceiverProperties receiverProperties;
		Dvd dvd;
		ContextGrammar contextGrammar;

		public Builder(ContextGrammar contextGrammar) {
			this.contextGrammar = contextGrammar;
		}

		public Builder tv(Tv tv, TvProperties properties) {
			this.tv = tv;
			tvProperties = properties;
			return this;
		}

		public Builder receiver(Receiver receiver, ReceiverProperties properties) {
			this.receiver = receiver;
			receiverProperties = properties;
			return this;
		}

		public Builder dvd(Dvd dvd) {
			this.dvd = dvd;
			return this;
		}

		public MacroGrammar build() {
			return new MacroGrammar(this);
		}
	}

	public void allOff() {
		tv.setOn(false);
		receiver.setOn(false);
		dvd.setOn(false);
	}

	public void watchTv() {
		tv.setOn(true);
		tv.setMute(false);
		tv.setInput(Tv.TV_INPUT);
		TvGrammar.setTvContext(contextGrammar);
	}

	public void watchDvd() {
		receiver.setOn(true);
		receiver.setMute(false);
		receiver.setInput(receiverProperties.input("dvd").number);
		tv.setOn(true);
		tv.setInput(tvProperties.input("dvd").number);
		dvd.setOn(true);
		dvd.play();
		DvdGrammar.setDvdContext(contextGrammar);
	}

	public void playCd() {
		receiver.setOn(true);
		receiver.setMute(false);
		receiver.setInput(receiverProperties.input("dvd").number);
		receiver.setSurroundMode(receiverProperties.surroundMode("music").number);
		dvd.setOn(true);
		dvd.play();
		DvdGrammar.setDvdContext(contextGrammar);
	}

	public void playGame() {
		receiver.setOn(true);
		receiver.setMute(false);
		receiver.setInput(receiverProperties.input("game").number);
		receiver.setSurroundMode(receiverProperties.surroundMode("cinema").number);
		tv.setOn(true);
		tv.setInput(tvProperties.input("game").number);
	}

	private static String createJsgf() {
		try {
			return IoUtil.getClassResourceString(MacroGrammar.class, "jsgf");
		} catch (IOException e) {
			throw new IllegalStateException("Unable to load resource", e);
		}
	}

	@Override
	public boolean parseRule(String rule, List<String> tags) {
		if ("allOff".equals(rule)) allOff();
		else if ("watchTv".equals(rule)) watchTv();
		else if ("watchDvd".equals(rule)) watchDvd();
		else if ("playCd".equals(rule)) playCd();
		else if ("playGame".equals(rule)) playGame();
		else return false;
		return true;
	}

}
