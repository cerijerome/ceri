package ceri.serial.mlx90640;

public enum ReadingPattern {
	interleaved(0),
	chess(1);
	
	private final int id;
	
	public static ReadingPattern decode(int value) {
		return (value & 1) == 0 ? interleaved : chess;
	}
	
	private ReadingPattern(int id) {
		this.id = id;
	}
	
	public int encode() {
		return id;
	}
	
}
