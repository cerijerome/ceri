package ceri.jna.test;

import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertByte;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.jna.test.JnaTestUtil.assertMemory;
import static ceri.jna.test.JnaTestUtil.assertPointer;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import ceri.jna.util.JnaTestData.TestStruct;
import ceri.jna.util.JnaUtil;

public class JnaTestUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(JnaTestUtil.class);
	}

	@Test
	public void testMemCache() {
		try (var m = JnaTestUtil.memCache()) {
			JnaTestUtil.assertMemory(m.calloc(3).m, 0, 0, 0, 0);
			JnaTestUtil.assertMemory(m.mallocBytes(1, 2, 3).m, 0, 1, 2, 3);
		}
	}

	@Test
	public void testAssertNlong() {
		try (var m = new Memory(NativeLong.SIZE)) {
			JnaUtil.nlong(m, 0, Long.MIN_VALUE);
			JnaTestUtil.assertNlong(m, Long.MIN_VALUE);
			JnaUtil.nlong(m, 0, Long.MAX_VALUE);
			JnaTestUtil.assertNlong(m, Long.MAX_VALUE);
		}
	}

	@Test
	public void testAssertMemory() {
		try (Memory m = JnaUtil.mallocBytes(1, 2, 3, 4, 5)) {
			assertMemory(m, 0, 1, 2, 3, 4, 5);
			assertMemory(m, 1, 2, 3, 4, 5);
			assertAssertion(() -> assertMemory(m, 1, 2, 3, 4, 6));
			assertAssertion(() -> assertMemory(m, 0, 1, 2, 3, 4));
			assertAssertion(() -> assertMemory(m, 0, 1, 2, 3, 4, 5, 6));
		}
	}

	@Test
	public void testAssertPointer() {
		try (Memory p = JnaUtil.mallocBytes(1, 2, 3, 4, 5)) {
			assertPointer(p, 0, 1, 2, 3, 4, 5);
			assertPointer(p, 1, 2, 3, 4, 5);
			assertPointer(p, 0, 1, 2, 3, 4);
			assertAssertion(() -> assertPointer(p, 1, 2, 3, 4, 6));
			assertAssertion(() -> assertPointer(p, 0, 1, 2, 3, 4, 5, 6));
		}
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
	public void testBuffer() {
		var b = JnaTestUtil.buffer(-1, 0x7f, 0, 0x80);
		assertByte(b.get(), -1);
		assertByte(b.get(), 0x7f);
		assertByte(b.get(), 0);
		assertByte(b.get(), 0x80);
	}

	@Test
	public void testWorkMemory() {
		JnaTestUtil.workMemory(3, 8, 16);
	}

}
