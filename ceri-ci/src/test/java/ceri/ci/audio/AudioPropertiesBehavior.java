package ceri.ci.audio;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.property.BaseProperties;

public class AudioPropertiesBehavior {
	private static Properties props = new Properties();

	@BeforeClass
	public static void init() {
		props.put("x.enabled", "true");
		props.put("x.pitch", "1.2");
		props.put("x.voice", "a/b/c");
	}

	@Test
	public void shouldReadValuesWithPrefix() {
		AudioProperties audio = new AudioProperties(new BaseProperties(props) {}, "x");
		assertTrue(audio.enabled());
		assertEquals(audio.pitch(), 1.2f);
		assertEquals(audio.voice(), "a/b/c");
	}

}
