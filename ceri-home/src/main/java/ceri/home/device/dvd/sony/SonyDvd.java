package ceri.home.device.dvd.sony;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import ceri.home.device.common.IrProperties;
import ceri.home.device.common.IrSupport;
import ceri.home.device.dvd.Dvd;
import ceri.home.device.dvd.DvdProperties;
import ceri.home.device.dvd.DvdState;
import ceri.home.io.pcirlinc.PcIrLinc;
import ceri.home.io.pcirlinc.PcIrLincButton;

// pause: ff = slow2, ff = slow1, rew = -slow2, rew = -slow1
// play: ff = x2, ff = ff1, ff = ff2, ff = x2, rew = -x2, etc
// stop: play/menu only works
// skip keeps current state (playing, paused, fast, slow)
// menu = menu, menu = play (state ignored)
public class SonyDvd implements Dvd {
	public final DvdState state;
	private final IrSupport irSupport;
	private final Map<Direction, Integer> maxFastLevels;
	private final Map<Direction, Integer> maxSlowLevels;
	
	public SonyDvd(DvdState initialState, PcIrLinc pcIrLinc,
		IrProperties irProperties, DvdProperties properties) {
		this.state = DvdState.createCopy(initialState);
		this.irSupport = new IrSupport(irProperties, pcIrLinc);
		maxFastLevels = Collections.unmodifiableMap(
			createMaxLevels(properties.getFastSpeeds()));
		maxSlowLevels = Collections.unmodifiableMap(
			createMaxLevels(properties.getSlowSpeeds()));
	}

	@Override
    public boolean isOn() {
	    return state.isOn();
    }

	@Override
    public PlayState playState() {
	    return state.getPlayState();
    }

	@Override
    public Direction direction() {
	    return state.getDirection();
    }

	@Override
    public int level() {
	    return state.getLevel();
    }

	@Override
    public boolean setOn(boolean on) {
		boolean oldState = state.isOn();
		if (state.isOn() == on) return oldState;
		irSupport.sendButton(PcIrLincButton.BTN_POWER);
		if (on) {
			state.setLevel(0);
			state.setPlayState(PlayState.STOPPED);
		}
		state.setOn(on);
		return oldState;
    }

	@Override
    public PlayState play() {
		PlayState oldPlayState = state.getPlayState();
		irSupport.sendButton(PcIrLincButton.BTN_PLAY);
		state.setPlayState(PlayState.PLAYING);
		state.setLevel(0);
		return oldPlayState;
    }

	@Override
    public PlayState pause() {
		PlayState oldPlayState = state.getPlayState();
		if (EnumSet.of(PlayState.PAUSED, PlayState.STOPPED,
			PlayState.MENU).contains(oldPlayState)) return oldPlayState;
		irSupport.sendButton(PcIrLincButton.BTN_PAUSE);
		state.setPlayState(PlayState.PAUSED);
		state.setLevel(0);
		return oldPlayState;
    }

	@Override
    public PlayState stop() {
		PlayState oldPlayState = state.getPlayState();
		irSupport.sendButton(PcIrLincButton.BTN_STOP);
		state.setPlayState(PlayState.STOPPED);
		state.setLevel(0);
		return oldPlayState;
    }

	@Override
    public PlayState eject() {
		PlayState oldPlayState = state.getPlayState();
		irSupport.sendButton(PcIrLincButton.BTN_OPEN_CLOSE);
		state.setPlayState(PlayState.STOPPED);
		state.setLevel(0);
		return oldPlayState;
    }

	@Override
    public PlayState fast(Direction direction) {
		PlayState oldPlayState = state.getPlayState();
		if (oldPlayState != PlayState.FAST &&
			oldPlayState != PlayState.PLAYING) play();
		irSupport.sendButton(getScanButton(direction));
		int level = state.getDirection() == direction ? state.getLevel() : 0;
		if (++level > maxFastLevels.get(direction)) level = 1;
		state.setPlayState(PlayState.FAST);
		state.setDirection(direction);
		state.setLevel(level);
		return oldPlayState;
    }

