package ceri.common.time;

import org.junit.Test;
import ceri.common.test.Assert;

public class TimeSupplierBehavior {

	@Test
	public void shouldDelay() {
		TimeSupplier.nanos.delay(1);
		TimeSupplier.micros.delay(1);
		TimeSupplier.seconds.delay(0);
	}

	@Test
	public void shouldProvideTimeUnitSymbol() {
		Assert.equal(TimeSupplier.millis.symbol(), "ms");
		Assert.equal(TimeSupplier.nanos.symbol(), "ns");
	}

}
