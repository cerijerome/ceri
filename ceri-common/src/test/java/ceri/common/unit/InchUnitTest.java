package ceri.common.unit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class InchUnitTest {

	@Test(expected = IllegalArgumentException.class)
	public void testBadHeightFromString() {
		InchUnit.heightFromString("ab");
	}
	
	@Test
	public void testHeightFromString() {
		NormalizedValue<InchUnit> n = InchUnit.heightFromString("10'");
		assertThat(n.value, is(120L));
		assertThat(n.value(InchUnit.inch), is(0L));
		assertThat(n.value(InchUnit.foot), is(10L));
		assertThat(n.value(InchUnit.yard), is(0L));

		n = InchUnit.heightFromString("1'11");
		assertThat(n.value, is(23L));
		assertThat(n.value(InchUnit.inch), is(11L));
		assertThat(n.value(InchUnit.foot), is(1L));
		assertThat(n.value(InchUnit.yard), is(0L));
		
		n = InchUnit.heightFromString("2'0\"");
		assertThat(n.value, is(24L));
		assertThat(n.value(InchUnit.inch), is(0L));
		assertThat(n.value(InchUnit.foot), is(2L));
		assertThat(n.value(InchUnit.yard), is(0L));
		
		n = InchUnit.heightFromString("20\"");
		assertThat(n.value, is(20L));
		assertThat(n.value(InchUnit.inch), is(8L));
		assertThat(n.value(InchUnit.foot), is(1L));
		assertThat(n.value(InchUnit.yard), is(0L));

		n = InchUnit.heightFromString("50");
		assertThat(n.value, is(50L));
		assertThat(n.value(InchUnit.inch), is(2L));
		assertThat(n.value(InchUnit.foot), is(4L));
		assertThat(n.value(InchUnit.yard), is(0L));
	}
	
	@Test
	public void testNormalizeHeight() {
		NormalizedValue<InchUnit> n = InchUnit.normalizeHeight(100);
		assertThat(n.value, is(100L));
		assertThat(n.value(InchUnit.inch), is(4L));
		assertThat(n.value(InchUnit.foot), is(8L));
		assertThat(n.value(InchUnit.yard), is(0L));
	}

	@Test
	public void testNormalize() {
		NormalizedValue<InchUnit> n = InchUnit.normalize(InchUnit.mile.inches * 2 - 1);
		assertThat(n.value, is(126719L));
		assertThat(n.value(InchUnit.inch), is(11L));
		assertThat(n.value(InchUnit.foot), is(2L));
		assertThat(n.value(InchUnit.yard), is(1759L));
		assertThat(n.value(InchUnit.mile), is(1L));
	}
	
}
