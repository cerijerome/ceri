package ceri.common.xml;

import static ceri.common.test.AssertUtil.assertNotNull;
import org.junit.Test;

public class RuntimeXPathExceptionBehavior {

	@Test
	public void codeCoverage() {
		RuntimeXPathException e = new RuntimeXPathException("test");
		e = new RuntimeXPathException("test", e);
		assertNotNull(e);
	}

}
