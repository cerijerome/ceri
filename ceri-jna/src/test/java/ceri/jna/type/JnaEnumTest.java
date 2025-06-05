package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.jna.util.JnaEnum.Value;
import ceri.jna.util.JnaEnum.Valued;

public class JnaEnumTest {

	/** Enum using default value() ordinals */
	private static enum E0 implements Valued {
		A,
		B,
		C;
	}

	/** Enum using default value() with class annotation value 1 */
	@Value(1)
	private static enum E1 implements Valued {
		A,
		B,
		C;
	}

	/** Enum using default value() with enum annotation value 3 */
	private static enum E2 implements Valued {
		A,
		@Value(3)
		B,
		C;
	}

	/** Enum using overridden value() */
	private static enum E3 implements Valued {
		A,
		B,
		@Value(2) // ignored
		C;

		@Override
		public int value() {
			return ordinal() * ordinal();
		}
	}

	private static enum E4 implements Valued {}

	private static enum E5 implements Valued {
		A
	}

	private static class E6 implements Valued {
		private static final E6 A = new E6();
	}

	@Test
	public void testValue() {
		assertEquals(E0.A.value(), 0);
		assertEquals(E0.B.value(), 1);
		assertEquals(E0.C.value(), 2);
		assertEquals(E1.A.value(), 1);
		assertEquals(E1.B.value(), 2);
		assertEquals(E1.C.value(), 3);
		assertEquals(E2.A.value(), 0);
		assertEquals(E2.B.value(), 3);
		assertEquals(E2.C.value(), 4);
		assertEquals(E3.A.value(), 0);
		assertEquals(E3.B.value(), 1);
		assertEquals(E3.C.value(), 4);
		assertEquals(E6.A.value(), 0);
	}

	@Test
	public void testFromValue() {
		assertEquals(JnaEnum.from(E0.class, 0), E0.A);
		assertEquals(JnaEnum.from(E0.class, 1), E0.B);
		assertEquals(JnaEnum.from(E0.class, 2), E0.C);
		assertEquals(JnaEnum.from(E1.class, 1), E1.A);
		assertEquals(JnaEnum.from(E1.class, 2), E1.B);
		assertEquals(JnaEnum.from(E1.class, 3), E1.C);
		assertEquals(JnaEnum.from(E2.class, 0), E2.A);
		assertEquals(JnaEnum.from(E2.class, 3), E2.B);
		assertEquals(JnaEnum.from(E2.class, 4), E2.C);
		assertEquals(JnaEnum.from(E3.class, 0), E3.A);
		assertEquals(JnaEnum.from(E3.class, 1), E3.B);
		assertEquals(JnaEnum.from(E3.class, 4), E3.C);
	}

	@Test
	public void testFromBadValue() {
		assertEquals(JnaEnum.from(E0.class, 3), null);
		assertEquals(JnaEnum.from(E1.class, 4), null);
		assertEquals(JnaEnum.from(E2.class, 1), null);
		assertEquals(JnaEnum.from(E3.class, 2), null);
		assertEquals(JnaEnum.from(E4.class, 0), null);
		assertEquals(JnaEnum.from(E5.class, 1), null);
	}

	@Test
	public void testFromOrdinal() {
		assertEquals(JnaEnum.fromOrdinal(E0.class, 2), E0.C);
		assertEquals(JnaEnum.fromOrdinal(E1.class, 2), E1.C);
		assertEquals(JnaEnum.fromOrdinal(E2.class, 2), E2.C);
	}

	@Test
	public void testFromBadOrdinal() {
		assertEquals(JnaEnum.fromOrdinal(E1.class, 3), null);
		assertEquals(JnaEnum.fromOrdinal(E1.class, -1), null);
	}

}
