package ceri.common.util;

import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertUnsupported;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;

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
	public void testIndex() {
		int[] array = { 1, 2, 3, 4 };
		Validate.index(array.length, 0);
		Validate.index(array.length, 3);
		assertThrown(() -> Validate.index(array.length, -1));
		assertThrown(() -> Validate.index(array.length, 4));
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
