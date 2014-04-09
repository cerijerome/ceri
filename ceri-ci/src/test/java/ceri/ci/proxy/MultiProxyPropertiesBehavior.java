package ceri.ci.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiProxyPropertiesBehavior {
	private static Properties props = new Properties();
	
	@BeforeClass
	public static void init() {
		props.put("x.enabled", "true");
		props.put("x.targets", " a,  b, c ");
		props.put("x.threads", "99");
	}
	
	@Test
	public void shouldReadValuesWithPrefix() {
		MultiProxyProperties proxy = new MultiProxyProperties(props, "x");
		assertThat(proxy.enabled(), is(true));
		assertThat(proxy.proxyTargets(), is(Arrays.asList("a", "b", "c")));
		assertThat(proxy.threads(), is(99));
	}

}
