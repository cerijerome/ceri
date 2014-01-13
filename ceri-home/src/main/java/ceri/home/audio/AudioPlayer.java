package ceri.home.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer extends Thread {
	private final AudioFormat audioFormat;
	private final SourceDataLine out;

	public AudioPlayer(AudioFormat audioFormat)
		throws LineUnavailableException {
		this.audioFormat = audioFormat;
		out = AudioSystem.getSourceDataLine(audioFormat);
		out.open();
		out.start();
	}

	public boolean matches(AudioFormat audioFormat) {
		return this.audioFormat.matches(audioFormat);
	}

	public void play(byte[] bytes) {
		out.write(bytes, 0, bytes.length);
		out.drain();
		out.flush();
	}

	public void close() {
		out.close();
	}

}
