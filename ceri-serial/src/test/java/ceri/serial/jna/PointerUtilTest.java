package ceri.serial.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.serial.jna.JnaTestUtil.assertMemory;
import static ceri.serial.jna.JnaTestUtil.p;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.IntByReference;
import ceri.serial.clib.jna.CUtil;

public class PointerUtilTest {

	@Test
	public void testPointer() {
		assertNull(PointerUtil.pointer(0));
	}

	@Test
	public void testPeer() {
		assertEquals(PointerUtil.peer(null), 0L);
		Memory m = new Memory(1);
		assertEquals(PointerUtil.peer(m), Pointer.nativeValue(m));
	}

	@Test
	public void testPointerTypePointer() {
		assertNull(PointerUtil.pointer((PointerType) null));
		IntByReference ref = new IntByReference();
		assertEquals(PointerUtil.pointer(ref), ref.getPointer());
	}

	@Test
	public void testPointerOffset() {
		Pointer p = p(CUtil.mallocBytes(1, 2, 3));
		assertMemory(PointerUtil.offset(p, 0), 1, 2, 3);
		assertMemory(PointerUtil.offset(p, 1), 2, 3);
	}

}
