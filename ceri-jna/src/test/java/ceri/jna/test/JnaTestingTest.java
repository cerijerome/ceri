package ceri.jna.test;

import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.jna.test.JnaTesting.MemCache;
import ceri.jna.type.Struct;
import ceri.jna.util.JnaTestData;
import ceri.jna.util.JnaTestData.TestStruct;

public class JnaTestingTest {
	private MemCache mc = null;
	private Memory m = null;

	@After
	public void after() {
		m = Testing.close(m);
		mc = Testing.close(mc);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(JnaTesting.class);
	}

	@Test
	public void testMemCache() {
		mc = JnaTesting.memCache();
		JnaAssert.memory(mc.calloc(3).m, 0, 0, 0, 0);
		JnaAssert.memory(mc.mallocBytes(1, 2, 3).m, 0, 1, 2, 3);
	}

	@Test
	public void testBuffer() {
		var b = JnaTesting.buffer(-1, 0x7f, 0, 0x80);
		Assert.equals(b.get(), -1);
		Assert.equals(b.get(), 0x7f);
		Assert.equals(b.get(), 0);
		Assert.equals(b.get(), 0x80);
	}

	@Test
	public void testWorkMemory() {
		JnaTesting.workMemory(3, 8, 16);
	}

	@Test
	public void testHandleStructRef() {
		var t0 = new TestStruct(0x1111, null, 1, 2, 3);
		JnaTesting.handleStructRef(Struct.write(t0).getPointer(), new TestStruct(), t -> {
			JnaTestData.assertStruct(t, 0x1111, null, 1, 2, 3);
			t.i = 0x2222;
		});
		Assert.equal(Struct.read(t0).i, 0x2222);
	}
}
