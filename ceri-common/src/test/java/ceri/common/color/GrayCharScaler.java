package ceri.common.color;

import static ceri.common.text.StringUtil.repeat;
import java.io.IOException;
import ceri.common.array.ArrayUtil;
import ceri.common.io.IoUtil;
import ceri.common.io.RuntimeIoException;

/**
 * Tool to manually order characters by gray-level.
 * 
 * <pre>
 * x = exit
 * a = move to index 0
 * e = move last index - 1
 * [ = move left 1
 * d = delete current character (left-most)
 * s = swap characters (left-most and next)
 * </pre>
 */
public class GrayCharScaler {
	public static final String COURIER_GRAYSCALE =
		"@WMB#80Q&$%bdpOmqUXZkawho*CYJIunx1zfjtLv{}c[]?i()l<>|/\\r+;!~\"^:_-,'.` ";
	public static final String COURIER_GRAYSCALE_COMPACT =
		"@WMB#80Q&$%bOmqUXZkawho*CYJIunx1zfjtLv{c[?i(l<>|/r+;!~\"^:_-,'.` ";

	public static void main(String[] args) {
		String s = run(COURIER_GRAYSCALE_COMPACT, 10, 3.5);
		System.out.println(s);
	}

	public static String run(String s0, int size, double blocks) {
		char[] cs = s0.toCharArray();
		int i = 0;
		while (true) {
			System.out.println();
			printBlocks(cs, i, size, blocks);
			System.out.print("\n" + String.valueOf(cs) + "\n[" + i + "] > ");
			String input = input();
			if ("x".equals(input)) break;
			if ("a".equals(input)) i = 0;
			else if ("e".equals(input)) i = cs.length - 2; // end
			else if ("[".equals(input)) i = Math.max(0, i - 1); // back 1
			else if ("d".equals(input)) delete(cs, i); // delete
			else if ("s".equals(input)) swap(cs, i); // swap
			else i = Math.min(cs.length - 1, i + 1); // forward 1
		}
		return String.valueOf(cs);
	}

	private static void delete(char[] cs, int i) {
		char c = cs[i];
		if (i < cs.length - 1) ArrayUtil.chars.copy(cs, i + 1, cs, i);
		cs = ArrayUtil.chars.copyOf(cs, 0, cs.length - 1);
		System.out.println("Deleted " + c);
	}

	private static void swap(char[] cs, int i) {
		if (i >= cs.length - 1) return;
		char c = cs[i];
		cs[i] = cs[i + 1];
		cs[i + 1] = c;
		System.out.println("Swapped " + cs[i] + " <-> " + cs[i + 1]);
	}

	private static void printBlocks(char[] cs, int i, int size, double blocks) {
		String[] cols = new String[(int) Math.ceil(blocks)];
		for (int j = 0; j < cols.length; j++) {
			if (i + j >= cs.length) cols[j] = "";
			else if (j < cols.length - 1) cols[j] = repeat(cs[i + j], size * 2);
			else cols[j] = repeat(cs[i + j], (int) (size * 2 * (blocks - j)));
		}
		for (int j = 0; j < size; j++) {
			for (String col : cols)
				System.out.print(col);
			System.out.println();
		}
	}

	private static String input() {
		try {
			return IoUtil.pollString(System.in).trim();
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

}
