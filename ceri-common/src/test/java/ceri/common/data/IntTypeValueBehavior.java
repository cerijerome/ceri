package ceri.common.data;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class IntTypeValueBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		IntTypeValue<?> v = DataTestType.intValue(1);
		IntTypeValue<?> eq0 = DataTestType.intValue(1);
		IntTypeValue<?> ne0 = DataTestType.intValue(0);
		IntTypeValue<?> ne1 = DataTestType.intValue(3);
		IntTypeValue<?> ne2 = new IntTypeValue<>(DataTestType.one, null, 0);
		IntTypeValue<?> ne3 = new IntTypeValue<>(DataTestType.one, "one", 1);
		exerciseEquals(v, eq0);
		assertAllNotEqual(v, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldReturnName() {
		assertThat(new IntTypeValue<>(DataTestType.one, null, 1).name(), is("one"));
		assertThat(new IntTypeValue<>(DataTestType.one, "ONE", 1).name(), is("one"));
		assertThat(new IntTypeValue<>(null, "ONE", 1).name(), is("ONE"));
		assertThat(new IntTypeValue<>(null, null, 1).name(), is("IntTypeValue"));

	}

}
