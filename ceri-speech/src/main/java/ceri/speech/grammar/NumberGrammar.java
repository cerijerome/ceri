package ceri.speech.grammar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NumberGrammar {
	private static final Map<Integer, String> phraseMap = initPhraseMap();

	public static String loadJsgf() {
		return GrammarUtil.loadJsgfResource(NumberGrammar.class);
	}

	/**
	 * Handles number and digit tags.
	 */
	public static long getLongFromTags(Iterable<String> tags) {
		long value = 0;
		long temp = 0;
		boolean positive = true;
		for (String tag : tags) {
			long l = Long.valueOf(tag);
			if (l == -1) positive = !positive;
			else if (l >= 1000) {
				temp *= l;
				value += temp;
				temp = 0;
			} else if (l == 100) temp *= l;
			else temp += l;
		}
		value += temp;
		return positive ? value : -value;
	}

	public static String numberToWords(int value) {
		if (value > 99) return digitsToWords(value);
		String phrase = phraseMap.get(value);
		if (phrase != null) return phrase;
		int tens = (value / 10) * 10;
		int units = value % 10;
		return phraseMap.get(tens) + " " + phraseMap.get(units);
	}

	public static String digitsToWords(int value) {
		StringBuilder b = new StringBuilder();
		String digits = Integer.toString(value);
		for (char ch : digits.toCharArray()) {
			if (b.length() > 0) b.append(" ");
			b.append(phraseMap.get(ch - '0'));
		}
		return b.toString();
	}

	public static void main(String[] args) {
		int[] values = { 0, 9, 10, 11, 19, 20, 21, 77, 99, 100, 1234 };
		for (int i : values)
			System.out.println(i + ": " + numberToWords(i));
	}

	private static Map<Integer, String> initPhraseMap() {
		Map<Integer, String> map = new HashMap<>();
		map.put(0, "zero");
		map.put(1, "one");
		map.put(2, "two");
		map.put(3, "three");
		map.put(4, "four");
		map.put(5, "five");
		map.put(6, "six");
		map.put(7, "seven");
		map.put(8, "eight");
		map.put(9, "nine");
		map.put(10, "ten");
		map.put(11, "eleven");
		map.put(12, "twelve");
		map.put(13, "thirteen");
		map.put(14, "fourteen");
		map.put(15, "fifteen");
		map.put(16, "sixteen");
		map.put(17, "seventeen");
		map.put(18, "eighteen");
		map.put(19, "nineteen");
		map.put(20, "twenty");
		map.put(30, "thirty");
		map.put(40, "forty");
		map.put(50, "fifty");
		map.put(60, "sixty");
		map.put(70, "seventy");
		map.put(80, "eighty");
		map.put(90, "ninety");
		return Collections.unmodifiableMap(map);
	}

}
