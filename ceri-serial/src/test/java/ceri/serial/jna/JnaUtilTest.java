package ceri.serial.jna;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.data.ByteUtil;

public class JnaUtilTest {

	@Test
	public void testMemcpyBetweenPointers() {
		Memory m0 = JnaUtil.malloc(ByteUtil.toAscii("abcdefghijklm").copy());
		Memory m1 = JnaUtil.malloc(ByteUtil.toAscii("ABCDEFGHIJKLM").copy());
		JnaUtil.memcpy(m0, 3, m1, 3, 3);
		assertThat(JnaUtil.string(m0), is("abcDEFghijklm"));
		assertThat(JnaUtil.string(m1), is("ABCDEFGHIJKLM"));
	}

	@Test
	public void testMemcpySamePointer() {
		Memory m = JnaUtil.malloc(ByteUtil.toAscii("abcdefghijklm").copy());
		JnaUtil.memcpy(m, 0, m, 4, 3);
		assertThat(JnaUtil.string(m), is("efgdefghijklm"));
	}

	@Test
	public void testMemcpySamePointerWithOverlap() {
		Memory m = JnaUtil.malloc(ByteUtil.toAscii("abcdefghijklm").copy());
		JnaUtil.memcpy(m, 0, m, 3, 4);
		assertThat(JnaUtil.string(m), is("defgefghijklm"));
	}

}
