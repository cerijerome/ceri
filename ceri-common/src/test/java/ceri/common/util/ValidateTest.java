package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIndexOob;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnsupported;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.collection.Maps;
import ceri.common.collection.Sets;
import ceri.common.math.Bound;
import ceri.common.math.Interval;
import ceri.common.test.TestUtil;
import ceri.common.text.Formats;

public class ValidateTest {
	private static final Object OBJ = new Object();
	private static final Integer I0 = 999;
	private static final Integer I1 = 999;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Validate.class);
	}

	@Test
	public void testValidatePredicate() {
		Validate.validate(i -> i < 0, -1);
		assertThrown(() -> Validate.validate(i -> i < 0, 0));
		Validate.validate(i -> i < 0, -1, "int");
		assertThrown(() -> Validate.validate(i -> i < 0, 0, "int"));
	}

	@Test
	public void testValidateExpression() {
		Validate.validate(1 > 0);
		Validate.validate(1 > 0, null);
		Validate.validate(1 > 0, "test");
		assertThrown(() -> Validate.validate(1 < 0));
		assertThrown(() -> Validate.validate(1 < 0, null));
		assertThrown(() -> Validate.validate(1 < 0, "test"));
	}

	@Test
	public void testValidateWithFormattedException() {
		int i0 = -1, i1 = 1;
		Validate.validatef(i0 < 0, "%d >= 0", i0);
		assertThrown(() -> Validate.validatef(i1 < 0, "%d >= 0", i1));
	}

	@Test
	public void testValidateLookup() {
		Map<String, Integer> map = Map.of("one", 1, "two", 2, "three", 3);
		assertEquals(Validate.validateLookup(map::get, "two"), 2);
		assertThrown(() -> Validate.validateLookup(map::get, "TWO"));
	}

	@Test
	public void testValidateIntLookup() {
		Map<Integer, String> map = Map.of(1, "one", 2, "two", 3, "three");
		assertEquals(Validate.validateIntLookup(map::get, 2), "two");
		assertThrown(() -> Validate.validateIntLookup(map::get, 0));
	}

	@Test
	public void testValidateLongLookup() {
		Map<Long, String> map = Map.of(1L, "one", 2L, "two", 3L, "three");
		assertEquals(Validate.validateLongLookup(map::get, 2L), "two");
		assertThrown(() -> Validate.validateLongLookup(map::get, 0L));
	}

	@Test
	public void testValidateLookupEquals() {
		Map<String, Integer> map = Map.of("one", 1, "two", 2, "three", 3);
		assertEquals(Validate.validateLookupEquals(map::get, "two", 2), 2);
		assertThrown(() -> Validate.validateLookupEquals(map::get, "two", 3));
	}

	@Test
	public void testValidateIntLookupEquals() {
		Map<Integer, String> map = Map.of(1, "one", 2, "two", 3, "three");
		assertEquals(Validate.validateIntLookupEquals(map::get, 2, "two"), "two");
		assertThrown(() -> Validate.validateIntLookupEquals(map::get, 2, "three"));
	}

	@Test
	public void testValidateSupported() {
		Validate.validateSupported(OBJ, "test");
		assertUnsupported(() -> Validate.validateSupported(null, "test"));
	}

	@Test
	public void testValidateNotNull() {
		Validate.validateNotNull(OBJ);
		Validate.validateNotNull(OBJ, "test");
		assertThrown(() -> Validate.validateNotNull(null));
		assertThrown(() -> Validate.validateNotNull(null, "test"));
	}

	@Test
	public void testValidateAllNotNull() {
		Validate.validateAllNotNull(OBJ, "a", 1);
		assertThrown(() -> Validate.validateAllNotNull(OBJ, null));
		assertThrown(() -> Validate.validateAllNotNull((Object[]) null));
	}

	@Test
	public void testValidateNull() {
		Validate.validateNull(null);
		assertThrown(() -> Validate.validateNull(""));
	}

	@Test
	public void testValidateObjectEquality() {
		Validate.validateEqualObj(OBJ, OBJ);
		Validate.validateEqualObj(null, null);
		Validate.validateEqualObj(I0, I1);
		assertThrown(() -> Validate.validateEqualObj(null, OBJ));
		assertThrown(() -> Validate.validateEqualObj(OBJ, null));
		assertThrown(() -> Validate.validateEqualObj(OBJ, I0));
	}

	@Test
	public void testValidateObjectInequality() {
		Validate.validateNotEqualObj(null, OBJ);
		Validate.validateNotEqualObj(OBJ, null);
		Validate.validateNotEqualObj(I0, OBJ);
		assertThrown(() -> Validate.validateNotEqualObj(null, null));
		assertThrown(() -> Validate.validateNotEqualObj(OBJ, OBJ));
	}

	@Test
	public void testValidateLongEquality() {
		Validate.validateEqual(Long.MIN_VALUE, Long.MIN_VALUE, Formats.HEX);
		assertThrown(() -> Validate.validateEqual(Long.MIN_VALUE, Long.MAX_VALUE, Formats.HEX));
	}

	@Test
	public void testValidateDoubleEquality() {
		Validate.validateEqualFp(Double.MIN_VALUE, Double.MIN_VALUE, Formats.FP1);
		assertThrown(
			() -> Validate.validateEqualFp(Double.MAX_VALUE, Double.MIN_VALUE, Formats.FP));
	}

	@Test
	public void testValidateLongEqualityFormat() {
		assertEquals(TestUtil.thrown(
			() -> Validate.validateEqual(-1, 0xff, (String) null, Formats.DEC, Formats.HEX_SHORT))
			.getMessage(), "Value != (255, 0x00ff): (-1, 0xffff)");
	}

	@Test
	public void testValidateDoubleEqualityFormat() {
		assertEquals(TestUtil.thrown(() -> Validate.validateEqualFp(1.0, 1.111, "Num", Formats.FP1))
			.getMessage(), "Num != 1.1: 1.0");
	}

	@Test
	public void testValidateLongInequality() {
		Validate.validateNotEqual(Long.MIN_VALUE, Long.MAX_VALUE, Formats.UDEC);
		assertThrown(
			() -> Validate.validateNotEqual(Long.MIN_VALUE, Long.MIN_VALUE, Formats.HEX_SHORT));
	}

	@Test
	public void testValidateDoubleInequality() {
		Validate.validateNotEqualFp(Double.MIN_VALUE, Double.MAX_VALUE, Formats.FP);
		assertThrown(() -> Validate.validateNotEqualFp(1.00001, 1.00001, Formats.FP1));
	}

	@Test
	public void testValidateUbyte() {
		Validate.validateUbyte(0xff, -1);
		Validate.validateUbyte(0xffff, -1);
		assertThrown(() -> Validate.validateUbyte(0xf, -1, "test"));
		Validate.validateUbyte(0);
		Validate.validateUbyte(0xff);
		assertThrown(() -> Validate.validateUbyte(-1));
		assertThrown(() -> Validate.validateUbyte(0x100));
		assertEquals(
			TestUtil.thrown(() -> Validate.validateUbyte(-1, "Byte", Formats.DEC)).getMessage(),
			"Byte is not within [0, 255]: -1");
	}

	@Test
	public void testValidateUshort() {
		Validate.validateUshort(0xffff, -1);
		Validate.validateUshort(0xffffff, -1);
		assertThrown(() -> Validate.validateUshort(0xff, -1, "test"));
		Validate.validateUshort(0);
		Validate.validateUshort(0xffff);
		assertThrown(() -> Validate.validateUshort(-1));
		assertThrown(() -> Validate.validateUshort(0x10000));
	}

	@Test
	public void testValidateUint() {
		Validate.validateUint(0xffffffff, -1);
		Validate.validateUint(0xffffffffffL, -1);
		assertThrown(() -> Validate.validateUint(0xffff, -1, "test"));
		Validate.validateUint(0);
		Validate.validateUint(0xffffffffL);
		assertThrown(() -> Validate.validateUint(-1));
		assertThrown(() -> Validate.validateUint(0x100000000L));
	}

	@Test
	public void testValidateUlong() {
		Validate.validateUlong(0xffffffff12345678L, 0xffffffff12345678L);
		Validate.validateUlong(0xffffffffffffffffL, -1L);
		Validate.validateUlong(Long.MIN_VALUE, Long.MIN_VALUE, "test");
		assertThrown(() -> Validate.validateUlong(-1L, -2L, "test"));
		assertThrown(() -> Validate.validateUlong( //
			0xffffffff12345678L, 0xffffffff12345670L));
	}

	@Test
	public void testValidateWithinObj() {
		Validate.validateWithinObj(1, Interval.inclusive(0, 1));
		Validate.validateWithinObj(0.5, Interval.exclusive(0.0, 1.0));
		assertThrown(() -> Validate.validateWithinObj(1, Interval.exclusive(0, 1)));
		assertThrown(() -> Validate.validateWithinObj(0, Interval.exclusive(0, 1)));
		assertThrown(() -> Validate.validateWithinObj(1d, Interval.exclusive(0d, 1d)));
		assertThrown(() -> Validate.validateWithinObj(0d, Interval.exclusive(0d, 1d)));
	}

	@Test
	public void testValidateWithoutObj() {
		Validate.validateWithoutObj(1, Interval.exclusive(0, 1));
		assertThrown(() -> Validate.validateWithoutObj(1, Interval.inclusive(0, 1)));
	}

	@Test
	public void testValidateWithinLong() {
		assertThrown(() -> Validate.validateWithin(1, Interval.exclusive(0L, 1L)));
		Validate.validateWithin(1, Interval.inclusive(0L, 1L), Formats.HEX);
		assertThrown(() -> Validate.validateWithin(1, Interval.exclusive(0L, 1L), Formats.HEX));
	}

	@Test
	public void testValidateWithinDouble() {
		Validate.validateWithinFp(1, Interval.inclusive(0.0, 1.0), Formats.ROUND);
		assertThrown(
			() -> Validate.validateWithinFp(1, Interval.exclusive(0.0, 1.0), Formats.ROUND));
	}

	@Test
	public void testValidateWithoutLong() {
		Validate.validateWithout(1, Interval.exclusive(0L, 1L), Formats.HEX);
		assertThrown(() -> Validate.validateWithout(1, Interval.inclusive(0L, 1L), Formats.HEX));
	}

	@Test
	public void testValidateWithoutDouble() {
		Validate.validateWithoutFp(1, Interval.exclusive(0.0, 1.0), Formats.ROUND);
		assertThrown(
			() -> Validate.validateWithoutFp(1, Interval.inclusive(0.0, 1.0), Formats.ROUND));
	}

	@Test
	public void testValidateIndex() {
		int[] array = { 1, 2, 3, 4 };
		Validate.validateIndex(array.length, 0);
		Validate.validateIndex(array.length, 3);
		assertThrown(() -> Validate.validateIndex(array.length, -1));
		assertThrown(() -> Validate.validateIndex(array.length, 4));
	}

	@Test
	public void testValidateArraySlice() {
		Validate.validateSlice((boolean[]) null, 0, 0);
		Validate.validateSlice("ab".toCharArray(), 1, 1);
		Validate.validateSlice(ArrayUtil.bytes.of(-1, 0, 1), 0, 3);
		Validate.validateSlice(ArrayUtil.shorts.of(-1, 0, 1), 0, 2);
		Validate.validateSlice(ArrayUtil.ints.of(-1, 0, 1), 1, 0);
		Validate.validateSlice(ArrayUtil.longs.of(-1, 0, 1), 0, 0);
		Validate.validateSlice(ArrayUtil.floats.of(-1, 0, 1), 2, 1);
		Validate.validateSlice(ArrayUtil.doubles.of(-1, 0, 1), 3, 0);
		assertThrown(() -> Validate.validateSlice((byte[]) null, 0, 1));
		assertThrown(() -> Validate.validateSlice((short[]) null, 1, 0));
		assertThrown(() -> Validate.validateSlice(ArrayUtil.ints.of(0), -1, 0));
		assertThrown(() -> Validate.validateSlice(ArrayUtil.ints.of(0), 0, 2));
		assertThrown(() -> Validate.validateSlice(ArrayUtil.ints.of(0), 1, 1));
		assertThrown(() -> Validate.validateSlice(ArrayUtil.ints.of(0), 2, 0));
	}

	@Test
	public void testValidateSlice() {
		int[] array = { 1, 2, 3, 4 };
		Validate.validateSlice(array.length, 0, 4);
		Validate.validateSlice(array.length, 1, 2);
		assertThrown(() -> Validate.validateSlice(array.length, -1, 1));
		assertThrown(() -> Validate.validateSlice(array.length, 5, 1));
		assertThrown(() -> Validate.validateSlice(array.length, 2, 4));
	}

	@Test
	public void testValidateFullSlice() {
		int[] array = { 1, 2, 3, 4 };
		assertTrue(Validate.validateFullSlice(array.length, 0, 4));
		assertFalse(Validate.validateFullSlice(array.length, 0, 3));
		assertIndexOob(() -> Validate.validateFullSlice(array.length, 0, 5));
		assertFalse(Validate.validateFullSlice(array.length, 1, 3));
	}

	@Test
	public void testValidateSubRange() {
		int[] array = { 1, 2, 3, 4 };
		Validate.validateSubRange(array.length, 0, 4);
		Validate.validateSubRange(array.length, 1, 3);
		assertThrown(() -> Validate.validateSubRange(array.length, -1, 0));
		assertThrown(() -> Validate.validateSubRange(array.length, 5, 6));
		assertThrown(() -> Validate.validateSubRange(array.length, 2, 1));
		assertThrown(() -> Validate.validateSubRange(array.length, 2, 5));
	}

	@Test
	public void testValidateFullSubRange() {
		int[] array = { 1, 2, 3, 4 };
		assertTrue(Validate.validateFullSubRange(array.length, 0, 4));
		assertFalse(Validate.validateFullSubRange(array.length, 0, 3));
		assertIndexOob(() -> Validate.validateFullSubRange(array.length, 0, 5));
		assertFalse(Validate.validateFullSubRange(array.length, 1, 3));
	}

	@Test
	public void testvalidateMinLong() {
		Validate.validateMin(Long.MAX_VALUE, Long.MAX_VALUE);
		Validate.validateMin(Long.MAX_VALUE, Long.MIN_VALUE);
		Validate.validateMin(Long.MIN_VALUE, Long.MIN_VALUE);
		Validate.validateMin(Long.MAX_VALUE, 0, "test");
		assertThrown(() -> Validate.validateMin(Long.MIN_VALUE, 0));
		assertThrown(() -> Validate.validateMin(Long.MIN_VALUE, Long.MAX_VALUE));
		assertThrown(() -> Validate.validateMin(-1, 0, "test"));
	}

	@Test
	public void testValidateMinDouble() {
		Validate.validateMinFp(Double.MIN_VALUE, Double.MIN_VALUE);
		Validate.validateMinFp(Double.MAX_VALUE, Double.MIN_VALUE);
		Validate.validateMinFp(Double.MAX_VALUE, Double.MAX_VALUE);
		Validate.validateMinFp(Double.MIN_VALUE, 0, "test");
		assertThrown(() -> Validate.validateMinFp(Double.MIN_VALUE, Double.MAX_VALUE));
		assertThrown(() -> Validate.validateMinFp(0, Double.MIN_VALUE));
		assertThrown(() -> Validate.validateMinFp(-0.0, Double.MIN_NORMAL, "test"));
	}

	@Test
	public void testValidateMinLongWithBound() {
		Validate.validateMin(Long.MAX_VALUE, Long.MAX_VALUE, Bound.Type.inc);
		Validate.validateMin(Long.MIN_VALUE, Long.MIN_VALUE, Bound.Type.inc);
		assertThrown(() -> Validate.validateMin(Long.MAX_VALUE, Long.MAX_VALUE, Bound.Type.exc));
		assertThrown(() -> Validate.validateMin(Long.MIN_VALUE, Long.MIN_VALUE, Bound.Type.exc));
	}

	@Test
	public void testValidateMinDoubleWithBound() {
		Validate.validateMinFp(1.0, 0.999, Bound.Type.exc);
		Validate.validateMinFp(-0.999, -1.0, Bound.Type.exc);
		Validate.validateMinFp(1.0, 1.0, Bound.Type.inc);
		assertThrown(() -> Validate.validateMinFp(1.0, 1.0, Bound.Type.exc));
		assertThrown(() -> Validate.validateMinFp(-1.0, -1.0, Bound.Type.exc));
	}

	@Test
	public void testvalidateMaxLong() {
		Validate.validateMax(Long.MIN_VALUE, Long.MIN_VALUE);
		Validate.validateMax(Long.MIN_VALUE, Long.MAX_VALUE);
		Validate.validateMax(Long.MAX_VALUE, Long.MAX_VALUE);
		Validate.validateMax(-1, 0, "test");
		assertThrown(() -> Validate.validateMax(Long.MAX_VALUE, Long.MIN_VALUE));
		assertThrown(() -> Validate.validateMax(Long.MAX_VALUE, 0));
		assertThrown(() -> Validate.validateMax(0, -1, "test"));
	}

	@Test
	public void testValidateMaxDouble() {
		Validate.validateMaxFp(Double.MAX_VALUE, Double.MAX_VALUE);
		Validate.validateMaxFp(Double.MIN_VALUE, Double.MAX_VALUE);
		Validate.validateMaxFp(Double.MIN_VALUE, Double.MIN_VALUE);
		Validate.validateMaxFp(0, Double.MIN_VALUE, "test");
		assertThrown(() -> Validate.validateMaxFp(Double.MAX_VALUE, Double.MIN_VALUE));
		assertThrown(() -> Validate.validateMaxFp(Double.MIN_VALUE, 0));
		assertThrown(() -> Validate.validateMaxFp(Double.MIN_NORMAL, -0.0, "test"));
	}

	@Test
	public void testValidateMaxLongWithBound() {
		Validate.validateMax(Long.MAX_VALUE, Long.MAX_VALUE, Bound.Type.inc);
		Validate.validateMax(Long.MIN_VALUE, Long.MIN_VALUE, Bound.Type.inc);
		assertThrown(() -> Validate.validateMax(Long.MAX_VALUE, Long.MAX_VALUE, Bound.Type.exc));
		assertThrown(() -> Validate.validateMax(Long.MIN_VALUE, Long.MIN_VALUE, Bound.Type.exc));
	}

	@Test
	public void testValidateMaxDoubleWithBound() {
		Validate.validateMaxFp(0.999, 1.0, Bound.Type.exc);
		Validate.validateMaxFp(-1.0, -0.999, Bound.Type.exc);
		Validate.validateMaxFp(1.0, 1.0, Bound.Type.inc);
		assertThrown(() -> Validate.validateMaxFp(1.0, 1.0, Bound.Type.exc));
		assertThrown(() -> Validate.validateMaxFp(-1.0, -1.0, Bound.Type.exc));
	}

	@Test
	public void testValidateLongRange() {
		Validate.validateRange(0, Long.MIN_VALUE, Long.MAX_VALUE);
		Validate.validateRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		Validate.validateRange(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		Validate.validateRange(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
		Validate.validateRange(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE);
		Validate.validateRange(0, 0, 0);
		Validate.validateRange(0, -1, 1, "test");
		assertThrown(() -> Validate.validateRange(0, Long.MAX_VALUE, Long.MIN_VALUE));
		assertThrown(() -> Validate.validateRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE));
		assertThrown(() -> Validate.validateRange(Long.MIN_VALUE, Long.MAX_VALUE, Long.MAX_VALUE));
		assertThrown(() -> Validate.validateRange(-1, 0, 1, "test"));
		assertThrown(() -> Validate.validateRange(1, -1, 0, "test"));
	}

	@Test
	public void testValidateDoubleRange() {
		Validate.validateRangeFp(Double.MIN_VALUE, 0, Double.MAX_VALUE);
		Validate.validateRangeFp(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		Validate.validateRangeFp(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
		Validate.validateRangeFp(-0.0, 0.0, 0.0);
		Validate.validateRangeFp(0.0, -1.0, 1.0, "test");
		assertThrown(() -> Validate.validateRangeFp(0, Double.MIN_VALUE, Double.MAX_VALUE));
		assertThrown(
			() -> Validate.validateRangeFp(Double.MAX_VALUE, Double.MAX_VALUE, Double.MIN_VALUE));
		assertThrown(
			() -> Validate.validateRangeFp(Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE));
		assertThrown(() -> Validate.validateRangeFp(-1.0, 0.0, 1.0, "test"));
		assertThrown(() -> Validate.validateRangeFp(1.0, -1.0, 0.0, "test"));
	}

	@Test
	public void testValidateMinUnsigned() {
		Validate.validateUmin(Long.MAX_VALUE, Long.MAX_VALUE);
		Validate.validateUmin(Long.MIN_VALUE, Long.MIN_VALUE);
		Validate.validateUmin(Long.MIN_VALUE, Long.MAX_VALUE, "test");
		assertThrown(() -> Validate.validateUmin(Long.MAX_VALUE, Long.MIN_VALUE));
		assertThrown(() -> Validate.validateUmin(0, Long.MIN_VALUE, "test"));
		assertThrown(() -> Validate.validateUmin(0, -1, "test"));
	}

	@Test
	public void testValidateMaxUnsigned() {
		Validate.validateUmax(Long.MAX_VALUE, Long.MAX_VALUE);
		Validate.validateUmax(Long.MIN_VALUE, Long.MIN_VALUE);
		Validate.validateUmax(Long.MAX_VALUE, Long.MIN_VALUE, "test");
		assertThrown(() -> Validate.validateUmax(Long.MIN_VALUE, Long.MAX_VALUE));
		assertThrown(() -> Validate.validateUmax(Long.MIN_VALUE, 0, "test"));
		assertThrown(() -> Validate.validateUmax(-1, 0, "test"));
	}

	@Test
	public void testValidateRangeUnsigned() {
		Validate.validateUrange(Long.MAX_VALUE, 0, Long.MAX_VALUE);
		Validate.validateUrange(Long.MIN_VALUE, 0, Long.MIN_VALUE);
		Validate.validateUrange(Long.MAX_VALUE, 0, Long.MIN_VALUE, "test");
		assertThrown(() -> Validate.validateUrange(Long.MIN_VALUE, 0, Long.MAX_VALUE));
		assertThrown(() -> Validate.validateUrange(0, Long.MAX_VALUE, Long.MIN_VALUE));
		assertThrown(() -> Validate.validateUrange(0, Long.MIN_VALUE, 0, "test"));
		assertThrown(() -> Validate.validateUrange(-1, 0, -2, "test"));
	}

	@Test
	public void testValidateFind() {
		Validate.validateFind("123abc456", Pattern.compile("\\w+"));
		assertThrown(() -> Validate.validateFind("abc", Pattern.compile("\\d+")));
	}

	@Test
	public void testValidateEmptyCollection() {
		Validate.validateEmpty(List.of());
		var set = Validate.validateEmpty(Sets.<Integer>of());
		assertNotNull(set);
		assertThrown(() -> Validate.validateEmpty(Set.of(1)));
	}

	@Test
	public void testValidateEmptyMap() {
		Validate.validateEmpty(Map.of());
		var map = Validate.validateEmpty(Maps.<Integer, String>of());
		assertNotNull(map);
		assertThrown(() -> Validate.validateEmpty(Map.of(1, "a")));
	}

	@Test
	public void testValidateNotEmptyCollection() {
		assertThrown(() -> Validate.validateNotEmpty(Set.of()));
		assertThrown(() -> Validate.validateNotEmpty(List.of()));
		var set = Validate.validateNotEmpty(Set.of(1, 2, 3));
		assertNotNull(set);
	}

	@Test
	public void testValidateNotEmptyMap() {
		assertThrown(() -> Validate.validateNotEmpty(Map.of()));
		var map = Validate.validateNotEmpty(Map.of(3, "3"));
		assertNotNull(map);
	}

	@Test
	public void testValidateContains() {
		Validate.validateContains(List.of("a", "b", "c"), "b");
		Validate.validateContains(Set.of(1), 1);
		assertThrown(() -> Validate.validateContains(List.of(), "a"));
		assertThrown(() -> Validate.validateContains(Set.of(1), 2));
	}

	@Test
	public void testValidateContainsKey() {
		Validate.validateContainsKey(Map.of(1, "a", 2, "b"), 2);
		assertThrown(() -> Validate.validateContainsKey(Map.of(), 1));
		assertThrown(() -> Validate.validateContainsKey(Map.of(1, "a"), 2));
	}

	@Test
	public void testValidateContainsValue() {
		Validate.validateContainsValue(Map.of(1, "a", 2, "b"), "b");
		assertThrown(() -> Validate.validateContainsValue(Map.of(), "a"));
		assertThrown(() -> Validate.validateContainsValue(Map.of(1, "a"), "b"));
	}
}
