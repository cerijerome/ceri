package ceri.common.unit;

import static ceri.common.unit.Temperature.ZERO_C;
import static ceri.common.unit.Temperature.ZERO_F;
import static ceri.common.unit.Temperature.ZERO_K;
import static ceri.common.unit.Temperature.ZERO_R;
import static ceri.common.unit.Temperature.Scale.celsius;
import static ceri.common.unit.Temperature.Scale.fahrenheit;
import static ceri.common.unit.Temperature.Scale.kelvin;
import static ceri.common.unit.Temperature.Scale.rankine;
import org.junit.Test;
import ceri.common.test.Assert;

public class TemperatureBehavior {

	@Test
	public void shouldProvideZeroconstants() {
		Assert.equal(ZERO_C.to(kelvin).value(), 273.15);
		Assert.equal(ZERO_K.to(celsius).value(), -273.15);
		Assert.equal(ZERO_F.to(rankine).value(), 459.67);
		Assert.equal(ZERO_R.to(fahrenheit).value(), -459.67);
	}

	@Test
	public void shouldConvertTemperatureScales() {
		Assert.approx(celsius.to(celsius, 99), 99);
		Assert.approx(celsius.to(fahrenheit, 99), 210.2);
		Assert.approx(celsius.to(kelvin, 99), 372.15);
		Assert.approx(celsius.to(rankine, 99), 669.87);
		Assert.approx(fahrenheit.to(celsius, 99), 37.222);
		Assert.approx(fahrenheit.to(fahrenheit, 99), 99);
		Assert.approx(fahrenheit.to(kelvin, 99), 310.372);
		Assert.approx(fahrenheit.to(rankine, 99), 558.67);
		Assert.approx(kelvin.to(celsius, 99), -174.15);
		Assert.approx(kelvin.to(fahrenheit, 99), -281.47);
		Assert.approx(kelvin.to(kelvin, 99), 99);
		Assert.approx(kelvin.to(rankine, 99), 178.2);
		Assert.approx(rankine.to(celsius, 99), -218.15);
		Assert.approx(rankine.to(fahrenheit, 99), -360.67);
		Assert.approx(rankine.to(kelvin, 99), 55);
		Assert.approx(rankine.to(rankine, 99), 99);
		Assert.thrown(() -> celsius.to(null, 99));
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.string(celsius.temperature(50), "50.0\u00b0C");
	}
}
