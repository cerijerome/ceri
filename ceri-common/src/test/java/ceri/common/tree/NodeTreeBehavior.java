package ceri.common.tree;

import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.tree.TreeNodeTestHelper.builder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import org.junit.Test;
import ceri.common.tree.TreeNodeTestHelper.TestNode;

public class NodeTreeBehavior {

	@Test(expected = IllegalStateException.class)
	public void shouldNotAllowDuplicateIds() {
		TestNode root = builder(1).child(builder(1)).build();
		NodeTree.create(root);
	}

	@Test
	public void shouldNotIncludeParentNodes() {
		TestNode root = builder(1).child(builder(11).child(builder(111))).build();
		TestNode node11 = root.children().iterator().next();
		NodeTree<TestNode> tree = NodeTree.create(node11);
		assertThat(tree.root, is(node11));
		assertThat(tree.get(1), is(nullValue()));
	}

	@Test
	public void shouldLookUpNodesById() {
		TestNode root = builder(1).child(builder(11).child(builder(111))).build();
		NodeTree<TestNode> tree = NodeTree.create(root);
		TestNode node11 = root.children().iterator().next();
		TestNode node111 = node11.children().iterator().next();
		assertThat(tree.get(11), is(node11));
		assertThat(tree.get(111), is(node111));
	}

}
