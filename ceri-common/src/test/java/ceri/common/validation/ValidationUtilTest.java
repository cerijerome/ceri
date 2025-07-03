package ceri.common.validation;

import static ceri.common.math.Bound.Type.exclusive;
import static ceri.common.math.Bound.Type.inclusive;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIndexOob;
import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnsupported;
import static ceri.common.test.TestUtil.thrown;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.math.Interval;

public class ValidationUtilTest {
	private static final Object OBJ = new Object();
	private static final Integer I0 = 999;
	private static final Integer I1 = 999;

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ValidationUtil.class);
	}

	@Test
	public void testValidatePredicate() {
		ValidationUtil.validate(i -> i < 0, -1);
		assertThrown(() -> ValidationUtil.validate(i -> i < 0, 0));
		ValidationUtil.validate(i -> i < 0, -1, "int");
		assertThrown(() -> ValidationUtil.validate(i -> i < 0, 0, "int"));
	}

	@Test
	public void testValidateExpression() {
		ValidationUtil.validate(1 > 0);
		ValidationUtil.validate(1 > 0, null);
		ValidationUtil.validate(1 > 0, "test");
		assertThrown(() -> ValidationUtil.validate(1 < 0));
		assertThrown(() -> ValidationUtil.validate(1 < 0, null));
		assertThrown(() -> ValidationUtil.validate(1 < 0, "test"));
	}

	@Test
	public void testValidateWithFormattedException() {
		int i0 = -1, i1 = 1;
		ValidationUtil.validatef(i0 < 0, "%d >= 0", i0);
		assertThrown(() -> ValidationUtil.validatef(i1 < 0, "%d >= 0", i1));
	}

	@Test
	public void testValidateLookup() {
		Map<String, Integer> map = Map.of("one", 1, "two", 2, "three", 3);
		assertEquals(ValidationUtil.validateLookup(map::get, "two"), 2);
		assertThrown(() -> ValidationUtil.validateLookup(map::get, "TWO"));
	}

	@Test
	public void testValidateIntLookup() {
		Map<Integer, String> map = Map.of(1, "one", 2, "two", 3, "three");
		assertEquals(ValidationUtil.validateIntLookup(map::get, 2), "two");
		assertThrown(() -> ValidationUtil.validateIntLookup(map::get, 0));
	}

	@Test
	public void testValidateLongLookup() {
		Map<Long, String> map = Map.of(1L, "one", 2L, "two", 3L, "three");
		assertEquals(ValidationUtil.validateLongLookup(map::get, 2L), "two");
		assertThrown(() -> ValidationUtil.validateLongLookup(map::get, 0L));
	}

	@Test
	public void testValidateLookupEquals() {
		Map<String, Integer> map = Map.of("one", 1, "two", 2, "three", 3);
		assertEquals(ValidationUtil.validateLookupEquals(map::get, "two", 2), 2);
		assertThrown(() -> ValidationUtil.validateLookupEquals(map::get, "two", 3));
	}

	@Test
	public void testValidateIntLookupEquals() {
		Map<Integer, String> map = Map.of(1, "one", 2, "two", 3, "three");
		assertEquals(ValidationUtil.validateIntLookupEquals(map::get, 2, "two"), "two");
		assertThrown(() -> ValidationUtil.validateIntLookupEquals(map::get, 2, "three"));
	}

	@Test
	public void testValidateSupported() {
		ValidationUtil.validateSupported(OBJ, "test");
		assertUnsupported(() -> ValidationUtil.validateSupported(null, "test"));
	}

	@Test
	public void testValidateNotNull() {
		ValidationUtil.validateNotNull(OBJ);
		ValidationUtil.validateNotNull(OBJ, "test");
		assertThrown(() -> ValidationUtil.validateNotNull(null));
		assertThrown(() -> ValidationUtil.validateNotNull(null, "test"));
	}

	@Test
	public void testValidateAllNotNull() {
		ValidationUtil.validateAllNotNull(OBJ, "a", 1);
		assertThrown(() -> ValidationUtil.validateAllNotNull(OBJ, null));
		assertThrown(() -> ValidationUtil.validateAllNotNull((Object[]) null));
	}

	@Test
	public void testValidateNull() {
		ValidationUtil.validateNull(null);
		assertThrown(() -> ValidationUtil.validateNull(""));
	}

	@Test
	public void testValidateObjectEquality() {
		ValidationUtil.validateEqualObj(OBJ, OBJ);
		ValidationUtil.validateEqualObj(null, null);
		ValidationUtil.validateEqualObj(I0, I1);
		assertThrown(() -> ValidationUtil.validateEqualObj(null, OBJ));
		assertThrown(() -> ValidationUtil.validateEqualObj(OBJ, null));
		assertThrown(() -> ValidationUtil.validateEqualObj(OBJ, I0));
	}

	@Test
	public void testValidateObjectInequality() {
		ValidationUtil.validateNotEqualObj(null, OBJ);
		ValidationUtil.validateNotEqualObj(OBJ, null);
		ValidationUtil.validateNotEqualObj(I0, OBJ);
		assertThrown(() -> ValidationUtil.validateNotEqualObj(null, null));
		assertThrown(() -> ValidationUtil.validateNotEqualObj(OBJ, OBJ));
	}

	@Test
	public void testValidateLongEquality() {
		ValidationUtil.validateEqual(Long.MIN_VALUE, Long.MIN_VALUE, DisplayLong.hex);
		assertThrown(
			() -> ValidationUtil.validateEqual(Long.MIN_VALUE, Long.MAX_VALUE, DisplayLong.hex16));
	}

	@Test
	public void testValidateDoubleEquality() {
		ValidationUtil.validateEqualFp(Double.MIN_VALUE, Double.MIN_VALUE, DisplayDouble.round1);
		assertThrown(() -> ValidationUtil.validateEqualFp(Double.MAX_VALUE, Double.MIN_VALUE,
			DisplayDouble.std));
	}

	@Test
	public void testValidateLongEqualityFormat() {
		assertEquals(thrown(() -> ValidationUtil.validateEqual(-1, 0xff, (String) null,
			DisplayLong.dec, DisplayLong.hex4)).getMessage(),
			"Value != (255, 0x00ff): (-1, 0xffff)");
	}

	@Test
	public void testValidateDoubleEqualityFormat() {
		assertEquals(
			thrown(() -> ValidationUtil.validateEqualFp(1.0, 1.111, "Num", DisplayDouble.round1))
				.getMessage(),
			"Num != 1.1: 1.0");
	}

	@Test
	public void testValidateLongInequality() {
		ValidationUtil.validateNotEqual(Long.MIN_VALUE, Long.MAX_VALUE, DisplayLong.udec);
		assertThrown(() -> ValidationUtil.validateNotEqual(Long.MIN_VALUE, Long.MIN_VALUE,
			DisplayLong.hex4));
	}

	@Test
	public void testValidateDoubleInequality() {
		ValidationUtil.validateNotEqualFp(Double.MIN_VALUE, Double.MAX_VALUE, DisplayDouble.round);
		assertThrown(
			() -> ValidationUtil.validateNotEqualFp(1.00001, 1.00001, DisplayDouble.round1));
	}

	@Test
	public void testValidateUbyte() {
		ValidationUtil.validateUbyte(0xff, -1);
		ValidationUtil.validateUbyte(0xffff, -1);
		assertThrown(() -> ValidationUtil.validateUbyte(0xf, -1, "test"));
		ValidationUtil.validateUbyte(0);
		ValidationUtil.validateUbyte(0xff);
		assertThrown(() -> ValidationUtil.validateUbyte(-1));
		assertThrown(() -> ValidationUtil.validateUbyte(0x100));
		assertEquals(
			thrown(() -> ValidationUtil.validateUbyte(-1, "Byte", DisplayLong.dec)).getMessage(),
			"Byte is not within [0, 255]: -1");
	}

	@Test
	public void testValidateUshort() {
		ValidationUtil.validateUshort(0xffff, -1);
		ValidationUtil.validateUshort(0xffffff, -1);
		assertThrown(() -> ValidationUtil.validateUshort(0xff, -1, "test"));
		ValidationUtil.validateUshort(0);
		ValidationUtil.validateUshort(0xffff);
		assertThrown(() -> ValidationUtil.validateUshort(-1));
		assertThrown(() -> ValidationUtil.validateUshort(0x10000));
	}

	@Test
	public void testValidateUint() {
		ValidationUtil.validateUint(0xffffffff, -1);
		ValidationUtil.validateUint(0xffffffffffL, -1);
		assertThrown(() -> ValidationUtil.validateUint(0xffff, -1, "test"));
		ValidationUtil.validateUint(0);
		ValidationUtil.validateUint(0xffffffffL);
		assertThrown(() -> ValidationUtil.validateUint(-1));
		assertThrown(() -> ValidationUtil.validateUint(0x100000000L));
	}

	@Test
	public void testValidateUlong() {
		ValidationUtil.validateUlong(0xffffffff12345678L, 0xffffffff12345678L);
		ValidationUtil.validateUlong(0xffffffffffffffffL, -1L);
		ValidationUtil.validateUlong(Long.MIN_VALUE, Long.MIN_VALUE, "test");
		assertThrown(() -> ValidationUtil.validateUlong(-1L, -2L, "test"));
		assertThrown(() -> ValidationUtil.validateUlong( //
			0xffffffff12345678L, 0xffffffff12345670L));
	}

	@Test
	public void testValidateWithinObj() {
		ValidationUtil.validateWithinObj(1, Interval.inclusive(0, 1));
		ValidationUtil.validateWithinObj(0.5, Interval.exclusive(0.0, 1.0));
		assertThrown(() -> ValidationUtil.validateWithinObj(1, Interval.exclusive(0, 1)));
		assertThrown(() -> ValidationUtil.validateWithinObj(0, Interval.exclusive(0, 1)));
		assertThrown(() -> ValidationUtil.validateWithinObj(1d, Interval.exclusive(0d, 1d)));
		assertThrown(() -> ValidationUtil.validateWithinObj(0d, Interval.exclusive(0d, 1d)));
	}

	@Test
	public void testValidateWithoutObj() {
		ValidationUtil.validateWithoutObj(1, Interval.exclusive(0, 1));
		assertThrown(() -> ValidationUtil.validateWithoutObj(1, Interval.inclusive(0, 1)));
	}

	@Test
	public void testValidateWithinLong() {
		assertThrown(() -> ValidationUtil.validateWithin(1, Interval.exclusive(0L, 1L)));
		ValidationUtil.validateWithin(1, Interval.inclusive(0L, 1L), DisplayLong.hex);
		assertThrown(
			() -> ValidationUtil.validateWithin(1, Interval.exclusive(0L, 1L), DisplayLong.hex));
	}

	@Test
	public void testValidateWithinDouble() {
		ValidationUtil.validateWithinFp(1, Interval.inclusive(0.0, 1.0), DisplayDouble.round);
		assertThrown(() -> ValidationUtil.validateWithinFp(1, Interval.exclusive(0.0, 1.0),
			DisplayDouble.round));
	}

	@Test
	public void testValidateWithoutLong() {
		ValidationUtil.validateWithout(1, Interval.exclusive(0L, 1L), DisplayLong.hex);
		assertThrown(
			() -> ValidationUtil.validateWithout(1, Interval.inclusive(0L, 1L), DisplayLong.hex));
	}

	@Test
	public void testValidateWithoutDouble() {
		ValidationUtil.validateWithoutFp(1, Interval.exclusive(0.0, 1.0), DisplayDouble.round);
		assertThrown(() -> ValidationUtil.validateWithoutFp(1, Interval.inclusive(0.0, 1.0),
			DisplayDouble.round));
	}

	@Test
	public void testValidateIndex() {
		int[] array = { 1, 2, 3, 4 };
		ValidationUtil.validateIndex(array.length, 0);
		ValidationUtil.validateIndex(array.length, 3);
		assertThrown(() -> ValidationUtil.validateIndex(array.length, -1));
		assertThrown(() -> ValidationUtil.validateIndex(array.length, 4));
	}

	@Test
	public void testValidateArraySlice() {
		ValidationUtil.validateSlice((boolean[]) null, 0, 0);
		ValidationUtil.validateSlice("ab".toCharArray(), 1, 1);
		ValidationUtil.validateSlice(ArrayUtil.bytes(-1, 0, 1), 0, 3);
		ValidationUtil.validateSlice(ArrayUtil.shorts(-1, 0, 1), 0, 2);
		ValidationUtil.validateSlice(ArrayUtil.ints(-1, 0, 1), 1, 0);
		ValidationUtil.validateSlice(ArrayUtil.longs(-1, 0, 1), 0, 0);
		ValidationUtil.validateSlice(ArrayUtil.floats(-1, 0, 1), 2, 1);
		ValidationUtil.validateSlice(ArrayUtil.doubles(-1, 0, 1), 3, 0);
		assertThrown(() -> ValidationUtil.validateSlice((byte[]) null, 0, 1));
		assertThrown(() -> ValidationUtil.validateSlice((short[]) null, 1, 0));
		assertThrown(() -> ValidationUtil.validateSlice(ArrayUtil.ints(0), -1, 0));
		assertThrown(() -> ValidationUtil.validateSlice(ArrayUtil.ints(0), 0, 2));
		assertThrown(() -> ValidationUtil.validateSlice(ArrayUtil.ints(0), 1, 1));
		assertThrown(() -> ValidationUtil.validateSlice(ArrayUtil.ints(0), 2, 0));
	}

	@Test
	public void testValidateSlice() {
		int[] array = { 1, 2, 3, 4 };
		ValidationUtil.validateSlice(array.length, 0, 4);
		ValidationUtil.validateSlice(array.length, 1, 2);
		assertThrown(() -> ValidationUtil.validateSlice(array.length, -1, 1));
		assertThrown(() -> ValidationUtil.validateSlice(array.length, 5, 1));
		assertThrown(() -> ValidationUtil.validateSlice(array.length, 2, 4));
	}

	@Test
	public void testValidateFullSlice() {
		int[] array = { 1, 2, 3, 4 };
		assertTrue(ValidationUtil.validateFullSlice(array.length, 0, 4));
		assertFalse(ValidationUtil.validateFullSlice(array.length, 0, 3));
		assertIndexOob(() -> ValidationUtil.validateFullSlice(array.length, 0, 5));
		assertFalse(ValidationUtil.validateFullSlice(array.length, 1, 3));
	}

	@Test
	public void testValidateSubRange() {
		int[] array = { 1, 2, 3, 4 };
		ValidationUtil.validateSubRange(array.length, 0, 4);
		ValidationUtil.validateSubRange(array.length, 1, 3);
		assertThrown(() -> ValidationUtil.validateSubRange(array.length, -1, 0));
		assertThrown(() -> ValidationUtil.validateSubRange(array.length, 5, 6));
		assertThrown(() -> ValidationUtil.validateSubRange(array.length, 2, 1));
		assertThrown(() -> ValidationUtil.validateSubRange(array.length, 2, 5));
	}

	@Test
	public void testValidateFullSubRange() {
		int[] array = { 1, 2, 3, 4 };
		assertTrue(ValidationUtil.validateFullSubRange(array.length, 0, 4));
		assertFalse(ValidationUtil.validateFullSubRange(array.length, 0, 3));
		assertIndexOob(() -> ValidationUtil.validateFullSubRange(array.length, 0, 5));
		assertFalse(ValidationUtil.validateFullSubRange(array.length, 1, 3));
	}

	@Test
	public void testvalidateMinLong() {
		ValidationUtil.validateMin(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMin(Long.MAX_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMin(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMin(Long.MAX_VALUE, 0, "test");
		assertThrown(() -> ValidationUtil.validateMin(Long.MIN_VALUE, 0));
		assertThrown(() -> ValidationUtil.validateMin(Long.MIN_VALUE, Long.MAX_VALUE));
		assertThrown(() -> ValidationUtil.validateMin(-1, 0, "test"));
	}

	@Test
	public void testValidateMinDouble() {
		ValidationUtil.validateMinFp(Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMinFp(Double.MAX_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMinFp(Double.MAX_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMinFp(Double.MIN_VALUE, 0, "test");
		assertThrown(() -> ValidationUtil.validateMinFp(Double.MIN_VALUE, Double.MAX_VALUE));
		assertThrown(() -> ValidationUtil.validateMinFp(0, Double.MIN_VALUE));
		assertThrown(() -> ValidationUtil.validateMinFp(-0.0, Double.MIN_NORMAL, "test"));
	}

	@Test
	public void testValidateMinLongWithBound() {
		ValidationUtil.validateMin(Long.MAX_VALUE, Long.MAX_VALUE, inclusive);
		ValidationUtil.validateMin(Long.MIN_VALUE, Long.MIN_VALUE, inclusive);
		assertThrown(() -> ValidationUtil.validateMin(Long.MAX_VALUE, Long.MAX_VALUE, exclusive));
		assertThrown(() -> ValidationUtil.validateMin(Long.MIN_VALUE, Long.MIN_VALUE, exclusive));
	}

	@Test
	public void testValidateMinDoubleWithBound() {
		ValidationUtil.validateMinFp(1.0, 0.999, exclusive);
		ValidationUtil.validateMinFp(-0.999, -1.0, exclusive);
		ValidationUtil.validateMinFp(1.0, 1.0, inclusive);
		assertThrown(() -> ValidationUtil.validateMinFp(1.0, 1.0, exclusive));
		assertThrown(() -> ValidationUtil.validateMinFp(-1.0, -1.0, exclusive));
	}

	@Test
	public void testvalidateMaxLong() {
		ValidationUtil.validateMax(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateMax(Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMax(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateMax(-1, 0, "test");
		assertThrown(() -> ValidationUtil.validateMax(Long.MAX_VALUE, Long.MIN_VALUE));
		assertThrown(() -> ValidationUtil.validateMax(Long.MAX_VALUE, 0));
		assertThrown(() -> ValidationUtil.validateMax(0, -1, "test"));
	}

	@Test
	public void testValidateMaxDouble() {
		ValidationUtil.validateMaxFp(Double.MAX_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMaxFp(Double.MIN_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateMaxFp(Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateMaxFp(0, Double.MIN_VALUE, "test");
		assertThrown(() -> ValidationUtil.validateMaxFp(Double.MAX_VALUE, Double.MIN_VALUE));
		assertThrown(() -> ValidationUtil.validateMaxFp(Double.MIN_VALUE, 0));
		assertThrown(() -> ValidationUtil.validateMaxFp(Double.MIN_NORMAL, -0.0, "test"));
	}

	@Test
	public void testValidateMaxLongWithBound() {
		ValidationUtil.validateMax(Long.MAX_VALUE, Long.MAX_VALUE, inclusive);
		ValidationUtil.validateMax(Long.MIN_VALUE, Long.MIN_VALUE, inclusive);
		assertThrown(() -> ValidationUtil.validateMax(Long.MAX_VALUE, Long.MAX_VALUE, exclusive));
		assertThrown(() -> ValidationUtil.validateMax(Long.MIN_VALUE, Long.MIN_VALUE, exclusive));
	}

	@Test
	public void testValidateMaxDoubleWithBound() {
		ValidationUtil.validateMaxFp(0.999, 1.0, exclusive);
		ValidationUtil.validateMaxFp(-1.0, -0.999, exclusive);
		ValidationUtil.validateMaxFp(1.0, 1.0, inclusive);
		assertThrown(() -> ValidationUtil.validateMaxFp(1.0, 1.0, exclusive));
		assertThrown(() -> ValidationUtil.validateMaxFp(-1.0, -1.0, exclusive));
	}

	@Test
	public void testValidateLongRange() {
		ValidationUtil.validateRange(0, Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRange(Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRange(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateRange(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateRange(0, 0, 0);
		ValidationUtil.validateRange(0, -1, 1, "test");
		assertThrown(() -> ValidationUtil.validateRange(0, Long.MAX_VALUE, Long.MIN_VALUE));
		assertThrown(
			() -> ValidationUtil.validateRange(Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE));
		assertThrown(
			() -> ValidationUtil.validateRange(Long.MIN_VALUE, Long.MAX_VALUE, Long.MAX_VALUE));
		assertThrown(() -> ValidationUtil.validateRange(-1, 0, 1, "test"));
		assertThrown(() -> ValidationUtil.validateRange(1, -1, 0, "test"));
	}

	@Test
	public void testValidateDoubleRange() {
		ValidationUtil.validateRangeFp(Double.MIN_VALUE, 0, Double.MAX_VALUE);
		ValidationUtil.validateRangeFp(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		ValidationUtil.validateRangeFp(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
		ValidationUtil.validateRangeFp(-0.0, 0.0, 0.0);
		ValidationUtil.validateRangeFp(0.0, -1.0, 1.0, "test");
		assertThrown(() -> ValidationUtil.validateRangeFp(0, Double.MIN_VALUE, Double.MAX_VALUE));
		assertThrown(() -> ValidationUtil.validateRangeFp(Double.MAX_VALUE, Double.MAX_VALUE,
			Double.MIN_VALUE));
		assertThrown(() -> ValidationUtil.validateRangeFp(Double.MIN_VALUE, Double.MAX_VALUE,
			Double.MIN_VALUE));
		assertThrown(() -> ValidationUtil.validateRangeFp(-1.0, 0.0, 1.0, "test"));
		assertThrown(() -> ValidationUtil.validateRangeFp(1.0, -1.0, 0.0, "test"));
	}

	@Test
	public void testValidateMinUnsigned() {
		ValidationUtil.validateUmin(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateUmin(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateUmin(Long.MIN_VALUE, Long.MAX_VALUE, "test");
		assertThrown(() -> ValidationUtil.validateUmin(Long.MAX_VALUE, Long.MIN_VALUE));
		assertThrown(() -> ValidationUtil.validateUmin(0, Long.MIN_VALUE, "test"));
		assertThrown(() -> ValidationUtil.validateUmin(0, -1, "test"));
	}

	@Test
	public void testValidateMaxUnsigned() {
		ValidationUtil.validateUmax(Long.MAX_VALUE, Long.MAX_VALUE);
		ValidationUtil.validateUmax(Long.MIN_VALUE, Long.MIN_VALUE);
		ValidationUtil.validateUmax(Long.MAX_VALUE, Long.MIN_VALUE, "test");
		assertThrown(() -> ValidationUtil.validateUmax(Long.MIN_VALUE, Long.MAX_VALUE));
		assertThrown(() -> ValidationUtil.validateUmax(Long.MIN_VALUE, 0, "test"));
		assertThrown(() -> ValidationUtil.validateUmax(-1, 0, "test"));
	}

	@Test
	public void testValidateRangeUnsigned() {
		ValidationUtil.validateUrange(Long.MAX_VALUE, 0, Long.MAX_VALUE);
		ValidationUtil.validateUrange(Long.MIN_VALUE, 0, Long.MIN_VALUE);
		ValidationUtil.validateUrange(Long.MAX_VALUE, 0, Long.MIN_VALUE, "test");
		assertThrown(() -> ValidationUtil.validateUrange(Long.MIN_VALUE, 0, Long.MAX_VALUE));
		assertThrown(() -> ValidationUtil.validateUrange(0, Long.MAX_VALUE, Long.MIN_VALUE));
		assertThrown(() -> ValidationUtil.validateUrange(0, Long.MIN_VALUE, 0, "test"));
		assertThrown(() -> ValidationUtil.validateUrange(-1, 0, -2, "test"));
	}

	@Test
	public void testValidateFind() {
		ValidationUtil.validateFind("123abc456", Pattern.compile("\\w+"));
		assertThrown(() -> ValidationUtil.validateFind("abc", Pattern.compile("\\d+")));
	}

	@Test
	public void testValidateEmptyCollection() {
		ValidationUtil.validateEmpty(List.of());
		Set<Integer> set = ValidationUtil.validateEmpty(new HashSet<Integer>());
		assertNotNull(set);
		assertThrown(() -> ValidationUtil.validateEmpty(Set.of(1)));
	}

	@Test
	public void testValidateEmptyMap() {
		ValidationUtil.validateEmpty(Map.of());
		Map<Integer, String> map = ValidationUtil.validateEmpty(new HashMap<Integer, String>());
		assertNotNull(map);
		assertThrown(() -> ValidationUtil.validateEmpty(Map.of(1, "a")));
	}

	@Test
	public void testValidateNotEmptyCollection() {
		assertThrown(() -> ValidationUtil.validateNotEmpty(Set.of()));
		assertThrown(() -> ValidationUtil.validateNotEmpty(List.of()));
		Set<Integer> set = ValidationUtil.validateNotEmpty(Set.of(1, 2, 3));
		assertNotNull(set);
	}

	@Test
	public void testValidateNotEmptyMap() {
		assertThrown(() -> ValidationUtil.validateNotEmpty(Map.of()));
		Map<Integer, String> map = ValidationUtil.validateNotEmpty(Map.of(3, "3"));
		assertNotNull(map);
	}

	@Test
	public void testValidateContains() {
		ValidationUtil.validateContains(List.of("a", "b", "c"), "b");
		ValidationUtil.validateContains(Set.of(1), 1);
		assertThrown(() -> ValidationUtil.validateContains(List.of(), "a"));
		assertThrown(() -> ValidationUtil.validateContains(Set.of(1), 2));
	}

	@Test
	public void testValidateContainsKey() {
		ValidationUtil.validateContainsKey(Map.of(1, "a", 2, "b"), 2);
		assertThrown(() -> ValidationUtil.validateContainsKey(Map.of(), 1));
		assertThrown(() -> ValidationUtil.validateContainsKey(Map.of(1, "a"), 2));
	}

	@Test
	public void testValidateContainsValue() {
		ValidationUtil.validateContainsValue(Map.of(1, "a", 2, "b"), "b");
		assertThrown(() -> ValidationUtil.validateContainsValue(Map.of(), "a"));
		assertThrown(() -> ValidationUtil.validateContainsValue(Map.of(1, "a"), "b"));
	}

}
