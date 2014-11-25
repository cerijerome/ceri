package ceri.common.tree;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.tree.TreeNodeTestHelper.builder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.tree.TreeNodeTestHelper.TestNode;

public class TreeNodeBehavior {
	TreeNodeTestHelper helper = new TreeNodeTestHelper();

	static class TestNode2 extends TreeNode<TestNode2> {
		public TestNode2(TestNode2 parent, Builder builder) {
			super(parent, builder);
		}

		public static class Builder extends TreeNode.Builder<TestNode2> {
			public Builder(int id) {
				super(id);
			}

			@Override
			protected TestNode2 build(TestNode2 parent) {
				return new TestNode2(parent, this);
			}
		}
	}

	@Test
	public void shouldHaveTopLevelRootNode() {
		TestNode root = builder(0).child(builder(1)).build();
		NodeTree<TestNode> tree = NodeTree.create(root);
		assertThat(root.isRoot(), is(true));
		assertThat(tree.get(1).isRoot(), is(false));
	}

	@Test
	public void shouldObeyEqualsContract() {
		TestNode root = builder(0).child(builder(1)).build();
		TestNode root1 = builder(0).child(builder(1)).build();
		TestNode root2 = builder(0).child(builder(2)).build();
		TestNode2 root3 = new TestNode2(null, new TestNode2.Builder(0));
		assertFalse(root.equals(null));
		assertFalse(root.equals(""));
		assertThat(root, is(root));
		assertThat(root, is(root1));
		assertThat(root, is(not(root2)));
		assertThat(root, is(not(root3)));
		assertThat(root.hashCode(), is(root1.hashCode()));
	}

	@Test
	public void shouldKnowIfChildOfAnotherNode() {
		assertThat(helper.node(111).isChildOf(helper.node(11)), is(true));
		assertThat(helper.node(111).isChildOf(helper.node(0)), is(true));
		assertThat(helper.node(1).isChildOf(helper.node(1)), is(false));
		assertThat(helper.node(0).isChildOf(helper.node(1)), is(false));
	}

	@Test
	public void shouldKnowIfALeaf() {
		assertThat(helper.node(111).isLeaf(), is(true));
		assertThat(helper.node(3).isLeaf(), is(true));
		assertThat(helper.node(2).isLeaf(), is(false));
	}

	@Test
	public void shouldMatchBuilderChildStructure() {
		final TestNode node =
			TreeNodeTestHelper.builder(0).child(TreeNodeTestHelper.builder(1)).child(
				TreeNodeTestHelper.builder(2)).child(TreeNodeTestHelper.builder(3)).build(null);
		assertThat(node.children().size(), is(3));
	}

	@Test
	public void shouldHaveImmutableTree() {
		// Compiler won't let fields be changed, only able to test children
		assertException(UnsupportedOperationException.class, () -> helper.root.children().clear());
		assertException(UnsupportedOperationException.class, () -> helper.root.children().add(null));
	}

}
