package ceri.common.data;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertString;
import java.util.Set;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.collect.Enums;

public class XcoderTest {
	private static final N[] nullNs = null;
	private static final I[] nullIs = null;
	private static final L[] nullLs = null;
	private static final Set<N> nullNSet = null;
	private static final Set<I> nullISet = null;
	private static final Set<L> nullLSet = null;

	public enum N {
		;

		public static final Xcoder.Type<N> xc = Xcoder.type(N.class, _ -> 1);
		public static final Xcoder.Types<N> xcs = Xcoder.types(N.class, _ -> 1);
	}

	public enum I {
		a(0),
		b(1),
		c(3),
		d(0x80),
		e(0x8000),
		f(0x80000000);

		public static final Xcoder.Type<I> xc = Xcoder.type(I.class);
		public static final Xcoder.Types<I> xcs = Xcoder.types(I.class);
		public final int value;

		private I(int value) {
			this.value = value;
		}
	}

	public enum L {
		a(0L),
		b(1L),
		c(3L),
		d(0x80L),
		e(0x8000L),
		f(0x80000000L),
		g(0x80000000_00000000L);

		public static final Xcoder.Type<L> xc = Xcoder.type(L.class);
		public static final Xcoder.Types<L> xcs = Xcoder.types(L.class);
		public final long value;

		private L(long value) {
			this.value = value;
		}
	}

	@Test
	public void testAll() {
		assertOrdered(N.xc.all());
		assertOrdered(N.xcs.all());
		assertOrdered(I.xc.all(), Enums.of(I.class));
		assertOrdered(I.xcs.all(), Enums.of(I.class));
		assertOrdered(L.xc.all(), Enums.of(L.class));
		assertOrdered(L.xcs.all(), Enums.of(L.class));
	}

	@Test
	public void testMask() {
		assertEquals(N.xc.mask(), 0L);
		assertEquals(N.xc.maskInt(), 0);
		assertEquals(N.xcs.mask(), 0L);
		assertEquals(N.xcs.maskInt(), 0);
		assertEquals(I.xc.mask(), 0x80008083L);
		assertEquals(I.xc.maskInt(), 0x80008083);
		assertEquals(I.xcs.mask(), 0x80008083L);
		assertEquals(I.xcs.maskInt(), 0x80008083);
		assertEquals(L.xc.mask(), 0x80000000_80008083L);
		assertEquals(L.xc.maskInt(), 0x80008083);
		assertEquals(L.xcs.mask(), 0x80000000_80008083L);
		assertEquals(L.xcs.maskInt(), 0x80008083);
	}

	@Test
	public void testEncode() {
		assertEquals(N.xc.encode(null), 0L);
		assertEquals(N.xc.encodeInt(null), 0);
		assertEquals(N.xcs.encode(nullNs), 0L);
		assertEquals(N.xcs.encodeInt(nullNs), 0);
		assertEquals(N.xcs.encode(), 0L);
		assertEquals(N.xcs.encodeInt(), 0);
		assertEquals(I.xc.encode(null), 0L);
		assertEquals(I.xc.encodeInt(null), 0);
		assertEquals(I.xc.encode(I.f), 0x80000000L);
		assertEquals(I.xc.encodeInt(I.f), 0x80000000);
		assertEquals(I.xcs.encode(nullIs), 0L);
		assertEquals(I.xcs.encodeInt(nullIs), 0);
		assertEquals(I.xcs.encode(nullISet), 0L);
		assertEquals(I.xcs.encodeInt(nullISet), 0);
		assertEquals(I.xcs.encode(), 0L);
		assertEquals(I.xcs.encodeInt(), 0);
		assertEquals(I.xcs.encode(I.a, I.e, I.f), 0x80008000L);
		assertEquals(I.xcs.encodeInt(I.a, I.e, I.f), 0x80008000);
		assertEquals(L.xc.encode(null), 0L);
		assertEquals(L.xc.encodeInt(null), 0);
		assertEquals(L.xc.encode(L.f), 0x80000000L);
		assertEquals(L.xc.encodeInt(L.f), 0x80000000);
		assertEquals(L.xcs.encode(nullLs), 0L);
		assertEquals(L.xcs.encodeInt(nullLs), 0);
		assertEquals(L.xcs.encode(nullLSet), 0L);
		assertEquals(L.xcs.encodeInt(nullLSet), 0);
		assertEquals(L.xcs.encode(), 0L);
		assertEquals(L.xcs.encodeInt(), 0);
		assertEquals(L.xcs.encode(L.a, L.e, L.f, L.g), 0x80000000_80008000L);
		assertEquals(L.xcs.encodeInt(L.a, L.e, L.f, L.g), 0x80008000);
	}

