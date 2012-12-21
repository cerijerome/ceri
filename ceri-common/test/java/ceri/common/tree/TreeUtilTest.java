package ceri.common.tree;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TreeUtilTest {
	private final TreeNodeTestHelper helper = new TreeNodeTestHelper();
	
	@Test
	public void testRootOf() {
		assertThat(TreeUtil.rootOf(helper.get(111)), is(helper.root));
		assertThat(TreeUtil.rootOf(helper.get(21)), is(helper.root));
		assertThat(TreeUtil.rootOf(helper.get(3)), is(helper.root));
		assertThat(TreeUtil.rootOf(helper.root), is(helper.root));
		assertThat(TreeUtil.rootOf(helper.get(0)), is(helper.root));
	}

	@Test
	public void testToString() {
		String[] s = TreeUtil.toString(helper.root).split("[\r\n]+");
		assertThat(s.length, is(7)); // 7 nodes total
		assertThat(s, is(new String[] {
			"TestNode(0,0)",
			"\tTestNode(1,1)",
			"\t\tTestNode(11,2)",
			"\t\t\tTestNode(111,3)",
			"\tTestNode(2,1)",
			"\t\tTestNode(21,2)",
			"\tTestNode(3,1)",
		}));
	}

}
