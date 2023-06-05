package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class JnaSizeTest {

	@Test
	public void testOverrideSizes() {
		JnaSize.POINTER.size(3);
		JnaSize.BOOL.size(3);
		JnaSize.WCHAR.size(3);
		JnaSize.LONG.size(3);
		JnaSize.LONG_DOUBLE.size(3);
		JnaSize.SIZE_T.size(3);
		assertEquals(JnaSize.POINTER.size(), 3);
		assertEquals(JnaSize.BOOL.size(), 3);
		assertEquals(JnaSize.WCHAR.size(), 3);
		assertEquals(JnaSize.LONG.size(), 3);
		assertEquals(JnaSize.LONG_DOUBLE.size(), 3);
		assertEquals(JnaSize.POINTER.size(), 3);
		JnaSize.clear();
		assertEquals(JnaSize.POINTER.size(), JnaSize.POINTER.size);
		assertEquals(JnaSize.BOOL.size(), JnaSize.BOOL.size);
		assertEquals(JnaSize.WCHAR.size(), JnaSize.WCHAR.size);
		assertEquals(JnaSize.LONG.size(), JnaSize.LONG.size);
		assertEquals(JnaSize.LONG_DOUBLE.size(), JnaSize.LONG_DOUBLE.size);
		assertEquals(JnaSize.SIZE_T.size(), JnaSize.SIZE_T.size);
	}

}
