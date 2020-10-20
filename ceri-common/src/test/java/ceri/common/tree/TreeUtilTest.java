package ceri.common.tree;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class TreeUtilTest {
	private final TreeNodeTestHelper helper = new TreeNodeTestHelper();

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(TreeUtil.class);
	}

	@Test
	public void testIterable() {
		assertThrown(() -> TreeUtil.iterable(null));
	}

	@Test
	public void testIsChild() {
		assertThrown(() -> TreeUtil.isChild(null, null));
		assertThrown(() -> TreeUtil.isChild(helper.root, null));
	}

	@Test(expected = NullPointerException.class)
	public void testLeavesOfNull() {
		TreeUtil.leaves(null);
	}

	@Test
	public void testLeaves() {
		assertEquals(TreeUtil.leaves(helper.root), helper.nodes(111, 21, 3));
	}

	@Test(expected = NullPointerException.class)
	public void testRootOfNull() {
		TreeUtil.rootOf(null);
	}

	@Test
	public void testRootOf() {
		assertEquals(TreeUtil.rootOf(helper.node(111)), helper.root);
		assertEquals(TreeUtil.rootOf(helper.node(21)), helper.root);
		assertEquals(TreeUtil.rootOf(helper.node(3)), helper.root);
		assertEquals(TreeUtil.rootOf(helper.root), helper.root);
		assertEquals(TreeUtil.rootOf(helper.node(0)), helper.root);
	}

	@Test
	public void testToStringOfNull() {
		assertEquals(TreeUtil.toString(null), "null");
	}

	@Test
	public void testToString() {
		String[] s = TreeUtil.toString(helper.root).split("[\r\n]+");
		assertArray(s, "TestNode(0,0)", "\tTestNode(1,1)", "\t\tTestNode(11,2)",
			"\t\t\tTestNode(111,3)", "\tTestNode(2,1)", "\t\tTestNode(21,2)", "\tTestNode(3,1)");
	}

}
