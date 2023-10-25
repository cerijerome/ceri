package ceri.serial.comm.util;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;

public class PortSupplierBehavior {

	@Test
	public void testFixed() throws IOException {
		assertEquals(PortSupplier.fixed("test").get(), "test");
		assertEquals(PortSupplier.fixed(null), null);
	}

}
