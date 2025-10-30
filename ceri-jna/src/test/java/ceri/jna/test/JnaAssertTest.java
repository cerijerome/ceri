package ceri.jna.test;

import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Structure;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.jna.clib.jna.CUnistd.size_t;
import ceri.jna.test.JnaTesting.MemCache;
import ceri.jna.type.CLong;
import ceri.jna.type.CUlong;
import ceri.jna.util.JnaTestData.TestStruct;
import ceri.jna.util.Jna;

public class JnaAssertTest {
	private MemCache mc = null;
	private Memory m = null;

	@After
	public void after() {
		m = Testing.close(m);
		mc = Testing.close(mc);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(JnaAssert.class);
	}

	@Test
	public void testCLong() {
		m = new Memory(CLong.SIZE);
		Jna.clong(m, 0, Long.MIN_VALUE);
		JnaAssert.clong(m, Long.MIN_VALUE);
		Jna.clong(m, 0, Long.MAX_VALUE);
		JnaAssert.clong(m, Long.MAX_VALUE);
	}

	@Test
	public void testAssertCUlong() {
		m = new Memory(CUlong.SIZE);
		Jna.culong(m, 0, Long.MIN_VALUE);
		JnaAssert.culong(m, Long.MIN_VALUE);
		Jna.culong(m, 0, Long.MAX_VALUE);
		JnaAssert.culong(m, Long.MAX_VALUE);
	}

	@Test
	public void testIntTypeRef() {
		m = new Memory(8);
		new size_t(123).write(m, 0);
		JnaAssert.ref(m, new size_t(123));
		Assert.assertion(() -> JnaAssert.ref(m, new size_t(122)));
	}

	@Test
	public void testMemory() {
		m = Jna.mallocBytes(1, 2, 3, 4, 5);
		JnaAssert.memory(m, 0, 1, 2, 3, 4, 5);
		JnaAssert.memory(m, 1, 2, 3, 4, 5);
		Assert.assertion(() -> JnaAssert.memory(m, 1, 2, 3, 4, 6));
		Assert.assertion(() -> JnaAssert.memory(m, 0, 1, 2, 3, 4));
		Assert.assertion(() -> JnaAssert.memory(m, 0, 1, 2, 3, 4, 5, 6));
	}

	@Test
	public void testPointer() {
		m = Jna.mallocBytes(1, 2, 3, 4, 5);
		JnaAssert.pointer(m, 0, 1, 2, 3, 4, 5);
		JnaAssert.pointer(m, 1, 2, 3, 4, 5);
		JnaAssert.pointer(m, 0, 1, 2, 3, 4);
		Assert.assertion(() -> JnaAssert.pointer(m, 1, 2, 3, 4, 6));
		Assert.assertion(() -> JnaAssert.pointer(m, 0, 1, 2, 3, 4, 5, 6));
	}

	@Test
	public void testStructPointer() {
		var t = new TestStruct(0x1111, null, 1, 2, 3);
		JnaAssert.pointer(t, t.getPointer());
		JnaAssert.pointer((Structure) null, null);
		Assert.assertion(() -> JnaAssert.pointer(t, null));
		Assert.assertion(() -> JnaAssert.pointer(t, new Memory(1)));
	}

	@Test
	public void testLastError() {
		JnaAssert.lastError(() -> Assert.throwIt(JnaTesting.lastError(1)));
		JnaAssert.lastError(1, () -> Assert.throwIt(JnaTesting.lastError(1)));
		Assert
			.assertion(() -> JnaAssert.lastError(2, () -> Assert.throwIt(JnaTesting.lastError(1))));
	}
}
