package ceri.common.tree;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
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

	public TestNode node(int id) {
		return tree.get(id);
	}

	public Collection<TestNode> nodes(int...ids) {
		Set<TestNode> nodes = new LinkedHashSet<>();
		for (int id : ids) nodes.add(node(id));
		return nodes;
	}

	public TestNode.Builder builder() {
		return new TestNode.Builder(count++);
	}

	public static TreeNode.Builder<TestNode> builder(int id) {
		return new TestNode.Builder(id);
	}

	public static class TestNode extends TreeNode<TestNode> {
		private final String toString;
		public final int id2;

		public static class Builder extends TreeNode.Builder<TestNode> {
			int id2 = 0;
			
			public Builder(int id) {
				super(id);
			}

			public Builder id2(int id2) {
				this.id2 = id2;
				return this;
			}
			
			@Override
			protected TestNode build(TestNode parent) {
				return new TestNode(parent, this);
			}

		}

		public TestNode(TestNode parent, Builder builder) {
			super(parent, builder);
			id2 = builder.id2;
			toString = ToStringHelper.createByClass(this, id, level).toString();
		}

		@Override
		public String toString() {
			return toString;
		}
	}

}
