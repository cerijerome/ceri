package ceri.common.collection;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import java.util.Map;
import org.junit.Test;

public class NodeBehavior {
	private static final Node<?> NULL = Node.of();
	/**
	 * <pre>
	 * Node(a0,A00) {
	 *     Node(b0,B00)
	 *     Node(b1,B01) {
	 *         Node(c0,C00)
	 *         Node(null,C01)
	 *     }
	 *     Node(b2,null)
	 *     Node(null,B03) {
	 *         Node(null,null)
	 *         Node(c3,null)
	 *         Node(null,C04)
	 *     }
	 *     Node(b4,null) {
	 *         Node(,)
	 *     }
	 * }
	 * </pre>
	 */
	private static final Node<?> NODE = NodeBuilder.<String>of().startGroup("a0", "A00")
		.value("b0", "B00").startGroup("b1", "B01").value("c0", "C00").value(null, "C01")
		.closeGroup().value("b2", null).startGroup(null, "B03").value(null, null).value("c3", null)
		.value(null, "C04").closeGroup().startGroup("b4", null).value("", "").build().child(0);

	@Test
	public void shouldNotBreachEqualsContract() {
		Node<?> t = Node.of("name", 9);
		Node<?> eq0 = Node.of("name", 9);
		Node<?> eq1 = Node.builder(9).name("name").build();
		Node<?> ne0 = Node.of();
		Node<?> ne1 = Node.of("Name", 9);
		Node<?> ne2 = Node.of("name", 8);
		Node<?> ne3 = Node.builder(9).name("name").add(Node.builder(null)).build();
		Node<?> ne4 = Node.of(9);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldDetermineIfHasValue() {
		assertThat(NULL.hasValue(), is(false));
		assertThat(Node.of(null).hasValue(), is(false));
		assertThat(Node.of("").hasValue(), is(true));
	}

	@Test
	public void shouldDetermineIfNull() {
		assertThat(NULL.isNull(), is(true));
		assertThat(Node.of(null).isNull(), is(true));
		assertThat(Node.of(1).isNull(), is(false));
		assertThat(Node.of("", null).isNull(), is(false));
		Node<?> node = Node.builder(null).add(Node.builder(null)).build();
		assertThat(node.isNull(), is(false));
	}

	@Test
	public void shouldDetermineIfHasName() {
		assertThat(NULL.isNamed(), is(false));
		assertThat(Node.of(null).isNamed(), is(false));
		assertThat(Node.of(1).isNamed(), is(false));
		assertThat(Node.of("", 1).isNamed(), is(true));
	}

	@Test
	public void shouldAccessValue() {
		assertNull(NULL.asBoolean());
		assertThat(NULL.asBoolean(true), is(Boolean.TRUE));
		assertThat(Node.of(false).asBoolean(), is(Boolean.FALSE));
		assertThat(Node.of(false).asBoolean(true), is(Boolean.FALSE));
		assertThat(Node.of("true").asBoolean(), is(Boolean.TRUE));
		assertThat(Node.of("true").asBoolean(false), is(Boolean.TRUE));

		assertNull(NULL.asInt());
		assertThat(NULL.asInt(1), is(1));
		assertThat(Node.of(2).asInt(), is(2));
		assertThat(Node.of(2).asInt(1), is(2));
		assertThat(Node.of("2").asInt(), is(2));
		assertThat(Node.of("2").asInt(1), is(2));

		assertNull(NULL.asLong());
		assertThat(NULL.asLong(1), is(1L));
		assertThat(Node.of(2).asLong(), is(2L));
		assertThat(Node.of(2).asLong(1), is(2L));
		assertThat(Node.of("2").asLong(), is(2L));
		assertThat(Node.of("2").asLong(1), is(2L));

		assertNull(NULL.asDouble());
		assertThat(NULL.asDouble(1), is(1.0));
		assertThat(Node.of(2).asDouble(), is(2.0));
		assertThat(Node.of(2).asDouble(1), is(2.0));
		assertThat(Node.of("2").asDouble(), is(2.0));
		assertThat(Node.of("2").asDouble(1), is(2.0));
	}

	@Test
	public void shouldDetermineChildNamePaths() {
		assertCollection(NODE.namedPaths(), "b0", "b1", "b1.c0", "b2", "b4");
		assertCollection(NODE.child(3).namedPaths(), "c3");
	}

	@Test
	public void shouldFindChildByDottedPath() {
		assertNodeNameValue(NODE.find("b1.c0"), "c0", "C00");
		assertNodeNameValue(NODE.find("3.c3"), "c3", null);
		assertNodeNameValue(NODE.find("3.2"), null, "C04");
	}

	@Test
	public void shouldDetermineChildren() {
		assertIterable(NODE.childNames(), "b0", "b1", "b2", "b4");
		assertThat(NODE.hasChild("b2"), is(true));
		assertThat(NODE.hasChild("3"), is(false));
		assertThat(NODE.hasChild("b3"), is(false));
		Map<String, Node<?>> map = NODE.namedChildren();
		assertThat(map.size(), is(4));
		assertNodeNameValue(map.get("b0"), "b0", "B00");
		assertNodeNameValue(map.get("b1"), "b1", "B01");
		assertNodeNameValue(map.get("b2"), "b2", null);
		assertNodeNameValue(map.get("b4"), "b4", null);
	}

	@Test
	public void shouldAccessChildrenByNamedPath() {
		assertNodeNameValue(NODE.child(new String[0]), "a0", "A00");
		assertNodeNameValue(NODE.child("b0"), "b0", "B00");
		assertNodeNameValue(NODE.child("b1"), "b1", "B01");
		assertNodeNameValue(NODE.child("b1", "c0"), "c0", "C00");
		assertNodeNameValue(NODE.child("b1", "1"), null, "C01");
		assertNodeNameValue(NODE.child("b2"), "b2", null);
		assertNodeNameValue(NODE.child("3"), null, "B03");
		assertNodeNameValue(NODE.child("3", "0"), null, null);
		assertNodeNameValue(NODE.child("3", "c3"), "c3", null);
		assertNodeNameValue(NODE.child("3", "2"), null, "C04");
		assertNodeNameValue(NODE.child("b4"), "b4", null);
		assertNodeNameValue(NODE.child("b4", "0"), "", "");
	}

	@Test
	public void shouldAccessChildrenByArrayIndexPath() {
		assertNodeNameValue(NODE.child(new int[0]), "a0", "A00");
		assertNodeNameValue(NODE.child(0), "b0", "B00");
		assertNodeNameValue(NODE.child(1), "b1", "B01");
		assertNodeNameValue(NODE.child(1, 0), "c0", "C00");
		assertNodeNameValue(NODE.child(1, 1), null, "C01");
		assertNodeNameValue(NODE.child(2), "b2", null);
		assertNodeNameValue(NODE.child(3), null, "B03");
		assertNodeNameValue(NODE.child(3, 0), null, null);
		assertNodeNameValue(NODE.child(3, 1), "c3", null);
		assertNodeNameValue(NODE.child(3, 2), null, "C04");
		assertNodeNameValue(NODE.child(4), "b4", null);
		assertNodeNameValue(NODE.child(4, 0), "", "");
	}

	@Test
	public void shouldReturnNullForChildOutOfBounds() {
		assertThat(NODE.child(-1), is(NULL));
		assertThat(NODE.child(5), is(NULL));
		assertThat(NODE.child(3, 3), is(NULL));
		assertThat(NODE.child("5"), is(NULL));
		assertThat(NODE.child("3", "3"), is(NULL));
		assertThat(NODE.child("x"), is(NULL));
	}

	private static void assertNodeNameValue(Node<?> node, String name, Object value) {
		assertThat(node.name, is(name));
		assertThat(node.value, is(value));
	}

}
