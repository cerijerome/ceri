package ceri.common.util;

import org.junit.Test;
import ceri.common.function.Excepts;
import ceri.common.test.Assert;

public class BasicsTest {

	private static class TestRef extends Basics.Ref<AutoCloseable> {
		private TestRef(AutoCloseable c) {
			super(c);
		}
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Basics.class);
	}

	@Test
	public void shouldProvideRefAccess() throws Exception {
		try (AutoCloseable c = () -> {}) {
			var r = new TestRef(c);
			Assert.equal(r.ref, c);
		}
	}

	@Test
	public void testAllNull() {
		Assert.equal(Basics.allNull((Object[]) null), true);
		Assert.equal(Basics.allNull(), true);
		Assert.equal(Basics.allNull(new Object[] { null }), true);
		Assert.equal(Basics.allNull("a", null), false);
		Assert.equal(Basics.allNull("a", "B"), false);
	}

	@Test
	public void testAnyNull() {
		Assert.equal(Basics.anyNull((Object[]) null), false);
		Assert.equal(Basics.anyNull(), false);
		Assert.equal(Basics.anyNull(new Object[] { null }), true);
		Assert.equal(Basics.anyNull("a", null), true);
		Assert.equal(Basics.anyNull("a", "B"), false);
	}

	@Test
	public void testNoneNull() {
		Assert.equal(Basics.noneNull((Object[]) null), true);
		Assert.equal(Basics.noneNull(), true);
		Assert.equal(Basics.noneNull(new Object[] { null }), false);
		Assert.equal(Basics.noneNull("a", null), false);
		Assert.equal(Basics.noneNull("a", "B"), true);
	}

	@Test
	public void testDef() throws Exception {
		Assert.isNull(Basics.def(null, null));
		Assert.equal(Basics.def(null, 1), 1);
		Assert.equal(Basics.def(1, null), 1);
		Assert.equal(Basics.def(1, (Excepts.Supplier<Exception, Integer>) null), 1);
		Assert.equal(Basics.def(1, 2), 1);
	}

	@Test
	public void testDefInt() {
		Assert.nullPointer(() -> Basics.defInt(null, null));
		Assert.equal(Basics.defInt(null, -1), -1);
		Assert.equal(Basics.defInt(1, -1), 1);
		Assert.equal(Basics.defInt(null, () -> -1), -1);
		Assert.equal(Basics.defInt(1, () -> -1), 1);
	}

	@Test
	public void testDefLong() {
		Assert.nullPointer(() -> Basics.defLong(null, null));
		Assert.equal(Basics.defLong(null, -1), -1L);
		Assert.equal(Basics.defLong(1L, -1), 1L);
		Assert.equal(Basics.defLong(null, () -> -1), -1L);
		Assert.equal(Basics.defLong(1L, () -> -1), 1L);
	}

	@Test
	public void testDefDouble() {
		Assert.nullPointer(() -> Basics.defDouble(null, null));
		Assert.equal(Basics.defDouble(null, -1), -1.0);
		Assert.equal(Basics.defDouble(1.0, -1), 1.0);
		Assert.equal(Basics.defDouble(null, () -> -1), -1.0);
		Assert.equal(Basics.defDouble(1.0, () -> -1), 1.0);
	}

	@Test
	public void testDefGet() throws Exception {
		Assert.equal(Basics.defGet(null, null), null);
		Assert.equal(Basics.defGet(null, () -> null), null);
		Assert.equal(Basics.defGet(null, () -> 1), 1);
		Assert.equal(Basics.defGet(() -> null, null), null);
		Assert.equal(Basics.defGet(() -> null, () -> null), null);
		Assert.equal(Basics.defGet(() -> null, () -> 1), 1);
		Assert.equal(Basics.defGet(() -> -1, null), -1);
		Assert.equal(Basics.defGet(() -> -1, () -> null), -1);
		Assert.equal(Basics.defGet(() -> -1, () -> 1), -1);
	}

	@Test
	public void testDefGetInt() {
		Assert.nullPointer(() -> Basics.defGetInt(null, null));
		Assert.equal(Basics.defGetInt(null, () -> 1), 1);
		Assert.nullPointer(() -> Basics.defGetInt(() -> null, null));
		Assert.equal(Basics.defGetInt(() -> null, () -> 1), 1);
		Assert.equal(Basics.defGetInt(() -> -1, null), -1);
		Assert.equal(Basics.defGetInt(() -> -1, () -> 1), -1);
	}

	@Test
	public void testDefGetLong() {
		Assert.nullPointer(() -> Basics.defGetLong(null, null));
		Assert.equal(Basics.defGetLong(null, () -> 1), 1L);
		Assert.nullPointer(() -> Basics.defGetLong(() -> null, null));
		Assert.equal(Basics.defGetLong(() -> null, () -> 1), 1L);
		Assert.equal(Basics.defGetLong(() -> -1L, null), -1L);
		Assert.equal(Basics.defGetLong(() -> -1L, () -> 1), -1L);
	}

	@Test
	public void testDefGetDouble() {
		Assert.nullPointer(() -> Basics.defGetDouble(null, null));
		Assert.equal(Basics.defGetDouble(null, () -> 1), 1.0);
		Assert.nullPointer(() -> Basics.defGetDouble(() -> null, null));
		Assert.equal(Basics.defGetDouble(() -> null, () -> 1), 1.0);
		Assert.equal(Basics.defGetDouble(() -> -1.0, null), -1.0);
		Assert.equal(Basics.defGetDouble(() -> -1.0, () -> 1), -1.0);
	}

	@Test
	public void testTernaryGet() {
		Assert.equal(Basics.ternaryGet(false, () -> "x"), null);
		Assert.equal(Basics.ternaryGet(true, () -> "x"), "x");
		Assert.equal(Basics.ternaryGet(true, () -> "x", () -> "y"), "x");
	}

	@Test
	public void testTernaryGetWithNull() {
		Assert.equal(Basics.ternaryGet(null, () -> "T", () -> "F", () -> null), null);
		Assert.equal(Basics.ternaryGet(true, () -> "T", () -> "F", () -> null), "T");
		Assert.equal(Basics.ternaryGet(false, () -> "T", () -> "F", () -> null), "F");
		Assert.equal(Basics.ternaryGet(null, () -> "T", () -> "F", null), null);
	}

	@Test
	public void testTernary() {
		Assert.equal(Basics.ternary(true, "a", "b"), "a");
		Assert.equal(Basics.ternary(false, "a", "b"), "b");
		Assert.equal(Basics.ternary(null, "a", "b", "c"), "c");
		Assert.equal(Basics.ternary(true, "a", "b", "c"), "a");
		Assert.equal(Basics.ternary(false, "a", "b", "c"), "b");
	}

	@Test
	public void testTernaryInt() {
		Assert.equal(Basics.ternaryInt(true, 1, -1), 1);
		Assert.equal(Basics.ternaryInt(false, 1, -1), -1);
		Assert.equal(Basics.ternaryInt(null, 1, -1, 0), 0);
		Assert.equal(Basics.ternaryInt(true, 1, -1, 0), 1);
		Assert.equal(Basics.ternaryInt(false, 1, -1, 0), -1);
	}

	@Test
	public void testTernaryLong() {
		Assert.equal(Basics.ternaryLong(true, 1, -1), 1L);
		Assert.equal(Basics.ternaryLong(false, 1, -1), -1L);
		Assert.equal(Basics.ternaryLong(null, 1, -1, 0), 0L);
		Assert.equal(Basics.ternaryLong(true, 1, -1, 0), 1L);
		Assert.equal(Basics.ternaryLong(false, 1, -1, 0), -1L);
	}

	@Test
	public void testTernaryDouble() {
		Assert.equal(Basics.ternaryDouble(true, 1, -1), 1.0);
		Assert.equal(Basics.ternaryDouble(false, 1, -1), -1.0);
		Assert.equal(Basics.ternaryDouble(null, 1, -1, 0), 0.0);
		Assert.equal(Basics.ternaryDouble(true, 1, -1, 0), 1.0);
		Assert.equal(Basics.ternaryDouble(false, 1, -1, 0), -1.0);
	}
}
