package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNpe;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEnum;
import org.junit.Test;
import ceri.common.util.Align;

public class EnumUtilTest {

	private static enum Enum {
		a,
		b,
		c;
	}

	@Test
	public void testVerifyAllowed() {
		EnumUtil.verifyAllowed(Enum.a, Enum.a, Enum.b, Enum.c);
		EnumUtil.verifyAllowed(Enum.a, Enum.a);
		assertIllegalArg(() -> EnumUtil.verifyAllowed(Enum.a, Enum.b, Enum.c));
		assertIllegalArg(() -> EnumUtil.verifyAllowed(Enum.a));
	}

	@Test
	public void testVerifyDisallowed() {
		EnumUtil.verifyDisallowed(Enum.a, Enum.b, Enum.c);
		EnumUtil.verifyDisallowed(Enum.a);
		assertIllegalArg(() -> EnumUtil.verifyDisallowed(Enum.a, Enum.b, Enum.a));
		assertIllegalArg(() -> EnumUtil.verifyDisallowed(Enum.a, Enum.a));
	}

	@Test
	public void testFind() {
		assertEquals(EnumUtil.find(Align.H.class, t -> t != Align.H.left), Align.H.center);
		assertEquals(EnumUtil.find(Align.H.class, t -> t.name().endsWith("t")), Align.H.left);
		assertNull(EnumUtil.find(Align.H.class, t -> t.name().endsWith("x")));
	}

	@Test
	public void testEnums() {
		assertIterable(EnumUtil.enums(Align.H.class), Align.H.left, Align.H.center, Align.H.right);
		assertIterable(EnumUtil.enums(Align.V.class), Align.V.top, Align.V.middle, Align.V.bottom);
		exerciseEnum(Align.H.class);
		exerciseEnum(Align.V.class);
	}

	@Test
	public void testEnumsReversed() {
		assertIterable(EnumUtil.enumsReversed(Align.H.class), Align.H.right, Align.H.center,
			Align.H.left);
	}

	@Test
	public void testValueOf() {
		assertEquals(EnumUtil.valueOf(null, "a", Enum.a), Enum.a);
		assertEquals(EnumUtil.valueOf(Enum.class, "a"), Enum.a);
		assertNull(EnumUtil.valueOf(Enum.class, "ab"));
		assertNull(EnumUtil.valueOf(Enum.class, null, null));
		assertEquals(EnumUtil.valueOf(Enum.class, "b", null), Enum.b);
		assertEquals(EnumUtil.valueOf(Enum.class, null, Enum.a), Enum.a);
		assertEquals(EnumUtil.valueOf(Enum.class, "ab", Enum.c), Enum.c);
		assertNull(EnumUtil.valueOf(Enum.class, "ab", null));
		assertEquals(EnumUtil.valueOf("a", Enum.c), Enum.a);
		assertEquals(EnumUtil.valueOf("ab", Enum.c), Enum.c);
		assertNpe(() -> EnumUtil.valueOf("a", (Enum) null));
	}

	@Test
	public void testFromOrdinal() {
		assertEquals(EnumUtil.fromOrdinal(Enum.class, 0), Enum.a);
		assertEquals(EnumUtil.fromOrdinal(Enum.class, 1), Enum.b);
		assertEquals(EnumUtil.fromOrdinal(Enum.class, 2), Enum.c);
		assertNull(EnumUtil.fromOrdinal(Enum.class, -1));
		assertNull(EnumUtil.fromOrdinal(Enum.class, 3));
	}

	@Test
	public void testFromOrdinalValid() {
		assertEquals(EnumUtil.fromOrdinalValid(Enum.class, 0), Enum.a);
		assertEquals(EnumUtil.fromOrdinalValid(Enum.class, 1), Enum.b);
		assertEquals(EnumUtil.fromOrdinalValid(Enum.class, 2), Enum.c);
		assertThrown(() -> EnumUtil.fromOrdinalValid(Enum.class, -1));
		assertThrown(() -> EnumUtil.fromOrdinalValid(Enum.class, 3));
	}

}
