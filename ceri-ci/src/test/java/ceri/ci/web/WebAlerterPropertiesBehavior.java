package ceri.ci.web;

import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;

public class WebAlerterPropertiesBehavior {
	private static Properties props = new Properties();
	
	@BeforeClass
	public static void init() {
		props.put("x.key", "aaa");
		props.put("key", "xxx");
	}
	
	@Test
	public void shouldReadValuesWithPrefix() {
		WebAlerterProperties web = new WebAlerterProperties(props, "x");
	}

	@Test
	public void shouldReadValuesWithoutPrefix() {
		WebAlerterProperties web = new WebAlerterProperties(props);
	}

}
