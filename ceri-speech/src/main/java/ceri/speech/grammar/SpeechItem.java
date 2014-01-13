package ceri.speech.grammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ceri.common.collection.ImmutableUtil;
import ceri.common.util.StringUtil;

public abstract class SpeechItem<T extends SpeechItem<T>> implements Comparable<T> {
	private final Class<T> cls;
	public final int number;
	public final String name;
	public final List<String> spokenWords;

	protected SpeechItem(Class<T> cls, int number, String name, String... spokenWords) {
		this.cls = cls;
		this.number = number;
		this.name = name;
		this.spokenWords = ImmutableUtil.asList(spokenWords);
	}

	public static <T extends SpeechItem<T>> String createNameRule(Collection<? extends T> items) {
		List<String> words = new ArrayList<>();
		for (T item : items) {
			int number = item.number;
			words.add(number + " {" + number + "}");
			for (String word : item.spokenWords)
				words.add(word + " {" + number + "}");
		}
		return StringUtil.toString("", "", " | ", words);
	}

	public static <T extends SpeechItem<T>> T getByName(String name, Iterable<? extends T> items) {
		for (T item : items)
			if (item.name.equalsIgnoreCase(name)) return item;
		throw new IllegalArgumentException("Input does not exist " + items + ": " + name);
	}

	@Override
	public String toString() {
		return number + ":" + name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!cls.isInstance(obj)) return false;
		return cls.cast(obj).number == number;
	}

	@Override
	public int hashCode() {
		return number;
	}

	@Override
	public int compareTo(T item) {
		return item.number > number ? 1 : item.number < number ? -1 : 0;
	}

}
