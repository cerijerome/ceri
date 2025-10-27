package ceri.jna.type;

import org.junit.Test;
import ceri.common.test.Assert;

public class CUlongBehavior {

	@Test
	public void shouldProvideRef() {
		var ref = new CUlong.ByRef();
		Assert.equal(ref.longValue(), 0L);
		ref = new CUlong.ByRef(-1);
		Assert.equal(ref.longValue(), 0xffffffffL);
		ref = new CUlong.ByRef(-1L);
		Assert.equal(ref.longValue(), -1L);
	}

}
