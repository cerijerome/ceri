package ceri.ci.alert;

import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Test;

public class NodeBehavior {

	@Test
	public void shouldReadIndexFromSystemNodeProperty() {
		System.setProperty("node", "777");
		Node node = Node.createFromEnv();
		assertThat(node.index, is(777));
	}

}
