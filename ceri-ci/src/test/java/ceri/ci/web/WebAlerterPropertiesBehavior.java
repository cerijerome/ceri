package ceri.ci.web;

import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.property.BaseProperties;

public class WebAlerterPropertiesBehavior {
	private static Properties props = new Properties();

	@BeforeClass
	public static void init() {
		props.put("x.enabled", "true");
	}

	@Test
	public void shouldReadValuesWithPrefix() {
		WebAlerterProperties web = new WebAlerterProperties(new BaseProperties(props) {}, "x");
		assertThat(web.enabled(), is(true));
	}

}
