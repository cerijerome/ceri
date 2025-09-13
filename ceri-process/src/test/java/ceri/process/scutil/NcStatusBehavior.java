package ceri.process.scutil;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.collection.Node;
import ceri.common.test.TestUtil;

public class NcStatusBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Node<Void> node0 = Node.<Void>tree().startGroup("grp", null).value("val", 7).build();
		Node<Void> node1 = Node.<Void>tree().startGroup("grp", null).value("val", 8).build();
		NcStatus t = NcStatus.of(NcServiceState.connecting, node0);
		NcStatus eq0 = NcStatus.of(NcServiceState.connecting, node0);
		NcStatus ne0 = NcStatus.of(NcServiceState.unknown, node0);
		NcStatus ne1 = NcStatus.of(NcServiceState.connecting, node1);
		TestUtil.exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldCreateFromOutput() {
		NcStatus ns = NcStatus.from("No service");
		assertEquals(ns.state, NcServiceState.noService);
		assertEquals(ns.data, Node.NULL);
	}

}
