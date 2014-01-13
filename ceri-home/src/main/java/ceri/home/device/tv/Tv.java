package ceri.home.device.tv;


public interface Tv {
	static final int TV_INPUT = 0; 
	
	boolean setOn(boolean on);
	int setChannel(int channel);
	int channelShift(int offset);
	int lastChannel();
	int setVolume(int volume);
	int volumeShift(int offset);
	boolean setMute(boolean mute);
	int setInput(int index);
	
	boolean isOn();
	int getChannel();
	int getVolume();
	boolean isMuted();
	int getInput();
	
	void reset();
}
