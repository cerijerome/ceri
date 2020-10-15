package ceri.common.comparator;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
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
		assertThat(comparator.compare(TestEnum.C, TestEnum.B) < 0, is(true));
		assertThat(comparator.compare(TestEnum.B, TestEnum.A) < 0, is(true));
		assertThat(comparator.compare(TestEnum.A, TestEnum.C) > 0, is(true));
	}

	@Test
	public void testByName() {
		Comparator<TestEnum> comparator = EnumComparators.name();
		assertThat(comparator.compare(TestEnum.C, TestEnum.B) > 0, is(true));
		assertThat(comparator.compare(TestEnum.B, TestEnum.A) > 0, is(true));
		assertThat(comparator.compare(TestEnum.A, TestEnum.C) < 0, is(true));
	}

}
