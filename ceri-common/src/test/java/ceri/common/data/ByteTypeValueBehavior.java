package ceri.common.data;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ByteTypeValueBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		ByteTypeValue<?> v = DataTestType.byteValue(1);
		ByteTypeValue<?> eq0 = DataTestType.byteValue(1);
		ByteTypeValue<?> ne0 = DataTestType.byteValue(0);
		ByteTypeValue<?> ne1 = DataTestType.byteValue(3);
		ByteTypeValue<?> ne2 = new ByteTypeValue<>(DataTestType.one, null, (byte) 0);
		ByteTypeValue<?> ne3 = new ByteTypeValue<>(DataTestType.one, "one", (byte) 1);
		exerciseEquals(v, eq0);
		assertAllNotEqual(v, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldReturnName() {
		assertThat(new ByteTypeValue<>(DataTestType.one, null, (byte) 1).name(), is("one"));
		assertThat(new ByteTypeValue<>(DataTestType.one, "ONE", (byte) 1).name(), is("one"));
		assertThat(new ByteTypeValue<>(null, "ONE", (byte) 1).name(), is("ONE"));
		assertThat(new ByteTypeValue<>(null, null, (byte) 1).name(), is("ByteTypeValue"));

	}

}
