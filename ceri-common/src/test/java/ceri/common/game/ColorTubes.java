package ceri.common.game;

import static ceri.common.game.ColorTubes.Color.aqua;
import static ceri.common.game.ColorTubes.Color.blue;
import static ceri.common.game.ColorTubes.Color.brown;
import static ceri.common.game.ColorTubes.Color.cyan;
import static ceri.common.game.ColorTubes.Color.gray;
import static ceri.common.game.ColorTubes.Color.green;
import static ceri.common.game.ColorTubes.Color.olive;
import static ceri.common.game.ColorTubes.Color.orange;
import static ceri.common.game.ColorTubes.Color.pink;
import static ceri.common.game.ColorTubes.Color.purple;
import static ceri.common.game.ColorTubes.Color.red;
import static ceri.common.game.ColorTubes.Color.yellow;
import ceri.common.text.AnsiEscape;
import ceri.common.time.TimeSupplier;

/**
 * Solves colored layer water tube puzzles.
 */
public class ColorTubes {
	private static final int SIZE = 4;
	private static final int MOVE_FROM_INDEX = 0;
	private static final int MOVE_TO_INDEX = 1;
	private static final int MOVE_COUNT_INDEX = 2;
	private static int count = 0;

	enum Color {
		none(0xffffff), // 0
		red(0xf00000), // 1
		pink(0xf07080), // 2
		orange(0xf0a000), // 3
		brown(0x905020), // 4
		yellow(0xf0f000), // 5
		green(0x308050), // 6
		olive(0xa0b000), // 7
		aqua(0x70f0b0), // 8
		cyan(0x00d0e0), // 9
		blue(0x0000f0), // 10
		purple(0x8060e0), // 11
		gray(0x909090); // 12

		public static final Color[] values = Color.values();
		public final int rgb;

		private Color(int rgb) {
			this.rgb = rgb;
		}
	}

	public static int[] level877() {
		return new int[] { //
			tube(pink, blue, yellow, gray), //
			tube(orange, purple, cyan, orange), //
			tube(cyan, green, green, pink), //
			tube(red, purple, olive, gray), //
			tube(red, aqua, aqua, olive), //
			tube(brown, cyan, yellow, red), //
			tube(olive, cyan, orange, yellow), //
			tube(olive, green, brown, brown), //
			tube(purple, blue, blue, yellow), //
			tube(pink, red, gray, pink), //
			tube(purple, blue, aqua, brown), //
			tube(aqua, green, orange, gray), //
			0, 0 };
	}
	
	public static int[] level843() {
		return new int[] { //
			tube(olive, cyan, green, gray), //
			tube(purple, green, pink, red), //
			tube(blue, gray, brown, aqua), //
			tube(brown, cyan, olive, cyan), //
			tube(orange, orange, orange, gray), //
			tube(brown, brown, olive, red), //
			tube(aqua, red, yellow, blue), //
			tube(gray, orange, yellow, red), //
			tube(green, aqua, yellow, purple), //
			tube(blue, yellow, cyan, purple), //
			tube(pink, aqua, blue, olive), //
			tube(purple, green, pink, pink), //
			0, 0 };
	}

	public static int[] level833() {
		return new int[] { //
			tube(yellow, purple, olive, green), //
			tube(orange, gray, brown, green), //
			tube(cyan, olive, red, orange), //
			tube(green, olive, yellow, aqua), //
			tube(blue, brown, yellow, green), //
			tube(purple, purple, cyan, blue), //
			tube(red, gray, pink, aqua), //
			tube(aqua, gray, blue, pink), //
			tube(cyan, red, pink, cyan), //
			tube(brown, orange, red, orange), //
			tube(brown, gray, purple, olive), //
			tube(aqua, yellow, pink, blue), //
			0, 0 };
	}

	public static int[] level824() {
		return new int[] { //
			tube(red, gray, orange, pink), //
			tube(blue, pink, gray, pink), //
			tube(aqua, cyan, blue, olive), //
			tube(orange, red, cyan, olive), //
			tube(gray, blue, aqua, purple), //
			tube(purple, pink, olive, orange), //
			tube(cyan, cyan, aqua, purple), //
			tube(olive, purple, aqua, red), //
			tube(gray, red, orange, blue), //
			0, 0 };
	}

	public static int[] level823() {
		return new int[] { //
			tube(brown, purple, orange, cyan), //
			tube(gray, cyan, orange, green), //
			tube(purple, olive, aqua, red), //
			tube(brown, green, red, cyan), //
			tube(red, purple, purple, aqua), //
			tube(blue, blue, green, yellow), //
			tube(orange, pink, cyan, olive), //
			tube(olive, aqua, gray, aqua), //
			tube(gray, blue, pink, brown), //
			tube(gray, yellow, green, yellow), //
			tube(pink, brown, red, olive), //
			tube(blue, orange, pink, yellow), //
			0, 0 };
	}

