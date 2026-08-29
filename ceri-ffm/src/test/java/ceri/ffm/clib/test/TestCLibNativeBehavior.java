package ceri.ffm.clib.test;

import org.junit.Test;
import ceri.common.test.Assert;

public class TestCLibNativeBehavior {

	@Test
	public void shouldWrapSigSet() {
		var mask = new TestCLibNative.SigSet.Mask(1);
		Assert.yes(mask.has(1));
		Assert.no(mask.has(0));
		TestCLibNative.SigSet.write(mask, null); // allowed
	}
}
