package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;
import com.sun.jna.Pointer;
import com.sun.jna.Union;
import ceri.jna.util.JnaTestData.TestStruct;

public class UnionFieldTest {

	public static class TestUnion extends Union {
		public byte b;
		public short s;
		public int i;
		public TestStruct struct;

		public TestUnion() {}

		public TestUnion(Pointer p) {
			super(p);
		}
	}

	@Test
	public void testConstructorIsPrivate() {
		//assertPrivateConstructor(UnionField.class);
	}

	private static TestUnion ref(TestUnion union) {
		return Struct.read(new TestUnion(Struct.write(union).getPointer()));
	}
}
