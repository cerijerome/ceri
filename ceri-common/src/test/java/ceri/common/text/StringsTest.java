package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class StringsTest {

	@Test
	public void testIsNameBoundary() {
		assertEquals(Strings.isNameBoundary(null, 0), false);
		assertEquals(Strings.isNameBoundary("", 0), true);
		assertEquals(Strings.isNameBoundary("abc", 0), true);
		assertEquals(Strings.isNameBoundary("abc", 1), false);
		assertEquals(Strings.isNameBoundary("abc", 2), false);
		assertEquals(Strings.isNameBoundary("abc", 3), true);
		assertEquals(Strings.isNameBoundary("abCde", 1), false);
		assertEquals(Strings.isNameBoundary("abCde", 2), true);
		assertEquals(Strings.isNameBoundary("abCde", 3), false);
		assertEquals(Strings.isNameBoundary("ab123", 1), false);
		assertEquals(Strings.isNameBoundary("ab123", 2), true);
		assertEquals(Strings.isNameBoundary("ab123", 3), false);
		assertEquals(Strings.isNameBoundary("ab__de", 1), false);
		assertEquals(Strings.isNameBoundary("ab__de", 2), true);
		assertEquals(Strings.isNameBoundary("ab__de", 3), false);
		assertEquals(Strings.isNameBoundary("ab__de", 4), true);
	}
}
