package ceri.serial.jna.test;

import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.serial.jna.test.JnaTestUtil.assertMemory;
import static ceri.serial.jna.test.JnaTestUtil.assertPointer;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.serial.jna.JnaUtil;

public class JnaTestUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(JnaTestUtil.class);
	}

	@Test
	public void testAssertMemory() {
		Memory m = JnaUtil.mallocBytes(1, 2, 3, 4, 5);
		assertMemory(m, 0, 1, 2, 3, 4, 5);
		assertMemory(m, 1, 2, 3, 4, 5);
		assertAssertion(() -> assertMemory(m, 1, 2, 3, 4, 6));
		assertAssertion(() -> assertMemory(m, 0, 1, 2, 3, 4));
		assertAssertion(() -> assertMemory(m, 0, 1, 2, 3, 4, 5, 6));
	}

	@Test
	public void testAssertPointer() {
		Pointer p = JnaUtil.mallocBytes(1, 2, 3, 4, 5);
		assertPointer(p, 0, 1, 2, 3, 4, 5);
		assertPointer(p, 1, 2, 3, 4, 5);
		assertPointer(p, 0, 1, 2, 3, 4);
		assertAssertion(() -> assertPointer(p, 1, 2, 3, 4, 6));
		assertAssertion(() -> assertPointer(p, 0, 1, 2, 3, 4, 5, 6));
	}

}
