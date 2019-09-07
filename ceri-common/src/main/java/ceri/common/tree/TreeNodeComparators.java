package ceri.common.tree;

import java.util.Comparator;
import ceri.common.comparator.Comparators;
import ceri.common.util.BasicUtil;

/**
 * Comparators for TreeNode types.
 */
public class TreeNodeComparators {
	private static final Comparator<TreeNode<?>> ID = Comparators
		.nonNull((lhs, rhs) -> Comparators.INT.compare(lhs.id, rhs.id));

	private TreeNodeComparators() {}

	/**
	 * Comparator for tree nodes by id.
	 */
	public static <T extends TreeNode<T>> Comparator<T> id() {
		return BasicUtil.uncheckedCast(ID);
	}

}
