package ceri.common.xml;

import org.junit.Test;
import ceri.common.test.Assert;

public class RuntimeXPathExceptionBehavior {

	@Test
	public void codeCoverage() {
		var e = new RuntimeXPathException("test");
		e = new RuntimeXPathException("test", e);
		Assert.notNull(e);
	}
}
