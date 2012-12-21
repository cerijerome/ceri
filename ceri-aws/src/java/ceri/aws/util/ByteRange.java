package ceri.aws.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ByteRange {
	private static final Pattern RANGE_REGEX = Pattern.compile("^bytes (\\d+)\\-(\\d+)/\\*$");
	public final long start;
	public final int size;

	public ByteRange(long start, int size) {
		this.start = start;
		this.size = size;
	}

	public long end() {
		return start + size - 1;
	}
	
	@Override
	public String toString() {
		return asString(start, size);
	}

	public static ByteRange fromString(String str) {
		Matcher m = RANGE_REGEX.matcher(str);
		if (!m.find()) throw new IllegalArgumentException("Range doesn't match pattern: " + str);
		int i = 1;
		long start = Long.valueOf(m.group(i++));
		long end = Long.valueOf(m.group(i++));
		int size = (int)(end + 1 - start);
		return new ByteRange(start, size);
	}
	
	public static String asString(long start, int size) {
		return String.format("bytes %s-%s/*", start, start + size - 1);
	}
	
}
