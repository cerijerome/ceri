package ceri.common.tree;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.tree.TreeNodeTestHelper.TestNode;

public class TreeNodeBehavior {
	TreeNodeTestHelper helper = new TreeNodeTestHelper();
	
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
			TreeNodeTestHelper.builder(0).child(TreeNodeTestHelper.builder(1))
				.child(TreeNodeTestHelper.builder(2)).child(TreeNodeTestHelper.builder(3))
				.build(null);
		assertThat(node.children().size(), is(3));
	}

	@Test
	public void shouldHaveImmutableTree() {
		// Compiler won't let fields be changed, only able to test children

		assertException(UnsupportedOperationException.class, new Runnable() {
			@Override
			public void run() {
				helper.root.children().clear();
			}
		});

		assertException(UnsupportedOperationException.class, new Runnable() {
			@Override
			public void run() {
				helper.root.children().add(null);
			}
		});

	}

}
