package ceri.ci.alert;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class NodeBehavior {

	@Test
	public void shouldReadIndexFromSystemNodeProperty() {
		System.setProperty("node", "777");
		Node node = Node.createFromEnv();
		assertEquals(node.index, 777);
	}

}
