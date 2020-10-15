package ceri.process.scutil;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.collection.Node;
import ceri.common.collection.NodeBuilder;

public class NcStatusBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Node<Void> node0 = NodeBuilder.<Void>of().startGroup("grp", null).value("val", 7).build();
		Node<Void> node1 = NodeBuilder.<Void>of().startGroup("grp", null).value("val", 8).build();
		NcStatus t = NcStatus.of(NcServiceState.connecting, node0);
		NcStatus eq0 = NcStatus.of(NcServiceState.connecting, node0);
		NcStatus ne0 = NcStatus.of(NcServiceState.unknown, node0);
		NcStatus ne1 = NcStatus.of(NcServiceState.connecting, node1);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldCreateFromOutput() {
		NcStatus ns = NcStatus.from("No service");
		assertThat(ns.state, is(NcServiceState.noService));
		assertThat(ns.data, is(Node.NULL));
	}

}
