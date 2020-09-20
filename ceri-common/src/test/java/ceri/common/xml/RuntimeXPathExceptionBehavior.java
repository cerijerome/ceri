package ceri.common.xml;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class RuntimeXPathExceptionBehavior {

	@Test
	public void codeCoverage() {
		RuntimeXPathException e = new RuntimeXPathException("test");
		e = new RuntimeXPathException("test", e);
		assertNotNull(e);
	}

}
