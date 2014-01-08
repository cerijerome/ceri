package ceri.common.tree;

import java.util.Comparator;
import ceri.common.comparator.BaseComparator;
import ceri.common.comparator.Comparators;
import ceri.common.util.BasicUtil;

/**
 * Comparators for TreeNode types.
 */
public class TreeNodeComparators {
	private static final Comparator<TreeNode<?>> ID = new BaseComparator<TreeNode<?>>() {
		@Override
		protected int compareNonNull(TreeNode<?> o1, TreeNode<?> o2) {
			return Comparators.INTEGER.compare(o1.id, o2.id);
		}
	};
	
	private TreeNodeComparators() {}

	/**
	 * Comparator for tree nodes by id.
	 */
	public static <T extends TreeNode<T>> Comparator<T> id() {
		return BasicUtil.<Comparator<T>>uncheckedCast(ID);
	}

}
