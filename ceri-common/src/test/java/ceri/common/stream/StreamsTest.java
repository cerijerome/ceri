package ceri.common.stream;

import static ceri.common.test.Assert.assertPrivateConstructor;
import static ceri.common.test.Assert.assertStream;
import org.junit.Test;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;

public class StreamsTest {
	public static final int IMIN = Integer.MIN_VALUE;
	public static final int IMAX = Integer.MAX_VALUE;
	public static final long LMIN = Long.MIN_VALUE;
	public static final long LMAX = Long.MAX_VALUE;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Streams.class);
	}

	@Test
	public void testLongSlice() {
		assertStream(Streams.slice(LMIN, 3), LMIN, LMIN + 1, LMIN + 2);
	}

	@Test
	public void testUnmap() {
		var map = Immutable.mapOf(Maps::link, -1, "B", null, "A", 1, null);
		assertStream(Streams.unmap(null, map));
		assertStream(Streams.unmap((k, v) -> "" + k + v, null));
		assertStream(Streams.unmap((k, v) -> "" + k + v, map), "-1B", "nullA", "1null");
	}

}
