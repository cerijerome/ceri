package ceri.common.comparator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Comparator;
import org.junit.Test;

public class EnumComparatorsTest {
	private static enum TestEnum {
		C, B, A
	};
	
	@Test
	public void testByOrdinal() {
		Comparator<TestEnum> comparator = EnumComparators.byOrdinal();
		assertThat(comparator.compare(TestEnum.C, TestEnum.B) < 0, is(true));
		assertThat(comparator.compare(TestEnum.B, TestEnum.A) < 0, is(true));
		assertThat(comparator.compare(TestEnum.A, TestEnum.C) > 0, is(true));
	}

	@Test
	public void testByName() {
		Comparator<TestEnum> comparator = EnumComparators.byName();
		assertThat(comparator.compare(TestEnum.C, TestEnum.B) > 0, is(true));
		assertThat(comparator.compare(TestEnum.B, TestEnum.A) > 0, is(true));
		assertThat(comparator.compare(TestEnum.A, TestEnum.C) < 0, is(true));
	}

}
