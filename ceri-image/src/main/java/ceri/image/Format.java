package ceri.image;

public enum Format {
	JPEG("jpg"),
	PNG;

	public final String name = name().toLowerCase();
	public final String suffix;

	private Format() {
		suffix = name().toLowerCase();
	}

	private Format(String suffix) {
		this.suffix = suffix;
	}

	public static Format fromFilename(String filename) {
		int i = filename.lastIndexOf('.');
		if (i != -1) {
			String suffix = filename.substring(i + 1).toLowerCase();
			for (Format format : Format.values()) {
				if (format.suffix.equals(suffix)) return format;
			}
		}
		throw new IllegalArgumentException("Unknown filename format: " + filename);
	}

}
