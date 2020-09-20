package ceri.ci.audio;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
		assertThat(audio.enabled(), is(true));
		assertThat(audio.pitch(), is(1.2f));
		assertThat(audio.voice(), is("a/b/c"));
	}

}
