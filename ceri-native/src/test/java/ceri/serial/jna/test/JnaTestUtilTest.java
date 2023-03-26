package ceri.serial.jna.test;

import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.serial.jna.test.JnaTestUtil.assertMemory;
import static ceri.serial.jna.test.JnaTestUtil.assertPointer;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import ceri.serial.jna.JnaTestData.TestStruct;
import ceri.serial.jna.JnaUtil;

public class JnaTestUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(JnaTestUtil.class);
	}

	@Test
	public void testMemCache() {
		var m = JnaTestUtil.memCache();
		JnaTestUtil.assertMemory(m.calloc(3), 0, 0, 0, 0);
		JnaTestUtil.assertMemory(m.mallocBytes(1, 2, 3), 0, 1, 2, 3);
		m.clear();
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

	@Test
	public void testAssertStructPointer() {
		TestStruct t = new TestStruct(0x1111, null, 1, 2, 3);
		assertPointer(t, t.getPointer());
		assertPointer((Structure) null, null);
		assertAssertion(() -> assertPointer(t, null));
		assertAssertion(() -> assertPointer(t, new Memory(1)));
	}

	@Test
	public void testWorkMemory() {
		JnaTestUtil.workMemory(3, 8, 16);
	}

}
