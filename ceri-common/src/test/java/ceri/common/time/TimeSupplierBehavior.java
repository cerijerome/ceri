package ceri.common.time;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;

public class TimeSupplierBehavior {

	@Test
	public void shouldDelay() {
		TimeSupplier.nanos.delay(1);
		TimeSupplier.micros.delay(1);
		TimeSupplier.seconds.delay(0);
	}

	@Test
	public void shouldProvideTimeUnitSymbol() {
		assertEquals(TimeSupplier.millis.symbol(), "ms");
		assertEquals(TimeSupplier.nanos.symbol(), "ns");
	}

}
