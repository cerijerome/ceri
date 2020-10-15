package ceri.serial.jna;

import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.data.ByteUtil;
import ceri.serial.clib.jna.CUtil;

public class JnaUtilTest {

	@Test
	public void testMemcpyBetweenPointers() {
		Memory m0 = CUtil.malloc(ByteUtil.toAscii("abcdefghijklm").copy(0));
		Memory m1 = CUtil.malloc(ByteUtil.toAscii("ABCDEFGHIJKLM").copy(0));
		CUtil.memcpy(m0, 3, m1, 3, 3);
		assertThat(JnaUtil.string(m0), is("abcDEFghijklm"));
		assertThat(JnaUtil.string(m1), is("ABCDEFGHIJKLM"));
	}

	@Test
	public void testMemcpySamePointer() {
		Memory m = CUtil.malloc(ByteUtil.toAscii("abcdefghijklm").copy(0));
		CUtil.memcpy(m, 0, m, 4, 3);
		assertThat(JnaUtil.string(m), is("efgdefghijklm"));
	}

	@Test
	public void testMemcpySamePointerWithOverlap() {
		Memory m = CUtil.malloc(ByteUtil.toAscii("abcdefghijklm").copy(0));
		CUtil.memcpy(m, 0, m, 3, 4);
		assertThat(JnaUtil.string(m), is("defgefghijklm"));
	}

}
