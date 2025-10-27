package ceri.common.data;

import java.util.Set;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.collect.Enums;
import ceri.common.test.Assert;

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
		Assert.ordered(N.xc.all());
		Assert.ordered(N.xcs.all());
		Assert.ordered(I.xc.all(), Enums.of(I.class));
		Assert.ordered(I.xcs.all(), Enums.of(I.class));
		Assert.ordered(L.xc.all(), Enums.of(L.class));
		Assert.ordered(L.xcs.all(), Enums.of(L.class));
	}

	@Test
	public void testMask() {
		Assert.equal(N.xc.mask(), 0L);
		Assert.equal(N.xc.maskInt(), 0);
		Assert.equal(N.xcs.mask(), 0L);
		Assert.equal(N.xcs.maskInt(), 0);
		Assert.equal(I.xc.mask(), 0x80008083L);
		Assert.equal(I.xc.maskInt(), 0x80008083);
		Assert.equal(I.xcs.mask(), 0x80008083L);
		Assert.equal(I.xcs.maskInt(), 0x80008083);
		Assert.equal(L.xc.mask(), 0x80000000_80008083L);
		Assert.equal(L.xc.maskInt(), 0x80008083);
		Assert.equal(L.xcs.mask(), 0x80000000_80008083L);
		Assert.equal(L.xcs.maskInt(), 0x80008083);
	}

	@Test
	public void testEncode() {
		Assert.equal(N.xc.encode(null), 0L);
		Assert.equal(N.xc.encodeInt(null), 0);
		Assert.equal(N.xcs.encode(nullNs), 0L);
		Assert.equal(N.xcs.encodeInt(nullNs), 0);
		Assert.equal(N.xcs.encode(), 0L);
		Assert.equal(N.xcs.encodeInt(), 0);
		Assert.equal(I.xc.encode(null), 0L);
		Assert.equal(I.xc.encodeInt(null), 0);
		Assert.equal(I.xc.encode(I.f), 0x80000000L);
		Assert.equal(I.xc.encodeInt(I.f), 0x80000000);
		Assert.equal(I.xcs.encode(nullIs), 0L);
		Assert.equal(I.xcs.encodeInt(nullIs), 0);
		Assert.equal(I.xcs.encode(nullISet), 0L);
		Assert.equal(I.xcs.encodeInt(nullISet), 0);
		Assert.equal(I.xcs.encode(), 0L);
		Assert.equal(I.xcs.encodeInt(), 0);
		Assert.equal(I.xcs.encode(I.a, I.e, I.f), 0x80008000L);
		Assert.equal(I.xcs.encodeInt(I.a, I.e, I.f), 0x80008000);
		Assert.equal(L.xc.encode(null), 0L);
		Assert.equal(L.xc.encodeInt(null), 0);
		Assert.equal(L.xc.encode(L.f), 0x80000000L);
		Assert.equal(L.xc.encodeInt(L.f), 0x80000000);
		Assert.equal(L.xcs.encode(nullLs), 0L);
		Assert.equal(L.xcs.encodeInt(nullLs), 0);
		Assert.equal(L.xcs.encode(nullLSet), 0L);
		Assert.equal(L.xcs.encodeInt(nullLSet), 0);
		Assert.equal(L.xcs.encode(), 0L);
		Assert.equal(L.xcs.encodeInt(), 0);
		Assert.equal(L.xcs.encode(L.a, L.e, L.f, L.g), 0x80000000_80008000L);
		Assert.equal(L.xcs.encodeInt(L.a, L.e, L.f, L.g), 0x80008000);
	}

	@Test
	public void testDecode() {
		Assert.equal(N.xc.decode(0), null);
		Assert.equal(N.xcs.decode(0), null);
		Assert.equal(I.xc.decode(0), I.a);
		Assert.equal(I.xc.decode(3), I.c);
		Assert.equal(I.xc.decode(5), null);
		Assert.equal(I.xc.decode(0x80000000L), I.f);
		Assert.equal(I.xcs.decode(0), I.a);
		Assert.equal(I.xcs.decode(3), I.c);
		Assert.equal(I.xcs.decode(5), null);
		Assert.equal(I.xcs.decode(0x80000000L), I.f);
		Assert.equal(L.xc.decode(0), L.a);
		Assert.equal(L.xc.decode(3), L.c);
		Assert.equal(L.xc.decode(5), null);
		Assert.equal(L.xc.decode(0x80000000L), L.f);
		Assert.equal(L.xc.decode(0x80000000_00000000L), L.g);
		Assert.equal(L.xcs.decode(0), L.a);
		Assert.equal(L.xcs.decode(3), L.c);
		Assert.equal(L.xcs.decode(5), null);
		Assert.equal(L.xcs.decode(0x80000000L), L.f);
		Assert.equal(L.xcs.decode(0x80000000_00000000L), L.g);
	}

	@Test
	public void testDecodeDef() {
		Assert.equal(N.xc.decode(0, null), null);
		Assert.equal(N.xcs.decode(0, null), null);
		Assert.equal(I.xc.decode(2, null), null);
		Assert.equal(I.xc.decode(3, I.e), I.c);
		Assert.equal(I.xc.decode(5, I.e), I.e);
		Assert.equal(I.xcs.decode(2, null), null);
		Assert.equal(I.xcs.decode(3, I.e), I.c);
		Assert.equal(I.xcs.decode(5, I.e), I.e);
	}

	@Test
	public void testDecodeValid() {
		Assert.illegalArg(() -> N.xc.decodeValid(0));
		Assert.equal(I.xc.decodeValid(3), I.c);
		Assert.equal(I.xc.decodeValid(3, "I"), I.c);
		Assert.illegalArg(() -> I.xc.decodeValid(2));
		Assert.illegalArg(() -> I.xc.decodeValid(5, "I"));
	}

	@Test
	public void testDecodeRem() {
		assertRem(N.xc.decodeRem(0), 0);
		assertRem(N.xc.decodeRem(1), 1);
		assertRem(I.xc.decodeRem(0), 0, I.a);
		assertRem(I.xc.decodeRem(2), 2, I.a);
		assertRem(I.xc.decodeRem(3), 0, I.c);
		assertRem(I.xc.decodeRem(5), 4, I.b);
		Assert.equal(I.xc.decodeRem(15).first(), I.b);
		Assert.string(I.xc.decodeRem(15), "[b]+14|0xe");
	}

	@Test
	public void testIsValid() {
		Assert.equal(N.xc.isValid(0), true);
		Assert.equal(N.xc.isValid(1), false);
		Assert.equal(I.xc.isValid(0), true);
		Assert.equal(I.xc.isValid(1), true);
		Assert.equal(I.xc.isValid(2), false);
	}

	@Test
	public void testIs() {
		Assert.equal(N.xc.is(0, null), true);
		Assert.equal(I.xc.is(0, I.a), true);
		Assert.equal(I.xc.is(2, null), true);
		Assert.equal(I.xc.is(3, I.b), false);
		Assert.equal(I.xc.is(3, I.c), true);
		Assert.equal(I.xc.is(3, I.d), false);
	}

	@Test
	public void testIsAny() {
		Assert.equal(N.xc.isAny(0, nullNs), false);
		Assert.equal(N.xc.isAny(0), false);
		Assert.equal(I.xc.isAny(0, nullIs), false);
		Assert.equal(I.xc.isAny(0), false);
		Assert.equal(I.xc.isAny(0, I.b, I.c), false);
		Assert.equal(I.xc.isAny(0, I.b, I.a), true);
		Assert.equal(I.xc.isAny(3, I.a, I.b), false);
		Assert.equal(I.xc.isAny(3, I.a, I.b, I.c), true);
		Assert.equal(I.xc.isAny(3, I.b, I.d), false);
		Assert.equal(I.xc.isAny(3, Set.of(I.b, I.c)), true);
	}

	@Test
	public void testHas() {
		Assert.equal(N.xc.has(0, null), false);
		Assert.equal(I.xc.has(0, I.a), true);
		Assert.equal(I.xc.has(2, null), false);
		Assert.equal(I.xc.has(3, I.b), true);
		Assert.equal(I.xc.has(3, I.c), true);
		Assert.equal(I.xc.has(3, I.d), false);
	}

	@Test
	public void testHasAny() {
		Assert.equal(N.xc.hasAny(0, nullNs), false);
		Assert.equal(N.xc.hasAny(0), false);
		Assert.equal(I.xc.hasAny(0, nullIs), false);
		Assert.equal(I.xc.hasAny(0), false);
		Assert.equal(I.xc.hasAny(0, I.b, I.c), false);
		Assert.equal(I.xc.hasAny(0, I.b, I.a), true);
		Assert.equal(I.xc.hasAny(3, I.a, I.b), true);
		Assert.equal(I.xc.hasAny(3, I.a, I.b, I.c), true);
		Assert.equal(I.xc.hasAny(3, I.b, I.d), true);
		Assert.equal(I.xc.hasAny(3, Set.of(I.b, I.d)), true);
	}

	@Test
	public void testAdd() {
		Assert.equal(N.xcs.add(1, nullNs), 1);
		Assert.equal(N.xcs.add(1, Set.of()), 1);
		Assert.equal(I.xcs.add(1, nullIs), 1);
		Assert.equal(I.xcs.add(1, Set.of()), 1);
		Assert.equal(I.xcs.add(1), 1);
		Assert.equal(I.xcs.add(1L), 1L);
		Assert.equal(I.xcs.add(1, I.c, I.f), 0x80000003);
		Assert.equal(I.xcs.add(1L, I.c, I.f), 0x80000003L);
		Assert.equal(I.xcs.add(9, Set.of(I.c, I.f)), 0x8000000b);
		Assert.equal(I.xcs.add(9L, Set.of(I.c, I.f)), 0x8000000bL);
		Assert.equal(L.xcs.add(9, Set.of(L.c, L.g)), 0xb);
		Assert.equal(L.xcs.add(9L, Set.of(L.c, L.g)), 0x80000000_0000000bL);
	}

	@Test
	public void testRemove() {
		Assert.equal(N.xcs.remove(1, nullNs), 1);
		Assert.equal(N.xcs.remove(1, Set.of()), 1);
		Assert.equal(I.xcs.remove(1, nullIs), 1);
		Assert.equal(I.xcs.remove(1, Set.of()), 1);
		Assert.equal(I.xcs.remove(1), 1);
		Assert.equal(I.xcs.remove(1L), 1L);
		Assert.equal(I.xcs.remove(9, I.c, I.f), 8);
		Assert.equal(I.xcs.remove(9L, I.c, I.f), 8L);
		Assert.equal(I.xcs.remove(-1, I.c, I.d, I.e, I.f), 0x7fff7f7c);
		Assert.equal(I.xcs.remove(-1L, I.c, I.d, I.e, I.f), 0xffffffff_7fff7f7cL);
		Assert.equal(L.xcs.remove(9, L.c, L.g), 8);
		Assert.equal(L.xcs.remove(9L, L.c, L.g), 8L);
		Assert.equal(L.xcs.remove(-1, L.c, L.d, L.e, L.f, L.g), 0x7fff7f7c);
		Assert.equal(L.xcs.remove(-1L, L.c, L.d, L.e, L.f, L.g), 0x7fffffff_7fff7f7cL);
	}

	@Test
	public void testDecodeAll() {
		Assert.ordered(N.xcs.decodeAll(0));
		Assert.ordered(N.xcs.decodeAll(1));
		Assert.ordered(I.xcs.decodeAll(0), I.a);
		Assert.ordered(I.xcs.decodeAll(1), I.b);
		Assert.ordered(I.xcs.decodeAll(-1), I.b, I.d, I.e, I.f);
		Assert.ordered(L.xcs.decodeAll(0), L.a);
		Assert.ordered(L.xcs.decodeAll(1), L.b);
		Assert.ordered(L.xcs.decodeAll(-1), L.b, L.d, L.e, L.f);
		Assert.ordered(L.xcs.decodeAll(-1L), L.b, L.d, L.e, L.f, L.g);
	}

	@Test
	public void testsDecodeAllValid() {
		Assert.ordered(N.xcs.decodeAllValid(0));
		Assert.illegalArg(() -> N.xcs.decodeAllValid(1));
		Assert.ordered(I.xcs.decodeAllValid(0x80000001L), I.b, I.f);
		Assert.ordered(I.xcs.decodeAllValid(0x80000001, "I"), I.b, I.f);
		Assert.ordered(I.xcs.decodeAllValid(0x80000001L, "I"), I.b, I.f);
		Assert.illegalArg(() -> I.xcs.decodeAllValid(0x80000003));
		Assert.illegalArg(() -> I.xcs.decodeAllValid(0x80000003L));
		Assert.illegalArg(() -> I.xcs.decodeAllValid(0x80000003L, "I"));
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
		Assert.equal(N.xcs.isValid(0), true);
		Assert.equal(N.xcs.isValid(1), false);
		Assert.equal(I.xcs.isValid(0), true);
		Assert.equal(I.xcs.isValid(1), true);
		Assert.equal(I.xcs.isValid(2), false);
		Assert.equal(I.xcs.isValid(3), true);
		Assert.equal(I.xcs.isValid(0x80000001), true);
		Assert.equal(I.xcs.isValid((long) 0x80000001), false);
		Assert.equal(I.xcs.isValid(0x80000001L), true);
	}

	@Test
	public void testsIsAll() {
		Assert.equal(N.xcs.isAll(0, nullNs), true);
		Assert.equal(N.xcs.isAll(0, nullNSet), true);
		Assert.equal(N.xcs.isAll(0), true);
		Assert.equal(N.xcs.isAll(1), false);
		Assert.equal(I.xcs.isAll(0, nullIs), true);
		Assert.equal(I.xcs.isAll(0, nullISet), true);
		Assert.equal(I.xcs.isAll(0), true);
		Assert.equal(I.xcs.isAll(0, I.b, I.c), false);
		Assert.equal(I.xcs.isAll(0, Set.of(I.a)), true);
		Assert.equal(I.xcs.isAll(3, I.a, I.b), false);
		Assert.equal(I.xcs.isAll(3, I.a, I.b, I.c), true);
		Assert.equal(I.xcs.isAll(3, I.b, I.d), false);
		Assert.equal(I.xcs.isAll(3, Set.of(I.b, I.d)), false);
	}

	@Test
	public void testsHasAll() {
		Assert.equal(N.xcs.hasAll(0, nullNs), true);
		Assert.equal(N.xcs.hasAll(0, nullNSet), true);
		Assert.equal(N.xcs.hasAll(0), true);
		Assert.equal(N.xcs.hasAll(1), true);
		Assert.equal(I.xcs.hasAll(0, nullIs), true);
		Assert.equal(I.xcs.hasAll(0, nullISet), true);
		Assert.equal(I.xcs.hasAll(0), true);
		Assert.equal(I.xcs.hasAll(0, I.b, I.c), false);
		Assert.equal(I.xcs.hasAll(0, Set.of(I.a)), true);
		Assert.equal(I.xcs.hasAll(3, I.a, I.b), true);
		Assert.equal(I.xcs.hasAll(3, I.a, I.b, I.c), true);
		Assert.equal(I.xcs.hasAll(3, I.b, I.d), false);
		Assert.equal(I.xcs.hasAll(3, Set.of(I.b, I.d)), false);
	}

	@SafeVarargs
	private static <T> void assertRem(Xcoder.Rem<T> rem, long diff, T... ts) {
		Assert.ordered(rem.types(), ts);
		Assert.equal(rem.first(), ArrayUtil.at(ts, 0));
		Assert.equal(rem.diff(), diff);
		Assert.equal(rem.diffInt(), (int) diff);
		Assert.equal(rem.isExact(), diff == 0L);
		Assert.equal(rem.isEmpty(), ArrayUtil.isEmpty(ts) && diff == 0L);
	}
}
