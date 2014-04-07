package ceri.ci.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import ceri.ci.build.BuildTestUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;

public class MultiProxyBehavior {
	private MultiProxy proxy;
	
	@Before
	public void init() {
		proxy = new MultiProxy(10, "a", "b");
	}

	
	@Test
	public void should() {
	}


}
