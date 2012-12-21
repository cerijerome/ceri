package ceri.aws.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ByteRangeBehavior {
	
	@Test
	public void shouldHandleZero() {
		ByteRange range = new ByteRange(0, Integer.MAX_VALUE);
		assertThat(range.toString(), is("bytes 0-2147483646/*"));
	}

	@Test
	public void shouldHandleMaxLongValue() {
		ByteRange range = new ByteRange(Long.MAX_VALUE - Integer.MAX_VALUE + 1, Integer.MAX_VALUE);
		assertThat(range.toString(), is("bytes 9223372034707292161-9223372036854775807/*"));
	}

	@Test
	public void shouldParseRangeString() {
		String startStr = Long.toString(Long.MAX_VALUE - Integer.MAX_VALUE + 1);
		String endStr = Long.toString(Long.MAX_VALUE);
		ByteRange range = ByteRange.fromString("bytes " + startStr + "-" + endStr + "/*");
		assertThat(range.start, is(Long.MAX_VALUE - Integer.MAX_VALUE + 1));
		assertThat(range.size, is(Integer.MAX_VALUE));
	}

}
