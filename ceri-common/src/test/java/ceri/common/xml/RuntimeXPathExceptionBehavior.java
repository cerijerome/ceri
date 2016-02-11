package ceri.common.xml;

import org.junit.Test;

public class RuntimeXPathExceptionBehavior {

	@Test
	public void codeCoverage() {
		RuntimeXPathException e = new RuntimeXPathException("test");
		e = new RuntimeXPathException("test", e);
	}
	
}