	@Test
	public void testDecode() {
		assertEquals(N.xc.decode(0), null);
		assertEquals(N.xcs.decode(0), null);
		assertEquals(I.xc.decode(0), I.a);
		assertEquals(I.xc.decode(3), I.c);
		assertEquals(I.xc.decode(5), null);
		assertEquals(I.xc.decode(0x80000000L), I.f);
		assertEquals(I.xcs.decode(0), I.a);
		assertEquals(I.xcs.decode(3), I.c);
		assertEquals(I.xcs.decode(5), null);
		assertEquals(I.xcs.decode(0x80000000L), I.f);
		assertEquals(L.xc.decode(0), L.a);
		assertEquals(L.xc.decode(3), L.c);
		assertEquals(L.xc.decode(5), null);
		assertEquals(L.xc.decode(0x80000000L), L.f);
		assertEquals(L.xc.decode(0x80000000_00000000L), L.g);
		assertEquals(L.xcs.decode(0), L.a);
		assertEquals(L.xcs.decode(3), L.c);
		assertEquals(L.xcs.decode(5), null);
		assertEquals(L.xcs.decode(0x80000000L), L.f);
		assertEquals(L.xcs.decode(0x80000000_00000000L), L.g);
	}

	@Test
	public void testDecodeDef() {
		assertEquals(N.xc.decode(0, null), null);
		assertEquals(N.xcs.decode(0, null), null);
		assertEquals(I.xc.decode(2, null), null);
		assertEquals(I.xc.decode(3, I.e), I.c);
		assertEquals(I.xc.decode(5, I.e), I.e);
		assertEquals(I.xcs.decode(2, null), null);
		assertEquals(I.xcs.decode(3, I.e), I.c);
		assertEquals(I.xcs.decode(5, I.e), I.e);
	}

	@Test
	public void testDecodeValid() {
		assertIllegalArg(() -> N.xc.decodeValid(0));
		assertEquals(I.xc.decodeValid(3), I.c);
		assertEquals(I.xc.decodeValid(3, "I"), I.c);
		assertIllegalArg(() -> I.xc.decodeValid(2));
		assertIllegalArg(() -> I.xc.decodeValid(5, "I"));
	}

	@Test
	public void testDecodeRem() {
		assertRem(N.xc.decodeRem(0), 0);
		assertRem(N.xc.decodeRem(1), 1);
		assertRem(I.xc.decodeRem(0), 0, I.a);
		assertRem(I.xc.decodeRem(2), 2, I.a);
		assertRem(I.xc.decodeRem(3), 0, I.c);
		assertRem(I.xc.decodeRem(5), 4, I.b);
		assertEquals(I.xc.decodeRem(15).first(), I.b);
		assertString(I.xc.decodeRem(15), "[b]+14|0xe");
	}

	@Test
	public void testIsValid() {
		assertEquals(N.xc.isValid(0), true);
		assertEquals(N.xc.isValid(1), false);
		assertEquals(I.xc.isValid(0), true);
		assertEquals(I.xc.isValid(1), true);
		assertEquals(I.xc.isValid(2), false);
	}

	@Test
	public void testIs() {
		assertEquals(N.xc.is(0, null), true);
		assertEquals(I.xc.is(0, I.a), true);
		assertEquals(I.xc.is(2, null), true);
		assertEquals(I.xc.is(3, I.b), false);
		assertEquals(I.xc.is(3, I.c), true);
		assertEquals(I.xc.is(3, I.d), false);
	}

	@Test
	public void testIsAny() {
		assertEquals(N.xc.isAny(0, nullNs), false);
		assertEquals(N.xc.isAny(0), false);
		assertEquals(I.xc.isAny(0, nullIs), false);
		assertEquals(I.xc.isAny(0), false);
		assertEquals(I.xc.isAny(0, I.b, I.c), false);
		assertEquals(I.xc.isAny(0, I.b, I.a), true);
		assertEquals(I.xc.isAny(3, I.a, I.b), false);
		assertEquals(I.xc.isAny(3, I.a, I.b, I.c), true);
		assertEquals(I.xc.isAny(3, I.b, I.d), false);
		assertEquals(I.xc.isAny(3, Set.of(I.b, I.c)), true);
	}