	public static void main(String[] args) {
		int[] tubes = level877();
		printTubes(tubes);
		validateTubes(tubes);
		var path = new int[100];
		long t0 = TimeSupplier.micros.time();
		// int n = solveRecursively(path, 0, tubes);
		int n = solveIteratively(path, tubes);
		long t1 = TimeSupplier.micros.time();
		showSolution(path, n, tubes);
		System.out.printf("count=%d t=%dus\n", count, t1 - t0);
	}

	private static void showSolution(int[] path, int n, int[] tubes) {
		for (int i = 0; i < n; i++) {
			int move = path[i];
			applyMove(move, tubes);
			printMove(i + 1, move, tubes);
			printTubes(tubes);
		}
	}

	/**
	 * Solves the puzzle recursively.
	 */
	public static int solveRecursively(int[] path, int pathIndex, int[] tubes) {
		if (complete(tubes)) return pathIndex;
		int[] moves = new int[tubes.length * 2];
		int n = setMoves(moves, 0, tubes);
		for (int i = n - 1; i >= 0; i--) { // to match iterative order
			// for (int i = 0; i < n; i++) {
			int move = moves[i];
			path[pathIndex] = move;
			if (move == 0) return -1;
			count++;
			applyMove(move, tubes);
			int result = solveRecursively(path, pathIndex + 1, tubes);
			undoMove(move, tubes);
			if (result >= 0) return result;
		}
		return -1;
	}

	/**
	 * Solve the puzzle iteratively.
	 */
	public static int solveIteratively(int[] path, int[] tubes) {
		// moves: xxxxx0xxx0xxxx0xx each layer separated by 0
		// work back from the end of each layer
		int[] moves = new int[(tubes.length + 1) * (tubes.length + 1)]; // max estimate
		int pathIndex = 0;
		int complete = -1;
		int i = setMoves(moves, 0, tubes) - 1;
		while (i >= 0) {
			if (moves[i] == 0) {
				pathIndex--;
				undoMove(moves[--i], tubes);
				moves[i--] = 0;
			} else if (complete >= 0) {
				i--;
			} else {
				int move = moves[i++];
				path[pathIndex++] = move;
				count++;
				applyMove(move, tubes);
				if (complete(tubes)) complete = pathIndex;
				else i += setMoves(moves, i + 1, tubes);
			}
		}
		return complete;
	}

	public static void validateTubes(int[] tubes) {
		int[] counts = new int[Color.values.length];
		for (int tube : tubes) {
			for (int i = 0; i < SIZE; i++) {
				counts[color(tube, i)]++;
			}
		}
		for (int i = 1; i < counts.length; i++) {
			int n = counts[i];
			if (n > SIZE) System.err.printf("Too many %s: %d\n", Color.values[i], n);
			if (n > 0 && n < SIZE) System.err.printf("Too few %s: %d\n", Color.values[i], n);
		}
	}

	/**
	 * Determine moves and set in given array. Terminate array with 0 value.
	 */
	private static int setMoves(int[] moves, int index, int[] tubes) {
		int n = 0;
		for (int from = 0; from < tubes.length; from++) {
			int fromColor = topColor(tubes[from]);
			int fromFree = topFree(tubes[from]);
			int count = topCount(tubes[from]);
			if (count > 0 && count < SIZE) for (int to = 0; to < tubes.length; to++) {
				if (to == from) continue;
				int toColor = topColor(tubes[to]);
				int toFree = topFree(tubes[to]);
				if (toFree < count) continue;
				if (toFree == SIZE && (count + fromFree) == SIZE) continue;
				if (toColor != 0 && toColor != fromColor) continue;
				moves[index + n++] = toMove(from, to, count);
			}
		}
		moves[index + n] = 0; // 0 to mark end of moves
		return n;
	}

	/**
	 * Apply the move to the tubes.
	 */
	private static void applyMove(int move, int[] tubes) {
		if (move == 0) return;
		int fromIndex = moveFrom(move);
		int toIndex = moveTo(move);
		int count = moveCount(move);
		int fromColor = topColor(tubes[fromIndex]);
		int fromFree = topFree(tubes[fromIndex]);
		int toFree = topFree(tubes[toIndex]);
		for (int i = 0; i < count; i++) {
			tubes[fromIndex] = color(tubes[fromIndex], SIZE - fromFree - 1 - i, 0);
			tubes[toIndex] = color(tubes[toIndex], SIZE - toFree + i, fromColor);
		}
	}

