package ceri.serial.comm;

import org.junit.jupiter.api.Test;
import ceri.common.test.Assert;

public class SerialEventBehavior {

	@Test
	public void shouldLookUpById() {
		Assert.equal(SerialEvent.from(5), SerialEvent.ringIndicator);
	}

}