	@Test
	public void testHas() {
		assertEquals(N.xc.has(0, null), false);
		assertEquals(I.xc.has(0, I.a), true);
		assertEquals(I.xc.has(2, null), false);
		assertEquals(I.xc.has(3, I.b), true);
		assertEquals(I.xc.has(3, I.c), true);
		assertEquals(I.xc.has(3, I.d), false);
	}

	@Test
	public void testHasAny() {
		assertEquals(N.xc.hasAny(0, nullNs), false);
		assertEquals(N.xc.hasAny(0), false);
		assertEquals(I.xc.hasAny(0, nullIs), false);
		assertEquals(I.xc.hasAny(0), false);
		assertEquals(I.xc.hasAny(0, I.b, I.c), false);
		assertEquals(I.xc.hasAny(0, I.b, I.a), true);
		assertEquals(I.xc.hasAny(3, I.a, I.b), true);
		assertEquals(I.xc.hasAny(3, I.a, I.b, I.c), true);
		assertEquals(I.xc.hasAny(3, I.b, I.d), true);
		assertEquals(I.xc.hasAny(3, Set.of(I.b, I.d)), true);
	}

	@Test
	public void testAdd() {
		assertEquals(N.xcs.add(1, nullNs), 1);
		assertEquals(N.xcs.add(1, Set.of()), 1);
		assertEquals(I.xcs.add(1, nullIs), 1);
		assertEquals(I.xcs.add(1, Set.of()), 1);
		assertEquals(I.xcs.add(1), 1);
		assertEquals(I.xcs.add(1L), 1L);
		assertEquals(I.xcs.add(1, I.c, I.f), 0x80000003);
		assertEquals(I.xcs.add(1L, I.c, I.f), 0x80000003L);
		assertEquals(I.xcs.add(9, Set.of(I.c, I.f)), 0x8000000b);
		assertEquals(I.xcs.add(9L, Set.of(I.c, I.f)), 0x8000000bL);
		assertEquals(L.xcs.add(9, Set.of(L.c, L.g)), 0xb);
		assertEquals(L.xcs.add(9L, Set.of(L.c, L.g)), 0x80000000_0000000bL);
	}

	@Test
	public void testRemove() {
		assertEquals(N.xcs.remove(1, nullNs), 1);
		assertEquals(N.xcs.remove(1, Set.of()), 1);
		assertEquals(I.xcs.remove(1, nullIs), 1);
		assertEquals(I.xcs.remove(1, Set.of()), 1);
		assertEquals(I.xcs.remove(1), 1);
		assertEquals(I.xcs.remove(1L), 1L);
		assertEquals(I.xcs.remove(9, I.c, I.f), 8);
		assertEquals(I.xcs.remove(9L, I.c, I.f), 8L);
		assertEquals(I.xcs.remove(-1, I.c, I.d, I.e, I.f), 0x7fff7f7c);
		assertEquals(I.xcs.remove(-1L, I.c, I.d, I.e, I.f), 0xffffffff_7fff7f7cL);
		assertEquals(L.xcs.remove(9, L.c, L.g), 8);
		assertEquals(L.xcs.remove(9L, L.c, L.g), 8L);
		assertEquals(L.xcs.remove(-1, L.c, L.d, L.e, L.f, L.g), 0x7fff7f7c);
		assertEquals(L.xcs.remove(-1L, L.c, L.d, L.e, L.f, L.g), 0x7fffffff_7fff7f7cL);
	}

	@Test
	public void testDecodeAll() {
		assertOrdered(N.xcs.decodeAll(0));
		assertOrdered(N.xcs.decodeAll(1));
		assertOrdered(I.xcs.decodeAll(0), I.a);
		assertOrdered(I.xcs.decodeAll(1), I.b);
		assertOrdered(I.xcs.decodeAll(-1), I.b, I.d, I.e, I.f);
		assertOrdered(L.xcs.decodeAll(0), L.a);
		assertOrdered(L.xcs.decodeAll(1), L.b);
		assertOrdered(L.xcs.decodeAll(-1), L.b, L.d, L.e, L.f);
		assertOrdered(L.xcs.decodeAll(-1L), L.b, L.d, L.e, L.f, L.g);
	}

