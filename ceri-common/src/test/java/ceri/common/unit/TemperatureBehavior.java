package ceri.common.unit;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.unit.Temperature.ZERO_C;
import static ceri.common.unit.Temperature.ZERO_F;
import static ceri.common.unit.Temperature.ZERO_K;
import static ceri.common.unit.Temperature.ZERO_R;
import static ceri.common.unit.Temperature.Scale.celsius;
import static ceri.common.unit.Temperature.Scale.fahrenheit;
import static ceri.common.unit.Temperature.Scale.kelvin;
import static ceri.common.unit.Temperature.Scale.rankine;
import org.junit.Test;

public class TemperatureBehavior {

	@Test
	public void shouldProvideZeroconstants() {
		assertEquals(ZERO_C.to(kelvin).value(), 273.15);
		assertEquals(ZERO_K.to(celsius).value(), -273.15);
		assertEquals(ZERO_F.to(rankine).value(), 459.67);
		assertEquals(ZERO_R.to(fahrenheit).value(), -459.67);
	}

	@Test
	public void shouldConvertTemperatureScales() {
		assertApprox(celsius.to(celsius, 99), 99);
		assertApprox(celsius.to(fahrenheit, 99), 210.2);
		assertApprox(celsius.to(kelvin, 99), 372.15);
		assertApprox(celsius.to(rankine, 99), 669.87);
		assertApprox(fahrenheit.to(celsius, 99), 37.222);
		assertApprox(fahrenheit.to(fahrenheit, 99), 99);
		assertApprox(fahrenheit.to(kelvin, 99), 310.372);
		assertApprox(fahrenheit.to(rankine, 99), 558.67);
		assertApprox(kelvin.to(celsius, 99), -174.15);
		assertApprox(kelvin.to(fahrenheit, 99), -281.47);
		assertApprox(kelvin.to(kelvin, 99), 99);
		assertApprox(kelvin.to(rankine, 99), 178.2);
		assertApprox(rankine.to(celsius, 99), -218.15);
		assertApprox(rankine.to(fahrenheit, 99), -360.67);
		assertApprox(rankine.to(kelvin, 99), 55);
		assertApprox(rankine.to(rankine, 99), 99);
		assertThrown(() -> celsius.to(null, 99));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertString(celsius.temperature(50), "50.0\u00b0C");
	}

}
