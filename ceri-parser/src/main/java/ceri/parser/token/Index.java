package ceri.parser.token;

public class Index {
	private int i = 0;
	
	public int value() {
		return i;
	}
	
	public int inc() {
		return ++i;
	}
	
	public void set(int i) {
		this.i = i;
	}
	
	@Override
	public String toString() {
		return Integer.toString(i);
	}
}
