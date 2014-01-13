package ceri.home.device.dvd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ceri.home.device.dvd.Dvd.Direction;
import ceri.home.device.dvd.Dvd.PlayState;

public class DvdState {
	private static final String TO_STRING_FORMAT =
		"%s:state=%s:level=%d";
	private static final Pattern FROM_STRING_PATTERN = Pattern.compile(
		"^(on|off):state=(\\w+):level=(-?\\d+)$");
	private boolean isOn = false;
	private PlayState playState = PlayState.STOPPED;
	private Direction direction = Direction.FORWARD;
	private int level = 0;

	public static DvdState createCopy(DvdState state) {
		return createFromString(state.toString());
	}
	
	public static DvdState createFromString(String s) {
		Matcher m = FROM_STRING_PATTERN.matcher(s);
		if (!m.matches()) throw new IllegalArgumentException(
			"Regex does not match "	+ m.pattern().pattern() + ": " + s);
		DvdState state = new DvdState();
		int group = 1;
		state.isOn = "on".equals(m.group(group++));
		state.playState = PlayState.valueOf(m.group(group++));
		int level = Integer.valueOf(m.group(group++));
		state.direction = level >= 0 ? Direction.FORWARD : Direction.REVERSE;
		state.level = Math.abs(level);
		return state;
	}

	@Override
    public String toString() {
		int level = this.level;
		if (direction == Direction.REVERSE) level = -level;
		return String.format(TO_STRING_FORMAT, (isOn ? "on" : "off"),
			playState.name(), level);
	}

	public boolean isOn() {
		return isOn;
	}

	public void setOn(boolean isOn) {
		this.isOn = isOn;
	}

	public PlayState getPlayState() {
    	return playState;
    }

	public void setPlayState(PlayState playState) {
    	this.playState = playState;
    }

	public Direction getDirection() {
    	return direction;
    }

	public void setDirection(Direction direction) {
    	this.direction = direction;
    }

	public int getLevel() {
    	return level;
    }

	public void setLevel(int level) {
		if (level < 0) throw new IllegalArgumentException(
			"Level cannot be < 0: " + level);
    	this.level = level;
    }

}
