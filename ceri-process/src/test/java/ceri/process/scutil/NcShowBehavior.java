package ceri.process.scutil;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;
import ceri.common.collection.Node;

public class NcShowBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		NcListItem item0 = NcListItem.from("* (Connecting) X P --> D \"N\" [P:T]");
		NcListItem item1 = NcListItem.from("  (Connecting) X P --> D \"N\" [P:T]");
		Node<Void> node0 = Node.<Void>tree().startGroup("grp", null).value("val", 7).build();
		Node<Void> node1 = Node.<Void>tree().startGroup("grp", null).value("val", 8).build();
		NcShow t = NcShow.of(item0, node0);
		NcShow eq0 = NcShow.of(item0, node0);
		NcShow ne0 = NcShow.of(item1, node0);
		NcShow ne1 = NcShow.of(item0, node1);
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldCreateFromOutput() {
		NcShow ns = NcShow.from("* (Connecting) X P --> D \"N\" [P:T]");
		assertEquals(ns.item, NcListItem.builder().enabled(true).state("Connecting")
			.passwordHash("X").protocol("P").device("D").name("N").type("T").build());
		assertEquals(ns.data, Node.NULL);
	}

}
