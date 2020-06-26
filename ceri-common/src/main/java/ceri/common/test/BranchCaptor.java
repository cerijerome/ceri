package ceri.common.test;

import java.util.Set;
import java.util.TreeSet;
import ceri.common.data.ByteUtil;

/**
 * Class to help analyze missed branches in code coverage. Compare the captor results with
 * short-circuit expression combinations to determine which tests to add.
 */
public class BranchCaptor {
	private static final String TRUE_SYMBOL = "+ ";
	private static final String FALSE_SYMBOL = "- ";
	private final Set<String> branches = new TreeSet<>();
	private int size = 0;

	public static String string(boolean... exprs) {
		StringBuilder b = new StringBuilder();
		for (boolean expr : exprs)
			b.append(symbol(expr));
		return b.toString();
	}

	public void add(boolean... exprs) {
		branches.add(string(exprs));
		size = Math.max(size, exprs.length);
	}

	public void report() {
		System.out.println(branches() + " branches:");
		for (var branch : branches)
			System.out.println("  " + branch);
		Set<String> missing = missing();
		if (missing.isEmpty()) return;
		System.out.println(missing.size() + " missing:");
		for (var branch : missing)
			System.out.println("  " + branch);
	}

	public int branches() {
		return branches.size();
	}

	public Set<String> missing() {
		Set<String> missing = new TreeSet<>();
		if (size > 0) for (int i = 0; i < 1 << size; i++) {
			StringBuilder b = new StringBuilder();
			for (int j = 0; j < size; j++)
				b.append(symbol(ByteUtil.bit(i, j)));
			String s = b.toString();
			if (!branches.contains(s)) missing.add(s);
		}
		return missing;
	}

	private static String symbol(boolean expr) {
		return expr ? TRUE_SYMBOL : FALSE_SYMBOL;
	}
}
