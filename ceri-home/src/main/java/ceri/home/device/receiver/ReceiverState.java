package ceri.home.device.receiver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiverState {
	private static final String TO_STRING_FORMAT = "%s:input=%d:vol=%d%s:mode=%d";
	private static final Pattern FROM_STRING_PATTERN = Pattern
		.compile("^(on|off):input=(\\d+):vol=(-?\\d+)(\\(muted\\))?:mode=(\\d+)$");
	private boolean isOn = false;
	private int volume = 0;
	private boolean muted = false;
	private int input = 0;
	private int surroundMode = 0;

	public static ReceiverState createCopy(ReceiverState state) {
		return createFromString(state.toString());
	}

	public static ReceiverState createFromString(String s) {
		Matcher m = FROM_STRING_PATTERN.matcher(s);
		if (!m.matches()) throw new IllegalArgumentException("Regex does not match " +
			m.pattern().pattern() + ": " + s);
		ReceiverState state = new ReceiverState();
		int group = 1;
		state.isOn = "on".equals(m.group(group++));
		state.input = Integer.valueOf(m.group(group++));
		state.volume = Integer.valueOf(m.group(group++));
		state.muted = "(muted)".equals(m.group(group++));
		state.surroundMode = Integer.valueOf(m.group(group++));
		return state;
	}

	@Override
	public String toString() {
		return String.format(TO_STRING_FORMAT, (isOn ? "on" : "off"), input, volume, (muted
			? "(muted)" : ""), surroundMode);
	}

	public boolean isMuted() {
		return muted;
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}

	public boolean isOn() {
		return isOn;
	}

	public void setOn(boolean isOn) {
		this.isOn = isOn;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public int getInput() {
		return input;
	}

	public void setInput(int input) {
		this.input = input;
	}

	public int getSurroundMode() {
		return surroundMode;
	}

	public void setSurroundMode(int surroundMode) {
		this.surroundMode = surroundMode;
	}

}
