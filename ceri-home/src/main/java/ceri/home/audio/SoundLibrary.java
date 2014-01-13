package ceri.home.audio;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoundLibrary<T> {
	private final Map<T, Audio> cache = new HashMap<>();
	private final SoundPlayer player;
	
	private SoundLibrary(SoundPlayer player) {
		this.player = player;
	}
	
	public static <T> SoundLibrary<T> create(SoundPlayer player) {
		return new SoundLibrary<>(player);
	}
	
	public void add(T t, Audio audio) {
		cache.put(t, audio);
	}
	
	public void play(T t) throws IOException {
		Audio audio = cache.get(t);
		if (audio == null)
			throw new IllegalArgumentException(t + " not found");
		player.play(audio);
	}
	
}
