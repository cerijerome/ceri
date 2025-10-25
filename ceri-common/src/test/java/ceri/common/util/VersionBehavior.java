package ceri.common.util;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertOrdered;
import static ceri.common.test.Assert.assertString;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class VersionBehavior {
	private static final Version v1 = new Version(1, null, null, null);
	private static final Version v1_1 = new Version(1, 1, null, null);
	private static final Version v1_2 = new Version(1, 2, null, null);
	private static final Version v1__1 = new Version(1, null, 1, null);
	private static final Version v1__2 = new Version(1, null, 2, null);
	private static final Version v1_1_1 = new Version(1, 1, 1, null);
	private static final Version v1_1_2 = new Version(1, 1, 2, null);
	private static final Version v1___x = new Version(1, null, null, "x");
	private static final Version v1___y = new Version(1, null, null, "y");
	private static final Version v1_1__x = new Version(1, 1, null, "x");
	private static final Version v1__1_x = new Version(1, null, 1, "x");
	private static final Version v1_1_1_x = new Version(1, 1, 1, "x");

	@Test
	public void shouldProvideStringRepresentation() {
		assertString(v1, "1");
		assertString(v1_2, "1.2");
		assertString(v1__2, "1.2");
		assertString(v1___y, "1.y");
		assertString(v1_1__x, "1.1.x");
		assertString(v1__1_x, "1.1.x");
		assertString(v1_1_1_x, "1.1.1.x");
	}

	@Test
	public void shouldCompare() {
		var list = Arrays.asList(v1, v1_1, v1_2, v1__1, v1__2, v1_1_1, v1_1_2, v1___x, v1___y,
			v1_1__x, v1__1_x, v1_1_1_x);
		Collections.sort(list);
		assertOrdered(list, v1, v1___x, v1___y, v1__1, v1__1_x, v1__2, v1_1, v1_1__x, v1_1_1,
			v1_1_1_x, v1_1_2, v1_2);
	}

	@Test
	public void shouldCreateFromKernelValue() {
		assertEquals(Version.kernel(0x1234567), new Version(0x123, 0x45, 0x67, null));
	}

	@Test
	public void shouldProvideKernelValue() {
		assertEquals(v1.kernel(), 0x10000);
		assertEquals(v1_1__x.kernel(), 0x10100);
		assertEquals(v1__1_x.kernel(), 0x10001);
		assertEquals(v1_1_1_x.kernel(), 0x10101);
	}

}
