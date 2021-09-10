package ceri.serial.clib.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.data.ByteUtil;
import ceri.serial.jna.JnaUtil;

public class CUtilTest {

	@Test
	public void testMemcpyBetweenPointers() {
		Memory m0 = CUtil.malloc(ByteUtil.toAscii("abcdefghijklm").copy(0));
		Memory m1 = CUtil.malloc(ByteUtil.toAscii("ABCDEFGHIJKLM").copy(0));
		CUtil.memcpy(m0, 3, m1, 3, 3);
		assertEquals(JnaUtil.string(m0), "abcDEFghijklm");
		assertEquals(JnaUtil.string(m1), "ABCDEFGHIJKLM");
	}

	@Test
	public void testMemcpySamePointer() {
		Memory m = CUtil.malloc(ByteUtil.toAscii("abcdefghijklm").copy(0));
		CUtil.memcpy(m, 0, m, 4, 3);
		assertEquals(JnaUtil.string(m), "efgdefghijklm");
	}

	@Test
	public void testMemcpySamePointerWithOverlap() {
		Memory m = CUtil.malloc(ByteUtil.toAscii("abcdefghijklm").copy(0));
		CUtil.memcpy(m, 0, m, 3, 4);
		assertEquals(JnaUtil.string(m), "defgefghijklm");
	}

}
