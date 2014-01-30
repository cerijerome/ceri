package ceri.common.tree;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TreeUtilTest {
	private final TreeNodeTestHelper helper = new TreeNodeTestHelper();

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(TreeUtil.class);
	}

	@Test(expected = NullPointerException.class)
	public void testLeavesOfNull() {
		TreeUtil.leaves(null);
	}

	@Test
	public void testLeaves() {
		assertThat(TreeUtil.leaves(helper.root), is(helper.nodes(111, 21, 3)));
	}

	@Test(expected = NullPointerException.class)
	public void testRootOfNull() {
		TreeUtil.rootOf(null);
	}

	@Test
	public void testRootOf() {
		assertThat(TreeUtil.rootOf(helper.node(111)), is(helper.root));
		assertThat(TreeUtil.rootOf(helper.node(21)), is(helper.root));
		assertThat(TreeUtil.rootOf(helper.node(3)), is(helper.root));
		assertThat(TreeUtil.rootOf(helper.root), is(helper.root));
		assertThat(TreeUtil.rootOf(helper.node(0)), is(helper.root));
	}

	@Test
	public void testToStringOfNull() {
		assertThat(TreeUtil.toString(null), is("null"));
	}

	@Test
	public void testToString() {
		String[] s = TreeUtil.toString(helper.root).split("[\r\n]+");
		assertThat(s.length, is(7)); // 7 nodes total
		assertThat(s, is(new String[] { "TestNode(0,0)", "\tTestNode(1,1)", "\t\tTestNode(11,2)",
			"\t\t\tTestNode(111,3)", "\tTestNode(2,1)", "\t\tTestNode(21,2)", "\tTestNode(3,1)", }));
	}

}
