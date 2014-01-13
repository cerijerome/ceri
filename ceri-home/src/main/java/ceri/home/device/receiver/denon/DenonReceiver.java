package ceri.home.device.receiver.denon;

import ceri.home.device.common.IrSupport;
import ceri.home.device.receiver.Receiver;
import ceri.home.device.receiver.ReceiverIrProperties;
import ceri.home.device.receiver.ReceiverProperties;
import ceri.home.device.receiver.ReceiverState;
import ceri.home.io.pcirlinc.PcIrLinc;
import ceri.home.io.pcirlinc.PcIrLincButton;

public class DenonReceiver implements Receiver {
	public final ReceiverState state;
	private final IrSupport irSupport;
	private final ReceiverIrProperties irProperties;
	private final int minVolume;
	private final int maxVolume;
	private final int inputs;
	private final int surroundModes;

	public DenonReceiver(ReceiverState initialState, PcIrLinc pcIrLinc,
		ReceiverIrProperties irProperties, ReceiverProperties properties) {
		this.state = ReceiverState.createCopy(initialState);
		this.irSupport = new IrSupport(irProperties.common(), pcIrLinc);
		this.irProperties = irProperties;
		minVolume = properties.minVolume();
		maxVolume = properties.maxVolume();
		inputs = properties.inputs().size();
		surroundModes = properties.surroundModes().size();
	}

	@Override
	public boolean isOn() {
		return state.isOn();
	}

	@Override
	public int getVolume() {
		return state.getVolume();
	}

	@Override
	public boolean isMuted() {
		return state.isMuted();
	}

	@Override
	public int getInput() {
		return state.getInput();
	}

	@Override
	public int getSurroundMode() {
		return state.getSurroundMode();
	}

	@Override
	public boolean setOn(boolean on) {
		boolean oldState = state.isOn();
		if (state.isOn() == on) return oldState;
		irSupport.sendButton(PcIrLincButton.BTN_POWER);
		if (on) state.setMuted(false); // Turning on unmutes TV
		state.setOn(on);
		return oldState;
	}

	@Override
	public int setInput(int input) {
		if (input < 1 || input > inputs) throw new IllegalArgumentException(
			"Input index out of range 1-" + inputs + ": " + input);
		int oldInput = state.getInput();
		irSupport.sendButton(irProperties.inputButton(input));
		state.setInput(input);
		return oldInput;
	}

	@Override
	public boolean setMute(boolean mute) {
		boolean oldState = state.isMuted();
		if (state.isMuted() == mute) return oldState;
		irSupport.sendButton(PcIrLincButton.BTN_MUTE);
		state.setMuted(mute);
		return oldState;
	}

	@Override
	public int setVolume(int volume) {
		if (volume < minVolume || volume > maxVolume) throw new IllegalArgumentException(
			"Volume out of range " + minVolume + " to " + maxVolume + ": " + volume);
		return volumeShift(volume - state.getVolume());
	}

	@Override
	public int volumeShift(int offset) {
		int oldVolume = state.getVolume();
		if (offset == 0) return oldVolume;
		PcIrLincButton button =
			offset > 0 ? PcIrLincButton.BTN_VOLUME_UP : PcIrLincButton.BTN_VOLUME_DOWN;

		for (int i = Math.abs(offset); i > 0; i--) {
			irSupport.sendButton(button);
			int newVolume = Math.max(minVolume, state.getVolume() + Integer.signum(offset));
			state.setMuted(false);
			state.setVolume(newVolume);
			if (newVolume == maxVolume) break;
		}
		return oldVolume;
	}

	@Override
	public int setSurroundMode(int surroundMode) {
		if (surroundMode < 1 || surroundMode > surroundModes) throw new IllegalArgumentException(
			"Surround mode index out of range 1-" + surroundModes + ": " + surroundMode);
		int oldSurroundMode = state.getSurroundMode();
		if (oldSurroundMode == surroundMode) return oldSurroundMode;
		irSupport.sendButton(irProperties.surroundModeButton(surroundMode));
		state.setSurroundMode(surroundMode);
		return oldSurroundMode;
	}

	@Override
	public void reset() {
		int input = getInput();
		int surroundMode = getSurroundMode();
		int volume = getVolume();
		boolean isMuted = isMuted();

		volumeShift(minVolume - maxVolume);
		if (!isMuted) setVolume(volume);
		else {
			volumeShift(1);
			setMute(true);
		}
		setInput(input);
		setSurroundMode(surroundMode);
	}

}
