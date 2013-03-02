package ceri.image;

public enum Format {
	JPEG("jpg"), PNG;
	
	public final String name = name().toLowerCase();
	public final String suffix;
	
	private Format() {
		suffix = name().toLowerCase();
	}
	
	private Format(String suffix) {
		this.suffix = suffix;
	}
	
}
