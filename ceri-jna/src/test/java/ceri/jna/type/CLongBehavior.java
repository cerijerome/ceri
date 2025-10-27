package ceri.jna.type;

import org.junit.Test;
import ceri.common.test.Assert;

public class CLongBehavior {

	@Test
	public void shouldProvideRef() {
		var ref = new CLong.ByRef();
		Assert.equal(ref.longValue(), 0L);
		ref = new CLong.ByRef(-1);
		Assert.equal(ref.longValue(), -1L);
		ref = new CLong.ByRef(-1L);
		Assert.equal(ref.longValue(), -1L);
	}

}
