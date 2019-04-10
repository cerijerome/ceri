package ceri.serial.pigpio;

public enum PigpioSpiChannel {
	channel0(0),
	channel1(1);
	
	public final int value;
	
	private PigpioSpiChannel(int value) {
		this.value = value;
	}
	
	public static PigpioSpiChannel from(int value) {
		if (value == 0) return channel0;
		if (value == 1) return channel1;
		return null;
	}
	
}
