package ceri.home.audio;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundPlayer implements Closeable {
	private AudioPlayer audioPlayer = null;

	public static void main(String[] args) throws Exception {
		File root =
			new File("C:/Documents and Settings/cjerome/"
				+ "My Documents/workspace/home/data/speech/");
		File[] files = new File[21];
		try (SoundPlayer player = new SoundPlayer()) {
			for (int i = 0; i < files.length; i++) {
				files[i] = new File(root, "numbers/" + i + ".wav");
				player.playFiles(files[i]);
			}
			player.playFiles(files);
		}
	}

	public void playFilesInDir(File dir, String... filenames) throws IOException {
		File[] files = new File[filenames.length];
		int i = 0;
		for (String filename : filenames)
			files[i++] = new File(dir, filename);
		playFiles(files);
	}

	public void playFiles(File... files) throws IOException {
		try {
			for (File file : files) {
				try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
					Audio audio = Audio.createFromStream(in);
					play(audio);
				}
			}
		} catch (UnsupportedAudioFileException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void close() {
		if (audioPlayer != null) audioPlayer.close();
		audioPlayer = null;
	}

	public void play(AudioRef... audioRefs) throws IOException {
		for (AudioRef audioRef : audioRefs)
			playAudio(audioRef.getAudio());
	}

	public void play(Iterable<? extends AudioRef> audioRefs) throws IOException {
		for (AudioRef audioRef : audioRefs)
			playAudio(audioRef.getAudio());
	}

	private void playAudio(Audio audio) throws IOException {
		try {
			AudioFormat format = audio.getFormat();
			if (audioPlayer != null && !audioPlayer.matches(format)) close();
			if (audioPlayer == null) audioPlayer = new AudioPlayer(format);
			audioPlayer.play(audio.getData());
		} catch (LineUnavailableException e) {
			throw new IOException(e);
		}
	}

}
