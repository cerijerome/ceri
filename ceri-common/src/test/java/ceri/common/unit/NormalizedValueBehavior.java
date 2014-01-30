package ceri.common.unit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import org.junit.Test;

public class NormalizedValueBehavior {

	private static enum EnumUnit implements Unit {
		_3(3),
		_33(33),
		_999(999),
		_i(Integer.MAX_VALUE),
		_l(Long.MAX_VALUE);

		private final long value;

		private EnumUnit(long value) {
			this.value = value;
		}

		@Override
		public long units() {
			return value;
		}
	}

	@Test
	public void shouldMaintainValueIfNoUnitsSpecified() {
		NormalizedValue<?> n = NormalizedValue.create(100, Collections.<EnumUnit>emptySet());
		assertThat(n.value, is(100L));
		n = NormalizedValue.create(Long.MAX_VALUE, Collections.<Unit>emptySet());
		assertThat(n.value, is(Long.MAX_VALUE));
		n = NormalizedValue.builder(Collections.<EnumUnit>emptySet()).value(Long.MAX_VALUE).build();
		assertThat(n.value, is(Long.MAX_VALUE));
	}

	@Test
	public void shouldBuildCorrectValueFromUnits() {
		NormalizedValue<EnumUnit> n =
			NormalizedValue.builder(EnumUnit.class).value(33).value(11, EnumUnit._3).value(1,
				EnumUnit._33).build();
		assertThat(n.value, is(99L));
		assertThat(n.value(EnumUnit._3), is(0L));
		assertThat(n.value(EnumUnit._33), is(3L));
		assertThat(n.value(EnumUnit._999), is(0L));
	}

	@Test
	public void shouldNormalizeValuesBasedOnGivenUnits() {
		NormalizedValue<EnumUnit> n = NormalizedValue.create(99L, EnumUnit._3, EnumUnit._999);
		assertThat(n.value, is(99L));
		assertThat(n.value(EnumUnit._3), is(33L));
		assertThat(n.value(EnumUnit._33), is(0L));
		assertThat(n.value(EnumUnit._999), is(0L));
	}

	@Test
	public void shouldBeEqualForSameValueAndUnits() {
		NormalizedValue<EnumUnit> n1 = NormalizedValue.create(99L, EnumUnit._3, EnumUnit._999);
		NormalizedValue<EnumUnit> n2 =
			NormalizedValue.builder(EnumUnit._3, EnumUnit._999).value(33).value(66).build();
		assertThat(n1.value, is(n2.value));
		assertThat(n1.hashCode(), is(n2.hashCode()));
		assertThat(n1, is(n2));
		assertThat(n1.toString(), is(n2.toString()));
	}

	@Test
	public void shouldNotBeEqualForSameValueButDifferentUnits() {
		NormalizedValue<EnumUnit> n1 = NormalizedValue.create(99L, EnumUnit._3, EnumUnit._999);
		NormalizedValue<EnumUnit> n2 = NormalizedValue.create(99L, EnumUnit.class);
		assertThat(n1.value, is(n2.value));
		assertThat(n1, is(not(n2)));
	}

	@Test
	public void shouldConformToEqualsContract() {
		NormalizedValue<EnumUnit> n1 = NormalizedValue.create(100L, EnumUnit._3, EnumUnit._33);
		NormalizedValue<EnumUnit> n2 = NormalizedValue.create(101L, EnumUnit._3, EnumUnit._33);
		assertTrue(n1.equals(n1));
		assertFalse(n1.equals(null));
		assertFalse(n1.equals(""));
		assertFalse(n1.equals(n2));
	}

}
