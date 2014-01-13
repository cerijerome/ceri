package ceri.home.device.dvd;


public interface Dvd {
	
	static enum PlayState {
		PLAYING, PAUSED, STOPPED, FAST, SLOW, MENU
	}
	
	static enum Direction {
		FORWARD, REVERSE
	}
	
	boolean setOn(boolean on);
	PlayState play();
	PlayState pause();
	PlayState stop();
	PlayState eject();
	PlayState fast(Direction direction);
	PlayState fast(Direction direction, int level);
	PlayState slow(Direction direction);
	PlayState slow(Direction direction, int level);
	PlayState skip(Direction direction, int count);
	PlayState menu();
	void up();
	void down();
	void left();
	void right();
	void select();
	
	boolean isOn();
	PlayState playState();
	Direction direction();
	int level();
	
	void reset();
}
