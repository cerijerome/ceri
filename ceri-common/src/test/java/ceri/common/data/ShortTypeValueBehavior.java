package ceri.common.data;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ShortTypeValueBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		ShortTypeValue<?> v = DataTestType.shortValue(1);
		ShortTypeValue<?> eq0 = DataTestType.shortValue(1);
		ShortTypeValue<?> ne0 = DataTestType.shortValue(0);
		ShortTypeValue<?> ne1 = DataTestType.shortValue(3);
		ShortTypeValue<DataTestType> ne2 = new ShortTypeValue<>(DataTestType.one, null, (short) 0);
		ShortTypeValue<DataTestType> ne3 = new ShortTypeValue<>(DataTestType.one, "one", (short) 1);
		exerciseEquals(v, eq0);
		assertAllNotEqual(v, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldReturnName() {
		assertThat(new ShortTypeValue<>(DataTestType.one, null, (short) 1).name(), is("one"));
		assertThat(new ShortTypeValue<>(DataTestType.one, "ONE", (short) 1).name(), is("one"));
		assertThat(new ShortTypeValue<>(null, "ONE", (short) 1).name(), is("ONE"));
		assertThat(new ShortTypeValue<>(null, null, (short) 1).name(), is("ShortTypeValue"));

	}

}
