package ceri.speech.grammar;

import java.io.IOException;
import java.util.List;
import ceri.common.io.IoUtil;

public class GrammarUtil {

	private GrammarUtil() {}

	public static boolean matches(List<String> words, String... expected) {
		if (words.size() != expected.length) return false;
		for (int i = 0; i < expected.length; i++) {
			if (!expected[i].equalsIgnoreCase(words.get(i))) return false;
		}
		return true;
	}

	public static String loadJsgfResource(Class<?> cls) {
		try {
			return IoUtil.getClassResourceString(cls, "jsgf");
		} catch (IOException e) {
			throw new IllegalStateException("Unable to load resource", e);
		}
	}

}
