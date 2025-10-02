package ceri.jna.test;

import static ceri.common.test.AssertUtil.assertAssertion;
import static ceri.common.test.AssertUtil.assertByte;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.throwIt;
import static ceri.jna.test.JnaTestUtil.assertCLong;
import static ceri.jna.test.JnaTestUtil.assertCUlong;
import static ceri.jna.test.JnaTestUtil.assertLastError;
import static ceri.jna.test.JnaTestUtil.assertMemory;
import static ceri.jna.test.JnaTestUtil.assertPointer;
import static ceri.jna.test.JnaTestUtil.assertRef;
import static ceri.jna.test.JnaTestUtil.handleStructRef;
import static ceri.jna.test.JnaTestUtil.lastError;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Structure;
import ceri.common.function.Closeables;
import ceri.jna.clib.jna.CUnistd.size_t;
import ceri.jna.test.JnaTestUtil.MemCache;
import ceri.jna.type.CLong;
import ceri.jna.type.CUlong;
import ceri.jna.type.Struct;
import ceri.jna.util.JnaTestData;
import ceri.jna.util.JnaTestData.TestStruct;
import ceri.jna.util.JnaUtil;

public class JnaTestUtilTest {
	private MemCache mc = null;
	private Memory m = null;

	@After
	public void after() {
		Closeables.close(m, mc);
		m = null;
		mc = null;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(JnaTestUtil.class);
	}

	@Test
	public void testMemCache() {
		mc = JnaTestUtil.memCache();
		assertMemory(mc.calloc(3).m, 0, 0, 0, 0);
		assertMemory(mc.mallocBytes(1, 2, 3).m, 0, 1, 2, 3);
	}

	@Test
	public void testAssertCLong() {
		m = new Memory(CLong.SIZE);
		JnaUtil.clong(m, 0, Long.MIN_VALUE);
		assertCLong(m, Long.MIN_VALUE);
		JnaUtil.clong(m, 0, Long.MAX_VALUE);
		assertCLong(m, Long.MAX_VALUE);
	}

	@Test
	public void testAssertCUlong() {
		m = new Memory(CUlong.SIZE);
		JnaUtil.culong(m, 0, Long.MIN_VALUE);
		assertCUlong(m, Long.MIN_VALUE);
		JnaUtil.culong(m, 0, Long.MAX_VALUE);
		assertCUlong(m, Long.MAX_VALUE);
	}

	@Test
	public void testAssertIntTypeRef() {
		m = new Memory(8);
		new size_t(123).write(m, 0);
		assertRef(m, new size_t(123));
		assertAssertion(() -> assertRef(m, new size_t(122)));
	}

	@Test
	public void testAssertMemory() {
		m = JnaUtil.mallocBytes(1, 2, 3, 4, 5);
		assertMemory(m, 0, 1, 2, 3, 4, 5);
		assertMemory(m, 1, 2, 3, 4, 5);
		assertAssertion(() -> assertMemory(m, 1, 2, 3, 4, 6));
		assertAssertion(() -> assertMemory(m, 0, 1, 2, 3, 4));
		assertAssertion(() -> assertMemory(m, 0, 1, 2, 3, 4, 5, 6));
	}

	@Test
	public void testAssertPointer() {
		m = JnaUtil.mallocBytes(1, 2, 3, 4, 5);
		assertPointer(m, 0, 1, 2, 3, 4, 5);
		assertPointer(m, 1, 2, 3, 4, 5);
		assertPointer(m, 0, 1, 2, 3, 4);
		assertAssertion(() -> assertPointer(m, 1, 2, 3, 4, 6));
		assertAssertion(() -> assertPointer(m, 0, 1, 2, 3, 4, 5, 6));
	}

	@Test
	public void testAssertStructPointer() {
		var t = new TestStruct(0x1111, null, 1, 2, 3);
		assertPointer(t, t.getPointer());
		assertPointer((Structure) null, null);
		assertAssertion(() -> assertPointer(t, null));
		assertAssertion(() -> assertPointer(t, new Memory(1)));
	}

	@Test
	public void testAssertLastError() {
		assertLastError(() -> throwIt(lastError(1)));
		assertLastError(1, () -> throwIt(lastError(1)));
		assertAssertion(() -> assertLastError(2, () -> throwIt(lastError(1))));
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

	@Test
	public void testHandleStructRef() {
		var t0 = new TestStruct(0x1111, null, 1, 2, 3);
		handleStructRef(Struct.write(t0).getPointer(), new TestStruct(), t -> {
			JnaTestData.assertStruct(t, 0x1111, null, 1, 2, 3);
			t.i = 0x2222;
		});
		assertEquals(Struct.read(t0).i, 0x2222);
	}

}
