package ceri.serial.mlx90640;

public enum ThresholdMode {
	normal(0, "normal"),
	_1_8v(1, "1.8V");
	
	private final int id;
	private final String desc;
	
	public static ThresholdMode decode(int id) {
		return (id & 1) == 0 ? normal : _1_8v;
	}
	
	private ThresholdMode(int id, String desc) {
		this.id = id;
		this.desc = desc;
	}
	
	public int encode() {
		return id;
	}
	
	@Override
	public String toString() {
		return desc;
	}
}
