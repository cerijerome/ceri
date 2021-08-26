package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.TestUtil.exerciseEnum;
import org.junit.Test;
import ceri.common.util.Align;

public class EnumUtilTest {

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

	private static enum Enum {
		a,
		b,
		c;
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
	}

	@Test
	public void testFromOrdinal() {
		assertEquals(EnumUtil.fromOrdinal(Enum.class, 0), Enum.a);
		assertEquals(EnumUtil.fromOrdinal(Enum.class, 1), Enum.b);
		assertEquals(EnumUtil.fromOrdinal(Enum.class, 2), Enum.c);
		assertNull(EnumUtil.fromOrdinal(Enum.class, -1));
		assertNull(EnumUtil.fromOrdinal(Enum.class, 3));
	}

}
