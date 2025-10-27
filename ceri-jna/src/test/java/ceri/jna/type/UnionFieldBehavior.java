package ceri.jna.type;

import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.test.Assert;
import ceri.jna.type.Struct.Fields;
import ceri.jna.util.JnaTestData.TestStruct;

public class UnionFieldBehavior {

	@Fields({ "i", "ts" })
	public static class TestUnion extends Union {
		public static final UnionField<TestUnion, Integer> I =
			UnionField.of("i", u -> u.i, (u, i) -> u.i = i);
		public static final UnionField<TestUnion, TestStruct> TS =
			UnionField.of("ts", u -> u.ts, (u, t) -> u.ts = t);
		public int i;
		public byte[] b = new byte[3];
		public TestStruct ts;
	}

	@Test
	public void shouldSetValues() {
		var tu = tu(0, null);
		TestUnion.I.write(tu, 3);
		Assert.equal(TestUnion.I.get(tu), 3);
		TestUnion.I.set(tu, 2);
		Assert.equal(TestUnion.I.get(tu), 2);
		Assert.equal(TestUnion.I.read(tu), 3);
	}

	@Test
	public void shouldWriteValues() {
		var tu = tu(3, null);
		Assert.equal(TestUnion.I.read(tu), 0);
		TestUnion.I.set(tu, 2);
		TestUnion.I.write(tu);
		Assert.equal(TestUnion.I.read(tu), 2);
	}

	@Test
	public void shouldSetType() {
		var tu = tu(3, null);
		TestUnion.TS.write(tu, t -> t.i = 2);
		Assert.equal(TestUnion.TS.read(tu).i, 2);
	}

	@Test
	public void shouldHandleNullUnions() {
		Assert.equal(TestUnion.I.get(null), null);
		TestUnion.I.set(null, 1);
		TestUnion.I.set(null);
		Assert.equal(TestUnion.I.read(null), null);
		TestUnion.I.write(null, 1);
		TestUnion.I.write(null, _ -> {});
		TestUnion.I.write(null);
	}

	private static TestUnion tu(int i, Pointer p, int... b) {
		var tu = new TestUnion();
		tu.i = i;
		tu.ts = new TestStruct(i, p, b);
		return tu;
	}
}
