package ceri.ci.audio;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import ceri.common.io.IoUtil;

public class Audio {
	private final AudioFormat format;
	private final byte[] data;

	public Audio(AudioFormat format, byte[] data) {
		this.format = format;
		this.data = data;
	}

	public static Audio create(File file) throws IOException {
		try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
			AudioFormat format = in.getFormat();
			byte[] data = IoUtil.getContent(in, 0);
			return new Audio(format, data);
		} catch (UnsupportedAudioFileException e) {
			throw new IOException(e);
		}
	}

	public void play() throws IOException {
		try (SourceDataLine out = AudioSystem.getSourceDataLine(format)) {
			out.open();
			out.start();
			out.write(data, 0, data.length);
			out.drain();
			out.flush();
		} catch (LineUnavailableException e) {
			throw new IOException(e);
		}
	}

}

