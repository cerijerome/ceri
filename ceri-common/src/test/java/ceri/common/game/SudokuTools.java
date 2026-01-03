package ceri.common.game;

import java.util.Set;
import ceri.common.collect.Sets;
import ceri.common.data.Bytes;
import ceri.common.function.Functions;

/**
 * Utilities to help solve sudoku variant puzzles.
 */
public class SudokuTools {
	private static final int[][] FACTORS = { { 2, 3, 4, 5, 6, 7, 8, 9 }, // 1
		{ 1, 4, 6, 8 }, // 2
		{ 1, 6, 9 }, // 3
		{ 1, 2, 8 }, // 4
		{ 1 }, // 5
		{ 1, 2, 3 }, // 6
		{ 1 }, // 7
		{ 1, 2, 4 }, // 8
		{ 1, 3 } // 9
	};

	public static void main(String[] args) {
		for (int i = 2; i <= 6; i++)
			generate("gw", i, SudokuTools::germanWhispers);
	}

	/**
	 * Generate and print a set of sequences for a given size.
	 */
	public static void generate(String name, int n, Functions.IntFunction<Set<Integer>> generator) {
		var values = generator.apply(n);
		System.out.printf("%n%s/%d (%d)", name, n, values.size());
		int i = 0;
		for (var value : values) {
			if (i++ % 10 == 0) System.out.println();
			System.out.print("  " + value);
		}
		System.out.println();
	}

	/**
	 * Generates unique sequences for given size.
	 */
	public static Set<Integer> germanWhispers(int count) {
		var set = Sets.<Integer>tree();
		recurse(count, (seq, i, m) -> {
			if (seq[0] != 6) return false;
			if (!uniqueDigit(seq, i, m)) return false;
			if (!germanWhisper(seq, i)) return false;
			if (!full(seq, i)) return true;
			if (set.contains(reverseNumber(seq))) return false;
			set.add(number(seq));
			return true;
		});
		return set;
	}

	/**
	 * Generates unique sequences for given size.
	 */
	public static Set<Integer> dutchWhispers(int count) {
		var set = Sets.<Integer>tree();
		recurse(count, (seq, i, m) -> {
			if (!uniqueDigit(seq, i, m)) return false;
			if (!dutchWhisper(seq, i)) return false;
			if (!full(seq, i)) return true;
			if (set.contains(reverseNumber(seq))) return false;
			set.add(number(seq));
			return true;
		});
		return set;
	}

	/**
	 * Recursion predicate for German whispers.
	 */
	public static boolean germanWhisper(int[] seq, int index) {
		if (seq[index] == 5) return false;
		return index == 0 || Math.abs(seq[index] - seq[index - 1]) >= 5;
	}

	/**
	 * Recursion predicate for Dutch whispers.
	 */
	public static boolean dutchWhisper(int[] seq, int index) {
		return index == 0 || Math.abs(seq[index] - seq[index - 1]) >= 4;
	}

	/**
	 * Recursion predicate for increasing values.
	 */
	public static boolean increasing(int[] seq, int index) {
		return index == 0 || seq[index] > seq[index - 1];
	}

	/**
	 * Recursion predicate for uniqueness.
	 */
	public static boolean uniqueDigit(int[] seq, int index, int mask) {
		return index == 0 || !Bytes.bit(mask, seq[index]);
	}

	/**
	 * Recursion predicate for full sequence.
	 */
	public static boolean full(int[] seq, int index) {
		return index == seq.length - 1;
	}

	/**
	 * Returns true if the sequence contains the value.
	 */
	public static boolean has(int[] seq, int value) {
		for (int n : seq)
			if (n == value) return true;
		return false;
	}

	/**
	 * Returns true if the sequence contains all the values.
	 */
	public static boolean hasAll(int[] seq, int... values) {
		for (int value : values)
			if (!has(seq, value)) return false;
		return true;
	}

	/**
	 * Returns true if the sequence contains any of the values.
	 */
	public static boolean hasAny(int[] seq, int... values) {
		for (int value : values)
			if (has(seq, value)) return true;
		return false;
	}

	/**
	 * Returns true if the value contains all factors.
	 */
	public static boolean factorAll(long value, int... factors) {
		for (var factor : factors)
			if (value % factor != 0) return false;
		return true;
	}

	/**
	 * Returns the first matching factor, or 0 if none.
	 */
	public static int factor(long value, int... factors) {
		for (var factor : factors)
			if (value % factor == 0) return factor;
		return 0;
	}

	/**
	 * Calculates the product of a sequence.
	 */
	public static long product(int[] seq) {
		long p = 1;
		for (int n : seq)
			p *= n;
		return p;
	}

	/**
	 * Formats a sequence with blank spaces for missing values.
	 */
	public static String format(int[] seq) {
		var b = new StringBuilder("[");
		for (int i = 1; i <= 9; i++)
			b.append(has(seq, i) ? i : " ");
		return b.append(']').toString();
	}

	/**
	 * Returns the sequence as a string.
	 */
	public static String string(int[] seq) {
		var b = new StringBuilder("[");
		for (int i = 0; i < seq.length; i++)
			b.append(seq[i]);
		return b.append(']').toString();
	}

	/**
	 * Returns the sequence as an integer. Only valid up to length 9.
	 */
	public static int number(int[] seq) {
		int n = 0;
		for (int i = 0; i < seq.length; i++)
			n = (10 * n) + seq[i];
		return n;
	}

	/**
	 * Returns the reverse sequence as an integer. Only valid up to length 9.
	 */
	public static int reverseNumber(int[] seq) {
		int n = 0;
		for (int i = 0, m = 1; i < seq.length; i++, m *= 10)
			n += m * seq[i];
		return n;
	}

	/**
	 * Iterates factors.
	 */
	public static void factor(Functions.ObjIntPredicate<int[]> predicate) {
		int[] seq = new int[9];
		for (int i = 1; i <= 9; i++) {
			seq[0] = i;
			factor(seq, 1, predicate);
		}
	}

	public interface Recursor {
		boolean test(int[] seq, int index, int mask);
	}

	/**
	 * Iterates a fixed size, with mask and predicate.
	 */
	public static int recurse(int len, Recursor recursor) {
		return recurse(new int[len], 0, 0, 0, recursor);
	}

	// support

	private static int recurse(int[] seq, int mask, int index, int count, Recursor recursor) {
		if (index >= seq.length) return count + 1;
		for (int i = 1; i <= 9; i++) {
			seq[index] = i;
			if (!recursor.test(seq, index, mask)) continue;
			count = recurse(seq, mask | Bytes.maskOfBitsInt(i), index + 1, count, recursor);
		}
		return count;
	}

	private static void factor(int[] seq, int len, Functions.ObjIntPredicate<int[]> predicate) {
		int n = seq[len - 1];
		int[] factors = FACTORS[n - 1];
		int count = 0;
		for (int factor : factors) {
			if (contains(seq, len, factor)) continue;
			seq[len] = factor;
			factor(seq, len + 1, predicate);
			seq[len] = 0;
			count++;
		}
		if (count == 0) print(seq, len, predicate);

	}

	private static boolean contains(int[] seq, int len, int factor) {
		for (int i = 0; i < len; i++)
			if (seq[i] == factor) return true;
		return false;
	}

	private static void print(int[] seq, int len, Functions.ObjIntPredicate<int[]> predicate) {
		if (!predicate.test(seq, len)) return;
		for (int i = 0; i < len; i++)
			System.out.print(seq[i]);
		System.out.print(" .. ");
		System.out.println(len);
	}
}
