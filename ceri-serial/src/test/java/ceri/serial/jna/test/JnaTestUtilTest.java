package ceri.serial.jna.test;

import static ceri.common.test.TestUtil.assertAssertion;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.serial.jna.test.JnaTestUtil.assertMemory;
import static ceri.serial.jna.test.JnaTestUtil.assertPointer;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.serial.clib.jna.CUtil;

public class JnaTestUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(JnaTestUtil.class);
	}

	@Test
	public void testAssertMemory() {
		Memory m = CUtil.malloc(1, 2, 3, 4, 5);
		assertMemory(m, 0, 1, 2, 3, 4, 5);
		assertMemory(m, 1, 2, 3, 4, 5);
		assertAssertion(() -> assertMemory(m, 1, 2, 3, 4, 6));
		assertAssertion(() -> assertMemory(m, 0, 1, 2, 3, 4));
		assertAssertion(() -> assertMemory(m, 0, 1, 2, 3, 4, 5, 6));
	}

	@Test
	public void testAssertPointer() {
		Pointer p = CUtil.malloc(1, 2, 3, 4, 5);
		assertPointer(p, 0, 1, 2, 3, 4, 5);
		assertPointer(p, 1, 2, 3, 4, 5);
		assertPointer(p, 0, 1, 2, 3, 4);
		assertAssertion(() -> assertPointer(p, 1, 2, 3, 4, 6));
		assertAssertion(() -> assertPointer(p, 0, 1, 2, 3, 4, 5, 6));
	}

}
