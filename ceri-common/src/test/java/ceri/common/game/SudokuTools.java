package ceri.common.game;

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
		gen((seq, len) -> (len >= 7 //
			&& seq[0] == 2 //
			&& not(seq[1], 4, 8) //
			&& not(seq[2], 4, 8) //
			&& not(seq[3], 2, 6, 3) //
			&& not(seq[4], 2, 6, 3) //
			&& not(seq[5], 1, 9) //
			&& not(seq[6], 1, 9) //
		));
	}

	public static boolean not(int value, int... nots) {
		for (int not : nots)
			if (value == not) return false;
		return true;
	}

	public static boolean any(int value, int... anys) {
		for (int any : anys)
			if (value == any) return true;
		return false;
	}

	private static void gen(Functions.ObjIntPredicate<int[]> predicate) {
		int[] seq = new int[9];
		for (int i = 1; i <= 9; i++) {
			seq[0] = i;
			gen(seq, 1, predicate);
		}
	}

	private static void gen(int[] seq, int len, Functions.ObjIntPredicate<int[]> predicate) {
		int n = seq[len - 1];
		int[] factors = FACTORS[n - 1];
		int count = 0;
		for (int factor : factors) {
			if (contains(seq, len, factor)) continue;
			seq[len] = factor;
			gen(seq, len + 1, predicate);
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
