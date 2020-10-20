package ceri.common.comparator;

import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Comparator;
import org.junit.Test;

public class EnumComparatorsTest {
	private enum TestEnum {
		C,
		B,
		A
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(EnumComparators.class);
	}

	@Test
	public void testByOrdinal() {
		Comparator<TestEnum> comparator = EnumComparators.ordinal();
		assertTrue(comparator.compare(TestEnum.C, TestEnum.B) < 0);
		assertTrue(comparator.compare(TestEnum.B, TestEnum.A) < 0);
		assertTrue(comparator.compare(TestEnum.A, TestEnum.C) > 0);
	}

	@Test
	public void testByName() {
		Comparator<TestEnum> comparator = EnumComparators.name();
		assertTrue(comparator.compare(TestEnum.C, TestEnum.B) > 0);
		assertTrue(comparator.compare(TestEnum.B, TestEnum.A) > 0);
		assertTrue(comparator.compare(TestEnum.A, TestEnum.C) < 0);
	}

}
