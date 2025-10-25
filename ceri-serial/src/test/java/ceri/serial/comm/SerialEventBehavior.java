package ceri.serial.comm;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;

public class SerialEventBehavior {

	@Test
	public void shouldLookUpById() {
		assertEquals(SerialEvent.from(5), SerialEvent.ringIndicator);
	}

}
