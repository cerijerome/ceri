package ceri.jna.type;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;

public class CUlongBehavior {

	@Test
	public void shouldProvideRef() {
		var ref = new CUlong.ByRef();
		assertEquals(ref.longValue(), 0L);
		ref = new CUlong.ByRef(-1);
		assertEquals(ref.longValue(), 0xffffffffL);
		ref = new CUlong.ByRef(-1L);
		assertEquals(ref.longValue(), -1L);
	}

}
