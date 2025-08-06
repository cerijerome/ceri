package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertOrdered;
import org.junit.Test;

public class EnumsTest {

	private static enum E {
		a,
		b,
		c;
	}

	private static enum Prefix {
		a_b_c_123,
		a_b_c_123456,
		a_b_c_12345;
	}

	private static enum Prefix2 {
		abc,
		abcd,
		abcde;
	}

	@Test
	public void testOf() {
		assertOrdered(Enums.of(String.class)); // no enums
		assertOrdered(Enums.of(E.class), E.a, E.b, E.c);
	}

	@Test
	public void testValueOf() {
		assertEquals(Enums.valueOf(null, "a", E.a), E.a);
		assertEquals(Enums.valueOf(E.class, "a"), E.a);
		assertNull(Enums.valueOf(E.class, "ab"));
		assertNull(Enums.valueOf(E.class, null, null));
		assertEquals(Enums.valueOf(E.class, "b", null), E.b);
		assertEquals(Enums.valueOf(E.class, null, E.a), E.a);
		assertEquals(Enums.valueOf(E.class, "ab", E.c), E.c);
		assertNull(Enums.valueOf(E.class, "ab", null));
		assertEquals(Enums.valueOf("a", E.c), E.a);
		assertEquals(Enums.valueOf("ab", E.c), E.c);
		assertEquals(Enums.valueOf("a", (E) null), null);
	}

	@Test
	public void testShortName() {
		assertEquals(Enums.shortName(null), "null");
		assertEquals(Enums.shortName(E.a), "a");
		assertEquals(Enums.shortName(E.b), "b");
		assertEquals(Enums.shortName(E.c), "c");
		assertEquals(Enums.shortName(Prefix.a_b_c_123), "123");
		assertEquals(Enums.shortName(Prefix.a_b_c_12345), "12345");
		assertEquals(Enums.shortName(Prefix.a_b_c_123456), "123456");
		assertEquals(Enums.shortName(Prefix2.abc), "abc");
		assertEquals(Enums.shortName(Prefix2.abcd), "abcd");
		assertEquals(Enums.shortName(Prefix2.abcde), "abcde");
	}

	@Test
	public void testFind() {
		assertEquals(Enums.find(E.class, t -> t != E.a), E.b);
		assertEquals(Enums.find(E.class, t -> t.name().equals("c")), E.c);
		assertEquals(Enums.find(E.class, t -> t.name().equals("x")), null);
		assertEquals(Enums.find(E.class, t -> t.name().equals("x"), E.b), E.b);
	}

}
