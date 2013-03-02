package ceri.image;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.util.HashCoder;

public class CropParams {
	public static CropParams NULL = new CropParams(0, 0, null);
	private static final Pattern FROM_STRING_PATTERN = Pattern.compile("(\\d++)x(\\d++)(\\w+)");
	public final int width;
	public final int height;
	public final Resolution resolution;
	private final int hashCode;

	private CropParams(int width, int height, Resolution resolution) {
		this.width = width;
		this.height = height;
		this.resolution = resolution;
		hashCode = HashCoder.hash(width, height, resolution);
	}

	public static CropParams create(int width, int height, Resolution resolution) {
		if (width <= 0 || height <= 0) throw new IllegalArgumentException(
			"Width and height cannot be <= 0: " + width + ", " + height);
		if (resolution == null) throw new IllegalArgumentException("Resolution cannot be null: " +
			resolution);
		return new CropParams(width, height, resolution);
	}

	public static CropParams createFromString(String s) {
		Matcher m = FROM_STRING_PATTERN.matcher(s);
		if (!m.find()) throw new IllegalArgumentException("Invalid format: " + s);
		int group = 1;
		int width = Integer.parseInt(m.group(group++));
		int height = Integer.parseInt(m.group(group++));
		Resolution resolution = Resolution.valueOf(m.group(group++).toUpperCase());
		return create(width, height, resolution);
	}

	public boolean isNull() {
		return width == 0;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CropParams)) return false;
		CropParams other = (CropParams) obj;
		if (height != other.height) return false;
		if (resolution != other.resolution) return false;
		if (width != other.width) return false;
		return true;
	}

	@Override
	public String toString() {
		return Integer.toString(width) + "x" + Integer.toString(height) +
			resolution.name().toLowerCase();
	}

}
