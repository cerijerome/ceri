package ceri.speech.synthesis.custom;

import java.util.ArrayList;
import java.util.List;

public enum Numbers {
	INSTANCE;

	private static final long MAX_LONG = 999999999999999999L;

	public List<String> getDigitsAsWords(long value) {
		char[] chars = Long.toString(value).toCharArray();
		List<String> parts = new ArrayList<>(chars.length);
		for (char ch : chars)
			parts.add(String.valueOf(ch));
		return parts;
	}

	public List<String> getOrdinalAsWords(long value) {
		List<String> parts = getLongAsWords(value);
		String lastPart = parts.remove(parts.size() - 1);
		if (lastPart.equals("1")) lastPart += "st";
		else if (lastPart.equals("2")) lastPart += "nd";
		else if (lastPart.equals("3")) lastPart += "rd";
		else lastPart += "th";
		parts.add(lastPart);
		return parts;
	}

	public List<String> getLongAsWords(long value) {
		if (value > MAX_LONG) throw new IllegalArgumentException("Number is greater than " +
			MAX_LONG);
		if (value < -MAX_LONG) throw new IllegalArgumentException("Number is less than " +
			-MAX_LONG);
		List<String> parts = new ArrayList<>();
		if (value < 0) {
			addLong(parts, -value);
			parts.add(0, "minus");
			return parts;
		}
		if (value == 0) {
			parts.add("0");
			return parts;
		}
		long currentValue = value;
		StringBuilder b = new StringBuilder();
		List<String> subParts = new ArrayList<>(5);
		while (currentValue > 0) {
			int hundreds = (int) (currentValue % 1000);
			currentValue /= 1000;
			if (hundreds > 0) {
				addHundreds(subParts, hundreds, currentValue != 0);
				if (b.length() > 0) subParts.add(b.toString());
				parts.addAll(0, subParts);
			}
			subParts.clear();
			b.append("000");
		}
		return parts;
	}

	private void addLong(List<String> parts, long value) {
		if (value > MAX_LONG) throw new IllegalArgumentException("Number is bigger than " +
			MAX_LONG);
		if (value == 0) {
			parts.add("0");
			return;
		}
		long currentValue = value;
		StringBuilder b = new StringBuilder();
		List<String> subParts = new ArrayList<>(5);
		while (currentValue > 0) {
			int hundreds = (int) (currentValue % 1000);
			currentValue /= 1000;
			if (hundreds > 0) {
				addHundreds(subParts, hundreds, currentValue != 0);
				if (b.length() > 0) subParts.add(b.toString());
				parts.addAll(0, subParts);
			}
			subParts.clear();
			b.append("000");
		}
	}

	private void addHundreds(List<String> parts, int value, boolean hasMore) {
		if (value > 999) throw new IllegalArgumentException("Number is > 999: " + value);
		if (value == 0) return;
		int hundreds = (value / 100);
		int remainder = value % 100;

		if (hundreds > 0) {
			parts.add(Integer.toString(hundreds));
			parts.add("00");
			hasMore = true;
		}
		if (remainder == 0) return;
		if (hasMore) parts.add("and");
		addTens(parts, remainder);
	}

	private void addTens(List<String> parts, int value) {
		if (value > 99) throw new IllegalArgumentException("Number is > 99: " + value);
		if (value == 0) return;
		int tens = (value / 10) * 10;
		int units = value % 10;

		if (value <= 20 || units == 0) parts.add(Integer.toString(value));
		else {
			parts.add(Integer.toString(tens));
			parts.add(Integer.toString(units));
		}
	}

}
