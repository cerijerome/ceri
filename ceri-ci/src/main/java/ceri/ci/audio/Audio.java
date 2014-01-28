package ceri.ci.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

	public static class Builder {
		float pitch = 1.0f;
		AudioFormat format;
		byte[] data;

		public Builder(byte[] data) throws IOException {
			try (InputStream in = new ByteArrayInputStream(data)) {
				setFromInputStream(in);
			}
		}
		
		public Builder(File file) throws IOException {
			try (InputStream in = new FileInputStream(file)) {
				setFromInputStream(in);
			}
		}
		
		public Builder(InputStream in) throws IOException {
			setFromInputStream(in);
		}
		
		public Builder pitch(float pitch) {
			this.pitch = pitch;
			return this;
		}
		
		private void setFromInputStream(InputStream is) throws IOException {
			try (AudioInputStream in = AudioSystem.getAudioInputStream(is)) {
				format = in.getFormat();
				data = IoUtil.getContent(in, 0);
			} catch (UnsupportedAudioFileException e) {
				throw new IOException(e);
			}
		}
		
		public Audio build() {
			AudioFormat f = pitch == 1.0f ? format : adjustPitch(format, pitch);
			return new Audio(f, data);
		}
	}

	Audio(AudioFormat format, byte[] data) {
		this.format = format;
		this.data = data;
	}

	static AudioFormat adjustPitch(AudioFormat format, float pitch) {
		return new AudioFormat(format.getEncoding(), format.getSampleRate() * pitch, format
			.getSampleSizeInBits(), format.getChannels(), format.getFrameSize(), format
			.getFrameRate(), format.isBigEndian());
	}

	public static Audio create(byte[] data) throws IOException {
		return new Builder(data).build();
	}

	public static Audio create(File file) throws IOException {
		return new Builder(file).build();
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
