package ceri.common.tree;

import ceri.common.util.ToStringHelper;

/**
 * Test node and builder static classes to help test TreeNodes. Sample tree to
 * be used for testing.
 */
public class TreeNodeTestHelper {
	public final TestNode root;
	public final NodeTree<TestNode> tree;
	private int count = 0;

	public TreeNodeTestHelper() {
		// 0-+-1---11--111
		//   |-2---21
		//   `-3
		root =
			builder(0).child(builder(1).child(builder(11).child(builder(111)))).child(
				builder(2).child(builder(21))).child(builder(3)).build();
		tree = NodeTree.create(root);
	}

	public TestNode get(int id) {
		return tree.get(id);
	}

	public TreeNode.Builder<TestNode> builder() {
		return new TestNodeBuilder(count++);
	}

	public static TreeNode.Builder<TestNode> builder(int id) {
		return new TestNodeBuilder(id);
	}

	public static class TestNode extends TreeNode<TestNode> {
		private final String toString;

		public TestNode(TestNode parent, Builder<TestNode> builder) {
			super(TestNode.class, parent, builder);
			toString = ToStringHelper.createByClass(this, id, level).toString();
		}

		@Override
		public String toString() {
			return toString;
		}
	}

	static class TestNodeBuilder extends TreeNode.Builder<TestNode> {

		public TestNodeBuilder(int id) {
			super(id);
		}

		@Override
		protected TestNode build(TestNode parent) {
			return new TestNode(parent, this);
		}

	}

}
