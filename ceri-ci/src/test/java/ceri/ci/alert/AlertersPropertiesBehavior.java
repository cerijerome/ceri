package ceri.ci.alert;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;

public class AlertersPropertiesBehavior {
	private static Properties props = new Properties();
	
	@BeforeClass
	public static void init() {
		props.put("x.x10.enabled", "true");
		props.put("x.zwave.enabled", "false");
		props.put("x.web.enabled", "true");
		props.put("x.audio.enabled", "false");
		props.put("x10.enabled", "false");
		props.put("zwave.enabled", "true");
		props.put("web.enabled", "false");
		props.put("audio.enabled", "true");
	}
	
	@Test
	public void shouldReadValuesWithPrefix() {
		AlertersProperties alerters = new AlertersProperties(props, "x");
		assertTrue(alerters.x10Enabled());
		assertFalse(alerters.zwaveEnabled());
		assertTrue(alerters.webEnabled());
		assertFalse(alerters.audioEnabled());
	}

	@Test
	public void shouldReadValuesWithoutPrefix() {
		AlertersProperties alerters = new AlertersProperties(props);
		assertFalse(alerters.x10Enabled());
		assertTrue(alerters.zwaveEnabled());
		assertFalse(alerters.webEnabled());
		assertTrue(alerters.audioEnabled());
	}

}
