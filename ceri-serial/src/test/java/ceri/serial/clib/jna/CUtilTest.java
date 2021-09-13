package ceri.serial.clib.jna;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import static ceri.serial.jna.JnaTestUtil.*;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteUtil;
import ceri.common.test.CallSync;
import ceri.serial.jna.JnaUtil;

public class CUtilTest {

	@Test
	public void testValidFd() {
		assertEquals(CUtil.validFd(-1), false);
		assertEquals(CUtil.validFd(0), true);
		assertEquals(CUtil.validFd(1), true);
	}
	
	@Test
	public void testCalloc() {
		assertNull(CUtil.calloc(0));
		assertMemory(CUtil.calloc(5), 0, 0, 0, 0, 0);
	}
	
	@Test
	public void testCallocArray() {
		assertArray(CUtil.callocArray(0));
		Pointer[] array = CUtil.callocArray(6,  3);
		assertArray(array, array[0], array[0].share(6), array[0].share(12));
		assertMemory(array[0], 0, 0, 0, 0, 0, 0);
		assertMemory(array[1], 0, 0, 0, 0, 0, 0);
		assertMemory(array[2], 0, 0, 0, 0, 0, 0);
	}
	
	@Test
	public void testMallocArray() {
		assertArray(CUtil.mallocArray(0));
		Pointer[] array = CUtil.callocArray(6,  3);
		assertArray(array, array[0], array[0].share(6), array[0].share(12));
	}

	@Test
	public void testMalloc() {
		assertNull(CUtil.malloc(0));
		assertNull(CUtil.malloc(new byte[0]));
		assertNull(CUtil.malloc(ArrayUtil.bytes(1, 2, 3), 1, 0));
	}		

	@Test
	public void testMemcpy() {
		Memory m = CUtil.mallocBytes(1, 2, 3, 4, 5, 6, 7, 8, 9);
		assertEquals(CUtil.memcpy(m, 3, 3, 5), 5);
		assertMemory(m, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		assertEquals(CUtil.memcpy(m, 3, 0, 5), 5);
		assertMemory(m, 1, 2, 3, 1, 2, 3, 4, 5, 9);
	}
	
	@Test
	public void testMemcpyForLargeBuffer() {
		Memory m = CUtil.malloc(8 * 1024 + 8);
		assertEquals(CUtil.memcpy(m, 8, 0, 8 * 1024), 8 * 1024);
	}
	
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