	@Test
	public void testsDecodeAllValid() {
		assertOrdered(N.xcs.decodeAllValid(0));
		assertIllegalArg(() -> N.xcs.decodeAllValid(1));
		assertOrdered(I.xcs.decodeAllValid(0x80000001L), I.b, I.f);
		assertOrdered(I.xcs.decodeAllValid(0x80000001, "I"), I.b, I.f);
		assertOrdered(I.xcs.decodeAllValid(0x80000001L, "I"), I.b, I.f);
		assertIllegalArg(() -> I.xcs.decodeAllValid(0x80000003));
		assertIllegalArg(() -> I.xcs.decodeAllValid(0x80000003L));
		assertIllegalArg(() -> I.xcs.decodeAllValid(0x80000003L, "I"));
	}

	@Test
	public void testDecodeAllRem() {
		assertRem(N.xcs.decodeAllRem(0), 0);
		assertRem(N.xcs.decodeAllRem(1), 1);
		assertRem(I.xcs.decodeAllRem(0x80008005), 4, I.b, I.e, I.f);
		assertRem(I.xcs.decodeAllRem((long) 0x80008005), 0xffffffff_00000004L, I.b, I.e, I.f);
		assertRem(I.xcs.decodeAllRem(0x80008005L), 4, I.b, I.e, I.f);
	}

	@Test
	public void testsIsValid() {
		assertEquals(N.xcs.isValid(0), true);
		assertEquals(N.xcs.isValid(1), false);
		assertEquals(I.xcs.isValid(0), true);
		assertEquals(I.xcs.isValid(1), true);
		assertEquals(I.xcs.isValid(2), false);
		assertEquals(I.xcs.isValid(3), true);
		assertEquals(I.xcs.isValid(0x80000001), true);
		assertEquals(I.xcs.isValid((long) 0x80000001), false);
		assertEquals(I.xcs.isValid(0x80000001L), true);
	}

	@Test
	public void testsIsAll() {
		assertEquals(N.xcs.isAll(0, nullNs), true);
		assertEquals(N.xcs.isAll(0, nullNSet), true);
		assertEquals(N.xcs.isAll(0), true);
		assertEquals(N.xcs.isAll(1), false);
		assertEquals(I.xcs.isAll(0, nullIs), true);
		assertEquals(I.xcs.isAll(0, nullISet), true);
		assertEquals(I.xcs.isAll(0), true);
		assertEquals(I.xcs.isAll(0, I.b, I.c), false);
		assertEquals(I.xcs.isAll(0, Set.of(I.a)), true);
		assertEquals(I.xcs.isAll(3, I.a, I.b), false);
		assertEquals(I.xcs.isAll(3, I.a, I.b, I.c), true);
		assertEquals(I.xcs.isAll(3, I.b, I.d), false);
		assertEquals(I.xcs.isAll(3, Set.of(I.b, I.d)), false);
	}

	@Test
	public void testsHasAll() {
		assertEquals(N.xcs.hasAll(0, nullNs), true);
		assertEquals(N.xcs.hasAll(0, nullNSet), true);
		assertEquals(N.xcs.hasAll(0), true);
		assertEquals(N.xcs.hasAll(1), true);
		assertEquals(I.xcs.hasAll(0, nullIs), true);
		assertEquals(I.xcs.hasAll(0, nullISet), true);
		assertEquals(I.xcs.hasAll(0), true);
		assertEquals(I.xcs.hasAll(0, I.b, I.c), false);
		assertEquals(I.xcs.hasAll(0, Set.of(I.a)), true);
		assertEquals(I.xcs.hasAll(3, I.a, I.b), true);
		assertEquals(I.xcs.hasAll(3, I.a, I.b, I.c), true);
		assertEquals(I.xcs.hasAll(3, I.b, I.d), false);
		assertEquals(I.xcs.hasAll(3, Set.of(I.b, I.d)), false);
	}

	@SafeVarargs
	private static <T> void assertRem(Xcoder.Rem<T> rem, long diff, T... ts) {
		assertOrdered(rem.types(), ts);
		assertEquals(rem.first(), ArrayUtil.at(ts, 0));
		assertEquals(rem.diff(), diff);
		assertEquals(rem.diffInt(), (int) diff);
		assertEquals(rem.isExact(), diff == 0L);
		assertEquals(rem.isEmpty(), ArrayUtil.isEmpty(ts) && diff == 0L);
	}
}
