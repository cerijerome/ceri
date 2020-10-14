package ceri.common.tree;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import java.util.Comparator;
import org.junit.Test;
import ceri.common.tree.TreeNodeTestHelper.TestNode;

public class TreeNodeComparatorsTest {
	private final TreeNodeTestHelper helper = new TreeNodeTestHelper();

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(TreeNodeComparators.class);
	}

	@Test
	public void testById() {
		Comparator<TestNode> comparator = TreeNodeComparators.id();
		assertThat(comparator.compare(null, null) == 0, is(true));
		assertThat(comparator.compare(helper.root, null) > 0, is(true));
		assertThat(comparator.compare(null, helper.root) < 0, is(true));
		assertThat(comparator.compare(helper.node(111), helper.node(21)) > 0, is(true));
		assertThat(comparator.compare(helper.node(1), helper.node(11)) < 0, is(true));
		assertThat(comparator.compare(helper.node(3), helper.node(3)) == 0, is(true));
	}

}
