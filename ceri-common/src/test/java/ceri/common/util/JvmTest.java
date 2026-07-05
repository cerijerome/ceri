package ceri.common.util;

import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.test.Assert;

public class JvmTest {

	@Test
	public void testMemory() {
		var m = Jvm.memory();
		Assert.equal(m.diff(null), m);
		Assert.equal(m.used(), m.total() - m.available());
		Assert.equal(m.free(), m.max() + m.available() - m.total());
	}

	@Test
	public void testMemoryTracker() {
		var t = Jvm.trackMemory();
		Assert.find(t.report(), "used=\\d+ free=\\d+");
		System.gc();
		t.update();
		// failed with + and - once; why?
		Assert.find(t.report(), "used=\\d+\\-\\d+ free=\\d+\\+\\d+");
	}

	@Test
	public void testArgs() {
		var p = Pattern.compile("-.+");
		for (var arg : Jvm.args())
			Assert.match(arg, p);
	}

}
