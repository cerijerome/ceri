package ceri.common.time;

import org.junit.Test;

public class TimeSupplierBehavior {

	@Test
	public void shouldDelay() {
		TimeSupplier.nanos.delay(1);
		TimeSupplier.micros.delay(1);
		TimeSupplier.seconds.delay(0);
	}

}
