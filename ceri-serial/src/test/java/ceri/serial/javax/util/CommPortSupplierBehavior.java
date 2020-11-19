package ceri.serial.javax.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import java.io.IOException;
import org.junit.Test;

public class CommPortSupplierBehavior {

	@Test
	public void testFixed() throws IOException {
		assertNull(CommPortSupplier.fixed(null));
		assertEquals(CommPortSupplier.fixed("com0").get(), "com0");
	}

}