	@Override
    public PlayState fast(Direction direction, int level) {
		if (level == 0) return play();
		if (level > maxFastLevels.get(direction))
			throw new IllegalArgumentException("Only level 0-" + 
				maxFastLevels.get(direction) + " available " +
				direction + ": " + level);
		if (state.getPlayState() == PlayState.FAST &&
			state.getDirection() == direction &&
			state.getLevel() == level) return state.getPlayState();
		PlayState oldPlayState = state.getPlayState();
		while (state.getLevel() != level) fast(direction);
		return oldPlayState;
    }

	@Override
    public PlayState slow(Direction direction) {
		PlayState oldPlayState = state.getPlayState();
		if (oldPlayState != PlayState.SLOW &&
			oldPlayState != PlayState.PAUSED) pause();
		irSupport.sendButton(getScanButton(direction));
		int level = state.getDirection() == direction ? state.getLevel() : 0;
		if (++level > maxSlowLevels.get(direction)) level = 1;
		state.setPlayState(PlayState.SLOW);
		state.setDirection(direction);
		state.setLevel(level);
		return oldPlayState;
    }

	@Override
    public PlayState slow(Direction direction, int level) {
		if (level == 0) return pause();
		if (level > maxSlowLevels.get(direction))
			throw new IllegalArgumentException("Only level 0-" + 
				maxSlowLevels.get(direction) + " available " + 
				direction + ": " + level);
		if (state.getPlayState() == PlayState.SLOW &&
			state.getDirection() == direction &&
			state.getLevel() == level) return state.getPlayState();
		PlayState oldPlayState = state.getPlayState();
		while (state.getLevel() != level) fast(direction);
		return oldPlayState;
    }

	@Override
    public PlayState skip(Direction direction, int count) {
		if (count < 0) throw new IllegalArgumentException(
			"Skip count cannot be < 0: " + count);
		PlayState oldPlayState = state.getPlayState();
		PcIrLincButton button = getSkipButton(direction);
		for (int i = Math.abs(count); i > 0; i--) irSupport.sendButton(button);
		return oldPlayState;
    }

	@Override
    public PlayState menu() {
		PlayState oldPlayState = state.getPlayState();
		if (oldPlayState == PlayState.MENU)
		irSupport.sendButton(PcIrLincButton.BTN_MENU);
		return oldPlayState;
    }

	@Override
    public void left() {
		irSupport.sendButton(PcIrLincButton.BTN_MENU_LEFT);
    }

	@Override
    public void right() {
		irSupport.sendButton(PcIrLincButton.BTN_MENU_RIGHT);
    }

	@Override
    public void up() {
		irSupport.sendButton(PcIrLincButton.BTN_MENU_UP);
    }
	
	@Override
    public void down() {
		irSupport.sendButton(PcIrLincButton.BTN_MENU_DOWN);
    }

	@Override
    public void select() {
		irSupport.sendButton(PcIrLincButton.BTN_ENTER);
    }

	@Override
	public void reset() {
		stop();
	}
	
	private PcIrLincButton getScanButton(Direction direction) {
		return direction == Direction.FORWARD ?
			PcIrLincButton.BTN_SEARCH_FORW : PcIrLincButton.BTN_SEARCH_REV;
	}

	private PcIrLincButton getSkipButton(Direction direction) {
		return direction == Direction.FORWARD ?
			PcIrLincButton.BTN_TRACK_FORW : PcIrLincButton.BTN_TRACK_REV;
	}

	private Map<Direction, Integer> createMaxLevels(
		Map<Direction, ? extends Collection<?>> speeds) {
		Map<Direction, Integer> maxLevels = new HashMap<>();
		for (Direction direction : Direction.values())
			maxLevels.put(direction, speeds.get(direction).size());
		return maxLevels;
	}
	
}
