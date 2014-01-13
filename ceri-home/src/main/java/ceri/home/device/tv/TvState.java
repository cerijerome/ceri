package ceri.home.device.tv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TvState {
	private static final String TO_STRING_FORMAT = "%s:input=%d:chan=%d%s:last=%d:vol=%d%s";
	private static final Pattern FROM_STRING_PATTERN = Pattern
		.compile("^(on|off):input=(\\d+):chan=(\\d+)(\\(last\\))?:"
			+ "last=(\\d+):vol=(\\d+)(\\((muted)\\))?$");
	private static final int INVALID_CHANNEL = 0;
	private boolean isOn = false;
	private int channel = INVALID_CHANNEL;
	private boolean useChannelAsLast = true;
	private int lastChannel = INVALID_CHANNEL;
	private int volume = 0;
	private boolean muted = false;
	private int input = 0;

	public static TvState createCopy(TvState state) {
		return createFromString(state.toString());
	}

	public static TvState createFromString(String s) {
		Matcher m = FROM_STRING_PATTERN.matcher(s);
		if (!m.matches()) throw new IllegalArgumentException("Regex does not match " +
			m.pattern().pattern() + ": " + s);
		TvState state = new TvState();
		int group = 1;
		state.isOn = "on".equals(m.group(group++));
		state.input = Integer.valueOf(m.group(group++));
		state.channel = Integer.valueOf(m.group(group++));
		state.useChannelAsLast = "(last)".equals(m.group(group++));
		state.lastChannel = Integer.valueOf(m.group(group++));
		state.volume = Integer.valueOf(m.group(group++));
		state.muted = "(muted)".equals(m.group(group++));
		return state;
	}

	@Override
	public String toString() {
		return String.format(TO_STRING_FORMAT, (isOn ? "on" : "off"), input, channel,
			(useChannelAsLast ? "(last)" : ""), lastChannel, volume, (muted ? "(muted)" : ""));
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

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
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

	public int getLastChannel() {
		return lastChannel;
	}

	public void setLastChannel(int lastChannel) {
		this.lastChannel = lastChannel;
	}

	public boolean useChannelAsLast() {
		return useChannelAsLast;
	}

	public void setUseChannelAsLast(boolean useChannelAsLast) {
		this.useChannelAsLast = useChannelAsLast;
	}

}
