package ceri.home.device.tv.sony;

import java.util.Arrays;
import java.util.Set;
import ceri.common.util.BasicUtil;
import ceri.home.device.common.IrSupport;
import ceri.home.device.tv.Tv;
import ceri.home.device.tv.TvIrProperties;
import ceri.home.device.tv.TvProperties;
import ceri.home.device.tv.TvState;
import ceri.home.io.pcirlinc.PcIrLinc;
import ceri.home.io.pcirlinc.PcIrLincButton;

public class SonyTv implements Tv {
	public final TvState state;
	private final IrSupport irSupport;
	private final TvIrProperties irProperties;
	private final int[] channels;
	private final int maxChannel;
	private final int maxVolume;
	private final int inputs;

	public SonyTv(TvState initialState, PcIrLinc pcIrLinc, TvIrProperties irProperties,
		TvProperties properties) {
		this.state = TvState.createCopy(initialState);
		this.irSupport = new IrSupport(irProperties.common(), pcIrLinc);
		this.irProperties = irProperties;
		channels = createChannels(properties.channelMap().keySet());
		maxChannel = properties.maxChannel();
		maxVolume = properties.maxVolume();
		inputs = properties.inputs().size();
	}

	@Override
	public boolean isOn() {
		return state.isOn();
	}

	@Override
	public int getChannel() {
		return state.getChannel();
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
	public boolean setOn(boolean on) {
		boolean oldState = state.isOn();
		if (state.isOn() == on) return oldState;
		irSupport.sendButton(PcIrLincButton.BTN_POWER);
		if (on) {
			state.setMuted(false); // Turning on unmutes TV
			state.setLastChannel(state.getChannel());
		}
		state.setOn(on);
		return oldState;
	}

	@Override
	public int channelShift(int offset) {
		int oldChannel = state.getChannel();
		if (offset == 0) return oldChannel;
		PcIrLincButton button =
			offset > 0 ? PcIrLincButton.BTN_CHANNEL_UP : PcIrLincButton.BTN_CHANNEL_DOWN;

		for (int i = Math.abs(offset); i > 0; i--) {
			irSupport.sendButton(button);
			int newChannel =
				offset > 0 ? getNextChannel(state.getChannel()) : getPreviousChannel(state
					.getChannel());
			if (state.useChannelAsLast()) state.setLastChannel(state.getChannel());
			state.setChannel(newChannel);
			state.setUseChannelAsLast(false);
			state.setInput(TV_INPUT);
		}
		return oldChannel;
	}

	@Override
	public int lastChannel() {
		int oldChannel = state.getChannel();
		irSupport.sendButton(PcIrLincButton.BTN_PREV_CHANNEL);
		setChannelState(state.getLastChannel());
		state.setInput(TV_INPUT);
		return oldChannel;
	}

	@Override
	public int setChannel(int channel) {
		if (channel <= 0 || channel > maxChannel) throw new IllegalArgumentException(
			"Channel out of range 1-" + maxChannel + ": " + channel);
		int oldChannel = state.getChannel();
		String s = String.valueOf(channel);
		for (char ch : s.toCharArray())
			irSupport.sendButton(PcIrLincButton.getDigitButton((byte) (ch - '0')));
		BasicUtil.delay(irProperties.getChannelDelayMs());
		setChannelState(channel);
		state.setInput(TV_INPUT);
		return oldChannel;
	}

	@Override
	public int setInput(int input) {
		if (input < 0 || input >= inputs) throw new IllegalArgumentException(
			"Input index out of range 0-" + (inputs - 1) + ": " + input);
		int oldInput = state.getInput();
		if (oldInput == input) return oldInput;
		while (true) {
			irSupport.sendButton(PcIrLincButton.BTN_TV_VIDEO);
			int newInput = state.getInput() + 1;
			if (newInput >= inputs) newInput = 0;
			state.setInput(newInput);
			if (newInput == input) break;
		}
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
		if (volume < 0 || volume > maxVolume) throw new IllegalArgumentException(
			"Volume out of range 0-" + maxVolume + ": " + volume);
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
			int newVolume = Math.max(0, state.getVolume() + Integer.signum(offset));
			if (offset > 0) state.setMuted(false);
			state.setVolume(newVolume);
			if (newVolume == maxVolume) break;
		}
		return oldVolume;
	}

	@Override
	public void reset() {
		int volume = getVolume();
		boolean isMuted = isMuted();
		int channel = getChannel();
		int input = getInput();

		volumeShift(-maxVolume);
		if (isMuted) volumeShift(volume);
		else {
			volumeShift(1);
			setMute(true);
		}
		setChannel(channel);
		setInput(input);
	}

	private int[] createChannels(Set<Integer> channelSet) {
		int[] channels = new int[channelSet.size()];
		int i = 0;
		for (Integer channel : channelSet)
			channels[i++] = channel;
		Arrays.sort(channels);
		return channels;
	}

	private int getNextChannel(int currentChannel) {
		for (int channel : channels)
			if (channel > currentChannel) return channel;
		return channels[0];
	}

	private int getPreviousChannel(int currentChannel) {
		for (int i = channels.length - 1; i >= 0; i--)
			if (channels[i] < currentChannel) return channels[i];
		return channels[channels.length - 1];
	}

	private void setChannelState(int channel) {
		state.setLastChannel(state.getChannel());
		state.setChannel(channel);
		state.setUseChannelAsLast(true);
	}

}
