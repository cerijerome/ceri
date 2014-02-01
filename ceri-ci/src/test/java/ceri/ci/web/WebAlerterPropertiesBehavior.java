package ceri.ci.web;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;

public class WebAlerterPropertiesBehavior {
	private static Properties props = new Properties();
	
	@BeforeClass
	public static void init() {
		props.put("x.enabled", "true");
	}
	
	@Test
	public void shouldReadValuesWithPrefix() {
		WebAlerterProperties web = new WebAlerterProperties(props, "x");
		assertThat(web.enabled(), is(true));
	}

}
