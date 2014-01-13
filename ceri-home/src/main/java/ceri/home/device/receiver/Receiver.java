package ceri.home.device.receiver;


public interface Receiver {
	
	boolean setOn(boolean on);
	int setVolume(int volume);
	int volumeShift(int offset);
	boolean setMute(boolean mute);
	int setInput(int index);
	int setSurroundMode(int surroundMode);
	
	
	boolean isOn();
	int getVolume();
	boolean isMuted();
	int getInput();
	int getSurroundMode();
	
	void reset();
}
