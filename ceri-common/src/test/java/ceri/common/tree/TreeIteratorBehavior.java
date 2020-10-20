package ceri.common.tree;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import java.util.Iterator;
import org.junit.Test;
import ceri.common.tree.TreeNodeTestHelper.TestNode;

public class TreeIteratorBehavior {
	private final TreeNodeTestHelper helper = new TreeNodeTestHelper();

	@Test
	public void shouldNotAllowRemove() {
		Iterator<TestNode> iterator = helper.node(11).tree().iterator();
		assertThrown(iterator::remove);
	}

	@Test
	public void shouldIterateParentThenChildren() {
		Iterator<TestNode> iterator = helper.node(11).tree().iterator();
		assertEquals(iterator.next(), helper.node(11));
		assertEquals(iterator.next(), helper.node(111));
	}

	@Test
	public void shouldIterateNextParentAfterChildren() {
		Iterator<TestNode> iterator = helper.root.tree().iterator();
		iterator.next();
		iterator.next();
		iterator.next();
		assertEquals(iterator.next(), helper.node(111));
		assertEquals(iterator.next(), helper.node(2));
	}

	@Test
	public void shouldNotIterateAboveStartingNode() {
		Iterator<TestNode> iterator = helper.node(11).tree().iterator();
		assertEquals(iterator.next(), helper.node(11));
		assertEquals(iterator.next(), helper.node(111));
	}

}