	/**
	 * Reverse the move on the tubes.
	 */
	private static void undoMove(int move, int[] tubes) {
		if (move == 0) return;
		int fromIndex = moveFrom(move);
		int toIndex = moveTo(move);
		int count = moveCount(move);
		int fromColor = topColor(tubes[toIndex]);
		int fromFree = topFree(tubes[fromIndex]) - count;
		int toFree = topFree(tubes[toIndex]) + count;
		for (int i = 0; i < count; i++) {
			tubes[fromIndex] = color(tubes[fromIndex], SIZE - fromFree - 1 - i, fromColor);
			tubes[toIndex] = color(tubes[toIndex], SIZE - toFree + i, 0);
		}
	}

	/**
	 * Construct a move value for from index, to index, and count.
	 */
	private static int toMove(int from, int to, int count) {
		return shift(count, MOVE_COUNT_INDEX) | shift(to, MOVE_TO_INDEX)
			| shift(from, MOVE_FROM_INDEX);
	}

	/**
	 * Extract the from index of the move.
	 */
	private static int moveFrom(int move) {
		return unshift(move, MOVE_FROM_INDEX);
	}

	/**
	 * Extract the to index of the move.
	 */
	private static int moveTo(int move) {
		return unshift(move, MOVE_TO_INDEX);
	}

	/**
	 * Extract the count of the move.
	 */
	private static int moveCount(int move) {
		return unshift(move, MOVE_COUNT_INDEX);
	}

	/**
	 * Return true if all tubes are complete; empty or full of a single color.
	 */
	private static boolean complete(int[] tubes) {
		for (int tube : tubes)
			if (!complete(tube)) return false;
		return true;
	}

	/**
	 * Return true if the tube is complete; empty or full of a single color.
	 */
	private static boolean complete(int tube) {
		return tube == 0 || topCount(tube) == SIZE;
	}

	/**
	 * Get the color at the top of the tube, or 0 if none.
	 */
	private static int topColor(int tube) {
		if (tube != 0) for (int i = SIZE - 1; i >= 0; i--) {
			int c = color(tube, i);
			if (c != 0) return c;
		}
		return 0;
	}

	/**
	 * Count the number of free spaces at the top of the tube.
	 */
	private static int topFree(int tube) {
		if (tube != 0) for (int i = SIZE - 1; i >= 0; i--) {
			int c = color(tube, i);
			if (c != 0) return SIZE - 1 - i;
		}
		return SIZE;
	}

	/**
	 * Count the same color blocks at the top of the tube.
	 */
	private static int topCount(int tube) {
		int color = 0;
		int n = 0;
		if (tube != 0) for (int i = SIZE - 1; i >= 0; i--) {
			int c = color(tube, i);
			if (c == 0) continue;
			if (color != 0 && color != c) break;
			color = c;
			n++;
		}
		return n;
	}

	/**
	 * Get the color at index of tube.
	 */
	private static int color(int tube, int i) {
		return unshift(tube, i);
	}

	/**
	 * Set the color at index of tube. Returns the modified tube value.
	 */
	private static int color(int tube, int i, int color) {
		return (tube & ~shift(0xff, i)) | shift(color, i);
	}

	/**
	 * Create a tube value with given colors starting at the bottom.
	 */
	private static int tube(Color... colors) {
		int tube = 0;
		for (int i = 0; i < colors.length; i++)
			tube |= shift(colors[i].ordinal(), i);
		return tube;
	}

	private static int shift(int val, int i) {
		return val << (i << 3);
	}

	private static int unshift(int val, int i) {
		return (val >>> (i << 3)) & 0xff;
	}

	private static void printMove(int number, int move, int[] tubes) {
		int color = topColor(tubes[moveTo(move)]);
		int rgb = Color.values[color].rgb;
		System.out.printf("%d) %s  %s %d => %d\n\n", number, AnsiEscape.csi.sgr().bgColor24(rgb),
			AnsiEscape.csi.sgr().reset(), moveFrom(move) + 1, moveTo(move) + 1);
	}

	private static void printTubes(int[] tubes) {
		for (int i = SIZE - 1; i >= 0; i--) {
			for (int j = 0; j < tubes.length; j++) {
				var rgb = Color.values[color(tubes[j], i)].rgb;
				System.out.printf(" %s%s%s", AnsiEscape.csi.sgr().bgColor24(rgb),
					i == 0 ? "__" : "  ", AnsiEscape.csi.sgr().reset());
			}
			System.out.println();
		}
		System.out.println();
	}
}
