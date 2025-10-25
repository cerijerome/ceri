package ceri.jna.type;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;

public class CLongBehavior {

	@Test
	public void shouldProvideRef() {
		var ref = new CLong.ByRef();
		assertEquals(ref.longValue(), 0L);
		ref = new CLong.ByRef(-1);
		assertEquals(ref.longValue(), -1L);
		ref = new CLong.ByRef(-1L);
		assertEquals(ref.longValue(), -1L);
	}

}
