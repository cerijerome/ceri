package ceri.ci.audio;

import static ceri.common.test.TestUtil.assertNotNull;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.io.ResourcePath;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;

public class AudioBehavior {
	private static ResourcePath resourcePath;
	private static Path testFile;

	@BeforeClass
	public static void initClass() throws IOException {
		resourcePath = ResourcePath.of(AudioBehavior.class, "test.wav");
		testFile = resourcePath.path();
	}

	@AfterClass
	public static void closeClass() throws IOException {
		resourcePath.close();
	}

	@Test
	public void shouldClipToANewAudioObject() throws IOException {
		Audio audio = Audio.create(testFile);
		Audio audio2 = audio.clip(0, 16);
		assertThat(audio, not(audio2));
	}

	@Test
	public void shouldFailIfClipLimitsAreNotWithinDataRange() throws IOException {
		Audio audio = Audio.create(testFile);
		TestUtil.assertThrown(() -> audio.clip(11000, 11000));
		Audio audio2 = audio.clip(0, 16);
		assertThat(audio, not(audio2));
	}

	@Test
	public void shouldOnlyChangePitchIfNotNormalPitch() throws IOException {
		Audio audio = Audio.create(testFile);
		Audio audio2 = audio.changePitch(1.0f);
		Audio audio3 = audio.changePitch(2.0f);
		assertThat(audio, is(audio2));
		assertThat(audio, not(audio3));
	}

	@Test
	public void shouldNotThrowConstructorExceptionsForValidData() throws IOException {
		byte[] data = Files.readAllBytes(testFile);
		Audio audio = Audio.create(testFile);
		assertNotNull(audio);
		audio = Audio.create(data);
		assertNotNull(audio);
	}

	@Test(expected = IOException.class)
	public void shouldFailForInvalidData() throws IOException {
		try (FileTestHelper helper =
			FileTestHelper.builder().file("bad.wav", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx").build()) {
			Audio.create(helper.path("bad.wav"));
		}
	}

	@Test(expected = IOException.class)
	public void shouldThrowIOExceptionWhenLineIsUnavailable() throws IOException {
		Audio audio = Audio.create(testFile);
		audio = new Audio(null, new byte[0]) {
			@Override
			SourceDataLine getSourceDataLine(AudioFormat format) throws LineUnavailableException {
				throw new LineUnavailableException();
			}
		};
		audio.play();
	}

	@Test
	public void shouldPlayAudioData() throws IOException {
		final SourceDataLine line = Mockito.mock(SourceDataLine.class);
		Audio audio = Audio.create(testFile);
		audio = new Audio(null, new byte[0]) {
			@Override
			SourceDataLine getSourceDataLine(AudioFormat format) {
				return line;
			}
		};
		audio.play();
	}

}
