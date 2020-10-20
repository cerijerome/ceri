package ceri.ci.web;

import static ceri.common.test.AssertUtil.assertNotNull;
import static ceri.common.test.AssertUtil.assertNull;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import ceri.common.property.BaseProperties;

public class WebContainerBehavior {
	private Properties properties;

	@Before
	public void init() {
		properties = new Properties();
	}

	@Test
	public void shouldCreateAlerterIfPropertyIsEnabled() {
		properties.put("web.enabled", "true");
		WebContainer container = new WebContainer(baseProperties());
		assertNotNull(container.alerter);
	}

	@Test
	public void shouldNotCreateAlerterIfPropertyIsMissingOrDisabled() {
		WebContainer container = new WebContainer(baseProperties());
		assertNull(container.alerter);
		properties.put("web.enabled", "false");
		container = new WebContainer(baseProperties());
		assertNull(container.alerter);
	}

	private BaseProperties baseProperties() {
		return new BaseProperties(properties) {};
	}

}
