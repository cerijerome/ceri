package ceri.common.tree;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.common.tree.TreeNodeTestHelper.builder;
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
		assertTrue(root.isRoot());
		assertFalse(tree.get(1).isRoot());
	}

	@Test
	public void shouldObeyEqualsContract() {
		TestNode root = builder(0).child(builder(1)).build();
		TestNode root1 = builder(0).child(builder(1)).build();
		TestNode root2 = builder(0).child(builder(2)).build();
		TestNode2 root3 = new TestNode2(null, new TestNode2.Builder(0));
		exerciseEquals(root, root1);
		assertAllNotEqual(root, root2, root3);
	}

	@Test
	public void shouldKnowIfChildOfAnotherNode() {
		assertTrue(helper.node(111).isChildOf(helper.node(11)));
		assertTrue(helper.node(111).isChildOf(helper.node(0)));
		assertFalse(helper.node(1).isChildOf(helper.node(1)));
		assertFalse(helper.node(0).isChildOf(helper.node(1)));
	}

	@Test
	public void shouldKnowIfALeaf() {
		assertTrue(helper.node(111).isLeaf());
		assertTrue(helper.node(3).isLeaf());
		assertFalse(helper.node(2).isLeaf());
	}

	@Test
	public void shouldMatchBuilderChildStructure() {
		final TestNode node = TreeNodeTestHelper.builder(0).child(TreeNodeTestHelper.builder(1))
			.child(TreeNodeTestHelper.builder(2)).child(TreeNodeTestHelper.builder(3)).build(null);
		assertEquals(node.children().size(), 3);
	}

	@Test
	public void shouldHaveImmutableTree() {
		// Compiler won't let fields be changed, only able to test children
		assertThrown(UnsupportedOperationException.class, () -> helper.root.children().clear());
		assertThrown(UnsupportedOperationException.class, () -> helper.root.children().add(null));
	}

}
