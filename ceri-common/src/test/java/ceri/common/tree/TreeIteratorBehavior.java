package ceri.common.tree;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import java.util.Iterator;
import org.junit.Test;
import ceri.common.tree.TreeNodeTestHelper.TestNode;

public class TreeIteratorBehavior {
	private final TreeNodeTestHelper helper = new TreeNodeTestHelper();
	
	@Test
	public void shouldIterateParentThenChildren() {
		Iterator<TestNode> iterator = helper.node(11).tree().iterator();
		assertThat(iterator.next(), is(helper.node(11)));
		assertThat(iterator.next(), is(helper.node(111)));
	}

	@Test
	public void shouldIterateNextParentAfterChildren() {
		Iterator<TestNode> iterator = helper.root.tree().iterator();
		iterator.next();
		iterator.next();
		iterator.next();
		assertThat(iterator.next(), is(helper.node(111)));
		assertThat(iterator.next(), is(helper.node(2)));
	}

	@Test
	public void shouldNotIterateAboveStartingNode() {
		Iterator<TestNode> iterator = helper.node(11).tree().iterator();
		assertThat(iterator.next(), is(helper.node(11)));
		assertThat(iterator.next(), is(helper.node(111)));
	}

}